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

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FernflowerResultSaver implements IResultSaver {
    private final Map<String, String> results = new HashMap<>();

    public Map<String, String> getResults() {
        return this.results;
    }

    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        throw new IllegalArgumentException("Unexpected");
    }

    public void saveFolder(String path) {
    }

    public void copyFile(String source, String path, String entryName) {
    }

    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        if (mapping != null) {
            String[] splits = content.split("\r?\n");

            for (int i = 0; i < mapping.length; i += 2) {
                int srcLine = mapping[i + 1] - 1; // line in decompiled source
                int actualLine = mapping[i]; // actual source line
                splits[srcLine] = splits[srcLine] + " /* " + actualLine + " */";
            }

            content = Stream.of(splits).collect(Collectors.joining("\r\n"));
        }
        results.put(qualifiedName, content);
    }

    public void createArchive(String path, String archiveName, Manifest manifest) {
        throw new IllegalArgumentException("Unexpected");
    }

    public void saveDirEntry(String path, String archiveName, String entryName) {
        throw new IllegalArgumentException("Unexpected");
    }

    public void copyEntry(String source, String path, String archiveName, String entry) {
        throw new IllegalArgumentException("Unexpected");
    }

    public void closeArchive(String path, String archiveName) {
        throw new IllegalArgumentException("Unexpected");
    }
}
