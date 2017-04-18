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

package com.heliosdecompiler.transformerapi.assemblers;

import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.TransformationException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Assembler<SettingType, AssemblyType> {

    /**
     * Decompile the given class file using the default settings
     *
     * @param data The data of the class to decompile
     * @return The result
     */
    public final TransformationResult<byte[]> assemble(AssemblyType data) throws TransformationException {
        return assemble(Collections.singleton(data), defaultSettings());
    }

    /**
     * Decompile the given class file. If any classes are needed to provide additional information, they should be provided in the classpath parameter
     *
     * @param data      The data of the class to decompile
     * @param settings  The settings to use with this decompiler
     * @return The result
     */
    public final TransformationResult<byte[]> assemble(AssemblyType data, SettingType settings) throws TransformationException {
        return assemble(Collections.singleton(data), settings);
    }

    /**
     * Decompile the given class file. If any classes are needed to provide additional information, they should be provided in the classpath parameter
     *
     * @param data      The data of the class to decompile
     * @param settings  The settings to use with this decompiler
     * @return The result
     */
    public abstract TransformationResult<byte[]> assemble(Collection<AssemblyType> data, SettingType settings) throws TransformationException;

    public abstract SettingType defaultSettings();
}
