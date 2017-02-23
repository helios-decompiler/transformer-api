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

package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CFRCFS implements ClassFileSource {

    private final Map<String, byte[]> importantData = new HashMap<>();

    public CFRCFS(Map<String, byte[]> importantData) {
        this.importantData.putAll(importantData);
    }

    @Override
    public void informAnalysisRelativePathDetail(String s, String s1) {
        // Not quite sure what this does
    }

    @Override
    public Collection<String> addJar(String s) {
        throw new IllegalArgumentException("Unexpected");
    }

    @Override
    public String getPossiblyRenamedPath(String s) {
        // Not quite sure what this does
        return s;
    }

    @Override
    public Pair<byte[], String> getClassFileContent(String pathOrName) throws IOException {
        // CFR always appends a ".class" onto the end of the name if it can't find the file
        // This may be problematic because it also tries to check if a file with path "pathOrName" exists

        pathOrName = pathOrName.substring(0, pathOrName.length() - ".class".length());
        return Pair.make(importantData.get(pathOrName), pathOrName);
    }
}
