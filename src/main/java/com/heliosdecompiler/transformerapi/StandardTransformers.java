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

import com.heliosdecompiler.transformerapi.decompilers.cfr.CFRDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.fernflower.FernflowerDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.krakatau.KrakatauDecompiler;
import com.heliosdecompiler.transformerapi.decompilers.procyon.ProcyonDecompiler;
import com.heliosdecompiler.transformerapi.disassemblers.javap.JavapDisassembler;
import com.heliosdecompiler.transformerapi.disassemblers.krakatau.KrakatauDisassembler;

public class StandardTransformers {
    public static class Decompilers {
        public static final ProcyonDecompiler PROCYON = new ProcyonDecompiler();
        public static final CFRDecompiler CFR = new CFRDecompiler();
        public static final FernflowerDecompiler FERNFLOWER = new FernflowerDecompiler();
        public static final KrakatauDecompiler KRAKATAU = new KrakatauDecompiler();
    }

    public static class Disassemblers {
        public static final KrakatauDisassembler KRAKATAU = new KrakatauDisassembler();
        public static final JavapDisassembler JAVAP = new JavapDisassembler();
        public static final ProcyonDecompiler PROCYON = new ProcyonDecompiler();
    }
}
