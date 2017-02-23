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

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FernflowerBytecodeProvider implements IBytecodeProvider {

    private final Map<String, byte[]> byteData = new HashMap<>();

    public FernflowerBytecodeProvider(Map<String, byte[]> data) {
        byteData.putAll(data);
    }

    /**
     * Notes: externalPath and internalPath are provided via {@link org.jetbrains.java.decompiler.struct.lazy.LazyLoader.Link}
     * Since we create the links ourselves, we know that externalPath is the one to use
     */
    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        if (!byteData.containsKey(externalPath)) {
            throw new IllegalStateException("Expected data to be present for " + externalPath);
        }

        byte[] data = byteData.get(externalPath);
        return Arrays.copyOf(data, data.length);
    }
}
