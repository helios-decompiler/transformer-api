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

package com.heliosdecompiler.transformerapi.decompilers.krakatau;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.PackagedLibraryHelper;
import com.heliosdecompiler.transformerapi.Result;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauException;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
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

public class KrakatauDecompiler extends Decompiler<KrakatauDecompilerSettings> {
    /**
     * A note, since this decompiler will write all values inside the {@code classpath} parameter to a temporary file,
     * it's best to pass along any existing files via {@link KrakatauDecompilerSettings#setPath}
     */
    @Override
    public Result decompile(
            Collection<ClassData> data,
            KrakatauDecompilerSettings settings,
            Map<String, ClassData> classpath
    ) throws KrakatauException {
        Exception packageResult = PackagedLibraryHelper.checkPackagedLibrary(NAME, VERSION);
        if (packageResult != null) {
            throw new KrakatauException(packageResult, KrakatauException.Reason.MISSING_KRAKATAU, null, null);
        }

        if (settings.getPython2Exe() == null) {
            throw new KrakatauException("No Python 2 executable provided", KrakatauException.Reason.MISSING_PYTHON2, null, null);
        }

        File tempPathFile = null;

        try {
            List<File> pathFiles = new ArrayList<>();
            if (settings.getPath() != null)
                pathFiles.addAll(settings.getPath());

            if (classpath.size() != 0) {
                try {
                    tempPathFile = settings.getTempFileCreator() != null ? settings.getTempFileCreator().get() : File.createTempFile("krakatau-temp-", ".jar");

                    try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempPathFile))) {
                        for (ClassData classData : classpath.values()) {
                            ZipEntry zipEntry = new ZipEntry(classData.getInternalName() + ".class");
                            outputStream.putNextEntry(zipEntry);
                            outputStream.write(classData.getData());
                            outputStream.closeEntry();
                        }
                    }
                    pathFiles.add(tempPathFile);
                } catch (IOException ex) {
                    throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CREATE_TEMP_FILE, null, null);
                }
            }

            File sessionDirectory = null;

            Process createdProcess = null;

            try {
                try {
                    sessionDirectory = Files.createTempDirectory("krakatau-decompile-").toFile();
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
                args.add("decompile.py");
                args.add("-skip");
                args.add("-nauto");
                if (settings.isAssumeMagicThrow()) {
                    args.add("-xmagicthrow");
                }
                if (pathFiles.size() > 0) {
                    args.add("-path");
                    args.add(buildPath(pathFiles));
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
                            if (!name.endsWith(".java")) {
                                throw new KrakatauException("Unexpected output: " + name, KrakatauException.Reason.UNEXPECTED_OUTPUT, stdout, stderr);
                            }
                            name = name.substring(0, name.length() - ".java".length());

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
        } finally {
            FileUtils.deleteQuietly(tempPathFile);
        }
    }

    @Override
    public KrakatauDecompilerSettings defaultSettings() {
        return new KrakatauDecompilerSettings();
    }

    private String buildPath(List<File> jars) throws KrakatauException {
        StringBuilder path = new StringBuilder();
        for (File file : jars) {
            path.append(KrakatauConstants.canon(file)).append(";");
        }
        return path.toString();
    }
}
