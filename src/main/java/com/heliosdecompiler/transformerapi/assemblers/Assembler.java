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

import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.TransformationResult;

import java.util.Collection;
import java.util.Collections;

public abstract class Assembler<SettingType, AssemblyType> {

    /**
     * Assemble the given data using the default settings
     *
     * @param data The data of the class to assemble
     * @return The result
     */
    public final TransformationResult<byte[]> assemble(AssemblyType data) throws TransformationException {
        return assemble(data, defaultSettings());
    }

    /**
     * Assemble the given data
     *
     * @param data     The data of the class to assemble
     * @param settings The settings to use with this assembler
     * @return The result
     */
    public final TransformationResult<byte[]> assemble(AssemblyType data, SettingType settings) throws TransformationException {
        return assemble(Collections.singletonList(data), settings);
    }

    /**
     * Assembles the given data
     *
     * @param data      The data of the classes to assemble
     * @param settings  The settings to use with this assembler
     * @return The result
     */
    public abstract TransformationResult<byte[]> assemble(Collection<AssemblyType> data, SettingType settings) throws TransformationException;

    public abstract SettingType defaultSettings();
}
