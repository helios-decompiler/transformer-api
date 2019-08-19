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

import java.nio.charset.StandardCharsets;

/**
 * Holds the name and contents of a file
 */
public class FileContents {
    private final String name;
    private final byte[] data;

    public FileContents(String name, byte[] data) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
        this.data = data;
    }

    /**
     * Construct a {@link FileContents} object using only the bytes given. This will read the internal name using a {@link ClassReader}
     */
    public static FileContents fromClass(byte[] data) {
        try {
            ClassReader reader = new ClassReader(data);
            return construct(reader.getClassName(), data);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            // not a class file
            return null;
        }
    }

    /**
     * Construct a {@link FileContents} object using the name and data given.
     */
    public static FileContents construct(String name, String data) {
        return construct(name, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Construct a {@link FileContents} object using the name and bytes given.
     */
    public static FileContents construct(String name, byte[] data) {
        return new FileContents(name, data);
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
