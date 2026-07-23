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

import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

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
