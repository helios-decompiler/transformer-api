# Transformer API
[![Build Status](https://ci.samczsun.com/job/helios-decompiler/job/Transformer%20API/badge/icon)](https://ci.samczsun.com/job/helios-decompiler/job/Transformer%20API/)


The Transformer API provides convenient access to different transformers (currently decompilers only) under a unified
API. The API is still subject to major changes, but only with a major version bump.

## Usage

Currently, this API supports four decompilers (JD-GUI is, once again, excluded due to licensing issues). They are:

- Krakatau
- Fernflower
- Procyon
- CFR

Decompilers can be accessed either via `StandardTransformers.DECOMPILER` or by creating a new instance. They are also
stateless, which means you can use the same instance across different threads.

An example program decompiling a file using Fernflower is shown below:

```java
    byte[] dataOfClass = /* source */ null;
    ClassData classData = ClassData.construct(dataOfClass);
    
    Result decompilationResult = StandardTransformers.Decompilers.FERNFLOWER.decompile(classData);
    
    System.out.println("Decompiled: " + decompilationResult.getDecompiledResult());
```

In the future, compilers (javac, ecj), disassemblers (krakatau, procyon, smali), and assemblers (krakatau, baksmali)
will be added.

## Features

### Filesystem

Most decompilers (and/or APIs) haphazardly spew files onto disk by default, which causes them to hard fail when the filesystem
 decides that the filename is illegal. Some don't even properly support unicode inside the archives which again causes
 decompilation to hard fail. This API not only provides a common gateway to all decompilers, but also tries extremely
 hard to force decompilation to be in-memory only. This means very rarely will decompilation fail because of funky
 unicode paths or some other filesystem-related reason.
 
### Exception Handling

Logging is used when a decompiler supports it, and `System.out`/`System.err` is efficiently redirected (by patching the
decompiler beforehand) if it does not. The end result is minimal garbage in the console and easy access to debugging
information

### Updates

This API will be updated as decompilers receive updates, which means fixes reach you faster.

Also, this API ships with a [modified version](https://github.com/helios-decompiler/krakatau) of Krakatau which
 contains unicode fixes not yet merged with upstream.
 
