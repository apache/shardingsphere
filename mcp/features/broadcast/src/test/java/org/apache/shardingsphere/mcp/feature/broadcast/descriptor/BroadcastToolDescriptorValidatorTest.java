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

package org.apache.shardingsphere.mcp.feature.broadcast.descriptor;

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPPromptTemplateLoader;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        assertTrue(new BroadcastToolDescriptorValidator().supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME)));
    }
    
    @Test
    void assertPromptUsesGuidanceName() {
        MCPPromptDescriptor actual = findPrompt(BroadcastFeatureDefinition.PLAN_PROMPT_NAME);
        assertThat((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS),
                is(List.of(BroadcastFeatureDefinition.PLAN_TOOL_NAME, "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertFalse(MCPDescriptorCatalogIndex.getPromptDescriptors().stream().anyMatch(each -> BroadcastFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemaIsBroadcastSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey(BroadcastFeatureDefinition.TABLES_FIELD));
        assertFalse(properties.containsKey("column"));
        assertFalse(properties.containsKey("algorithm_type"));
        assertFalse(properties.containsKey("primary_algorithm_properties"));
    }
    
    @Test
    void assertDescriptorIsRuleDistSQLOnly() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        String descriptorText = descriptor.getDescription() + descriptor.getInputSchema() + descriptor.getOutputSchema() + descriptor.getMeta();
        assertFalse(descriptorText.contains("ddl_artifacts"));
        assertFalse(descriptorText.contains("index_plan"));
        assertFalse(descriptorText.contains("manual_artifact_package"));
        assertFalse(descriptorText.contains("storage unit"));
        assertTrue(descriptorText.contains("rule_distsql"));
    }
    
    @Test
    void assertDescriptorExposesSingleTablePresenceResource() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        assertTrue(String.valueOf(descriptor.getMeta()).contains("shardingsphere://features/broadcast/databases/{database}/tables/{table}/rule"));
        assertTrue(MCPDescriptorCatalogIndex.getResourceDescriptors().stream()
                .anyMatch(each -> "shardingsphere://features/broadcast/databases/{database}/tables/{table}/rule".equals(each.getUriTemplate())));
    }
    
    @Test
    void assertToolDeclaresRequiredMetadata() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/workflow-kind"));
        assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/related-resource-uris"));
        assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/follow-up-tools"));
        assertTrue(MCPDescriptorCatalogIndex.findToolRuntimeDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME)
                .filter(optional -> "plan".equals(optional.getWorkflowRole())).isPresent());
    }
    
    @Test
    void assertExposeCompletionTargets() {
        MCPCompletionTargetDescriptor actual = findCompletionTarget("prompt", BroadcastFeatureDefinition.PLAN_PROMPT_NAME);
        assertThat(actual.getArguments(), is(List.of("database")));
        assertThat(actual.getMaxValues(), is(50));
    }
    
    @Test
    void assertPromptGuardsNonDistSQLOperations() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-broadcast-rule.md");
        assertTrue(prompt.contains("Do not generate physical table statements"));
        assertTrue(prompt.contains("storage unit mutation operations"));
    }
    
    @Test
    void assertPromptRendersPlanningContext() throws IOException {
        String actual = MCPPromptTemplateLoader.render(readResource("META-INF/shardingsphere-mcp/prompts/plan-broadcast-rule.md"), Map.of(
                "database", "logic_db", "table", "t_order", "tables", "t_order,t_order_item", "operation_type", "create", "plan_id", "plan-1"));
        assertTrue(actual.contains("- database: logic_db"));
        assertTrue(actual.contains("- table: t_order"));
        assertTrue(actual.contains("- tables: t_order,t_order_item"));
        assertTrue(actual.contains("- operation_type: create"));
        assertTrue(actual.contains("- plan_id: plan-1"));
        assertFalse(actual.contains("{{"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredOutputFields")
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingTemplateOutputField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove(fieldName);
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new BroadcastToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_broadcast_rule` outputSchema must declare `" + fieldName + "`."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredMetadataFields")
    void assertValidateRejectsMissingMetadataField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(BroadcastFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> meta = new LinkedHashMap<>(descriptor.getMeta());
        meta.remove(fieldName);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new BroadcastToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), descriptor.getOutputSchema(), descriptor.getAnnotations(), meta)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_broadcast_rule` metadata must declare `" + fieldName + "`."));
    }
    
    private static Stream<String> requiredOutputFields() {
        return Stream.of(
                "response_mode", "plan_id", "workflow_kind", "status", "missing_required_inputs", "clarification_questions", "elicitation_support", "fallback_reason",
                "issues", "global_steps", "current_step", "algorithm_recommendations", "property_requirements", "validation_strategy", "delivery_mode", "execution_mode",
                "intent_inference", "argument_provenance", "review_focus", "proxy_topology_hint", "distsql_artifacts", "resources_to_read", "next_actions");
    }
    
    private static Stream<String> requiredMetadataFields() {
        return Stream.of("org.apache.shardingsphere/workflow-kind", "org.apache.shardingsphere/artifact-categories", "org.apache.shardingsphere/side-effect-scope",
                "org.apache.shardingsphere/related-resource-uris", "org.apache.shardingsphere/follow-up-tools");
    }
    
    private MCPPromptDescriptor findPrompt(final String promptName) {
        return MCPDescriptorCatalogIndex.getPromptDescriptors().stream().filter(each -> promptName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private MCPCompletionTargetDescriptor findCompletionTarget(final String referenceType, final String reference) {
        return MCPDescriptorCatalogIndex.getCompletionTargetDescriptors().stream()
                .filter(each -> referenceType.equals(each.getReferenceType()) && reference.equals(each.getReference()))
                .findFirst().orElseThrow();
    }
    
    private String readResource(final String resourceName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
