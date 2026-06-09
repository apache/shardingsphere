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

package org.apache.shardingsphere.mcp.feature.mask.descriptor;

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
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

class MaskToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        assertTrue(new MaskToolDescriptorValidator().supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(MaskFeatureDefinition.PLAN_TOOL_NAME)));
    }
    
    @Test
    void assertPromptUsesGuidanceName() {
        MCPPromptDescriptor actual = findPrompt(MaskFeatureDefinition.PLAN_PROMPT_NAME);
        assertThat((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS),
                is(List.of(MaskFeatureDefinition.PLAN_TOOL_NAME, "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertFalse(MCPDescriptorCatalogIndex.getPromptDescriptors().stream().anyMatch(each -> MaskFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())));
    }
    
    @Test
    void assertExposeCompletionTargets() {
        MCPCompletionTargetDescriptor promptCompletionTarget = findCompletionTarget("prompt", MaskFeatureDefinition.PLAN_PROMPT_NAME);
        assertThat(promptCompletionTarget.getArguments(), is(List.of("algorithm_type")));
        assertFalse(hasCompletionTarget("resource", MaskFeatureDefinition.ALGORITHMS_RESOURCE_URI));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertOutputSchemaDeclaresElicitationFallback() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(MaskFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getOutputSchema().get("properties");
        assertTrue(properties.containsKey("elicitation_support"));
        assertTrue(properties.containsKey("fallback_reason"));
        Map<String, Object> supportProperties = (Map<String, Object>) ((Map<String, Object>) properties.get("elicitation_support")).get("properties");
        assertTrue(supportProperties.containsKey("form_mode"));
        assertTrue(supportProperties.containsKey("url_mode"));
        assertTrue(supportProperties.containsKey("selected_interaction"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertStructuredIntentEvidenceIsMaskSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(MaskFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        Map<String, Object> structuredIntentEvidence = (Map<String, Object>) properties.get("structured_intent_evidence");
        Map<String, Object> actualProperties = (Map<String, Object>) structuredIntentEvidence.get("properties");
        assertTrue(actualProperties.containsKey("field_semantics"));
        assertFalse(actualProperties.containsKey("requires_decrypt"));
        assertFalse(actualProperties.containsKey("requires_equality_filter"));
        assertFalse(actualProperties.containsKey("requires_like_query"));
    }
    
    @Test
    void assertDescriptorIsRuleDistSQLOnly() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(MaskFeatureDefinition.PLAN_TOOL_NAME);
        String descriptorText = descriptor.getDescription() + descriptor.getInputSchema() + descriptor.getOutputSchema() + descriptor.getMeta();
        assertFalse(descriptorText.contains("logical metadata"));
        assertFalse(descriptorText.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns"));
    }
    
    @Test
    void assertPromptIsRuleDistSQLOnly() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-mask-rule.md");
        assertFalse(prompt.contains("column metadata"));
        assertFalse(prompt.contains("logical metadata"));
        assertFalse(prompt.contains("safe completion"));
        assertFalse(prompt.contains("completion/complete"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingOutputField() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(MaskFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("resources_to_read");
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new MaskToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_mask_rule` outputSchema must declare `resources_to_read`."));
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
    
    private String readResource(final String resourceName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
