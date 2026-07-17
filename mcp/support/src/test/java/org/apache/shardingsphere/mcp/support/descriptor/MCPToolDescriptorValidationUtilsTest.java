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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPToolDescriptorValidationUtilsTest {
    
    private static final Collection<String> WORKFLOW_PLAN_OUTPUT_FIELDS = List.of(
            "response_mode", MCPPayloadFieldNames.SUMMARY, WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs", "clarification_questions",
            "elicitation_support", "fallback_reason", "issues", "global_steps", "current_step", "algorithm_recommendations", "property_requirements",
            "validation_strategy", "delivery_mode", "execution_mode", "intent_inference", "argument_provenance", "review_focus", "proxy_topology_hint",
            "distsql_artifacts", MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.NEXT_ACTIONS);
    
    private static final Collection<String> REQUIRED_WORKFLOW_PLAN_SCHEMA_FIELDS = List.of(
            "response_mode", WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs",
            MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.NEXT_ACTIONS);
    
    private static final Collection<String> WORKFLOW_PLAN_META_FIELDS = List.of(
            "org.apache.shardingsphere/workflow-kind", "org.apache.shardingsphere/artifact-categories", "org.apache.shardingsphere/side-effect-scope",
            "org.apache.shardingsphere/related-resource-uris", "org.apache.shardingsphere/follow-up-tools");
    
    @Test
    void assertValidateRequiredWorkflowPlanOutputFields() {
        MCPToolDescriptor descriptor = createDescriptor(WORKFLOW_PLAN_OUTPUT_FIELDS, createWorkflowPlanMeta());
        assertDoesNotThrow(() -> MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(descriptor));
    }
    
    @Test
    void assertValidateRequiredWorkflowPlanOutputFieldsRejectsMissingAdditionalField() {
        MCPToolDescriptor descriptor = createDescriptor(WORKFLOW_PLAN_OUTPUT_FIELDS, createWorkflowPlanMeta());
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(descriptor, List.of("secret_reference_summary")));
        assertThat(actual.getMessage(), is("Tool `fixture_tool` outputSchema must declare `secret_reference_summary`."));
    }
    
    @Test
    void assertValidateRequiredWorkflowPlanOutputFieldsRejectsOptionalContractField() {
        MCPToolDescriptor descriptor = createDescriptor(WORKFLOW_PLAN_OUTPUT_FIELDS, REQUIRED_WORKFLOW_PLAN_SCHEMA_FIELDS.stream()
                .filter(each -> !MCPPayloadFieldNames.NEXT_ACTIONS.equals(each)).toList(), createWorkflowPlanMeta());
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(descriptor));
        assertThat(actual.getMessage(), is("Tool `fixture_tool` outputSchema must require `next_actions`."));
    }
    
    @Test
    void assertValidateRequiredOutputFieldsRejectsNonObjectField() {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("fixture_tool", "Fixture Tool", "Fixture tool.", Map.of(),
                Map.of("properties", Map.of("status", "ready")), createAnnotations(), Map.of());
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPToolDescriptorValidationUtils.validateRequiredOutputFields(descriptor, List.of("status")));
        assertThat(actual.getMessage(), is("Tool `fixture_tool` outputSchema property `status` must be an object."));
    }
    
    @Test
    void assertFindToolInputProperty() {
        MCPToolDescriptor descriptor = createDescriptor(
                Map.of("properties", Map.of("database", Map.of("type", "string"))), Map.of());
        Optional<Map<?, ?>> actual = MCPToolDescriptorValidationUtils.findToolInputProperty(descriptor, "database");
        assertTrue(actual.isPresent());
        assertThat(actual.get().get("type"), is("string"));
    }
    
    @Test
    void assertFindToolOutputProperty() {
        MCPToolDescriptor descriptor = createDescriptor(
                Map.of(), Map.of("properties", Map.of("response_mode", Map.of("enum", List.of("validation")))));
        Optional<Map<?, ?>> actual = MCPToolDescriptorValidationUtils.findToolOutputProperty(descriptor, "response_mode");
        assertTrue(actual.isPresent());
        assertThat(actual.get().get("enum"), is(List.of("validation")));
    }
    
    @Test
    void assertIsRequiredToolInput() {
        MCPToolDescriptor descriptor = createDescriptor(
                Map.of("required", List.of("database"), "properties", Map.of("database", Map.of("type", "string"))), Map.of());
        boolean actual = MCPToolDescriptorValidationUtils.isRequiredToolInput(descriptor, "database");
        assertTrue(actual);
    }
    
    @Test
    void assertValidateRequiredWorkflowPlanMetaFields() {
        MCPToolDescriptor descriptor = createDescriptor(WORKFLOW_PLAN_OUTPUT_FIELDS, createWorkflowPlanMeta());
        assertDoesNotThrow(() -> MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanMetaFields(descriptor));
    }
    
    @Test
    void assertValidateRequiredMetaFieldsRejectsMissingField() {
        MCPToolDescriptor descriptor = createDescriptor(WORKFLOW_PLAN_OUTPUT_FIELDS, Map.of("fixture/meta", "value"));
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPToolDescriptorValidationUtils.validateRequiredMetaFields(descriptor, List.of("fixture/missing")));
        assertThat(actual.getMessage(), is("Tool `fixture_tool` metadata must declare `fixture/missing`."));
    }
    
    @Test
    void assertCheckDescriptionRejectsBlankValue() {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorValidationUtils.checkDescription(" ", "Fixture description"));
        assertThat(actual.getMessage(), is("Fixture description is required."));
    }
    
    private MCPToolDescriptor createDescriptor(final Collection<String> outputFields, final Map<String, Object> meta) {
        return createDescriptor(outputFields, REQUIRED_WORKFLOW_PLAN_SCHEMA_FIELDS, meta);
    }
    
    private MCPToolDescriptor createDescriptor(final Collection<String> outputFields, final Collection<String> requiredFields, final Map<String, Object> meta) {
        return new MCPToolDescriptor("fixture_tool", "Fixture Tool", "Fixture tool.", Map.of(),
                Map.of("type", "object", "properties", createOutputProperties(outputFields), "required", requiredFields), createAnnotations(), meta);
    }
    
    private MCPToolDescriptor createDescriptor(final Map<String, Object> inputSchema, final Map<String, Object> outputSchema) {
        return new MCPToolDescriptor("fixture_tool", "Fixture Tool", "Fixture tool.", inputSchema, outputSchema, createAnnotations(), Map.of());
    }
    
    private MCPToolAnnotations createAnnotations() {
        return MCPToolAnnotations.builder().title("Fixture Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(false).build();
    }
    
    private Map<String, Object> createOutputProperties(final Collection<String> outputFields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String each : outputFields) {
            result.put(each, Map.of("type", "string", "description", each + " field."));
        }
        return result;
    }
    
    private Map<String, Object> createWorkflowPlanMeta() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String each : WORKFLOW_PLAN_META_FIELDS) {
            result.put(each, "value");
        }
        return result;
    }
}
