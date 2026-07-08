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

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;

import java.util.Collection;
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
        assertTrue(((Collection<?>) actual.get("supportedResources")).contains("shardingsphere://capabilities"));
        assertTrue(((Collection<?>) actual.get("supportedResources")).contains("shardingsphere://guidance"));
        assertTrue(
                ((Collection<?>) actual.get("supportedTools")).containsAll(List.of("database_gateway_search_metadata", "database_gateway_validate_runtime_database", "database_gateway_execute_query",
                        "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertThat(actual.get("guidanceResource"), is("shardingsphere://guidance"));
        assertFalse(((List<?>) actual.get("resources")).isEmpty());
        assertFalse(((List<?>) actual.get("resourceTemplates")).isEmpty());
        assertFalse(((List<?>) actual.get("tools")).isEmpty());
        assertFalse(((List<?>) actual.get("prompts")).isEmpty());
        assertFalse(((List<?>) actual.get("completionTargets")).isEmpty());
        assertFalse(((List<?>) actual.get("resourceNavigation")).isEmpty());
        assertFalse(actual.containsKey("fingerprints"));
        assertResourcePayloadContracts(actual);
        assertCoreToolSchemas(actual);
    }
    
    @Test
    void assertHandleReturnsGuidanceContract() {
        Map<String, Object> actual = createGuidancePayload();
        assertGuidanceTopLevelKeys(actual);
        assertModelFirstSummary(actual);
        assertModelContract(actual);
        assertSurfaceSummary(actual);
        assertFieldNamingContract(actual);
        assertNextActionContract(actual);
        assertCommonFlows(actual);
        assertSecurityHints(actual);
    }
    
    @Test
    void assertHandleReturnsImplementedProtocolAvailabilityOnly() {
        assertProtocolAvailability(createCapabilitiesPayload());
    }
    
    private Map<String, Object> createCapabilitiesPayload() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext())) {
            return new ServerCapabilitiesHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
        }
    }
    
    private Map<String, Object> createGuidancePayload() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext())) {
            return new ServerGuidanceHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
        }
    }
    
    private void assertBaselineTopLevelKeys(final Map<String, Object> capabilities) {
        assertThat(capabilities.keySet(), is(Set.of("response_mode", "supportedResources", "supportedTools", "supportedStatementClasses", "guidanceResource", "resources",
                "resourceTemplates", "tools", "prompts", "completionTargets", "resourceNavigation", "protocolAvailability")));
    }
    
    private void assertGuidanceTopLevelKeys(final Map<String, Object> guidance) {
        assertThat(guidance.keySet(), is(Set.of("response_mode", "guidance_resource", "model_first_summary", "model_contract", "surface_summary", "field_naming_contract",
                "next_action_contract", "common_flows", "security_hints")));
        assertThat(guidance.get("response_mode"), is("guidance"));
        assertThat(guidance.get("guidance_resource"), is("shardingsphere://guidance"));
    }
    
    private void assertProtocolAvailability(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("protocolAvailability");
        assertThat(actual, is(Map.of(
                "resources", true,
                "resourceTemplates", true,
                "tools", true,
                "toolAnnotations", true,
                "toolOutputSchemas", true,
                "prompts", true,
                "completions", true,
                "resourceNavigation", true)));
        for (String each : List.of("logging", "progress", "cancellation", "tasks", "sampling", "roots", "subscriptions", "listChanged")) {
            assertFalse(actual.containsKey(each), "Unexpected optional MCP capability: " + each);
        }
    }
    
    private void assertModelFirstSummary(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_first_summary");
        assertThat(actual.get("official_discovery_methods"), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("guidance_resource_role"),
                is("shardingsphere://guidance complements MCP list methods with ShardingSphere domain guidance, workflow guidance, and side-effect notes."));
        assertThat(actual.get("guidance_resource"), is("shardingsphere://guidance"));
        assertFalse(actual.containsKey("safe_first_resource"));
        Map<?, ?> firstRoute = findByKey((List<?>) actual.get("first_call_routes"), "intent", "inspect_metadata");
        assertThat(firstRoute.get("first_action"), is("read_resource shardingsphere://databases"));
        Map<?, ?> recoveryRoute = findByKey((List<?>) actual.get("first_call_routes"), "intent", "recover_error");
        assertThat(recoveryRoute.get("first_action"), is("follow top-level next_actions"));
        Map<?, ?> metadataRule = (Map<?, ?>) actual.get("metadata_rule");
        assertThat(metadataRule.get("first_resource"), is("shardingsphere://databases"));
        assertThat(metadataRule.get("search_tool"), is("database_gateway_search_metadata"));
        assertThat(((Map<?, ?>) actual.get("preflight_rule")).get("tool"), is("database_gateway_validate_runtime_database"));
        Map<?, ?> sqlToolSelection = (Map<?, ?>) actual.get("sql_tool_selection");
        assertThat(((Map<?, ?>) sqlToolSelection.get("read_only")).get("tool"), is("database_gateway_execute_query"));
        assertThat(((Map<?, ?>) sqlToolSelection.get("side_effecting")).get("first_mode"), is("preview"));
        assertTrue(String.valueOf(actual.get("side_effect_rule")).contains("requested side effect is still intended"));
        Map<?, ?> workflowRule = (Map<?, ?>) actual.get("workflow_rule");
        assertTrue(workflowRule.containsKey("planning_tools"));
        assertThat(((Map<?, ?>) workflowRule.get("preview_tool")).get("tool"), is("database_gateway_apply_workflow"));
        assertThat(((Map<?, ?>) workflowRule.get("execute_tool")).get("execute_requires"),
                is("execution_mode=review-then-execute and explicit approved_steps copied from preview_artifacts.approval_step"));
        assertThat(workflowRule.get("validate_tool"), is("database_gateway_validate_workflow"));
        assertTrue(String.valueOf(actual.get("completion_rule")).contains("before guessing identifiers"));
        assertTrue(String.valueOf(actual.get("recovery_rule")).contains("recovery.next_actions"));
    }
    
    private void assertModelContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_contract");
        assertThat(actual.get("public_surface_source"), is("MCP list methods expose the protocol surface: tools/list, resources/list, resources/templates/list, prompts/list."));
        assertThat(actual.get("official_discovery_methods"), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("guidance_resource"), is("shardingsphere://guidance"));
        assertFalse(actual.containsKey("safe_first_resource"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(String.valueOf(actual.get("preflight_rule")).contains("database_gateway_validate_runtime_database"));
        assertTrue(((Map<?, ?>) actual.get("sql_tool_selection")).containsKey("side_effecting"));
        assertTrue(actual.containsKey("workflow_session_rule"));
        assertTrue(actual.containsKey("next_action_rule"));
        assertTrue(actual.containsKey("recovery_rule"));
    }
    
    private void assertSurfaceSummary(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("surface_summary");
        assertThat(actual.get("official_discovery_methods"), is(createOfficialDiscoveryMethods()));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("guidance_resource"), is("shardingsphere://guidance"));
        assertThat(actual.get("metadata_search_tool"), is("database_gateway_search_metadata"));
        assertThat(actual.get("preflight_validation_tool"), is("database_gateway_validate_runtime_database"));
        assertThat(actual.get("side_effect_sql_tool"), is("database_gateway_execute_update"));
    }
    
    private void assertFieldNamingContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("field_naming_contract");
        assertThat(actual.get("official_discovery_methods"), is(List.of("tools/list", "resources/list", "resources/templates/list", "prompts/list")));
        assertThat(actual.get("argument_completion_method"), is("completion/complete"));
        assertThat(actual.get("catalog_fields"), is(List.of("supportedResources", "supportedTools", "supportedStatementClasses", "guidanceResource", "resourceTemplates",
                "completionTargets", "resourceNavigation", "protocolAvailability")));
        assertThat(actual.get("payload_fields"), is("ShardingSphere-owned structured payload fields use snake_case."));
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
        assertThat(((Map<?, ?>) actualClientSafetyPolicy.get("tool_call_limit")).get("scope"), is("session"));
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualClientSafetyPolicy.get("runtime_protection")).get("tool_call_limit")).get("scope"), is("session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("abuse_guard")).contains("counted before dispatch"));
    }
    
    private void assertResourcePayloadContracts(final Map<String, Object> capabilities) {
        Map<?, ?> capabilityCatalog = findResource(capabilities, "shardingsphere://capabilities");
        assertThat(getResourceMeta(capabilityCatalog).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("capability-catalog"));
        Map<?, ?> guidance = findResource(capabilities, "shardingsphere://guidance");
        assertThat(getResourceMeta(guidance).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("guidance"));
        Map<?, ?> runtimeStatus = findResource(capabilities, "shardingsphere://runtime");
        assertThat(getResourceMeta(runtimeStatus).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("detail"));
        Map<?, ?> databaseDetail = findResource(capabilities, "shardingsphere://databases/{database}");
        assertThat(getResourceMeta(databaseDetail).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("detail"));
        Map<?, ?> databaseCapabilities = findResource(capabilities, "shardingsphere://databases/{database}/capabilities");
        assertThat(getResourceMeta(databaseCapabilities).get(MCPShardingSphereMetadataKeys.RELATED_TOOLS), is(List.of("database_gateway_execute_query", "database_gateway_execute_update")));
        Map<?, ?> databases = findResource(capabilities, "shardingsphere://databases");
        assertThat(getResourceMeta(databases).get(MCPShardingSphereMetadataKeys.RELATED_TOOLS), is(List.of("database_gateway_search_metadata", "database_gateway_execute_query")));
        Map<?, ?> databaseVariable = findUriVariable(databaseDetail, "database");
        assertTrue((Boolean) databaseVariable.get("required"));
        Map<?, ?> navigation = findByKey((List<?>) capabilities.get("resourceNavigation"), "to", "shardingsphere://guidance");
        assertThat(navigation.get("from_type"), is("resource"));
        assertThat(navigation.get("to_type"), is("resource"));
    }
    
    private Map<?, ?> getResourceMeta(final Map<?, ?> resource) {
        assertFalse(resource.containsKey("meta"));
        return (Map<?, ?>) resource.get("_meta");
    }
    
    private Map<?, ?> findUriVariable(final Map<?, ?> resource, final String variableName) {
        return ((List<?>) getResourceMeta(resource).get(MCPShardingSphereMetadataKeys.URI_VARIABLES)).stream()
                .map(each -> (Map<?, ?>) each).filter(each -> variableName.equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private void assertCoreToolSchemas(final Map<String, Object> capabilities) {
        Map<?, ?> searchMetadataTool = findTool(capabilities, "database_gateway_search_metadata");
        Map<?, ?> searchMetadataOutputProperties = (Map<?, ?>) ((Map<?, ?>) searchMetadataTool.get("outputSchema")).get("properties");
        assertTrue(searchMetadataOutputProperties.containsKey("summary"));
        assertTrue(searchMetadataOutputProperties.containsKey("total_match_count"));
        assertTrue(searchMetadataOutputProperties.containsKey("returned_count"));
        assertTrue(searchMetadataOutputProperties.containsKey("truncated"));
        assertTrue(searchMetadataOutputProperties.containsKey("large_result_guidance"));
        assertThat(getInputFieldNames(searchMetadataTool), is(List.of("database", "schema", "query", "object_types")));
        Map<?, ?> objectTypesSchema = findInputSchema(searchMetadataTool, "object_types");
        assertTrue(((List<?>) ((Map<?, ?>) objectTypesSchema.get("items")).get("enum")).containsAll(List.of("database", "schema", "table", "view", "column", "index", "storage_unit", "sequence")));
        Map<?, ?> validateRuntimeDatabaseTool = findTool(capabilities, "database_gateway_validate_runtime_database");
        Map<?, ?> validateRuntimeDatabaseOutputProperties = (Map<?, ?>) ((Map<?, ?>) validateRuntimeDatabaseTool.get("outputSchema")).get("properties");
        assertThat(getInputFieldNames(validateRuntimeDatabaseTool), is(List.of("database")));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("summary"));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("status"));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("checks"));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("category"));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("recovery"));
        assertTrue(validateRuntimeDatabaseOutputProperties.containsKey("next_actions"));
        Map<?, ?> executeUpdateTool = findTool(capabilities, "database_gateway_execute_update");
        Map<?, ?> executeUpdateOutputProperties = (Map<?, ?>) ((Map<?, ?>) executeUpdateTool.get("outputSchema")).get("properties");
        assertTrue(executeUpdateOutputProperties.containsKey("response_mode"));
        assertTrue(executeUpdateOutputProperties.containsKey("summary"));
        assertTrue(((List<?>) ((Map<?, ?>) executeUpdateOutputProperties.get("result_kind")).get("enum")).containsAll(List.of("preview", "result_set", "update_count", "statement_ack")));
        assertTrue(executeUpdateOutputProperties.containsKey("preview_semantics"));
        assertTrue(executeUpdateOutputProperties.containsKey("review_summary"));
        assertFalse(executeUpdateOutputProperties.containsKey("approval_summary"));
        assertThat(executeUpdateTool.get("title"), is("Preview or Execute Side-Effecting SQL"));
        assertTrue(((String) executeUpdateTool.get("description")).contains("Preview is classification-only, not a database dry run."));
        assertThat(findInputSchema(executeUpdateTool, "execution_mode").get("description"),
                is("Required safety gate. Use preview to classify side effects without execution; preview is not a database dry run. Use execute only after reviewing the preview."));
        assertThat(((Map<?, ?>) executeUpdateOutputProperties.get("preview_semantics")).get("description"), is(
                "Preview meaning. classification_only means the server classified SQL and side-effect scope without parsing, rule validation, "
                        + "algorithm initialization, affected-row estimation, runtime execution, or database dry-run guarantees."));
        assertThat(getInputFieldNames(executeUpdateTool), is(List.of("database", "schema", "sql", "execution_mode", "max_rows", "timeout_ms")));
        Map<?, ?> applyWorkflowTool = findTool(capabilities, "database_gateway_apply_workflow");
        assertThat(getInputFieldNames(applyWorkflowTool), is(List.of("plan_id", "execution_mode", "approved_steps")));
        assertTrue(hasCompletionTarget(capabilities, "shardingsphere://workflows/{plan_id}", "plan_id"));
        Map<?, ?> inspectMetadataPrompt = findPrompt(capabilities, "inspect_metadata");
        Map<?, ?> schemaArgument = findPromptArgument(inspectMetadataPrompt, "schema");
        assertThat(((Map<?, ?>) schemaArgument.get("completion")).get("required_context_arguments"), is(List.of("database")));
    }
    
    private Map<?, ?> findResource(final Map<String, Object> capabilities, final String uriTemplate) {
        List<?> resources = (List<?>) capabilities.get(uriTemplate.contains("{") ? "resourceTemplates" : "resources");
        String uriFieldName = uriTemplate.contains("{") ? "uriTemplate" : "uri";
        return resources.stream().map(each -> (Map<?, ?>) each).filter(each -> uriTemplate.equals(each.get(uriFieldName))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findTool(final Map<String, Object> capabilities, final String toolName) {
        return ((List<?>) capabilities.get("tools")).stream().map(each -> (Map<?, ?>) each).filter(each -> toolName.equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findByKey(final List<?> values, final String key, final String expectedValue) {
        return values.stream().map(each -> (Map<?, ?>) each).filter(each -> expectedValue.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findInputSchema(final Map<?, ?> tool, final String fieldName) {
        return (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) tool.get("inputSchema")).get("properties")).get(fieldName);
    }
    
    private List<String> getInputFieldNames(final Map<?, ?> tool) {
        return ((Map<?, ?>) ((Map<?, ?>) tool.get("inputSchema")).get("properties")).keySet().stream().map(String::valueOf).toList();
    }
    
    private boolean hasCompletionTarget(final Map<String, Object> capabilities, final String reference, final String argumentName) {
        return ((List<?>) capabilities.get("completionTargets")).stream().map(each -> (Map<?, ?>) each)
                .anyMatch(each -> reference.equals(each.get("reference")) && ((List<?>) each.get("arguments")).contains(argumentName));
    }
    
    private Map<?, ?> findPrompt(final Map<String, Object> capabilities, final String promptName) {
        return ((List<?>) capabilities.get("prompts")).stream().map(each -> (Map<?, ?>) each).filter(each -> promptName.equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findPromptArgument(final Map<?, ?> prompt, final String argumentName) {
        return ((List<?>) prompt.get("arguments")).stream().map(each -> (Map<?, ?>) each).filter(each -> argumentName.equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private Map<String, Object> createOfficialDiscoveryMethods() {
        return Map.of("tools", "tools/list", "resources", "resources/list", "resource_templates", "resources/templates/list", "prompts", "prompts/list");
    }
}
