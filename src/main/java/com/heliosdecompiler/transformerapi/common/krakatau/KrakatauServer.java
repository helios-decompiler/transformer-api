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

package com.heliosdecompiler.transformerapi.common.krakatau;

import com.heliosdecompiler.transformerapi.FileContents;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class KrakatauServer {
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private ServerSocket server;

    private Map<String, FileContents> classpath = new HashMap<>();
    private Set<String> inputs = new HashSet<>();
    private Map<String, byte[]> outputs = new HashMap<>();

    public KrakatauServer(Collection<FileContents> inputs, Map<String, FileContents> classpath) {
        inputs.forEach(cd -> this.inputs.add(cd.getName()));

        this.classpath.putAll(classpath);
        inputs.forEach(cd -> this.classpath.put(cd.getName(), cd));
    }

    public void start() throws IOException {
        server = new ServerSocket(0);
        Thread serverThread = new Thread(() -> {
            while (!stopped.get()) {
                try {
                    Socket clientSocket = server.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void handleClient(Socket client) throws IOException {
        DataInputStream din = new DataInputStream(client.getInputStream());
        DataOutputStream dout = new DataOutputStream(client.getOutputStream());
        done:
        while (!client.isClosed()) {
            byte[] cmdBuf = new byte[1];
            din.readFully(cmdBuf);
            byte command = cmdBuf[0];
            switch (command) {
                // get targets
                case 1:
                    dout.writeInt(inputs.size());
                    for (String input : inputs) {
                        writeBytes(dout, input.getBytes(StandardCharsets.UTF_8));
                    }
                    break;
                // load classpath
                case 2:
                    String name = new String(readBytes(din));
                    if (!classpath.containsKey(name)) {
                        dout.writeBoolean(false);
                    } else {
                        FileContents fileContents = classpath.get(name);

                        dout.writeBoolean(true);
                        writeBytes(dout, fileContents.getData());
                    }
                    break;
                // decompilation output
                case 3:
                    String file = new String(readBytes(din));
                    byte[] contents = readBytes(din);
                    outputs.put(file, contents);
                    break;
                // disconnect
                case 4:
                    break done;
            }
        }
        client.close();
    }

    private byte[] readBytes(DataInputStream din) throws IOException {
        int len = din.readInt();
        byte[] buf = new byte[len];
        din.readFully(buf);
        return buf;
    }

    private void writeBytes(DataOutputStream dout, byte[] data) throws IOException {
        dout.writeInt(data.length);
        dout.write(data);
    }

    public void stop() throws IOException {
        stopped.set(true);
        server.close();
    }

    public int getPort() {
        return server.getLocalPort();
    }

    public Map<String, byte[]> getOutputs() {
        return outputs;
    }

    public Map<String, String> getOutputsAsString() {
        return outputs.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<String, String>(e.getKey(), new String(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
