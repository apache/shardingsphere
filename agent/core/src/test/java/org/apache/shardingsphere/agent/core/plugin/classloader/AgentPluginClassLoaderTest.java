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

package org.apache.shardingsphere.agent.core.plugin.classloader;

import net.bytebuddy.ByteBuddy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentPluginClassLoaderTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void assertLoadClassWithManifestAndResources() throws Exception {
        Map<String, byte[]> pluginEntries = new HashMap<>();
        pluginEntries.put("org/apache/shardingsphere/agent/core/plugin/fixture/PluginFixture.class",
                createClassBytes("org.apache.shardingsphere.agent.core.plugin.fixture.PluginFixture"));
        pluginEntries.put("org/apache/shardingsphere/agent/core/plugin/fixture/AnotherPluginFixture.class",
                createClassBytes("org.apache.shardingsphere.agent.core.plugin.fixture.AnotherPluginFixture"));
        pluginEntries.put("fixture-resource.txt", "foo_resource".getBytes(StandardCharsets.UTF_8));
        Manifest manifest = createManifest("spec_title", "1.0", "spec_vendor", "impl_title", "2.0", "impl_vendor");
        try (JarFile emptyJar = createJar("foo-empty.jar", new HashMap<>(), createBasicManifest());
             JarFile pluginJar = createJar("foo-plugin.jar", pluginEntries, manifest)) {
            Collection<JarFile> jars = new LinkedList<>();
            jars.add(emptyJar);
            jars.add(pluginJar);
            AgentPluginClassLoader classLoader = createClassLoader(jars);
            Class<?> loadedClass = classLoader.loadClass("org.apache.shardingsphere.agent.core.plugin.fixture.PluginFixture");
            Package loadedPackage = loadedClass.getPackage();
            assertNotNull(loadedPackage);
            assertThat(loadedPackage.getSpecificationTitle(), is("spec_title"));
            assertThat(loadedPackage.getSpecificationVersion(), is("1.0"));
            assertThat(loadedPackage.getSpecificationVendor(), is("spec_vendor"));
            assertThat(loadedPackage.getImplementationTitle(), is("impl_title"));
            assertThat(loadedPackage.getImplementationVersion(), is("2.0"));
            assertThat(loadedPackage.getImplementationVendor(), is("impl_vendor"));
            Class<?> anotherClass = classLoader.loadClass("org.apache.shardingsphere.agent.core.plugin.fixture.AnotherPluginFixture");
            assertNotNull(anotherClass);
            Enumeration<URL> resources = classLoader.findResources("fixture-resource.txt");
            assertTrue(resources.hasMoreElements());
            URL resourceUrl = resources.nextElement();
            assertThat(resourceUrl.toString(), is(String.format("jar:file:%s!/fixture-resource.txt", pluginJar.getName())));
            assertThat(resources.hasMoreElements(), is(false));
            URL directResource = classLoader.findResource("fixture-resource.txt");
            assertNotNull(directResource);
            assertThat(directResource.toString(), is(String.format("jar:file:%s!/fixture-resource.txt", pluginJar.getName())));
        }
    }
    
    @Test
    void assertLoadClassWithoutPackage() throws Exception {
        Map<String, byte[]> entries = new HashMap<>();
        entries.put("DefaultFixture.class", createClassBytes("DefaultFixture"));
        try (JarFile defaultJar = createJar("foo-default.jar", entries, createBasicManifest())) {
            AgentPluginClassLoader classLoader = createClassLoader(new LinkedList<>(Collections.singleton(defaultJar)));
            Class<?> loadedClass = classLoader.loadClass("DefaultFixture");
            assertThat(loadedClass.getSimpleName(), is("DefaultFixture"));
        }
    }
    
    @Test
    void assertClassNotFoundWhenClassMissing() {
        AgentPluginClassLoader classLoader = new AgentPluginClassLoader(new URLClassLoader(new URL[0], null), new LinkedList<>());
        assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass("org.apache.shardingsphere.agent.core.plugin.fixture.MissingClass"));
    }
    
    @Test
    void assertClassNotFoundWhenInputStreamBroken() throws Exception {
        Path jarPath = tempDir.resolve("foo-broken.jar");
        try (JarFile ignored = createJar("foo-broken.jar", new HashMap<>(), createBasicManifest());
             JarFile brokenJar = new BrokenJarFile(jarPath.toFile())) {
            Collection<JarFile> jars = new LinkedList<>();
            jars.add(brokenJar);
            AgentPluginClassLoader classLoader = createClassLoader(jars);
            assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass("BrokenClass"));
        }
    }
    
    @Test
    void assertFindResourceReturnsNullWhenUrlMalformed() throws Exception {
        Map<String, byte[]> entries = new HashMap<>();
        entries.put("malformed.txt", "bar_resource".getBytes(StandardCharsets.UTF_8));
        try (JarFile malformedJar = createJar("foo#malformed.jar", entries, createBasicManifest())) {
            Collection<JarFile> jars = new LinkedList<>();
            jars.add(malformedJar);
            AgentPluginClassLoader classLoader = createClassLoader(jars);
            URL resourceUrl = classLoader.findResource("malformed.txt");
            assertThat(resourceUrl, is(nullValue()));
            Enumeration<URL> resources = classLoader.findResources("malformed.txt");
            assertThat(resources.hasMoreElements(), is(false));
        }
    }
    
    private AgentPluginClassLoader createClassLoader(final Collection<JarFile> jars) {
        return new AgentPluginClassLoader(new URLClassLoader(new URL[0], null), jars);
    }
    
    private Manifest createManifest(final String specTitle, final String specVersion, final String specVendor,
                                    final String implTitle, final String implVersion, final String implVendor) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.SPECIFICATION_TITLE, specTitle);
        attributes.put(Attributes.Name.SPECIFICATION_VERSION, specVersion);
        attributes.put(Attributes.Name.SPECIFICATION_VENDOR, specVendor);
        attributes.put(Attributes.Name.IMPLEMENTATION_TITLE, implTitle);
        attributes.put(Attributes.Name.IMPLEMENTATION_VERSION, implVersion);
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, implVendor);
        return manifest;
    }
    
    private Manifest createBasicManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }
    
    private JarFile createJar(final String fileName, final Map<String, byte[]> entries, final Manifest manifest) throws IOException {
        Path jarPath = tempDir.resolve(fileName);
        try (JarOutputStream out = null == manifest ? new JarOutputStream(Files.newOutputStream(jarPath)) : new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            for (Map.Entry<String, byte[]> each : entries.entrySet()) {
                out.putNextEntry(new JarEntry(each.getKey()));
                out.write(each.getValue());
                out.closeEntry();
            }
        }
        return new JarFile(jarPath.toFile());
    }
    
    private byte[] createClassBytes(final String className) {
        return new ByteBuddy().subclass(Object.class).name(className).make().getBytes();
    }
    
    private static final class BrokenJarFile extends JarFile {
        
        BrokenJarFile(final java.io.File file) throws IOException {
            super(file);
        }
        
        @Override
        public ZipEntry getEntry(final String name) {
            return new ZipEntry(name);
        }
        
        @Override
        public InputStream getInputStream(final ZipEntry ze) throws IOException {
            throw new IOException("broken");
        }
    }
}
