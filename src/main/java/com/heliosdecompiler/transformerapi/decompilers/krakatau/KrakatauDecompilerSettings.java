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

package com.heliosdecompiler.transformerapi.decompilers.krakatau;

import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauSettings;
import com.heliosdecompiler.transformerapi.disassemblers.krakatau.KrakatauDisassemblerSettings;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class KrakatauDecompilerSettings extends KrakatauSettings {
    private List<File> path;
    private boolean assumeMagicThrow = false;
    private Supplier<File> tempFileCreator;

    public KrakatauDecompilerSettings setMagicThrow(boolean magicThrow) {
        this.assumeMagicThrow = magicThrow;
        return this;
    }

    public boolean isAssumeMagicThrow() {
        return assumeMagicThrow;
    }

    public List<File> getPath() {
        return path;
    }

    public KrakatauDecompilerSettings setPath(List<File> path) {
        this.path = path;
        return this;
    }

    public Supplier<File> getTempFileCreator() {
        return tempFileCreator;
    }

    public KrakatauDecompilerSettings setTempFileCreator(Supplier<File> creator) {
        this.tempFileCreator = creator;
        return this;
    }

    @Override
    public KrakatauDecompilerSettings setPythonExecutable(File location) {
        super.setPythonExecutable(location);
        return this;
    }

    @Override
    public KrakatauDecompilerSettings setProcessCreator(Function<ProcessBuilder, Process> creator) {
        super.setProcessCreator(creator);
        return this;
    }
}
