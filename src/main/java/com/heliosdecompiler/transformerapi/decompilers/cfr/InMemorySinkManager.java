/*
 * Copyright 2019 Sam Sun <github-contact@samczsun.com>
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

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InMemorySinkManager implements OutputSinkFactory {
    private List<SinkReturns.DecompiledMultiVer> outputs = new ArrayList<>();
    private List<SinkReturns.ExceptionMessage> exceptions = new ArrayList<>();
    private List<String> info = new ArrayList<>();

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
        switch (sinkType) {
            case JAVA:
                return Collections.singletonList(SinkClass.DECOMPILED_MULTIVER);
            case SUMMARY:
            case PROGRESS:
                return Collections.singletonList(SinkClass.STRING);
            case EXCEPTION:
                return Collections.singletonList(SinkClass.EXCEPTION_MESSAGE);
            default:
                throw new RuntimeException("unexpected sinktype " + sinkType);
        }
    }

    @SuppressWarnings("unchecked") // :(
    @Override
    public Sink getSink(SinkType sinkType, SinkClass sinkClass) {
        switch (sinkType) {
            case JAVA:
                return (Sink<SinkReturns.DecompiledMultiVer>) sinkable -> outputs.add(sinkable);
            case SUMMARY:
            case PROGRESS:
                return (Sink<String>) sinkable -> info.add(sinkable);
            case EXCEPTION:
                return (Sink<SinkReturns.ExceptionMessage>) sinkable -> exceptions.add(sinkable);
            default:
                throw new RuntimeException("unexpected sinktype " + sinkType);
        }
    }

    public List<SinkReturns.DecompiledMultiVer> getOutputs() {
        return outputs;
    }

    public List<SinkReturns.ExceptionMessage> getExceptions() {
        return exceptions;
    }

    public List<String> getInfo() {
        return info;
    }
}
