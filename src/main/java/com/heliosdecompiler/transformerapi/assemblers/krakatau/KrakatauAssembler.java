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

package com.heliosdecompiler.transformerapi.assemblers.krakatau;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.PackagedLibraryHelper;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.assemblers.Assembler;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauException;
import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants.NAME;
import static com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants.VERSION;

// We use jazzlib because there's like one particular combination of unicode characters where Java's ZIP impl just barfs
public class KrakatauAssembler extends Assembler<KrakatauAssemblerSettings, String> {

    @Override
    public TransformationResult<byte[]> assemble(Collection<String> data, KrakatauAssemblerSettings settings) throws TransformationException {
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
                sessionDirectory = Files.createTempDirectory("krakatau-assemble-").toFile();
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CREATE_TEMP_DIR, null, null);
            }

            File inputDirectory = new File(sessionDirectory, "input");

            if (!inputDirectory.mkdir()) {
                throw new KrakatauException(KrakatauException.Reason.FAILED_TO_CREATE_INPUT_FILE, null, null);
            }

            File outputFile = new File(sessionDirectory, "out.zip");

            try {
                int id = 0;
                for (String assembly : data) {
                    File inputFile = new File(inputDirectory, id++ + ".j");
                    try (FileOutputStream outputStream = new FileOutputStream(inputFile)) {
                        outputStream.write(assembly.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CREATE_INPUT_FILE, null, null);
            }

            List<String> args = new ArrayList<>();
            args.add(KrakatauConstants.canon(settings.getPython2Exe()));
            args.add("-O");
            args.add("assemble.py");
            args.add("-r");
            args.add("-out");
            args.add(KrakatauConstants.canon(outputFile));
            args.add(KrakatauConstants.canon(inputDirectory));

            createdProcess = KrakatauConstants.launchProcess(new ProcessBuilder(args).directory(PackagedLibraryHelper.getPackageRoot(NAME, VERSION)), settings);

            String stdout;
            StringBuilder stderr;

            try {
                stdout = IOUtils.toString(createdProcess.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                stdout = writer.toString();
            }
            try {
                stderr = new StringBuilder(IOUtils.toString(createdProcess.getErrorStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                stderr = new StringBuilder(writer.toString());
            }

            Map<String, byte[]> results = new HashMap<>();

            try {
                ZipFile zipFile = new ZipFile(outputFile);
                Enumeration<? extends ZipEntry> e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry next = e.nextElement();
                    if (!next.isDirectory()) {
                        String name = next.getName();
                        if (!name.endsWith(".class")) {
                            throw new KrakatauException("Unexpected output: " + name, KrakatauException.Reason.UNEXPECTED_OUTPUT, stdout, stderr.toString());
                        }
                        try (InputStream inputStream = zipFile.getInputStream(next)) {
                            ClassData classData = ClassData.construct(IOUtils.toByteArray(inputStream));
                            assert classData != null;
                            results.put(classData.getInternalName(), classData.getData());
                        } catch (IOException ex) {
                            StringWriter err = new StringWriter();
                            ex.printStackTrace(new PrintWriter(err));
                            stderr.append("\r\nError occurred while reading input: ").append(next.getName()).append("\r\n").append(err.toString());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_OPEN_OUTPUT, stdout, stderr.toString());
            }

            return new TransformationResult<>(results, stdout, stderr.toString());
        } finally {
            FileUtils.deleteQuietly(sessionDirectory);
            if (createdProcess != null)
                createdProcess.destroyForcibly();
        }
    }

    @Override
    public KrakatauAssemblerSettings defaultSettings() {
        return new KrakatauAssemblerSettings();
    }
}
