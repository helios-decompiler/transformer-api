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

package com.heliosdecompiler.transformerapi.disassemblers.procyon;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.common.procyon.ProcyonTypeLoader;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.Languages;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProcyonDisassembler extends Disassembler<DecompilerSettings> {
    @Override
    public TransformationResult<String> disassemble(Collection<ClassData> data, DecompilerSettings settings, Map<String, ClassData> classpath) throws TransformationException {
        Map<String, byte[]> importantClasses = new HashMap<>();
        for (ClassData classData : data) {
            importantClasses.put(classData.getInternalName(), classData.getData());
        }

        settings.setTypeLoader(new ProcyonTypeLoader(importantClasses));

        Map<String, String> result = new HashMap<>();

        ByteArrayOutputStream redirErr = new ByteArrayOutputStream();
        PrintStream printErr = new PrintStream(redirErr);

        for (ClassData classData : data) {
            StringWriter stringwriter = new StringWriter();
            try {
                Decompiler.decompile(classData.getInternalName(), new PlainTextOutput(stringwriter), settings);
                result.put(classData.getInternalName(), stringwriter.toString());
            } catch (Throwable t) {
                printErr.println("An exception occurred while disassembling: " + classData.getInternalName());
                t.printStackTrace(printErr);
            }
        }

        return new TransformationResult<>(result, null, new String(redirErr.toByteArray(), StandardCharsets.UTF_8));
    }

    @Override
    public DecompilerSettings defaultSettings() {
        DecompilerSettings decompilerSettings = new DecompilerSettings();
        decompilerSettings.setLanguage(Languages.bytecode());
        return decompilerSettings;
    }
}
