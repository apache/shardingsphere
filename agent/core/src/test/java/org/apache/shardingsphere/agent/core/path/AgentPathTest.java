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

package org.apache.shardingsphere.agent.core.path;

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentPathTest {
    
    private static final String AGENT_PATH_RESOURCE = "org/apache/shardingsphere/agent/core/path/AgentPath.class";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertGetRootPath() throws IOException, ReflectiveOperationException {
        Path jarPath = createAgentJar(tempDir.resolve("agent-path.jar"));
        URL resourceUrl = new URL(String.format("jar:%s!/%s", jarPath.toUri().toURL(), AGENT_PATH_RESOURCE));
        assertThat(invokeGetJarFile(resourceUrl.toString()), is(jarPath.toFile()));
    }
    
    @Test
    void assertGetRootPathWhenJarMissing() throws IOException {
        URL resourceUrl = new URL("jar:file:/non-existent/path/agent-path.jar!/org/apache/shardingsphere/agent/core/path/AgentPath.class");
        IllegalStateException ex = assertThrows(IllegalStateException.class, getGetJarFileExecutable(resourceUrl.toString()));
        assertThat(ex.getMessage(), is(String.format("Can not locate agent jar file by URL `%s`.", resourceUrl)));
    }
    
    @Test
    void assertGetRootPathWhenUrlMalformed() throws IOException {
        URL resourceUrl = new URL("jar:file:/invalid path!/org/apache/shardingsphere/agent/core/path/AgentPath.class");
        IllegalStateException ex = assertThrows(IllegalStateException.class, getGetJarFileExecutable(resourceUrl.toString()));
        assertThat(ex.getMessage(), is(String.format("Can not locate agent jar file by URL `%s`.", resourceUrl)));
        assertThat(ex.getCause(), isA(URISyntaxException.class));
    }
    
    private Path createAgentJar(final Path jarPath) throws IOException {
        try (InputStream inputStream = AgentPath.class.getResourceAsStream("/" + AGENT_PATH_RESOURCE)) {
            assertNotNull(inputStream);
            byte[] expectedBytes = ByteStreams.toByteArray(inputStream);
            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarPath))) {
                jarOutputStream.putNextEntry(new JarEntry(AGENT_PATH_RESOURCE));
                jarOutputStream.write(expectedBytes);
                jarOutputStream.closeEntry();
            }
        }
        return jarPath;
    }
    
    private Executable getGetJarFileExecutable(final String resourceUrl) {
        return () -> invokeGetJarFile(resourceUrl);
    }
    
    private File invokeGetJarFile(final String resourceUrl) throws ReflectiveOperationException {
        Method getJarFileMethod = AgentPath.class.getDeclaredMethod("getJarFile", String.class);
        try {
            return (File) Plugins.getMemberAccessor().invoke(getJarFileMethod, null, resourceUrl);
        } catch (final InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw ex;
        }
    }
}
