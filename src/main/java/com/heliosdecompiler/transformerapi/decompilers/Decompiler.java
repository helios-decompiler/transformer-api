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

package com.heliosdecompiler.transformerapi.decompilers;

import com.heliosdecompiler.transformerapi.ClassData;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.TransformationException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a particular Decompiler.
 * Note that decompiler implementations should be stateless, and thus can be reused (and are thread safe)
 */
public abstract class Decompiler<SettingType> {

    /**
     * Decompile the given class file using the default settings
     *
     * @param data The data of the class to decompile
     * @return The result
     */
    public final TransformationResult<String> decompile(ClassData data) throws TransformationException {
        return decompile(Collections.singleton(data), defaultSettings(), Collections.emptyMap());
    }

    /**
     * Decompile the given class file
     *
     * @param data     The data of the class to decompile
     * @param settings The settings to use with this decompiler
     * @return The result
     */
    public final TransformationResult<String> decompile(ClassData data, SettingType settings) throws TransformationException {
        return decompile(Collections.singleton(data), settings, Collections.emptyMap());
    }

    /**
     * Decompile the given class file. If any classes are needed to provide additional information, they should be provided in the classpath parameter
     *
     * @param data      The data of the class to decompile
     * @param settings  The settings to use with this decompiler
     * @param classpath The additional files which may be required for metadata. Keys should also be internal names
     * @return The result
     */
    public final TransformationResult<String> decompile(ClassData data, SettingType settings, Collection<ClassData> classpath) throws TransformationException {
        Map<String, ClassData> map = new HashMap<>();
        classpath.forEach(classData -> map.put(classData.getInternalName(), classData));
        return decompile(Collections.singleton(data), settings, map);
    }

    /**
     * Decompile the given class file. If any classes are needed to provide additional information, they should be provided in the classpath parameter
     *
     * @param data      The data of the class to decompile
     * @param settings  The settings to use with this decompiler
     * @param classpath The additional files which may be required for metadata. Keys should also be internal names
     * @return The result
     */
    public final TransformationResult<String> decompile(Collection<ClassData> data, SettingType settings, Collection<ClassData> classpath) throws TransformationException {
        Map<String, ClassData> map = new HashMap<>();
        classpath.forEach(classData -> map.put(classData.getInternalName(), classData));
        return decompile(data, settings, map);
    }

    /**
     * Decompile the given class file. If any classes are needed to provide additional information, they should be provided in the classpath parameter
     *
     * @param data      The data of the class to decompile
     * @param settings  The settings to use with this decompiler
     * @param classpath The additional files which may be required for metadata. Keys should also be internal names
     * @return The result
     */
    public abstract TransformationResult<String> decompile(Collection<ClassData> data, SettingType settings, Map<String, ClassData> classpath) throws TransformationException;

    public abstract SettingType defaultSettings();
}
