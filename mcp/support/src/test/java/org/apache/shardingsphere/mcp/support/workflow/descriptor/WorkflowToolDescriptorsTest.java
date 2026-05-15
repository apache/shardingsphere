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

package org.apache.shardingsphere.mcp.support.workflow.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowToolDescriptorsTest {
    
    @Test
    void assertCreatePlanningLoadsDescriptor() {
        MCPToolDescriptor actual = WorkflowToolDescriptors.createPlanning("database_gateway_plan_encrypt_rule");
        assertThat(actual.getName(), is("database_gateway_plan_encrypt_rule"));
        assertThat(actual.getTitle(), is("Plan Encrypt Rule"));
        assertThat(getInputFieldNames(actual), is(List.of(
                "plan_id", "database", "schema", "table", "column", "operation_type",
                "natural_language_intent", "structured_intent_evidence", "delivery_mode",
                "execution_mode", "algorithm_type", "user_overrides")));
        assertThat(findInputProperty(actual, "operation_type").get("enum"), is(List.of("create", "alter", "drop")));
        assertThat(findInputProperty(actual, "delivery_mode").get("enum"), is(List.of("all-at-once", "step-by-step")));
        assertThat(findInputProperty(actual, "execution_mode").get("enum"), is(List.of("review-then-execute", "manual-only")));
        assertTrue(findInputProperty(actual, "structured_intent_evidence").containsKey("properties"));
    }
    
    @Test
    void assertCreateExecutionBuildsExpectedFields() {
        MCPToolDescriptor actual = WorkflowToolDescriptors.createExecution();
        assertThat(actual.getName(), is("database_gateway_apply_workflow"));
        assertThat(actual.getTitle(), is("Apply Workflow"));
        assertThat(getInputFieldNames(actual), is(List.of("plan_id", "execution_mode", "approved_steps", "approved_by_user")));
        assertTrue(getRequiredInputNames(actual).contains("execution_mode"));
        assertThat(findInputProperty(actual, "execution_mode").get("enum"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertThat(findInputProperty(actual, "approved_steps"), is(Map.of(
                "type", "array",
                "description", "Optional execution filter, not an approval token. Omit to apply every artifact after user approval, or pass only visible preview_artifacts.approval_step values.",
                "items", Map.of("type", "string", "description", "Allowed workflow artifact step: ddl, index_ddl, or rule_distsql.", "enum", List.of("ddl", "index_ddl", "rule_distsql")))));
        assertThat(findInputProperty(actual, "approved_by_user").get("type"), is("boolean"));
        assertTrue(actual.getAnnotations().isDestructiveHint());
    }
    
    @Test
    void assertCreateValidationBuildsExpectedFields() {
        MCPToolDescriptor actual = WorkflowToolDescriptors.createValidation();
        assertThat(actual.getName(), is("database_gateway_validate_workflow"));
        assertThat(actual.getTitle(), is("Validate Workflow"));
        assertThat(getInputFieldNames(actual), is(List.of("plan_id")));
        assertTrue(getRequiredInputNames(actual).contains("plan_id"));
        assertTrue(actual.getAnnotations().isReadOnlyHint());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertWorkflowValidatorRejectsMissingOutputField() {
        MCPToolDescriptor descriptor = WorkflowToolDescriptors.createExecution();
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("manual_artifact_summary");
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new WorkflowToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_apply_workflow` outputSchema must declare `manual_artifact_summary`."));
    }
    
    private List<String> getInputFieldNames(final MCPToolDescriptor descriptor) {
        return getInputProperties(descriptor).keySet().stream().map(Object::toString).toList();
    }
    
    private List<?> getRequiredInputNames(final MCPToolDescriptor descriptor) {
        return (List<?>) descriptor.getInputSchema().get("required");
    }
    
    private Map<?, ?> findInputProperty(final MCPToolDescriptor descriptor, final String fieldName) {
        return (Map<?, ?>) getInputProperties(descriptor).get(fieldName);
    }
    
    private Map<?, ?> getInputProperties(final MCPToolDescriptor descriptor) {
        return (Map<?, ?>) descriptor.getInputSchema().get("properties");
    }
}
