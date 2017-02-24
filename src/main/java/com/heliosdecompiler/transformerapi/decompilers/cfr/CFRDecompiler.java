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

import com.heliosdecompiler.appifier.SystemHook;
import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.Result;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CFRDecompiler extends Decompiler<CFRSettings> {
    @Override
    public Result decompile(Collection<ClassData> data, CFRSettings settings, Map<String, ClassData> classpath) {
        Map<String, byte[]> importantData = new HashMap<>();

        for (ClassData classData : classpath.values()) {
            importantData.put(classData.getInternalName(), classData.getData());
        }

        // Ensure nothing in classpath will overwrite the actual data to decompile
        for (ClassData classData : data) {
            importantData.put(classData.getInternalName(), classData.getData());
        }

        PluginRunner pluginRunner = new PluginRunner(settings.getSettings(), new CFRCFS(importantData));

        ByteArrayOutputStream redirOut = new ByteArrayOutputStream();
        ByteArrayOutputStream redirErr = new ByteArrayOutputStream();

        SystemHook.out.set(new PrintStream(redirOut));
        SystemHook.err.set(new PrintStream(redirErr));

        Map<String, String> results = new HashMap<>();

        for (String s : importantData.keySet()) {
            try {
                String decomp = pluginRunner.getDecompilationFor(s);
                if (!decomp.isEmpty()) {
                    results.put(s, decomp);
                }
            } catch (Throwable t) {
                SystemHook.err.get().println("An exception occurred while decompiling " + s);
                t.printStackTrace(SystemHook.err.get());
            }
        }

        SystemHook.out.set(System.out);
        SystemHook.err.set(System.err);

        return new Result(results, new String(redirOut.toByteArray(), StandardCharsets.UTF_8), new String(redirErr.toByteArray(), StandardCharsets.UTF_8));
    }

    @Override
    public CFRSettings defaultSettings() {
        return new CFRSettings();
    }
}