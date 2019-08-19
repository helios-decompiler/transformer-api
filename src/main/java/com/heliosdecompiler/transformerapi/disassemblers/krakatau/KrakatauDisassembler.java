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

package com.heliosdecompiler.transformerapi.disassemblers.krakatau;

import com.heliosdecompiler.transformerapi.FileContents;
import com.heliosdecompiler.transformerapi.TransformationException;
import com.heliosdecompiler.transformerapi.TransformationResult;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauConstants;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauServer;
import com.heliosdecompiler.transformerapi.disassemblers.Disassembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KrakatauDisassembler extends Disassembler<KrakatauDisassemblerSettings> {
    @Override
    public TransformationResult<String> disassemble(
            Collection<FileContents> data,
            KrakatauDisassemblerSettings settings,
            Map<String, FileContents> classpath
    ) throws TransformationException {
        List<String> args = new ArrayList<>();
        if (settings.isRoundtrip()) {
            args.add("-roundtrip");
        }

        KrakatauServer server = new KrakatauServer(data, classpath);
        String[] out = KrakatauConstants.runKrakatau(settings, server, "disassemble.py", args);
        return new TransformationResult<>(server.getOutputsAsString(), out[0], out[1]);
    }

    @Override
    public KrakatauDisassemblerSettings defaultSettings() {
        return new KrakatauDisassemblerSettings();
    }
}
