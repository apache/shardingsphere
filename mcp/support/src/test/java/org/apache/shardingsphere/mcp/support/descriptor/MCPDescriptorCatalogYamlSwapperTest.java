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

import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptBindingDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceAnnotations;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPResourceNavigationDescriptor;
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
    
    @Test
    void assertSwapRejectsInvalidResourceAudience() {
        YamlMCPResourceAnnotations yamlAnnotations = new YamlMCPResourceAnnotations();
        yamlAnnotations.setAudience(List.of("model"));
        YamlMCPDescriptorCatalog yamlCatalog = createYamlCatalogWithResourceAnnotations(yamlAnnotations);
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resources[0].annotations.audience[0].<list element>` must be an MCP role.");
    }
    
    @Test
    void assertSwapRejectsInvalidResourcePriorityRange() {
        YamlMCPResourceAnnotations yamlAnnotations = new YamlMCPResourceAnnotations();
        yamlAnnotations.setAudience(List.of("assistant"));
        yamlAnnotations.setPriority(1.1D);
        YamlMCPDescriptorCatalog yamlCatalog = createYamlCatalogWithResourceAnnotations(yamlAnnotations);
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resources[0].annotations.priority` must be between 0.0 and 1.0.");
    }
    
    @Test
    void assertSwapRejectsInvalidResourcePriorityValue() {
        YamlMCPResourceAnnotations yamlAnnotations = new YamlMCPResourceAnnotations();
        yamlAnnotations.setAudience(List.of("assistant"));
        yamlAnnotations.setPriority(Double.NaN);
        YamlMCPDescriptorCatalog yamlCatalog = createYamlCatalogWithResourceAnnotations(yamlAnnotations);
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resources[0].annotations.priority` must be finite.");
    }
    
    @Test
    void assertSwapRejectsInvalidResourceLastModified() {
        YamlMCPResourceAnnotations yamlAnnotations = new YamlMCPResourceAnnotations();
        yamlAnnotations.setAudience(List.of("assistant"));
        yamlAnnotations.setLastModified("2026-05-13T00:00:00");
        YamlMCPDescriptorCatalog yamlCatalog = createYamlCatalogWithResourceAnnotations(yamlAnnotations);
        assertYamlValidationError(yamlCatalog,
                "MCP descriptor catalog property `resources[0].annotations.lastModified` must be an ISO 8601 timestamp with an explicit offset or UTC marker.");
    }
    
    @Test
    void assertSwapRejectsContradictoryToolAnnotations() {
        YamlMCPToolAnnotations yamlAnnotations = createYamlToolAnnotations();
        yamlAnnotations.setReadOnlyHint(true);
        yamlAnnotations.setDestructiveHint(true);
        YamlMCPToolDescriptor yamlTool = createYamlToolDescriptor();
        yamlTool.setAnnotations(yamlAnnotations);
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setTools(List.of(yamlTool));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `tools[0].annotations` cannot be both read-only and destructive.");
    }
    
    @Test
    void assertSwapRejectsPlaceholderDescription() {
        YamlMCPResourceDescriptor yamlResource = createYamlResourceDescriptor();
        yamlResource.setDescription("ShardingSphere MCP resource: server capability catalog.");
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setResources(List.of(yamlResource));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resources[0].description` must not be a placeholder description.");
    }
    
    @Test
    void assertSwapRejectsLargeCompletionMaxValues() {
        YamlMCPCompletionTargetDescriptor yamlCompletionTarget = createYamlCompletionTargetDescriptor();
        yamlCompletionTarget.setMaxValues(101);
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setCompletionTargets(List.of(yamlCompletionTarget));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `completionTargets[0].maxValues` must not exceed 100.");
    }
    
    @Test
    void assertSwapRejectsDuplicateCompletionArguments() {
        YamlMCPCompletionTargetDescriptor yamlCompletionTarget = createYamlCompletionTargetDescriptor();
        yamlCompletionTarget.setArguments(List.of("database", "database"));
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setCompletionTargets(List.of(yamlCompletionTarget));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `completionTargets[0].arguments` must not contain duplicate values.");
    }
    
    @Test
    void assertSwapRejectsDuplicateResourceNavigationArguments() {
        YamlMCPResourceNavigationDescriptor yamlResourceNavigation = new YamlMCPResourceNavigationDescriptor();
        yamlResourceNavigation.setFrom("database_gateway_search_metadata");
        yamlResourceNavigation.setTo("shardingsphere://capabilities");
        yamlResourceNavigation.setDescription("Navigate from metadata search to capabilities.");
        yamlResourceNavigation.setRequiredArguments(List.of("database", "database"));
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setResourceNavigation(List.of(yamlResourceNavigation));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resourceNavigation[0].requiredArguments` must not contain duplicate values.");
    }
    
    @Test
    void assertSwapRejectsUnnamespacedMetadataKey() {
        YamlMCPResourceDescriptor yamlResource = createYamlResourceDescriptor();
        yamlResource.setMeta(Map.of("feature", "metadata"));
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setResources(List.of(yamlResource));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `resources[0].meta<K>[feature].<map key>` must use the ShardingSphere MCP namespace.");
    }
    
    @Test
    void assertSwapRejectsDuplicatePromptArguments() {
        YamlMCPPromptDescriptor yamlPrompt = createYamlPromptDescriptor();
        yamlPrompt.setArguments(List.of(createYamlPromptArgument("database"), createYamlPromptArgument("database")));
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setPrompts(List.of(yamlPrompt));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `prompts[0].arguments` must not contain duplicate argument names.");
    }
    
    @Test
    void assertSwapRejectsMissingPromptGuidanceMeta() {
        YamlMCPPromptDescriptor yamlPrompt = createYamlPromptDescriptor();
        yamlPrompt.setMeta(Map.of());
        YamlMCPDescriptorCatalog yamlCatalog = new YamlMCPDescriptorCatalog();
        yamlCatalog.setPrompts(List.of(yamlPrompt));
        assertYamlValidationError(yamlCatalog, "MCP descriptor catalog property `prompts[0].meta` must declare org.apache.shardingsphere/stop-conditions.");
    }
    
    private void assertYamlValidationError(final YamlMCPDescriptorCatalog yamlCatalog, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPDescriptorCatalogYamlSwapper.swap(List.of(yamlCatalog)));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private YamlMCPDescriptorCatalog createYamlCatalogWithResourceAnnotations(final YamlMCPResourceAnnotations yamlAnnotations) {
        YamlMCPResourceDescriptor yamlResource = createYamlResourceDescriptor();
        yamlResource.setAnnotations(yamlAnnotations);
        YamlMCPDescriptorCatalog result = new YamlMCPDescriptorCatalog();
        result.setResources(List.of(yamlResource));
        return result;
    }
    
    private YamlMCPResourceDescriptor createYamlResourceDescriptor() {
        YamlMCPResourceDescriptor result = new YamlMCPResourceDescriptor();
        result.setUri("shardingsphere://capabilities");
        result.setName("server-capability-catalog");
        result.setTitle("Server Capability Catalog");
        result.setDescription("Read the model-facing capability catalog.");
        result.setMimeType("application/json");
        return result;
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
    
    private YamlMCPToolAnnotations createYamlToolAnnotations() {
        YamlMCPToolAnnotations result = new YamlMCPToolAnnotations();
        result.setReadOnlyHint(true);
        result.setDestructiveHint(false);
        result.setIdempotentHint(true);
        result.setOpenWorldHint(true);
        return result;
    }
    
    private YamlMCPCompletionTargetDescriptor createYamlCompletionTargetDescriptor() {
        YamlMCPCompletionTargetDescriptor result = new YamlMCPCompletionTargetDescriptor();
        result.setReferenceType("resource");
        result.setReference("shardingsphere://databases/{database}");
        result.setArguments(List.of("database"));
        result.setMaxValues(100);
        return result;
    }
    
    private YamlMCPPromptDescriptor createYamlPromptDescriptor() {
        YamlMCPPromptDescriptor result = new YamlMCPPromptDescriptor();
        result.setName("inspect_metadata");
        result.setTitle("Inspect Metadata");
        result.setDescription("Inspect metadata.");
        result.setArguments(List.of(createYamlPromptArgument("database")));
        result.setBinding(createYamlPromptBinding());
        result.setMeta(Map.of(MCPShardingSphereMetadataKeys.STOP_CONDITIONS, List.of("Stop after metadata is resolved."),
                MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS, List.of("Ask when metadata is ambiguous.")));
        return result;
    }
    
    private YamlMCPPromptArgumentDescriptor createYamlPromptArgument(final String name) {
        YamlMCPPromptArgumentDescriptor result = new YamlMCPPromptArgumentDescriptor();
        result.setName(name);
        result.setTitle("Database");
        result.setDescription("Logical database name.");
        return result;
    }
    
    private YamlMCPPromptBindingDescriptor createYamlPromptBinding() {
        YamlMCPPromptBindingDescriptor result = new YamlMCPPromptBindingDescriptor();
        result.setTemplateResource("META-INF/shardingsphere-mcp/prompts/inspect-metadata.md");
        return result;
    }
    
    private Map<String, Object> createSchema() {
        Map<String, Object> result = new LinkedHashMap<>(1, 1F);
        result.put("type", "object");
        return result;
    }
}
