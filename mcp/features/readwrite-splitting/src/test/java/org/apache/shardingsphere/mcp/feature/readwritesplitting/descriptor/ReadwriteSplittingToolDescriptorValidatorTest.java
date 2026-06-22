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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.descriptor;

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPPromptTemplateLoader;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationDescriptor;
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

class ReadwriteSplittingToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        ReadwriteSplittingToolDescriptorValidator validator = new ReadwriteSplittingToolDescriptorValidator();
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME)));
    }
    
    @Test
    void assertExposeCompletionTargets() {
        MCPCompletionTargetDescriptor promptCompletionTarget = findCompletionTarget("prompt", ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME);
        assertThat(promptCompletionTarget.getArguments(), is(List.of(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD)));
        assertThat(promptCompletionTarget.getMaxValues(), is(50));
        assertFalse(hasCompletionTarget("resource", ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI));
    }
    
    @Test
    void assertPromptUsesGuidanceName() {
        MCPPromptDescriptor actual = findPrompt(ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME);
        assertThat((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS),
                is(List.of(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME, "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertFalse(MCPDescriptorCatalogIndex.getPromptDescriptors().stream().anyMatch(each -> ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME.equals(each.getName())));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemaIsReadwriteSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey(ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD));
        assertTrue(properties.containsKey(ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD));
        assertFalse(properties.containsKey("column"));
        assertFalse(properties.containsKey("primary_algorithm_properties"));
        assertFalse(properties.containsKey("user_overrides"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertStatusInputSchemaUsesTargetStatusOnly() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD));
        assertFalse(properties.containsKey("operation_type"));
        assertFalse(properties.containsKey("user_overrides"));
    }
    
    @Test
    void assertStatusOutputExampleUsesTargetStatus() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME);
        String outputSchema = String.valueOf(descriptor.getOutputSchema());
        assertTrue(outputSchema.contains("target_status=enable"));
        assertFalse(outputSchema.contains("operation_type=enable"));
    }
    
    @Test
    void assertDescriptorIsRuleDistSQLOnly() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        String descriptorText = descriptor.getDescription() + descriptor.getInputSchema() + descriptor.getOutputSchema() + descriptor.getMeta();
        assertFalse(descriptorText.contains("ddl_artifacts"));
        assertFalse(descriptorText.contains("index_plan"));
        assertFalse(descriptorText.contains("manual_artifact_package"));
        assertTrue(descriptorText.contains("rule_distsql"));
    }
    
    @Test
    void assertRuntimeVisibleAlgorithmResourceDeclaresMetadata() {
        assertTrue(MCPDescriptorCatalogIndex.getResourceDescriptors().stream()
                .anyMatch(each -> ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI.equals(each.getUriTemplate())
                        && "ShardingSphere-Proxy".equals(each.getMeta().get("org.apache.shardingsphere/runtime-visibility"))));
    }
    
    @Test
    void assertToolsDeclareRequiredMetadata() {
        for (String each : List.of(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME, ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME)) {
            MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(each);
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/workflow-kind"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/related-resource-uris"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/follow-up-tools"));
            assertFalse(String.valueOf(descriptor.getOutputSchema()).contains("manual_artifact_package"));
        }
    }
    
    @Test
    void assertPromptGuardsNonDistSQLOperations() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-readwrite-splitting-rule.md");
        assertTrue(prompt.contains("Do not create, alter, unregister, or repair storage units"));
        assertTrue(prompt.contains("physical metadata probes"));
    }
    
    @Test
    void assertPromptsRenderPlanningContext() throws IOException {
        String actualRulePrompt = MCPPromptTemplateLoader.render(readResource("META-INF/shardingsphere-mcp/prompts/plan-readwrite-splitting-rule.md"), Map.of(
                "database", "logic_db", "rule", "readwrite_ds", "operation_type", "create", "write_storage_unit", "write_ds", "read_storage_units", "read_ds_0",
                "transactional_read_query_strategy", "DYNAMIC", "load_balancer_type", "ROUND_ROBIN", "plan_id", "plan-1"));
        assertTrue(actualRulePrompt.contains("- database: logic_db"));
        assertTrue(actualRulePrompt.contains("- plan_id: plan-1"));
        assertFalse(actualRulePrompt.contains("{{"));
        String actualStatusPrompt = MCPPromptTemplateLoader.render(readResource("META-INF/shardingsphere-mcp/prompts/plan-readwrite-splitting-status.md"), Map.of(
                "database", "logic_db", "rule", "readwrite_ds", "storage_unit", "read_ds_0", "target_status", "enable", "plan_id", "plan-1"));
        assertTrue(actualStatusPrompt.contains("- storage_unit: read_ds_0"));
        assertTrue(actualStatusPrompt.contains("- plan_id: plan-1"));
        assertFalse(actualStatusPrompt.contains("{{"));
    }
    
    @Test
    void assertPlanToolsNavigateToWorkflowAndApply() {
        assertPlanToolNavigation(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        assertPlanToolNavigation(ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME);
    }
    
    @Test
    void assertAlgorithmResourceNavigationCarriesLoadBalancerType() {
        MCPResourceNavigationDescriptor actual = findNavigation(ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        assertThat(actual.getCarriedArguments(), is(List.of(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredOutputFields")
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingTemplateOutputField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove(fieldName);
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ReadwriteSplittingToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_readwrite_splitting_rule` outputSchema must declare `" + fieldName + "`."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredMetadataFields")
    void assertValidateRejectsMissingMetadataField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> meta = new LinkedHashMap<>(descriptor.getMeta());
        meta.remove(fieldName);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ReadwriteSplittingToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), descriptor.getOutputSchema(), descriptor.getAnnotations(), meta)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_readwrite_splitting_rule` metadata must declare `" + fieldName + "`."));
    }
    
    private static Stream<String> requiredOutputFields() {
        return Stream.of(
                "response_mode", "plan_id", "workflow_kind", "status", "missing_required_inputs", "clarification_questions", "elicitation_support", "fallback_reason",
                "issues", "global_steps", "current_step", "algorithm_recommendations", "property_requirements", "validation_strategy", "delivery_mode", "execution_mode",
                "intent_inference", "argument_provenance", "review_focus", "proxy_topology_hint", "distsql_artifacts", "resources_to_read", "next_actions");
    }
    
    private static Stream<String> requiredMetadataFields() {
        return Stream.of("org.apache.shardingsphere/workflow-kind", "org.apache.shardingsphere/related-resource-uris", "org.apache.shardingsphere/follow-up-tools");
    }
    
    private MCPPromptDescriptor findPrompt(final String promptName) {
        return MCPDescriptorCatalogIndex.getPromptDescriptors().stream().filter(each -> promptName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private MCPCompletionTargetDescriptor findCompletionTarget(final String referenceType, final String reference) {
        return MCPDescriptorCatalogIndex.getCompletionTargetDescriptors().stream()
                .filter(each -> referenceType.equals(each.getReferenceType()) && reference.equals(each.getReference())).findFirst().orElseThrow();
    }
    
    private boolean hasCompletionTarget(final String referenceType, final String reference) {
        return MCPDescriptorCatalogIndex.getCompletionTargetDescriptors().stream()
                .anyMatch(each -> referenceType.equals(each.getReferenceType()) && reference.equals(each.getReference()));
    }
    
    private void assertPlanToolNavigation(final String toolName) {
        assertNavigation(toolName, "shardingsphere://workflows/{plan_id}");
        assertNavigation(toolName, "database_gateway_apply_workflow");
    }
    
    private void assertNavigation(final String from, final String to) {
        MCPResourceNavigationDescriptor actual = findNavigation(from, to);
        assertThat(actual.getRequiredArguments(), is(List.of("plan_id")));
        assertThat(actual.getCarriedArguments(), is(List.of("plan_id")));
    }
    
    private MCPResourceNavigationDescriptor findNavigation(final String from, final String to) {
        return MCPDescriptorCatalogIndex.getResourceNavigationDescriptors(from).stream()
                .filter(each -> to.equals(each.getTo())).findFirst().orElseThrow();
    }
    
    private String readResource(final String resourceName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
