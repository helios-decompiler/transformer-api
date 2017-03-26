/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heliosdecompiler.transformerapi.disassemblers.krakatau;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.PackagedLibraryHelper;
import com.heliosdecompiler.transformerapi.Result;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauException;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants.NAME;
import static com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants.VERSION;

public class KrakatauDisassembler extends Disassembler<KrakatauDisassemblerSettings> {
    @Override
    public Result disassemble(Collection<ClassData> data, KrakatauDisassemblerSettings settings, Map<String, ClassData> classpath) throws TransformationException {
        Exception packageResult = PackagedLibraryHelper.checkPackagedLibrary(NAME, VERSION);
        if (packageResult != null) {
            throw new KrakatauException(packageResult, KrakatauException.Reason.MISSING_KRAKATAU, null, null);
        }

        if (settings.getPython2Exe() == null) {
            throw new KrakatauException("No Python 2 executable provided", KrakatauException.Reason.MISSING_PYTHON2, null, null);
        }

        File sessionDirectory = null;

        Process createdProcess = null;

        try {
            try {
                sessionDirectory = Files.createTempDirectory("krakatau-disassemble-").toFile();
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CREATE_TEMP_DIR, null, null);
            }
            File inputFile = new File(sessionDirectory, "input.jar");
            File outputFile = new File(sessionDirectory, "out.jar");

            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(inputFile))) {
                for (ClassData classData : data) {
                    ZipEntry entry = new ZipEntry(classData.getInternalName() + ".class");
                    out.putNextEntry(entry);
                    out.write(classData.getData());
                    out.closeEntry();
                }
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CREATE_INPUT_FILE, null, null);
            }

            List<String> args = new ArrayList<>();
            args.add(KrakatauConstants.canon(settings.getPython2Exe()));
            args.add("-O");
            args.add("disassemble.py");
            if (settings.isRoundtrip()) {
                args.add("-roundtrip");
            }
            args.add("-out");
            args.add(KrakatauConstants.canon(outputFile));
            args.add(KrakatauConstants.canon(inputFile));

            createdProcess = KrakatauConstants.launchProcess(new ProcessBuilder(args).directory(PackagedLibraryHelper.getPackageRoot(NAME, VERSION)), settings);

            String stdout;
            String stderr;

            try {
                stdout = IOUtils.toString(createdProcess.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                stdout = writer.toString();
            }
            try {
                stderr = IOUtils.toString(createdProcess.getErrorStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                stderr = writer.toString();
            }

            Map<String, String> results = new HashMap<>();

            try (JarFile zipFile = new JarFile(outputFile)) {
                Enumeration<JarEntry> e = zipFile.entries();
                while (e.hasMoreElements()) {
                    JarEntry next = e.nextElement();
                    if (!next.isDirectory()) {
                        String name = next.getName();
                        if (!name.endsWith(".j")) {
                            throw new KrakatauException("Unexpected output: " + name, KrakatauException.Reason.UNEXPECTED_OUTPUT, stdout, stderr);
                        }
                        name = name.substring(0, name.length() - ".j".length());
                        try (InputStream inputStream = zipFile.getInputStream(next)) {
                            String result = new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
                            results.put(name, result);
                        } catch (IOException ex) {
                            StringWriter err = new StringWriter();
                            ex.printStackTrace(new PrintWriter(err));
                            stderr += "\r\nError occurred while reading input: " + next.getName() + "\r\n" + err.toString();
                        }
                    }
                }
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_OPEN_OUTPUT, stdout, stderr);
            }

            return new Result(results, stdout, stderr);
        } finally {
            FileUtils.deleteQuietly(sessionDirectory);
            if (createdProcess != null)
                createdProcess.destroyForcibly();
        }
    }

    @Override
    public KrakatauDisassemblerSettings defaultSettings() {
        return new KrakatauDisassemblerSettings();
    }
}
