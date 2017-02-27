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

package com.heliosdecompiler.transformerapi.common.krakatau;

import java.io.File;
import java.io.IOException;

public class KrakatauConstants {
    public static final String NAME = "Krakatau";
    public static final String VERSION = "5ca262a7318e89a13897725197f5f55381f8736e";

    public static Process launchProcess(ProcessBuilder builder, KrakatauSettings settings) throws KrakatauException {
        if (settings.getProcessCreator() == null) {
            try {
                return builder.start();
            } catch (IOException ex) {
                throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_LAUNCH_PROCESS, null, null);
            }
        } else {
            return settings.getProcessCreator().apply(builder);
        }
    }

    public static String canon(File file) throws KrakatauException {
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            throw new KrakatauException(ex, KrakatauException.Reason.FAILED_TO_CANONICALIZE_PATH, null, null);
        }
    }
}
