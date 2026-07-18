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

class MCPGuidancePayloadBuilderTest {
    
    @Test
    void assertBuild() {
        Map<String, Object> actual = MCPGuidancePayloadBuilder.build();
        assertThat(actual.get("response_mode"), is("guidance"));
        assertTrue(actual.containsKey("discovery"));
        assertTrue(actual.containsKey("model_contract"));
        assertTrue(actual.containsKey("security_hints"));
        assertFalse(actual.containsKey("model_first_summary"));
        assertFalse(actual.containsKey("surface_summary"));
        assertFalse(actual.containsKey("field_naming_contract"));
    }
    
    @Test
    void assertCreateDiscovery() {
        Map<String, Object> actual = MCPGuidancePayloadBuilder.createDiscovery();
        assertThat(castToMap(actual.get("official_discovery_methods")), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("guidance_resource_role"),
                is("shardingsphere://guidance complements MCP list methods with ShardingSphere domain guidance, workflow guidance, and side-effect notes."));
    }
    
    @Test
    void assertCreateModelContract() {
        Map<String, Object> actual = MCPGuidancePayloadBuilder.createModelContract();
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(String.valueOf(actual.get("preflight_rule")).contains("database_gateway_validate_runtime_database"));
        Map<?, ?> actualSqlToolSelection = castToMap(actual.get("sql_tool_selection"));
        assertThat(actualSqlToolSelection.keySet().stream().toList(), is(List.of("read_only", "explain", "side_effecting")));
        assertThat(actual.get("side_effect_rule"), is("Preview before side effects and continue only when the requested side effect is still intended."));
        assertThat(actual.get("completion_rule"),
                is("Use completion/complete for one uncertain argument at a time; when completion reports missing context, follow meta.next_actions before guessing."));
        assertThat(actual.get("resource_template_rule"),
                is("Use resources/templates/list to discover URI variables, then read the nearest concrete resource before filling dependent completion context."));
        assertThat(actual.get("detail_resource_rule"), is("Use resource descriptors, outputSchema, and returned payload keys before assuming detail fields."));
        assertThat(actual.get("recovery_rule"), is("When a call fails, follow top-level next_actions before inventing a new call."));
        assertThat(actual.get("payload_field_rule"), is("ShardingSphere-owned structured payload fields use snake_case."));
        assertFalse(actual.containsKey("official_discovery_methods"));
    }
    
    @Test
    void assertCreateNextActionContract() {
        List<Map<String, Object>> actual = MCPGuidancePayloadBuilder.createNextActionContract();
        assertThat(actual.size(), is(5));
        Map<?, ?> actualToolCall = findByType(actual, "tool_call");
        assertTrue(((Collection<?>) actualToolCall.get("required_fields")).contains("tool_name"));
        Map<?, ?> actualCompletion = findByType(actual, "completion");
        assertTrue(((Collection<?>) actualCompletion.get("required_fields")).contains("ref"));
        assertTrue(((Collection<?>) actualCompletion.get("required_fields")).contains("argument"));
        assertTrue(((Collection<?>) actualCompletion.get("optional_fields")).contains("resume_ref"));
        assertTrue(((Collection<?>) actualCompletion.get("optional_fields")).contains("resume_arguments"));
    }
    
    @Test
    void assertCreateCommonFlows() {
        List<Map<String, Object>> actual = MCPGuidancePayloadBuilder.createCommonFlows();
        Map<?, ?> actualInspectMetadata = findByKey(actual, "flow_id", "inspect_metadata");
        assertTrue(((Collection<?>) actualInspectMetadata.get("steps")).contains("resources/list"));
        Map<?, ?> actualValidateRuntimeDatabase = findByKey(actual, "flow_id", "validate_runtime_database");
        assertTrue(((Collection<?>) actualValidateRuntimeDatabase.get("steps")).contains("call_tool database_gateway_validate_runtime_database"));
        Map<?, ?> actualExplainQuery = findByKey(actual, "flow_id", "explain_query");
        assertTrue(((Collection<?>) actualExplainQuery.get("steps")).contains("call_tool database_gateway_execute_explain_query"));
        Map<?, ?> actualCompleteArgument = findByKey(actual, "flow_id", "complete_uncertain_argument");
        assertTrue(((Collection<?>) actualCompleteArgument.get("steps")).contains("call completion/complete for one argument"));
        Map<?, ?> actualSideEffectingSql = findByKey(actual, "flow_id", "side_effecting_sql");
        assertTrue(((Collection<?>) actualSideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=preview"));
        Map<?, ?> actualWorkflow = findByKey(actual, "flow_id", "workflow_plan_apply_validate");
        assertTrue(((Collection<?>) actualWorkflow.get("steps")).contains(
                "call_tool database_gateway_apply_workflow execution_mode=review-then-execute approved_steps=<preview_artifacts.approval_step>"));
        Map<?, ?> actualRecovery = findByKey(actual, "flow_id", "recover_from_error");
        assertThat(((Collection<?>) actualRecovery.get("steps")).iterator().next(), is("follow top-level next_actions"));
    }
    
    @Test
    void assertCreateSecurityHints() {
        Map<String, Object> actual = MCPGuidancePayloadBuilder.createSecurityHints();
        assertTrue(String.valueOf(actual.get("http_transport")).contains("unauthenticated by default"));
        assertTrue(String.valueOf(actual.get("origin_header")).contains("loopback origins"));
        Map<?, ?> actualClientSafetyPolicy = castToMap(actual.get("client_safety_policy"));
        assertThat(actualClientSafetyPolicy.get("identity_scope"), is("mcp_session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("transport_scope")).contains("trusted session attribution"));
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
