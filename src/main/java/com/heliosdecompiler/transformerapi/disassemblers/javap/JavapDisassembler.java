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

package com.heliosdecompiler.transformerapi.disassemblers.javap;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.Result;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;
import com.sun.tools.javap.JavapTask;
import com.sun.tools.javap.Options;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JavapDisassembler extends Disassembler<Options> {
    @Override
    public Result disassemble(Collection<ClassData> data, Options settings, Map<String, ClassData> classpath) throws TransformationException {
        Map<String, String> result = new HashMap<>();

        for (ClassData classData : data) {
            StringWriter stringWriter = new StringWriter();
            JavapTask task = new JavapTask();
            task.logToUse = new PrintWriter(stringWriter);
            task.classData = classData.getData();
            task.className = classData.getInternalName();
            task.options = settings;
            task.context.put(Options.class, settings);
            task.run();

            result.put(classData.getInternalName(), stringWriter.toString());
        }

        return new Result(result, null, null);
    }

    @Override
    public Options defaultSettings() {
        return new Options();
    }
}
