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
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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
    void assertGetRootPath() throws IOException {
        Path jarPath = createAgentJar(tempDir.resolve("agent-path.jar"));
        try (
                URLClassLoader customClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, null);
                SystemClassLoaderContext ignored = new SystemClassLoaderContext(customClassLoader)) {
            assertThat(AgentPath.getRootPath(), is(jarPath.getParent().toFile()));
        }
    }
    
    @Test
    void assertGetRootPathWhenJarMissing() throws IOException {
        URL resourceUrl = new URL("jar:file:/non-existent/path/agent-path.jar!/org/apache/shardingsphere/agent/core/path/AgentPath.class");
        ClassLoader customClassLoader = new SingleResourceClassLoader(resourceUrl);
        try (SystemClassLoaderContext ignored = new SystemClassLoaderContext(customClassLoader)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class, AgentPath::getRootPath);
            assertThat(ex.getMessage(), is(String.format("Can not locate agent jar file by URL `%s`.", resourceUrl)));
        }
    }
    
    @Test
    void assertGetRootPathWhenUrlMalformed() throws IOException {
        URL resourceUrl = new URL("jar:file:/invalid path!/org/apache/shardingsphere/agent/core/path/AgentPath.class");
        ClassLoader customClassLoader = new SingleResourceClassLoader(resourceUrl);
        try (SystemClassLoaderContext ignored = new SystemClassLoaderContext(customClassLoader)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class, AgentPath::getRootPath);
            assertThat(ex.getMessage(), is(String.format("Can not locate agent jar file by URL `%s`.", resourceUrl)));
            assertThat(ex.getCause(), isA(URISyntaxException.class));
        }
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
    
    private static final class SystemClassLoaderContext implements AutoCloseable {
        
        private static final Field SYSTEM_CLASS_LOADER_FIELD = createSystemClassLoaderField();
        
        private static boolean javaLangOpened;
        
        private final ClassLoader originalClassLoader;
        
        private SystemClassLoaderContext(final ClassLoader classLoader) {
            try {
                originalClassLoader = (ClassLoader) SYSTEM_CLASS_LOADER_FIELD.get(null);
                SYSTEM_CLASS_LOADER_FIELD.set(null, classLoader);
            } catch (final IllegalAccessException ex) {
                throw new IllegalStateException("Failed to override system class loader.", ex);
            }
        }
        
        @Override
        public void close() {
            try {
                SYSTEM_CLASS_LOADER_FIELD.set(null, originalClassLoader);
            } catch (final IllegalAccessException ex) {
                throw new IllegalStateException("Failed to restore system class loader.", ex);
            }
        }
        
        private static Field createSystemClassLoaderField() {
            ensureJavaLangOpened();
            try {
                Method getDeclaredFieldsMethod = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFieldsMethod.setAccessible(true);
                for (Field each : (Field[]) getDeclaredFieldsMethod.invoke(ClassLoader.class, false)) {
                    if ("scl".equals(each.getName())) {
                        each.setAccessible(true);
                        return each;
                    }
                }
                throw new IllegalStateException("System class loader field not found.");
            } catch (final ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to access system class loader field.", ex);
            }
        }
        
        private static void ensureJavaLangOpened() {
            if (javaLangOpened) {
                return;
            }
            try {
                Class<?> moduleClass = Class.forName("java.lang.Module");
                Method getModuleMethod = Class.class.getMethod("getModule");
                Object javaBaseModule = getModuleMethod.invoke(Object.class);
                Object currentModule = getModuleMethod.invoke(AgentPathTest.class);
                Method redefineModuleMethod = Instrumentation.class.getMethod("redefineModule", moduleClass, Set.class, Map.class, Map.class, Set.class, Map.class);
                redefineModuleMethod.invoke(ByteBuddyAgent.install(), javaBaseModule, Collections.emptySet(), Collections.emptyMap(),
                        Collections.singletonMap("java.lang", Collections.singleton(currentModule)), Collections.emptySet(), Collections.emptyMap());
                javaLangOpened = true;
            } catch (final ClassNotFoundException | NoSuchMethodException ignored) {
                javaLangOpened = true;
            } catch (final ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to open java.lang module for reflection.", ex);
            }
        }
    }
    
    private static final class SingleResourceClassLoader extends ClassLoader {
        
        private final URL resourceUrl;
        
        private SingleResourceClassLoader(final URL resourceUrl) {
            super(null);
            this.resourceUrl = resourceUrl;
        }
        
        @Override
        public URL getResource(final String name) {
            return AGENT_PATH_RESOURCE.equals(name) ? resourceUrl : super.getResource(name);
        }
    }
}
