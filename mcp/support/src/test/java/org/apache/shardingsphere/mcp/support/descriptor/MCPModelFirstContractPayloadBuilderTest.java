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

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPModelFirstContractPayloadBuilderTest {
    
    private final MCPModelFirstContractPayloadBuilder builder = new MCPModelFirstContractPayloadBuilder(MCPDescriptorCatalogLoader.load());
    
    @Test
    void assertCreateModelFirstSummary() {
        Map<String, Object> actual = builder.createModelFirstSummary();
        assertThat(castToMap(actual.get("official_discovery_methods")), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("catalog_resource_role"),
                is("shardingsphere://capabilities complements MCP list methods with ShardingSphere domain capability guidance, workflow guidance, and side-effect notes."));
        assertThat(actual.get("optional_catalog_resource"), is("shardingsphere://capabilities"));
        assertFalse(actual.containsKey("safe_first_resource"));
        assertThat(((Map<?, ?>) actual.get("preflight_rule")).get("tool"), is("database_gateway_validate_runtime_database"));
        assertThat(castToMap(castToMap(actual.get("sql_tool_selection")).get("side_effecting")).get("execute_requires"), is("execution_mode=execute"));
        Map<?, ?> actualWorkflowRule = castToMap(actual.get("workflow_rule"));
        assertThat(actualWorkflowRule.get("planning_tools"), is(List.of("database_gateway_plan_encrypt_rule")));
        assertThat(castToMap(actualWorkflowRule.get("preview_tool")).get("execution_mode"), is("preview"));
        assertThat(castToMap(actualWorkflowRule.get("execute_tool")).get("execute_requires"),
                is("execution_mode=review-then-execute and explicit approved_steps copied from preview_artifacts.approval_step"));
        assertThat(actualWorkflowRule.get("validate_tool"), is("database_gateway_validate_workflow"));
    }
    
    @Test
    void assertCreateModelContract() {
        Map<String, Object> actual = builder.createModelContract();
        assertThat(actual.get("public_surface_source"), is("MCP list methods expose the protocol surface: tools/list, resources/list, resources/templates/list, prompts/list."));
        assertThat(castToMap(actual.get("official_discovery_methods")), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("optional_catalog_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(String.valueOf(actual.get("preflight_rule")).contains("database_gateway_validate_runtime_database"));
        assertThat(actual.get("side_effect_rule"), is("Preview before side effects and continue only when the requested side effect is still intended."));
    }
    
    @Test
    void assertCreateNextActionContract() {
        List<Map<String, Object>> actual = builder.createNextActionContract();
        assertThat(actual.size(), is(5));
        Map<?, ?> actualToolCall = findByType(actual, "tool_call");
        assertTrue(((Collection<?>) actualToolCall.get("required_fields")).contains("tool_name"));
        assertFalse(((Collection<?>) actualToolCall.get("required_fields")).contains("requires_user_approval"));
        assertTrue(((Collection<?>) findByType(actual, "completion").get("optional_fields")).contains("resume_arguments"));
    }
    
    @Test
    void assertCreateCommonFlows() {
        List<Map<String, Object>> actual = builder.createCommonFlows();
        Map<?, ?> actualInspectMetadata = findByKey(actual, "flow_id", "inspect_metadata");
        assertTrue(((Collection<?>) actualInspectMetadata.get("steps")).contains("resources/list"));
        Map<?, ?> actualSideEffectingSql = findByKey(actual, "flow_id", "side_effecting_sql");
        Map<?, ?> actualValidateRuntimeDatabase = findByKey(actual, "flow_id", "validate_runtime_database");
        assertTrue(((Collection<?>) actualValidateRuntimeDatabase.get("steps")).contains("call_tool database_gateway_validate_runtime_database"));
        assertThat(actualValidateRuntimeDatabase.get("referenced_tools"), is(List.of("database_gateway_validate_runtime_database")));
        assertThat(actualSideEffectingSql.get("referenced_tools"), is(List.of("database_gateway_execute_update")));
        assertTrue(((Collection<?>) actualSideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=preview"));
        Map<?, ?> actualWorkflow = findByKey(actual, "flow_id", "workflow_plan_apply_validate");
        assertTrue(((Collection<?>) actualWorkflow.get("steps")).contains(
                "call_tool database_gateway_apply_workflow execution_mode=review-then-execute approved_steps=<preview_artifacts.approval_step>"));
    }
    
    @Test
    void assertCreateSecurityHints() {
        Map<String, Object> actual = builder.createSecurityHints();
        assertTrue(String.valueOf(actual.get("http_transport")).contains("unauthenticated by default"));
        assertTrue(String.valueOf(actual.get("origin_header")).contains("loopback origins"));
        Map<?, ?> actualClientSafetyPolicy = castToMap(actual.get("client_safety_policy"));
        assertThat(actualClientSafetyPolicy.get("identity_scope"), is("mcp_session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("transport_scope")).contains("trusted session attribution"));
        assertThat(castToMap(actualClientSafetyPolicy.get("tool_call_limit")).get("scope"), is("session"));
        assertThat(castToMap(castToMap(actualClientSafetyPolicy.get("runtime_protection")).get("tool_call_limit")).get("scope"), is("session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("external_model_boundary")).contains("never calls external model providers"));
    }
    
    private Map<?, ?> findByType(final List<Map<String, Object>> values, final String type) {
        return findByKey(values, "type", type);
    }
    
    private Map<?, ?> findByKey(final List<Map<String, Object>> values, final String key, final String value) {
        return values.stream().filter(each -> value.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> castToMap(final Object value) {
        return (Map<?, ?>) value;
    }
    
    private Map<String, Object> createOfficialDiscoveryMethods() {
        return Map.of("tools", "tools/list", "resources", "resources/list", "resource_templates", "resources/templates/list", "prompts", "prompts/list");
    }
}
