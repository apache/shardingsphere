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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.Collection;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPDescriptorCatalogYamlLoader {
    
    private static final String MCP_DESCRIPTOR_DIRECTORY = "META-INF/shardingsphere-mcp/mcp-descriptors";
    
    static Collection<YamlMCPDescriptorCatalog> load() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (Stream<String> resources = ClasspathResourceDirectoryReader.read(MCP_DESCRIPTOR_DIRECTORY)) {
            return resources.filter(each -> each.endsWith(".yaml") || each.endsWith(".yml")).distinct()
                    .flatMap(classLoader::resources).sorted(Comparator.comparing(URL::toExternalForm)).map(MCPDescriptorCatalogYamlLoader::loadYamlCatalog).toList();
        }
    }
    
    private static YamlMCPDescriptorCatalog loadYamlCatalog(final URL resourceUrl) {
        String resourceName = resourceUrl.toExternalForm();
        try (InputStream inputStream = resourceUrl.openStream()) {
            byte[] yamlBytes = inputStream.readAllBytes();
            MCPDescriptorYamlKeyValidator.validate(resourceName, yamlBytes);
            return YamlEngine.unmarshal(yamlBytes, YamlMCPDescriptorCatalog.class);
        } catch (final IOException ex) {
            throw new IllegalStateException(String.format("Failed to load MCP descriptor resource `%s`.", resourceName), ex);
        }
    }
}
