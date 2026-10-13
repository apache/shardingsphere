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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPDescriptorCatalogYamlLoaderTest {
    
    private static final String DESCRIPTOR_PATH = "META-INF/shardingsphere-mcp/mcp-descriptors/shared.yaml";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoadSameNamedResourcesFromAllClasspathRoots() throws IOException {
        Path firstRoot = createDescriptorRoot("first", "first_tool");
        Path secondRoot = createDescriptorRoot("second", "second_tool");
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{firstRoot.toUri().toURL(), secondRoot.toUri().toURL()}, null)) {
            Thread.currentThread().setContextClassLoader(classLoader);
            Collection<YamlMCPDescriptorCatalog> actual = MCPDescriptorCatalogYamlLoader.load();
            assertThat(actual.stream().flatMap(each -> each.getTools().stream()).map(YamlMCPToolDescriptor::getName).toList(),
                    containsInAnyOrder("first_tool", "second_tool"));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    @Test
    void assertLoadYmlResourceFiltersNonYamlFiles() throws IOException {
        Path root = createDescriptorRoot("foo", "foo_tool");
        Path descriptor = root.resolve(DESCRIPTOR_PATH);
        Files.move(descriptor, descriptor.resolveSibling("shared.yml"));
        Files.writeString(descriptor.resolveSibling("foo.txt"), "foo_ignored");
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{root.toUri().toURL()}, null)) {
            Thread.currentThread().setContextClassLoader(classLoader);
            Collection<YamlMCPDescriptorCatalog> actual = MCPDescriptorCatalogYamlLoader.load();
            assertThat(actual.stream().flatMap(each -> each.getTools().stream()).map(YamlMCPToolDescriptor::getName).toList(), is(List.of("foo_tool")));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    @Test
    void assertLoadWithResourceReadFailure() throws IOException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = mock(ClassLoader.class);
        URL resource = mock(URL.class);
        IOException expectedCause = new IOException("foo_read_failure");
        when(resource.toExternalForm()).thenReturn("file:/foo.yaml");
        when(resource.openStream()).thenThrow(expectedCause);
        when(classLoader.resources(DESCRIPTOR_PATH)).thenReturn(Stream.of(resource));
        try (MockedStatic<ClasspathResourceDirectoryReader> mocked = mockStatic(ClasspathResourceDirectoryReader.class)) {
            mocked.when(() -> ClasspathResourceDirectoryReader.read("META-INF/shardingsphere-mcp/mcp-descriptors")).thenReturn(Stream.of(DESCRIPTOR_PATH));
            Thread.currentThread().setContextClassLoader(classLoader);
            IllegalStateException actual = assertThrows(IllegalStateException.class, MCPDescriptorCatalogYamlLoader::load);
            assertThat(actual.getMessage(), is("Failed to load MCP descriptor resource `file:/foo.yaml`."));
            assertThat(actual.getCause(), sameInstance(expectedCause));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private Path createDescriptorRoot(final String directoryName, final String toolName) throws IOException {
        Path result = tempDir.resolve(directoryName);
        Path descriptor = result.resolve(DESCRIPTOR_PATH);
        Files.createDirectories(descriptor.getParent());
        Files.writeString(descriptor, String.format("""
                tools:
                  - name: %s
                    annotations:
                      readOnlyHint: true
                      destructiveHint: false
                      idempotentHint: true
                      openWorldHint: true
                """, toolName));
        return result;
    }
}
