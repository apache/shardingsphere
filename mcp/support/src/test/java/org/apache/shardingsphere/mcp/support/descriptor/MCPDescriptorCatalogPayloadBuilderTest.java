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

import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPDescriptorCatalogPayloadBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    void assertBuildResourceAnnotationsPayload() {
        MCPResourceDescriptor emptyAnnotationsResource = createResourceDescriptor("shardingsphere://empty", MCPResourceAnnotations.EMPTY);
        MCPResourceAnnotations priorityZeroAnnotations = new MCPResourceAnnotations(List.of("assistant"), 0D, true, null);
        MCPResourceDescriptor priorityZeroResource = createResourceDescriptor("shardingsphere://priority-zero", priorityZeroAnnotations);
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(createCatalog(List.of(emptyAnnotationsResource, priorityZeroResource), List.of()), List.of(), List.of(), List.of());
        List<Map<String, Object>> actualResources = (List<Map<String, Object>>) payload.get("resources");
        assertFalse(actualResources.get(0).containsKey("annotations"));
        assertThat((Map<String, Object>) actualResources.get(1).get("annotations"), is(Map.of("audience", List.of("assistant"), "priority", 0D)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void assertBuildToolAnnotationsPayload() {
        MCPToolDescriptor toolDescriptor = createToolDescriptor(new MCPToolAnnotations(null, false, true, false, true));
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(createCatalog(List.of(), List.of(toolDescriptor)), List.of(), List.of(), List.of());
        List<Map<String, Object>> actualTools = (List<Map<String, Object>>) payload.get("tools");
        assertThat((Map<String, Object>) actualTools.get(0).get("annotations"), is(Map.of(
                "readOnlyHint", false,
                "destructiveHint", true,
                "idempotentHint", false,
                "openWorldHint", true)));
    }

    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors) {
        return new MCPDescriptorCatalog(resourceDescriptors, List.of(), List.of(), toolDescriptors, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private MCPResourceDescriptor createResourceDescriptor(final String uri, final MCPResourceAnnotations annotations) {
        return new MCPResourceDescriptor(uri, "test-resource", "Test Resource", "Read a test resource.", "application/json", annotations, Map.of());
    }

    private MCPToolDescriptor createToolDescriptor(final MCPToolAnnotations annotations) {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", Map.of(), Map.of(), annotations, Map.of());
    }
}
