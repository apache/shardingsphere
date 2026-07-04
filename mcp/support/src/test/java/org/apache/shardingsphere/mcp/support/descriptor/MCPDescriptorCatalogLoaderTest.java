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
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorCatalogLoaderTest {
    
    @Test
    void assertLoadValidatesDescriptorQuality() {
        MCPDescriptorCatalog actual = MCPDescriptorCatalogLoader.load();
        Set<String> actualToolNames = actual.getProtocolDescriptors().getToolDescriptors().stream().map(MCPToolDescriptor::getName).collect(Collectors.toSet());
        assertToolNames(actualToolNames);
        assertOutputProperties(actual, "database_gateway_apply_workflow", Set.of(
                "response_mode", "plan_id", "execution_mode", "next_actions", "manual_artifact_package", "manual_artifact_summary", "manual_follow_up", "argument_provenance",
                "review_summary", "review_focus", "category", "message", "secret_reference_summary"));
        assertOutputProperties(actual, "database_gateway_validate_workflow", Set.of("response_mode", "plan_id", "status", "recovery_guidance", "next_actions", "sections", "mismatches"));
        assertPublicToolAnnotations(actual);
        assertPlanningToolAnnotations(actual);
        assertResourceDescriptor(actual);
        assertNoResponseFormatOptions(actual);
        assertNoToolExecutionPayload(actual);
    }
    
    @Test
    void assertLoadValidatesEnumFields() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        MCPToolDescriptor actual = findTool(catalog, "database_gateway_apply_workflow");
        assertThat(findInputProperty(actual, "execution_mode").get("enum"), is(List.of("preview", "review-then-execute", "manual-only")));
    }
    
    @Test
    void assertValidateRejectsUndeclaredPromptCompletionArgument() {
        MCPDescriptorCatalog actual = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(createResourceTemplateDescriptor()), List.of(), List.of(createPromptDescriptor())),
                new MCPShardingSphereDescriptorCatalog(List.of(createShardingSphereResourceMetadata()),
                        List.of(new MCPPromptTemplateBinding("inspect_metadata", "META-INF/shardingsphere-mcp/prompts/inspect-metadata.md")),
                        List.of(new MCPCompletionTargetDescriptor("prompt", "inspect_metadata", List.of("table"), 50, Map.of())), List.of(), List.of()));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogValidator.validate(actual));
        assertThat(exception.getMessage(), is("Completion target `prompt:inspect_metadata` argument `table` is not declared by prompt `inspect_metadata`."));
    }
    
    private void assertOutputProperties(final MCPDescriptorCatalog catalog, final String toolName, final Set<String> expectedProperties) {
        Map<?, ?> actualProperties = (Map<?, ?>) findTool(catalog, toolName).getOutputSchema().get("properties");
        for (String each : expectedProperties) {
            assertTrue(actualProperties.containsKey(each));
        }
    }
    
    private void assertToolNames(final Set<String> actualToolNames) {
        assertTrue(actualToolNames.contains("database_gateway_apply_workflow"));
        assertTrue(actualToolNames.contains("database_gateway_validate_workflow"));
        assertTrue(actualToolNames.stream().allMatch(each -> each.startsWith("database_gateway_")));
    }
    
    private void assertPublicToolAnnotations(final MCPDescriptorCatalog catalog) {
        for (MCPToolDescriptor each : catalog.getProtocolDescriptors().getToolDescriptors()) {
            assertNotNull(each.getAnnotations());
        }
    }
    
    private void assertPlanningToolAnnotations(final MCPDescriptorCatalog catalog) {
        for (String each : List.of("database_gateway_plan_encrypt_rule")) {
            MCPToolDescriptor actual = findTool(catalog, each);
            assertFalse(actual.getAnnotations().isReadOnlyHint());
            assertFalse(actual.getAnnotations().isDestructiveHint());
            assertFalse(actual.getAnnotations().isIdempotentHint());
            assertTrue(actual.getAnnotations().isOpenWorldHint());
        }
    }
    
    private MCPToolDescriptor findTool(final MCPDescriptorCatalog catalog, final String toolName) {
        return catalog.getProtocolDescriptors().getToolDescriptors().stream().filter(each -> toolName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private MCPResourceDescriptor findResource(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getProtocolDescriptors().getAllResourceDescriptors().stream().filter(each -> uriTemplate.equals(each.getUriOrTemplate())).findFirst().orElseThrow();
    }
    
    private MCPResourceDescriptor createResourceTemplateDescriptor() {
        return new MCPResourceDescriptor("shardingsphere://databases/{database}", "logical-database-detail", "Logical Database Detail",
                "Read one logical database detail.", "application/json", MCPResourceAnnotations.EMPTY, Collections.emptyMap());
    }
    
    private ShardingSphereMCPResourceMetadata createShardingSphereResourceMetadata() {
        return new ShardingSphereMCPResourceMetadata("shardingsphere://databases/{database}",
                List.of(new MCPUriVariableDescriptor("database", "Database", "Logical database name.", true, "database")),
                "detail", "database", "", List.of(), List.of(), List.of());
    }
    
    private MCPPromptDescriptor createPromptDescriptor() {
        return new MCPPromptDescriptor("inspect_metadata", "Inspect Metadata", "Inspect metadata.",
                List.of(
                        new MCPPromptArgumentDescriptor("database", "Database", "Database.", false),
                        new MCPPromptArgumentDescriptor("schema", "Schema", "Schema.", false),
                        new MCPPromptArgumentDescriptor("query", "Query", "Query.", false)),
                Map.of(MCPShardingSphereMetadataKeys.STOP_CONDITIONS, List.of("Stop after metadata is resolved."),
                        MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS, List.of("Ask when metadata is ambiguous.")));
    }
    
    private ShardingSphereMCPResourceMetadata findShardingSphereResourceMetadata(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getShardingSphereDescriptors().getResourceMetadata().stream().filter(each -> uriTemplate.equals(each.getUriOrTemplate())).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findInputProperty(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        Object properties = toolDescriptor.getInputSchema().get("properties");
        return (Map<?, ?>) ((Map<?, ?>) properties).get(fieldName);
    }
    
    private void assertResourceDescriptor(final MCPDescriptorCatalog catalog) {
        ShardingSphereMCPResourceMetadata metadata = findShardingSphereResourceMetadata(catalog, "shardingsphere://workflows/{plan_id}");
        assertThat(metadata.getResourceKind(), is("detail"));
        assertThat(metadata.getObjectScope(), is("workflow-plan"));
        assertThat(metadata.getRelatedTools(), is(List.of("database_gateway_validate_workflow", "database_gateway_apply_workflow")));
        MCPResourceDescriptor actual = findResource(catalog, "shardingsphere://workflows/{plan_id}");
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("detail"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS), is(List.of("database_gateway_validate_workflow", "database_gateway_apply_workflow")));
        assertThat(findShardingSphereResourceMetadata(catalog, "shardingsphere://workflow/test-resource").getResourceKind(), is("detail"));
    }
    
    private void assertNoResponseFormatOptions(final MCPDescriptorCatalog catalog) {
        for (MCPToolDescriptor each : catalog.getProtocolDescriptors().getToolDescriptors()) {
            assertFalse(containsResponseFormatOption(each.getInputSchema()));
            assertFalse(containsResponseFormatOption(each.getOutputSchema()));
        }
    }
    
    private void assertNoToolExecutionPayload(final MCPDescriptorCatalog catalog) {
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(catalog, List.of(), List.of(), List.of());
        for (Object each : (List<?>) payload.get("tools")) {
            assertFalse(((Map<?, ?>) each).containsKey("execution"));
        }
    }
    
    private boolean containsResponseFormatOption(final Object value) {
        if (value instanceof Map) {
            return containsResponseFormatOptionMap((Map<?, ?>) value);
        }
        if (value instanceof Iterable) {
            for (Object each : (Iterable<?>) value) {
                if (containsResponseFormatOption(each)) {
                    return true;
                }
            }
        }
        return value instanceof String && String.valueOf(value).toLowerCase().contains("response_format");
    }
    
    private boolean containsResponseFormatOptionMap(final Map<?, ?> value) {
        if (value.containsKey("response_format") || value.containsKey("responseFormat")) {
            return true;
        }
        for (Object each : value.values()) {
            if (containsResponseFormatOption(each)) {
                return true;
            }
        }
        return false;
    }
}
