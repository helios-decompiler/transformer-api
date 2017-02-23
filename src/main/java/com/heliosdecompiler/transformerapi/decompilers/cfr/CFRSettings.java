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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents settings which can be used to configure the particular decompiling session.
 * <p>
 * Since CFR has its own internal settings layout, that will be used here. See {@link org.benf.cfr.reader.util.getopt.OptionsImpl}
 */
public class CFRSettings {
    private Map<String, String> internalSettings = new HashMap<>();

    /**
     * Set the given key to the given value. Keys can be found in {@link org.benf.cfr.reader.util.getopt.OptionsImpl}
     *
     * @return The same instance, for chaining
     */
    public <T> CFRSettings set(String key, String value) {
        this.internalSettings.put(key, value);
        return this;
    }

    public Map<String, String> getSettings() {
        return this.internalSettings;
    }
}
