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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        assertThat(rulePromptCompletionTarget.getArguments(), is(List.of(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
        MCPCompletionTargetDescriptor defaultPromptCompletionTarget = findCompletionTarget("prompt", ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME);
        assertThat(defaultPromptCompletionTarget.getArguments(), is(List.of(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD)));
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
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingOutputField() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("resources_to_read");
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShadowToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_shadow_rule` outputSchema must declare `resources_to_read`."));
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
}
