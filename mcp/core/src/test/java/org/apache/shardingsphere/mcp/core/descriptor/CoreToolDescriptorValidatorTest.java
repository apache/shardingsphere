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

package org.apache.shardingsphere.mcp.core.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        assertTrue(new CoreToolDescriptorValidator().supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor("database_gateway_search_metadata")));
    }
    
    @Test
    void assertSearchMetadataDocumentsCompleteSearchResult() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor("database_gateway_search_metadata");
        assertFalse(getInputProperties(descriptor).containsKey("page_size"));
        assertFalse(getInputProperties(descriptor).containsKey("page_token"));
        assertFalse(getOutputProperties(descriptor).containsKey("next_page_token"));
        assertThat(findOutputProperty(descriptor, "has_more").get("description"),
                is("Whether an application-level continuation mode is active. Metadata search returns false; not MCP list pagination."));
        assertThat(findOutputProperty(descriptor, "continuation_mode").get("description"),
                is("ShardingSphere application continuation mode for this metadata search result; not MCP cursor or nextCursor semantics."));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingSearchMetadataItemField() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor("database_gateway_search_metadata");
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        Map<String, Object> items = new LinkedHashMap<>((Map<String, Object>) properties.get("items"));
        Map<String, Object> itemSchema = new LinkedHashMap<>((Map<String, Object>) items.get("items"));
        Map<String, Object> itemProperties = new LinkedHashMap<>((Map<String, Object>) itemSchema.get("properties"));
        itemProperties.remove("matched_value");
        itemSchema.put("properties", itemProperties);
        items.put("items", itemSchema);
        properties.put("items", items);
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new CoreToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_search_metadata` outputSchema item must declare `matched_value`."));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingExecuteUpdateMode() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor("database_gateway_execute_update");
        Map<String, Object> inputSchema = new LinkedHashMap<>(descriptor.getInputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) inputSchema.get("properties"));
        properties.remove("execution_mode");
        inputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new CoreToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), inputSchema, descriptor.getOutputSchema(), descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_execute_update` must declare execution_mode."));
    }
    
    private Map<?, ?> findOutputProperty(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        return (Map<?, ?>) getOutputProperties(toolDescriptor).get(fieldName);
    }
    
    private Map<?, ?> getInputProperties(final MCPToolDescriptor toolDescriptor) {
        return (Map<?, ?>) toolDescriptor.getInputSchema().get("properties");
    }
    
    private Map<?, ?> getOutputProperties(final MCPToolDescriptor toolDescriptor) {
        return (Map<?, ?>) toolDescriptor.getOutputSchema().get("properties");
    }
}
