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

package com.heliosdecompiler.transformerapi.decompilers.fernflower;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.Result;
import com.heliosdecompiler.transformerapi.decompilers.Decompiler;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.struct.ContextUnit;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a gateway to the Fernflower decompiler
 */
@SuppressWarnings("unchecked")
public class FernflowerDecompiler extends Decompiler<FernflowerSettings> {
    private static Field UNITS_FIELD;
    private static Field LOADER_FIELD;

    static {
        try {
            UNITS_FIELD = StructContext.class.getDeclaredField("units");
            UNITS_FIELD.setAccessible(true);
            LOADER_FIELD = StructContext.class.getDeclaredField("loader");
            LOADER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not initialize Fernflower decompiler", e);
        }
    }

    @Override
    public Result decompile(Collection<ClassData> data, FernflowerSettings settings, Map<String, ClassData> classpath) {
        Map<String, byte[]> importantData = new HashMap<>();

        for (ClassData classData : data) {
            importantData.put(classData.getInternalName(), classData.getData());

            ClassReader reader = new ClassReader(classData.getData());
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

            if (classNode.innerClasses != null) {
                for (InnerClassNode icn : classNode.innerClasses) {
                    byte[] innerClassData = classpath.get(icn.name).getData();
                    if (innerClassData != null) {
                        ClassReader sanityCheck = new ClassReader(innerClassData);
                        if (!sanityCheck.getClassName().equals(icn.name)) {
                            throw new IllegalArgumentException("sanity");
                        }

                        importantData.put(icn.name, innerClassData);
                    }
                }
            }
        }

        ByteArrayOutputStream log = new ByteArrayOutputStream();

        IBytecodeProvider provider = new FernflowerBytecodeProvider(importantData);
        FernflowerResultSaver saver = new FernflowerResultSaver();
        Fernflower baseDecompiler = new Fernflower(provider, saver, settings.getSettings(), new PrintStreamLogger(new PrintStream(log)));

        try {
            StructContext context = baseDecompiler.getStructContext();
            Map<String, ContextUnit> units = (Map<String, ContextUnit>) UNITS_FIELD.get(context);
            LazyLoader loader = (LazyLoader) LOADER_FIELD.get(context);

            ContextUnit defaultUnit = units.get("");

            for (Map.Entry<String, byte[]> ent : importantData.entrySet()) {
                try {
                    StructClass structClass = new StructClass(new DataInputFullStream(ent.getValue()), true, loader);
                    context.getClasses().put(structClass.qualifiedName, structClass);
                    defaultUnit.addClass(structClass, ent.getKey() + ".class"); // Fernflower will .substring(".class") to replace the extension
                    loader.addClassLink(structClass.qualifiedName, new LazyLoader.Link(1, ent.getKey(), (String) null));
                } catch (Throwable e) {
                    DecompilerContext.getLogger().writeMessage("Corrupted class file: " + ent.getKey(), e);
                }
            }

            baseDecompiler.decompileContext();
        } catch (Throwable t) {
            DecompilerContext.getLogger().writeMessage("Error while decompiling", t);
        } finally {
            baseDecompiler.clearContext();
        }

        return new Result(saver.getResults(), new String(log.toByteArray(), StandardCharsets.UTF_8), null);
    }

    @Override
    public FernflowerSettings defaultSettings() {
        return new FernflowerSettings();
    }
}
