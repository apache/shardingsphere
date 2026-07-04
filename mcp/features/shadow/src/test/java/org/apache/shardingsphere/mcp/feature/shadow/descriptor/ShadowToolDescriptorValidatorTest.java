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

package org.apache.shardingsphere.mcp.feature.shadow.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationDescriptor;
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

class ShadowToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        ShadowToolDescriptorValidator validator = new ShadowToolDescriptorValidator();
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME)));
    }
    
    @Test
    void assertExposeCompletionTargets() {
        MCPCompletionTargetDescriptor rulePromptCompletionTarget = findCompletionTarget("prompt", ShadowFeatureDefinition.PLAN_RULE_PROMPT_NAME);
        assertThat(rulePromptCompletionTarget.getArguments(), is(List.of("database", ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
        MCPCompletionTargetDescriptor defaultPromptCompletionTarget = findCompletionTarget("prompt", ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME);
        assertThat(defaultPromptCompletionTarget.getArguments(), is(List.of("database", ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
        MCPCompletionTargetDescriptor cleanupPromptCompletionTarget = findCompletionTarget("prompt", ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_PROMPT_NAME);
        assertThat(cleanupPromptCompletionTarget.getArguments(), is(List.of("database")));
        assertFalse(hasCompletionTarget("resource", ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemaIsShadowSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey(ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD));
        assertTrue(properties.containsKey(ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD));
        assertTrue(properties.containsKey(ShadowFeatureDefinition.ALGORITHM_PROPERTIES_FIELD));
        assertFalse(properties.containsKey("column"));
        assertFalse(properties.containsKey("ddl_artifacts"));
    }
    
    @Test
    void assertPromptGuardsPhysicalOperations() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-shadow-rule.md");
        assertTrue(prompt.contains("Do not create storage units"));
        assertTrue(prompt.contains("physical databases"));
        assertTrue(prompt.contains("data probes"));
    }
    
    @Test
    void assertRuntimeVisibleAlgorithmResourceDeclaresMetadata() {
        assertTrue(MCPDescriptorCatalogIndex.getResourceDescriptors().stream()
                .anyMatch(each -> ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI.equals(each.getUriTemplate())
                        && "ShardingSphere-Proxy".equals(each.getMeta().get("org.apache.shardingsphere/runtime-visibility"))));
    }
    
    @Test
    void assertToolsDeclareRequiredMetadata() {
        for (String each : List.of(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME, ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME,
                ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME)) {
            MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(each);
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/workflow-kind"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/related-resource-uris"));
            assertTrue(descriptor.getMeta().containsKey("org.apache.shardingsphere/follow-up-tools"));
            assertTrue(MCPDescriptorCatalogIndex.findToolRuntimeDescriptor(each).filter(optional -> "plan".equals(optional.getWorkflowRole())).isPresent());
        }
    }
    
    @Test
    void assertPlanToolsNavigateToWorkflowAndApply() {
        for (String each : List.of(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME, ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME,
                ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME)) {
            assertPlanToolNavigation(each);
        }
    }
    
    @Test
    void assertAlgorithmResourceNavigationCarriesAlgorithmType() {
        assertThat(findNavigation(ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI,
                ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME).getCarriedArguments(), is(List.of(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
        assertThat(findNavigation(ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI,
                ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME).getCarriedArguments(), is(List.of(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredOutputFields")
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingTemplateOutputField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove(fieldName);
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShadowToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_shadow_rule` outputSchema must declare `" + fieldName + "`."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredMetadataFields")
    void assertValidateRejectsMissingMetadataField(final String fieldName) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> meta = new LinkedHashMap<>(descriptor.getMeta());
        meta.remove(fieldName);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShadowToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), descriptor.getOutputSchema(), descriptor.getAnnotations(), meta)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_shadow_rule` metadata must declare `" + fieldName + "`."));
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
