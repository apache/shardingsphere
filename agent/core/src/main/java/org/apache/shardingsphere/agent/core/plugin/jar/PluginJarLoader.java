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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin jar loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginJarLoader {
    
    private static final Logger LOGGER = Logger.getLogger(PluginJarLoader.class.getName());
    
    /**
     * Load plugin jars.
     *
     * @param agentRootPath agent root path
     * @return plugin jars
     * @throws IOException IO exception
     */
    public static Collection<JarFile> load(final File agentRootPath) throws IOException {
        Collection<File> jarFiles = getJarFiles(new File(String.join(File.separator, agentRootPath.getPath(), "plugins")));
        Collection<JarFile> result = new LinkedList<>();
        for (File each : jarFiles) {
            result.add(new JarFile(each, true));
            LOGGER.log(Level.INFO, "Loaded jar: {0}", new String[]{each.getName()});
        }
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<File> getJarFiles(final File file) {
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) {
                File currentFile = path.toFile();
                if (currentFile.isFile() && !currentFile.isHidden() && currentFile.getName().endsWith(".jar")) {
                    result.add(currentFile);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
}
