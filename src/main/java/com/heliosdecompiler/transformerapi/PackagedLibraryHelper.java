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

package com.heliosdecompiler.transformerapi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackagedLibraryHelper {
    private static final File ROOT_DIR = new File(System.getProperty("user.home") + File.separatorChar + ".helios");
    private static final File PACKAGES_DIR = new File(ROOT_DIR, "packages");

    public static Exception checkPackagedLibrary(String name, String version) {
        File libFolder = new File(PACKAGES_DIR, name);
        if (libFolder.isFile()) {
            if (!libFolder.delete()) {
                return new IOException("Could not delete file with same name as folder");
            }
        }

        if (!libFolder.exists()) {
            if (!libFolder.mkdirs()) {
                return new IOException("Could not create folder");
            }
        }

        File[] files = libFolder.listFiles();
        if (files == null) {
            return new IOException("Could not list files");
        }

        for (File file : files) {
            if (!file.getName().equals(name + "-" + version)) {
                FileUtils.deleteQuietly(file);
            }
        }

        File versionDirectory = new File(libFolder, name + "-" + version);
        if (!versionDirectory.exists()) {
            InputStream inputStream = PackagedLibraryHelper.class.getResourceAsStream("/" + name + "-" + version + ".zip");
            if (inputStream == null) {
                return new IOException("Could not find packaged library");
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry ent;
                while ((ent = zipInputStream.getNextEntry()) != null) {
                    File file = new File(libFolder, ent.getName());

                    if (ent.isDirectory()) {
                        if (!file.exists() && !file.mkdirs()) {
                            return new IOException("Could not make directory " + ent.getName() + " " + file);
                        }
                    } else {
                        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                            return new IOException("Could not make directory " + ent.getName() + " " +file.getParentFile());
                        }
                        IOUtils.copy(zipInputStream, new FileOutputStream(file));
                    }
                }
            } catch (IOException ex) {
                return ex;
            }
        }
        return null;
    }

    public static File getPackageRoot(String name, String version) {
        return new File(PACKAGES_DIR, name + File.separatorChar + name + "-" + version);
    }
}
