/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.core.plugin.jar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginJarLoaderTest {
    
    @Test
    void assertLoadJarFiles(@TempDir final Path tempDir) throws IOException {
        Path pluginsDir = Files.createDirectory(tempDir.resolve("plugins"));
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(pluginsDir.resolve("sample.jar")), new Manifest())) {
            jarOutputStream.putNextEntry(new JarEntry("sample.txt"));
            jarOutputStream.write("sample".getBytes());
            jarOutputStream.closeEntry();
        }
        Files.write(pluginsDir.resolve("README.txt"), "ignored".getBytes(StandardCharsets.UTF_8));
        Collection<JarFile> actual = PluginJarLoader.load(tempDir.toFile());
        assertThat(actual.size(), is(1));
        JarFile jarFile = actual.iterator().next();
        assertThat(new File(jarFile.getName()).getName(), is("sample.jar"));
        for (JarFile each : actual) {
            each.close();
        }
    }
    
    @Test
    void assertLoadWithoutJar(@TempDir final Path tempDir) throws IOException {
        Files.createDirectory(tempDir.resolve("plugins"));
        assertTrue(PluginJarLoader.load(tempDir.toFile()).isEmpty());
    }
}
