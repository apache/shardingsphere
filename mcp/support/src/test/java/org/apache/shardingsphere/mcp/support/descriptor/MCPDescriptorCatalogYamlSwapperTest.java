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
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPDescriptorCatalogYamlSwapperTest {
    
    @Test
    void assertSwapWithMissingToolAnnotations() {
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setTools(List.of(createYamlToolDescriptor()));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPDescriptorCatalogYamlSwapper.swap(List.of(yamlCatalog)));
        assertThat(actual.getMessage(), is("MCP descriptor catalog property `tools[0].annotations` is required."));
    }
    
    @Test
    void assertSwapWithInvalidToolAnnotations() {
        YamlMCPToolAnnotations yamlAnnotations = new YamlMCPToolAnnotations();
        yamlAnnotations.setReadOnlyHint(true);
        yamlAnnotations.setDestructiveHint(false);
        yamlAnnotations.setIdempotentHint(true);
        YamlMCPToolDescriptor yamlTool = createYamlToolDescriptor();
        yamlTool.setAnnotations(yamlAnnotations);
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setTools(List.of(yamlTool));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPDescriptorCatalogYamlSwapper.swap(List.of(yamlCatalog)));
        assertThat(actual.getMessage(), is("MCP descriptor catalog property `tools[0].annotations.openWorldHint` is required."));
    }
    
    private YamlMCPToolDescriptor createYamlToolDescriptor() {
        YamlMCPToolDescriptor result = new YamlMCPToolDescriptor();
        result.setName("database_gateway_search_metadata");
        result.setTitle("Search Metadata");
        result.setDescription("Search metadata.");
        result.setInputSchema(createSchema());
        result.setOutputSchema(createSchema());
        return result;
    }
    
    private Map<String, Object> createSchema() {
        Map<String, Object> result = new LinkedHashMap<>(1, 1F);
        result.put("type", "object");
        return result;
    }
}
