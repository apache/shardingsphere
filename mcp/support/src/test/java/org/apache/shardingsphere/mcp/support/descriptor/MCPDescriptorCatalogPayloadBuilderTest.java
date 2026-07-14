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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
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
    void assertBuildCatalogMetadataContractPayload() {
        List<MCPResourceDescriptor> resourceDescriptors = List.of(
                createResourceDescriptor("shardingsphere://capabilities", MCPResourceAnnotations.EMPTY),
                createResourceDescriptor("shardingsphere://databases", MCPResourceAnnotations.EMPTY));
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(
                createCatalog(resourceDescriptors, List.of(createToolDescriptor(MCPToolAnnotations.builder()
                        .title(null).readOnlyHint(false).destructiveHint(true).idempotentHint(false).openWorldHint(true).build()))),
                List.of("shardingsphere://capabilities"), List.of("database_gateway_test_tool"), List.of("SelectStatement"));
        assertThat(payload.get("response_mode"), is("catalog"));
        assertThat(payload.get("guidanceResource"), is("shardingsphere://guidance"));
        assertFalse(payload.containsKey("model_first_summary"));
        assertFalse(payload.containsKey("model_contract"));
        assertFalse(payload.containsKey("common_flows"));
        assertFalse(payload.containsKey("security_hints"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildResourceAnnotationsPayload() {
        MCPResourceDescriptor emptyAnnotationsResource = createResourceDescriptor("shardingsphere://empty", MCPResourceAnnotations.EMPTY);
        MCPResourceAnnotations priorityZeroAnnotations = new MCPResourceAnnotations(List.of("assistant"), 0D, null);
        MCPResourceDescriptor priorityZeroResource = createResourceDescriptor("shardingsphere://priority-zero", priorityZeroAnnotations);
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(createCatalog(List.of(emptyAnnotationsResource, priorityZeroResource), List.of()), List.of(), List.of(), List.of());
        List<Map<String, Object>> actualResources = (List<Map<String, Object>>) payload.get("resources");
        assertFalse(actualResources.get(0).containsKey("annotations"));
        assertThat((Map<String, Object>) actualResources.get(1).get("annotations"), is(Map.of("audience", List.of("assistant"), "priority", 0D)));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildResourceMetaPayload() {
        Map<String, Object> meta = Map.of(MCPShardingSphereMetadataKeys.RESOURCE_KIND, "detail");
        MCPResourceDescriptor resource = createResourceDescriptor("shardingsphere://runtime", MCPResourceAnnotations.EMPTY, meta);
        MCPResourceDescriptor resourceTemplate = createResourceDescriptor("shardingsphere://databases/{database}", MCPResourceAnnotations.EMPTY, meta);
        MCPDescriptorCatalog catalog = createCatalog(List.of(resource), List.of(resourceTemplate), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(catalog, List.of(), List.of(), List.of());
        Map<String, Object> actualResource = ((List<Map<String, Object>>) payload.get("resources")).get(0);
        Map<String, Object> actualResourceTemplate = ((List<Map<String, Object>>) payload.get("resourceTemplates")).get(0);
        assertFalse(actualResource.containsKey("meta"));
        assertFalse(actualResourceTemplate.containsKey("meta"));
        assertThat((Map<String, Object>) actualResource.get("_meta"), is(meta));
        assertThat((Map<String, Object>) actualResourceTemplate.get("_meta"), is(meta));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildToolAnnotationsPayload() {
        MCPToolDescriptor toolDescriptor = createToolDescriptor(MCPToolAnnotations.builder()
                .title(null).readOnlyHint(false).destructiveHint(true).idempotentHint(false).openWorldHint(true).build());
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(createCatalog(List.of(), List.of(toolDescriptor)), List.of(), List.of(), List.of());
        List<Map<String, Object>> actualTools = (List<Map<String, Object>>) payload.get("tools");
        assertThat((Map<String, Object>) actualTools.get(0).get("annotations"), is(Map.of(
                "readOnlyHint", false,
                "destructiveHint", true,
                "idempotentHint", false,
                "openWorldHint", true)));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildPromptCompletionRequiredContextFromDescriptorMeta() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide a test prompt.", List.of(
                new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false),
                new MCPPromptArgumentDescriptor("schema", "Schema", "Schema.", false)), Map.of());
        MCPCompletionTargetDescriptor completionTarget = new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database", "schema"), 50,
                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("schema", List.of("database"))));
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(), List.of(), List.of(prompt), List.of(), List.of(completionTarget), List.of(), List.of());
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(catalog, List.of(), List.of(), List.of());
        Map<String, Object> actualPrompt = ((List<Map<String, Object>>) payload.get("prompts")).get(0);
        Map<String, Object> actualSchemaArgument = ((List<Map<String, Object>>) actualPrompt.get("arguments")).get(1);
        assertThat(((Map<String, Object>) actualSchemaArgument.get("completion")).get("required_context_arguments"), is(List.of("database")));
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors) {
        return createCatalog(resourceDescriptors, List.of(), List.of(), toolDescriptors, List.of(), List.of(), List.of(), List.of(), List.of());
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPResourceDescriptor> resourceTemplateDescriptors,
                                               final List<ShardingSphereMCPResourceMetadata> resourceMetadata, final List<MCPToolDescriptor> toolDescriptors,
                                               final List<MCPPromptDescriptor> promptDescriptors, final List<MCPPromptTemplateBinding> promptTemplateBindings,
                                               final List<MCPCompletionTargetDescriptor> completionTargetDescriptors,
                                               final List<MCPResourceNavigationDescriptor> resourceNavigationDescriptors, final List<MCPToolRuntimeDescriptor> toolRuntimeDescriptors) {
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(resourceDescriptors, resourceTemplateDescriptors, toolDescriptors, promptDescriptors),
                new MCPShardingSphereDescriptorCatalog(resourceMetadata, promptTemplateBindings, completionTargetDescriptors, resourceNavigationDescriptors, toolRuntimeDescriptors));
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final String uri, final MCPResourceAnnotations annotations) {
        return createResourceDescriptor(uri, annotations, Map.of());
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final String uri, final MCPResourceAnnotations annotations, final Map<String, Object> meta) {
        return new MCPResourceDescriptor(uri, "test-resource", "Test Resource", "Read a test resource.", "application/json", annotations, meta);
    }
    
    private MCPToolDescriptor createToolDescriptor(final MCPToolAnnotations annotations) {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", Map.of(), Map.of(), annotations, Map.of());
    }
    
}
