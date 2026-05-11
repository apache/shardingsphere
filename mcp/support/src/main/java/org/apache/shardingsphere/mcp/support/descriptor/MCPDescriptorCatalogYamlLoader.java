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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Stream;

final class MCPDescriptorCatalogYamlLoader {
    
    private static final String DESCRIPTOR_DIRECTORY = "META-INF/shardingsphere-mcp/descriptors";
    
    private MCPDescriptorCatalogYamlLoader() {
    }
    
    static Collection<YamlMCPDescriptorCatalog> load() {
        try (Stream<String> resources = ClasspathResourceDirectoryReader.read(DESCRIPTOR_DIRECTORY)) {
            return resources.filter(each -> each.endsWith(".yaml") || each.endsWith(".yml")).sorted().map(MCPDescriptorCatalogYamlLoader::loadYamlCatalog).toList();
        }
    }
    
    private static YamlMCPDescriptorCatalog loadYamlCatalog(final String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (null == inputStream) {
                throw new IllegalStateException(String.format("MCP descriptor resource `%s` is not found.", resourceName));
            }
            return YamlEngine.unmarshal(inputStream.readAllBytes(), YamlMCPDescriptorCatalog.class);
        } catch (final IOException ex) {
            throw new IllegalStateException(String.format("Failed to load MCP descriptor resource `%s`.", resourceName), ex);
        }
    }
}
