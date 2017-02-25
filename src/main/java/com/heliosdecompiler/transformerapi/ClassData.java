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

package com.heliosdecompiler.transformerapi;

import org.objectweb.asm.ClassReader;

/**
 * Represents some data about a class. Currently only the internal name and bytes
 */
public class ClassData {

    private final String internalName;
    private final byte[] data;

    public ClassData(String internalName, byte[] data) {
        if (internalName == null) {
            throw new IllegalArgumentException("Internal name must not be null");
        }
        this.internalName = internalName;
        this.data = data;
    }

    /**
     * Construct a {@link ClassData} object using only the bytes given. This will read the internal name using a {@link ClassReader}
     *
     * @param data The bytes of the classfile
     * @return The ClassData
     */
    public static ClassData construct(byte[] data) {
        ClassReader reader;
        try {
            reader = new ClassReader(data);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            // not a class file
            return null;
        }
        return construct(reader.getClassName(), data);
    }

    /**
     * Construct a {@link ClassData} object using the internal name and bytes given. No attempt will be made to validate the information
     *
     * @param internalClassName The internal name, such as java/lang/String or java/lang/String$CaseInsensitiveComparator
     * @param data              The bytes of the classfile
     * @return The ClassData
     */
    public static ClassData construct(String internalClassName, byte[] data) {
        return new ClassData(internalClassName, data);
    }

    public String getInternalName() {
        return internalName;
    }

    public byte[] getData() {
        return data;
    }
}
