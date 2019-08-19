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

package com.heliosdecompiler.transformerapi.common.krakatau;

import com.heliosdecompiler.transformerapi.TransformationException;

public class KrakatauException extends TransformationException {
    private final Reason reason;
    private final String stdout;
    private final String stderr;

    public KrakatauException(Reason reason, String stdout, String stderr) {
        this.reason = reason;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public KrakatauException(String message, Reason reason, String stdout, String stderr) {
        super(message);
        this.reason = reason;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public KrakatauException(String message, Throwable cause, Reason reason, String stdout, String stderr) {
        super(message, cause);
        this.reason = reason;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public KrakatauException(Throwable cause, Reason reason, String stdout, String stderr) {
        super(cause);
        this.reason = reason;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public KrakatauException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Reason reason, String stdout, String stderr) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.reason = reason;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public Reason getReason() {
        return reason;
    }

    public String getStdout() {
        return stdout == null ? "" : stdout;
    }

    public String getStderr() {
        return stderr == null ? "" : stderr;
    }

    public enum Reason {
        FAILED_TO_CANONICALIZE_PATH,
        FAILED_TO_CREATE_INPUT_FILE,
        FAILED_TO_LAUNCH_PROCESS,
        FAILED_TO_CREATE_TEMP_DIR,
        FAILED_TO_CREATE_TEMP_FILE,
        UNEXPECTED_OUTPUT,
        FAILED_TO_OPEN_OUTPUT,
        MISSING_PYTHON2,
        MISSING_KRAKATAU,
        FAILED_TO_LAUNCH_SERVER,
        SERVER_IO_ERROR,
        UNKNOWN
    }
}
