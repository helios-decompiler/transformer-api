/*
 * Sourced from The MIT License (MIT)

 * Copyright (c) 2011-2014 Lee Benfield - http://www.benf.org/other/cfr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.heliosdecompiler.transformerapi.decompilers.cfr;

import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// copypaste from CFR using Procyon with modifications to getDecompilationFor
public class PluginRunner {
    private final DCCommonState dcCommonState;
    private final IllegalIdentifierDump illegalIdentifierDump;
    private final ClassFileSource classFileSource;

    public PluginRunner() {
        this(MapFactory.newMap(), null);
    }

    public PluginRunner(final Map<String, String> options) {
        this(options, null);
    }

    public PluginRunner(final Map<String, String> options, final ClassFileSource classFileSource) {
        this.illegalIdentifierDump = new IllegalIdentifierDump.Nop();
        this.dcCommonState = initDCState(options, classFileSource);
        this.classFileSource = classFileSource;
    }

    private static DCCommonState initDCState(final Map<String, String> optionsMap, ClassFileSource classFileSource) {
        final OptionsImpl options = new OptionsImpl(null, null, optionsMap);
        if (classFileSource == null) {
            classFileSource = new ClassFileSourceImpl(options);
        }
        final DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
        return dcCommonState;
    }

    public Options getOptions() {
        return this.dcCommonState.getOptions();
    }

    public List<List<String>> addJarPaths(final String[] jarPaths) {
        final List<List<String>> res = new ArrayList<List<String>>();
        for (final String jarPath : jarPaths) {
            res.add(this.addJarPath(jarPath));
        }
        return res;
    }

    public List<String> addJarPath(final String jarPath) {
        try {
            final List<JavaTypeInstance> types = this.dcCommonState.explicitlyLoadJar(jarPath);
            return Functional.map(types, (UnaryFunction<JavaTypeInstance, String>) new UnaryFunction<JavaTypeInstance, String>() {
                @Override
                public String invoke(final JavaTypeInstance arg) {
                    return arg.getRawName();
                }
            });
        } catch (Exception e) {
            return new ArrayList<String>();
        }
    }

    public String getDecompilationFor(final String classFilePath) {
        final StringBuilder output = new StringBuilder();
        final DumperFactory dumperFactory = new PluginDumperFactory(output);
        Main.doClass(this.dcCommonState, classFilePath, dumperFactory);
        return output.toString();
    }

    class StringStreamDumper extends StreamDumper {
        private final StringBuilder stringBuilder;

        public StringStreamDumper(final StringBuilder sb, final TypeUsageInformation typeUsageInformation, final Options options) {
            super(typeUsageInformation, options, PluginRunner.this.illegalIdentifierDump);
            this.stringBuilder = sb;
        }

        @Override
        protected void write(final String s) {
            new Exception().printStackTrace();
            this.stringBuilder.append(s);
        }

        @Override
        public Dumper identifier(String s) {
            System.out.println("Identifier: " + s);
            return super.identifier(s);
        }

        @Override
        public void close() {
        }

        @Override
        public void addSummaryError(final Method method, final String s) {
        }
    }

    private class PluginDumperFactory implements DumperFactory {
        private final StringBuilder outBuffer;

        public PluginDumperFactory(final StringBuilder out) {
            this.outBuffer = out;
        }

        @Override
        public Dumper getNewTopLevelDumper(final Options options, final JavaTypeInstance classType, final SummaryDumper summaryDumper, final TypeUsageInformation typeUsageInformation, final IllegalIdentifierDump illegalIdentifierDump) {
            return new StringStreamDumper(this.outBuffer, typeUsageInformation, options);
        }

        @Override
        public SummaryDumper getSummaryDumper(final Options options) {
            if (!options.optionIsSet(OptionsImpl.OUTPUT_DIR)) {
                return new NopSummaryDumper();
            }
            return new FileSummaryDumper(options.getOption((PermittedOptionProvider.ArgumentParam<String, Void>) OptionsImpl.OUTPUT_DIR), options, null);
        }
    }
}


