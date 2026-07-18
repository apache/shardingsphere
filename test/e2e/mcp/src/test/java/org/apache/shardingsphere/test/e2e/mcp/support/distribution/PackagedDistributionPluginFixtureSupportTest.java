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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackagedDistributionPluginFixtureSupportTest {
    
    private static final String DESCRIPTOR_DIRECTORY = "META-INF/shardingsphere-mcp/mcp-descriptors";
    
    private static final String DESCRIPTOR_ENTRY = DESCRIPTOR_DIRECTORY + "/mcp-descriptor-test-fixture.yaml";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertCreateFixturePluginJar() throws IOException {
        Path actual = PackagedDistributionPluginFixtureSupport.createFixturePluginJar(tempDir);
        assertTrue(Files.isRegularFile(actual));
        try (
                JarFile jarFile = new JarFile(actual.toFile());
                URLClassLoader classLoader = new URLClassLoader(new URL[]{actual.toUri().toURL()}, getClass().getClassLoader());
                Stream<String> descriptorResources = ClasspathResourceDirectoryReader.read(classLoader, DESCRIPTOR_DIRECTORY)) {
            Thread currentThread = Thread.currentThread();
            ClassLoader originalClassLoader = currentThread.getContextClassLoader();
            JarEntry actualDescriptorDirectory = jarFile.getJarEntry(DESCRIPTOR_DIRECTORY + "/");
            assertNotNull(actualDescriptorDirectory);
            assertTrue(actualDescriptorDirectory.isDirectory());
            assertNotNull(jarFile.getJarEntry("META-INF/services/org.apache.shardingsphere.mcp.spi.MCPHandlerProvider"));
            assertNotNull(jarFile.getJarEntry("org/apache/shardingsphere/test/e2e/mcp/support/fixture/plugin/PluginFixtureHandlerProvider.class"));
            assertThat(descriptorResources.toList(), hasItem(DESCRIPTOR_ENTRY));
            try {
                currentThread.setContextClassLoader(classLoader);
                assertThat(MCPDescriptorCatalogLoader.load().getProtocolDescriptors().getToolDescriptors().stream()
                        .map(MCPToolDescriptor::getName).toList(), hasItem("fixture_ping"));
            } finally {
                currentThread.setContextClassLoader(originalClassLoader);
            }
        }
    }
    
    @Test
    void assertRemoveOfficialFeatureJars() throws IOException {
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-encrypt-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-mask-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-broadcast-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-readwrite-splitting-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-shadow-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-sharding-test.jar"), "");
        Files.writeString(tempDir.resolve("shardingsphere-mcp-feature-other-test.jar"), "");
        List<String> actual = PackagedDistributionPluginFixtureSupport.removeOfficialFeatureJars(tempDir);
        assertThat(actual, containsInAnyOrder(
                "shardingsphere-mcp-feature-encrypt-test.jar",
                "shardingsphere-mcp-feature-mask-test.jar",
                "shardingsphere-mcp-feature-broadcast-test.jar",
                "shardingsphere-mcp-feature-readwrite-splitting-test.jar",
                "shardingsphere-mcp-feature-shadow-test.jar",
                "shardingsphere-mcp-feature-sharding-test.jar"));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-encrypt-test.jar")));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-mask-test.jar")));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-broadcast-test.jar")));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-readwrite-splitting-test.jar")));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-shadow-test.jar")));
        assertTrue(Files.notExists(tempDir.resolve("shardingsphere-mcp-feature-sharding-test.jar")));
        assertTrue(Files.isRegularFile(tempDir.resolve("shardingsphere-mcp-feature-other-test.jar")));
    }
}
