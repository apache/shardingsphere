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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCapabilitiesHandlerTest {
    
    @Test
    void assertHandleReturnsCoreModelSurfaceContract() {
        Map<String, Object> actual = createCapabilitiesPayload();
        assertBaselineTopLevelKeys(actual);
        assertFalse(((Set<?>) actual.get("supportedStatementClasses")).isEmpty());
        assertFalse(((List<?>) actual.get("completionTargets")).isEmpty());
        assertFalse(((List<?>) actual.get("resourceNavigation")).isEmpty());
        Map<?, ?> navigation = findByKey((List<?>) actual.get("resourceNavigation"), "to", "shardingsphere://guidance");
        assertThat(navigation.get("from_type"), is("resource"));
        assertThat(navigation.get("to_type"), is("resource"));
        assertFalse(actual.containsKey("resources"));
        assertFalse(actual.containsKey("tools"));
        assertFalse(actual.containsKey("protocolAvailability"));
        assertFalse(actual.containsKey("fingerprints"));
    }
    
    @Test
    void assertHandleReturnsGuidanceContract() {
        Map<String, Object> actual = createGuidancePayload();
        assertGuidanceTopLevelKeys(actual);
        assertDiscovery(actual);
        assertModelContract(actual);
        assertNextActionContract(actual);
        assertCommonFlows(actual);
        assertSecurityHints(actual);
    }
    
    private Map<String, Object> createCapabilitiesPayload() {
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(), new MCPSessionIdentity("session-1", "", "", Map.of()));
        return new ServerCapabilitiesHandler().handle(requestContext, new MCPResourceURIVariables(Map.of())).toPayload();
    }
    
    private Map<String, Object> createGuidancePayload() {
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(), new MCPSessionIdentity("session-1", "", "", Map.of()));
        return new ServerGuidanceHandler().handle(requestContext, new MCPResourceURIVariables(Map.of())).toPayload();
    }
    
    private void assertBaselineTopLevelKeys(final Map<String, Object> capabilities) {
        assertThat(capabilities.keySet(), is(Set.of("response_mode", "supportedStatementClasses", "completionTargets", "resourceNavigation")));
    }
    
    private void assertGuidanceTopLevelKeys(final Map<String, Object> guidance) {
        assertThat(guidance.keySet(), is(Set.of("response_mode", "discovery", "model_contract", "next_action_contract", "common_flows", "security_hints")));
        assertThat(guidance.get("response_mode"), is("guidance"));
    }
    
    private void assertDiscovery(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("discovery");
        assertThat(actual.get("official_discovery_methods"), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("guidance_resource_role"),
                is("shardingsphere://guidance complements MCP list methods with ShardingSphere domain guidance, workflow guidance, and side-effect notes."));
    }
    
    private void assertModelContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_contract");
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(String.valueOf(actual.get("preflight_rule")).contains("database_gateway_validate_runtime_database"));
        Map<?, ?> sqlToolSelection = (Map<?, ?>) actual.get("sql_tool_selection");
        assertTrue(sqlToolSelection.containsKey("read_only"));
        assertTrue(sqlToolSelection.containsKey("explain"));
        assertTrue(sqlToolSelection.containsKey("side_effecting"));
        assertTrue(actual.containsKey("workflow_session_rule"));
        assertTrue(actual.containsKey("next_action_rule"));
        assertThat(actual.get("recovery_rule"), is("When a call fails, follow top-level next_actions before inventing a new call."));
        assertThat(actual.get("payload_field_rule"), is("ShardingSphere-owned structured payload fields use snake_case."));
        assertFalse(actual.containsKey("official_discovery_methods"));
    }
    
    private void assertNextActionContract(final Map<String, Object> capabilities) {
        Map<?, ?> callTool = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "tool_call");
        assertThat(callTool.get("required_fields"), is(List.of("order", "type", "title", "tool_name", "arguments")));
        Map<?, ?> readResource = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "resource_read");
        assertThat(readResource.get("required_fields"), is(List.of("order", "type", "title", "resource_uri")));
        Map<?, ?> completion = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "completion");
        assertThat(completion.get("required_fields"), is(List.of("order", "type", "title", "ref", "argument")));
        Map<?, ?> askUser = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "ask_user");
        assertThat(askUser.get("required_fields"), is(List.of("order", "type", "title", "question")));
        Map<?, ?> stop = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "terminal");
        assertThat(stop.get("required_fields"), is(List.of("order", "type", "title")));
    }
    
    private void assertCommonFlows(final Map<String, Object> capabilities) {
        Map<?, ?> inspectMetadata = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "inspect_metadata");
        assertTrue(((List<?>) inspectMetadata.get("steps")).containsAll(List.of("resources/list", "resources/templates/list", "call_tool database_gateway_search_metadata")));
        Map<?, ?> validateRuntimeDatabase = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "validate_runtime_database");
        assertTrue(((List<?>) validateRuntimeDatabase.get("steps")).contains("call_tool database_gateway_validate_runtime_database"));
        Map<?, ?> explainQuery = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "explain_query");
        assertTrue(((List<?>) explainQuery.get("steps")).contains("call_tool database_gateway_execute_explain_query"));
        Map<?, ?> sideEffectingSql = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "side_effecting_sql");
        assertTrue(((List<?>) sideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=preview"));
        assertTrue(((List<?>) sideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=execute"));
        Map<?, ?> workflow = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "workflow_plan_apply_validate");
        assertTrue(((List<?>) workflow.get("steps")).contains(
                "call_tool database_gateway_apply_workflow execution_mode=review-then-execute approved_steps=<preview_artifacts.approval_step>"));
        assertThat(workflow.get("stop_condition"), is("Reuse the same current-session plan_id and stop after validation succeeds."));
    }
    
    private void assertSecurityHints(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("security_hints");
        assertTrue(actual.containsKey("http_transport"));
        assertTrue(actual.containsKey("origin_header"));
        assertTrue(actual.containsKey("stdio_stdout"));
        Map<?, ?> actualClientSafetyPolicy = (Map<?, ?>) actual.get("client_safety_policy");
        assertThat(actualClientSafetyPolicy.get("identity_scope"), is("mcp_session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("transport_scope")).contains("trusted session attribution"));
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualClientSafetyPolicy.get("runtime_protection")).get("tool_call_limit")).get("scope"), is("session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("abuse_guard")).contains("counted before dispatch"));
    }
    
    private Map<?, ?> findByKey(final List<?> values, final String key, final String expectedValue) {
        return values.stream().map(each -> (Map<?, ?>) each).filter(each -> expectedValue.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private Map<String, Object> createOfficialDiscoveryMethods() {
        return Map.of("tools", "tools/list", "resources", "resources/list", "resource_templates", "resources/templates/list", "prompts", "prompts/list");
    }
}
