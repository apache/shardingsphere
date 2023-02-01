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

package org.apache.shardingsphere.agent.core.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.path.AgentPath;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;

/**
 * Agent logger class loader factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentLoggerClassLoaderFactory {
    
    /**
     * Create agent logger class loader.
     *
     * @return agent logger class loader
     */
    @SneakyThrows(URISyntaxException.class)
    public static AgentLoggerClassLoader create() {
        File agentFle = new File(AgentLoggerFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return agentFle.isFile() && agentFle.getName().endsWith(".jar") ? new AgentLoggerClassLoader(getLoggingJars(), getLoggingResourcePath()) : new AgentLoggerClassLoader();
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<JarFile> getLoggingJars() {
        Collection<JarFile> result = new LinkedList<>();
        Files.walkFileTree(new File(String.join(File.separator, AgentPath.getRootPath().getPath(), "log-lib")).toPath(), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) {
                if (path.toFile().isFile() && path.toFile().getName().endsWith(".jar")) {
                    result.add(getJarFile(path));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private static JarFile getJarFile(final Path path) {
        return new JarFile(path.toFile(), true);
    }
    
    private static File getLoggingResourcePath() {
        return new File(String.join(File.separator, AgentPath.getRootPath().getPath(), "conf"));
    }
}
