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

package com.heliosdecompiler.transformerapi.common.krakatau;

import com.heliosdecompiler.transformerapi.PackagedLibraryHelper;
import com.heliosdecompiler.transformerapi.TransformationResult;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KrakatauConstants {
    public static final String NAME = "Krakatau";
    public static final String VERSION = "9262b12b795795d29582663804f00d6fbd7246c5";

    public static String[] runKrakatau(KrakatauSettings settings, KrakatauServer server, String file, List<String> inArgs) throws KrakatauException {
        try {
            PackagedLibraryHelper.checkPackagedLibrary(NAME, VERSION);
        } catch (IOException e) {
            throw new KrakatauException(e, KrakatauException.Reason.MISSING_KRAKATAU, null, null);
        }

        if (settings.getPython2Exe() == null) {
            throw new KrakatauException("No Python 2 executable provided", KrakatauException.Reason.MISSING_PYTHON2, null, null);
        }

        Process createdProcess = null;

        try {
            server.start();

            List<String> args = new ArrayList<>();
            args.add(KrakatauConstants.canon(settings.getPython2Exe()));
            args.add("-O");
            args.add(file);
            args.add("-port");
            args.add(String.valueOf(server.getPort()));
            args.addAll(inArgs);
            args.add("unused_input");

            createdProcess = launchProcess(new ProcessBuilder(args).directory(PackagedLibraryHelper.getPackageRoot(NAME, VERSION)), settings);

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

            return new String[] {stdout, stderr.toString()};
        } catch (IOException e) {
            throw new KrakatauException(e, KrakatauException.Reason.SERVER_IO_ERROR, null, null);
        } finally {
            try {
                server.stop();
            } catch (IOException ignored) {
            }

            if (createdProcess != null)
                createdProcess.destroyForcibly();
        }
    }

    public static Process launchProcess(ProcessBuilder builder, KrakatauSettings settings) throws KrakatauException {
        try {
            if (settings.getProcessCreator() == null) {
                return builder.start();
            } else {
                return settings.getProcessCreator().apply(builder);
            }
        } catch (IOException ex) {
            throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_LAUNCH_PROCESS, null, null);
        }
    }

    public static String canon(File file) throws KrakatauException {
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CANONICALIZE_PATH, null, null);
        }
    }
}
