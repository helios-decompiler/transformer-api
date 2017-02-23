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

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

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
