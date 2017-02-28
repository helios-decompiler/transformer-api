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

import com.heliosdecompiler.transformerapi.ExceptionalFunction;
import com.heliosdecompiler.transformerapi.common.krakatau.KrakatauSettings;

import java.io.File;
import java.io.IOException;

public class KrakatauDisassemblerSettings extends KrakatauSettings {
    private boolean roundtrip = false;

    public boolean isRoundtrip() {
        return roundtrip;
    }

    public KrakatauDisassemblerSettings setRoundtrip(boolean roundtrip) {
        this.roundtrip = roundtrip;
        return this;
    }

    @Override
    public KrakatauDisassemblerSettings setPythonExecutable(File location) {
        super.setPythonExecutable(location);
        return this;
    }

    @Override
    public KrakatauDisassemblerSettings setProcessCreator(ExceptionalFunction<ProcessBuilder, Process, IOException> creator) {
        super.setProcessCreator(creator);
        return this;
    }
}
