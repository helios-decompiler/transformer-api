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

import com.heliosdecompiler.transformerapi.FileContents;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauException;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauServer;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KrakatauDecompiler extends Decompiler<KrakatauDecompilerSettings> {
    @Override
    public TransformationResult<String> decompile(
            Collection<FileContents> data,
            KrakatauDecompilerSettings settings,
            Map<String, FileContents> classpath
    ) throws KrakatauException {
        List<File> pathFiles = new ArrayList<>();
        if (settings.getPath() != null) pathFiles.addAll(settings.getPath());

        List<String> args = new ArrayList<>();
        args.add("-skip");
        args.add("-nauto");
        if (settings.isAssumeMagicThrow()) {
            args.add("-xmagicthrow");
        }
        if (pathFiles.size() > 0) {
            args.add("-path");
            args.add(buildPath(pathFiles));
        }

        KrakatauServer server = new KrakatauServer(data, classpath);
        String[] out = KrakatauConstants.runKrakatau(settings, server, "decompile.py", args);
        return new TransformationResult<>(server.getOutputsAsString(), out[0], out[1]);
    }

    @Override
    public KrakatauDecompilerSettings defaultSettings() {
        return new KrakatauDecompilerSettings();
    }

    private String buildPath(List<File> jars) throws KrakatauException {
        List<String> canonical = new ArrayList<>();
        for (File file : jars) {
            canonical.add(KrakatauConstants.canon(file));
        }
        return String.join(";", canonical);
    }
}
