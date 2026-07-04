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

package org.apache.shardingsphere.mcp.feature.sharding.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        ShardingToolDescriptorValidator validator = new ShardingToolDescriptorValidator();
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME)));
    }
    
    @Test
    void assertExposeCompletionTargets() {
        MCPCompletionTargetDescriptor algorithmCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_TABLE_RULE_PROMPT_NAME);
        assertThat(algorithmCompletionTarget.getArguments(), is(List.of("database", "algorithm_type")));
        assertThat(algorithmCompletionTarget.getMaxValues(), is(50));
        MCPCompletionTargetDescriptor tableReferenceCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_PROMPT_NAME);
        assertThat(tableReferenceCompletionTarget.getArguments(), is(List.of("database")));
        MCPCompletionTargetDescriptor defaultStrategyCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_PROMPT_NAME);
        assertThat(defaultStrategyCompletionTarget.getArguments(), is(List.of("database", "algorithm_type")));
        MCPCompletionTargetDescriptor keyGeneratorCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_KEY_GENERATOR_PROMPT_NAME);
        assertThat(keyGeneratorCompletionTarget.getArguments(), is(List.of("database", "key_generator_type")));
        assertThat(keyGeneratorCompletionTarget.getMaxValues(), is(50));
        MCPCompletionTargetDescriptor keyGenerateStrategyCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_PROMPT_NAME);
        assertThat(keyGenerateStrategyCompletionTarget.getArguments(), is(List.of("database", "key_generator_type")));
        MCPCompletionTargetDescriptor cleanupCompletionTarget = findCompletionTarget("prompt", ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_PROMPT_NAME);
        assertThat(cleanupCompletionTarget.getArguments(), is(List.of("database")));
        assertFalse(hasCompletionTarget("resource", ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI));
        assertFalse(hasCompletionTarget("resource", ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemaIsShardingSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey("data_nodes"));
        assertTrue(properties.containsKey("storage_units"));
        assertTrue(properties.containsKey("sharding_columns"));
        assertTrue(properties.containsKey("algorithm_properties"));
        assertTrue(properties.containsKey("key_generator_properties"));
        assertTrue(properties.containsKey("auditors"));
        assertFalse(properties.containsKey("reference_tables"));
        assertFalse(properties.containsKey("component_type"));
        assertFalse(properties.containsKey("default_strategy_type"));
        assertFalse(properties.containsKey("ddl_artifacts"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemasAreToolSpecific() {
        Map<String, Object> referenceProperties = (Map<String, Object>) MCPDescriptorCatalogIndex.getRequiredToolDescriptor(
                ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME).getInputSchema().get("properties");
        assertTrue(referenceProperties.containsKey("reference_tables"));
        assertFalse(referenceProperties.containsKey("data_nodes"));
        Map<String, Object> cleanupProperties = (Map<String, Object>) MCPDescriptorCatalogIndex.getRequiredToolDescriptor(
                ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME).getInputSchema().get("properties");
        assertTrue(cleanupProperties.containsKey("component_type"));
        assertTrue(cleanupProperties.containsKey("component_name"));
        assertFalse(cleanupProperties.containsKey("data_nodes"));
        assertFalse(cleanupProperties.containsKey("reference_tables"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertKeyGeneratorInputSchemasUseKeyGeneratorTypeOnly() {
        for (String each : List.of(ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME, ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME)) {
            Map<String, Object> properties = (Map<String, Object>) MCPDescriptorCatalogIndex.getRequiredToolDescriptor(each).getInputSchema().get("properties");
            assertTrue(properties.containsKey("key_generator_type"));
            assertFalse(properties.containsKey("algorithm_type"));
        }
    }
    
    @Test
    void assertPromptGuardsPhysicalOperations() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-sharding-table-rule.md");
        assertTrue(prompt.contains("Do not create physical databases"));
        assertTrue(prompt.contains("storage units"));
        assertTrue(prompt.contains("data probes"));
    }
    
    @Test
    void assertRuntimeVisibleAlgorithmResourcesDeclareMetadata() {
        assertTrue(MCPDescriptorCatalogIndex.getResourceDescriptors().stream()
                .anyMatch(each -> ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI.equals(each.getUriTemplate())
                        && "ShardingSphere-Proxy".equals(each.getMeta().get("org.apache.shardingsphere/runtime-visibility"))));
        assertTrue(MCPDescriptorCatalogIndex.getResourceDescriptors().stream()
                .anyMatch(each -> ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI.equals(each.getUriTemplate())
                        && "ShardingSphere-Proxy".equals(each.getMeta().get("org.apache.shardingsphere/runtime-visibility"))));
    }
    
    @Test
    void assertPromptsExposeRelatedResources() {
        assertTrue(MCPDescriptorCatalogIndex.getPromptDescriptors().stream()
                .anyMatch(each -> ShardingFeatureDefinition.PLAN_TABLE_RULE_PROMPT_NAME.equals(each.getName())
                        && String.valueOf(each.getMeta()).contains("shardingsphere://features/sharding/databases/{database}/table-rules")));
    }
    
    @Test
    void assertToolsDeclareRequiredMetadata() {
        for (String each : List.of(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME, ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME,
                ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME, ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME,
                ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME, ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME)) {
            MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(each);
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/workflow-kind"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/related-resource-uris"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/follow-up-tools"));
            assertTrue(MCPDescriptorCatalogIndex.findToolRuntimeDescriptor(each).filter(optional -> "plan".equals(optional.getWorkflowRole())).isPresent());
        }
    }
    
    @Test
    void assertPlanToolsNavigateToWorkflowAndApply() {
        for (String each : List.of(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME, ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME,
                ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME, ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME,
                ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME, ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME)) {
            assertPlanToolNavigation(each);
        }
    }
    
    @Test
    void assertAlgorithmResourceNavigationCarriesAlgorithmTypes() {
        assertThat(findNavigation(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI,
                ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME).getCarriedArguments(), is(List.of("algorithm_type")));
        assertThat(findNavigation(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI,
                ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME).getCarriedArguments(), is(List.of("algorithm_type")));
        assertThat(findNavigation(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI,
                ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME).getCarriedArguments(), is(List.of("key_generator_type")));
        assertThat(findNavigation(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI,
                ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME).getCarriedArguments(), is(List.of("key_generator_type")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredOutputFields")
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingTemplateOutputField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove(fieldName);
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShardingToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_sharding_table_rule` outputSchema must declare `" + fieldName + "`."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredMetadataFields")
    void assertValidateRejectsMissingMetadataField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME);
        Map<String, Object> meta = new LinkedHashMap<>(descriptor.getMeta());
        meta.remove(fieldName);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShardingToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), descriptor.getOutputSchema(), descriptor.getAnnotations(), meta)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_sharding_table_rule` metadata must declare `" + fieldName + "`."));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPToolDescriptorValidator.class).stream().anyMatch(each -> ShardingToolDescriptorValidator.class.equals(each.type())));
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
    
    private String readResource(final String resourceName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
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
}
