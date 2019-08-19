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

package com.heliosdecompiler.transformerapi.decompilers.cfr;

import com.heliosdecompiler.transformerapi.FileContents;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import org.apache.commons.io.output.StringBuilderWriter;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.SinkReturns;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CFRDecompiler extends Decompiler<CFRSettings> {
    @Override
    public TransformationResult<String> decompile(Collection<FileContents> data, CFRSettings settings, Map<String, FileContents> classpath) {
        Map<String, byte[]> aggregatedData = new HashMap<>();

        for (FileContents fileContents : classpath.values()) {
            aggregatedData.put(fileContents.getName(), fileContents.getData());
        }

        for (FileContents fileContents : data) {
            aggregatedData.put(fileContents.getName(), fileContents.getData());
        }

        InMemorySinkManager sinkManager = new InMemorySinkManager();

        CfrDriver driver = new CfrDriver.Builder()
                .withOptions(settings.getSettings())
                .withClassFileSource(new InMemoryClassFileSource(aggregatedData))
                .withOutputSink(sinkManager)
                .build();

        Map<String, String> results = new HashMap<>();
        for (FileContents fc : data) {
            sinkManager.getOutputs().clear();
            driver.analyse(Collections.singletonList(fc.getName()));

            if (sinkManager.getOutputs().size() > 1) {
                throw new RuntimeException("somehow got more than one output for " + fc.getName());
            }

            for (SinkReturns.DecompiledMultiVer output : sinkManager.getOutputs()) {
                results.put(fc.getName(), output.getJava());
            }
        }

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        for (String progress : sinkManager.getInfo()) {
            stdout.append(progress).append("\n");
        }

        for (SinkReturns.ExceptionMessage e : sinkManager.getExceptions()) {
            stderr.append("An exception occurred: \n")
                    .append("    message: ").append(e.getMessage())
                    .append("    path: ").append(e.getPath());
            e.getThrownException().printStackTrace(new PrintWriter(new StringBuilderWriter(stderr)));
            stderr.append("\n");
        }

        return new TransformationResult<>(results, stdout.toString(), stderr.toString());
    }

    @Override
    public CFRSettings defaultSettings() {
        return new CFRSettings();
    }
}
