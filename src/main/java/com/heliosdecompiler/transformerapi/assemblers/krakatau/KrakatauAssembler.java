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

package com.heliosdecompiler.transformerapi.assemblers.krakatau;

import com.heliosdecompiler.transformerapi.FileContents;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.assemblers.Assembler;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauServer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class KrakatauAssembler extends Assembler<KrakatauAssemblerSettings, FileContents> {
    @Override
    public TransformationResult<byte[]> assemble(Collection<FileContents> data, KrakatauAssemblerSettings settings) throws TransformationException {
        // normalize EOLs
        data = data.stream().map(fc -> FileContents.construct(fc.getName(), new String(fc.getData(), StandardCharsets.UTF_8).replace("\r\n", "\n"))).collect(Collectors.toList());

        KrakatauServer server = new KrakatauServer(data, Collections.emptyMap());
        String[] out = KrakatauConstants.runKrakatau(settings, server, "assemble.py", Collections.emptyList());
        return new TransformationResult<>(server.getOutputs(), out[0], out[1]);
    }

    @Override
    public KrakatauAssemblerSettings defaultSettings() {
        return new KrakatauAssemblerSettings();
    }
}
