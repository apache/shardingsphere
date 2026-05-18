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

package org.apache.shardingsphere.mcp.feature.encrypt.descriptor;

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        assertTrue(new EncryptToolDescriptorValidator().supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(EncryptFeatureDefinition.PLAN_TOOL_NAME)));
    }
    
    @Test
    void assertPromptUsesGuidanceName() {
        MCPPromptDescriptor actual = findPrompt(EncryptFeatureDefinition.PLAN_PROMPT_NAME);
        assertThat((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RELATED_TOOLS),
                is(List.of(EncryptFeatureDefinition.PLAN_TOOL_NAME, "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertFalse(MCPDescriptorCatalogIndex.getPromptDescriptors().stream().anyMatch(each -> EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())));
    }
    
    @Test
    void assertCompletionTargetUsesPromptName() {
        assertTrue(MCPDescriptorCatalogIndex.getCompletionTargetDescriptors().stream().anyMatch(this::isEncryptPlanningPromptCompletionTarget));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingOutputField() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(EncryptFeatureDefinition.PLAN_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("resources_to_read");
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new EncryptToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_encrypt_rule` outputSchema must declare `resources_to_read`."));
    }
    
    private MCPPromptDescriptor findPrompt(final String promptName) {
        return MCPDescriptorCatalogIndex.getPromptDescriptors().stream().filter(each -> promptName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private boolean isEncryptPlanningPromptCompletionTarget(final MCPCompletionTargetDescriptor descriptor) {
        return "prompt".equals(descriptor.getReferenceType()) && EncryptFeatureDefinition.PLAN_PROMPT_NAME.equals(descriptor.getReference());
    }
}
