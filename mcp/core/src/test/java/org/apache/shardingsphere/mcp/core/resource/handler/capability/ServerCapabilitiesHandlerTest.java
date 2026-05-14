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

import org.apache.shardingsphere.infra.util.json.JsonUtils;
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
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext())) {
            Map<String, Object> actual = new ServerCapabilitiesHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertGoldenTopLevelKeys(actual);
            assertTrue(((Collection<?>) actual.get("supportedResources")).contains("shardingsphere://capabilities"));
            assertTrue(((Collection<?>) actual.get("supportedTools")).containsAll(List.of("database_gateway_search_metadata", "database_gateway_execute_query",
                    "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
            assertFalse(((List<?>) actual.get("resources")).isEmpty());
            assertFalse(((List<?>) actual.get("resourceTemplates")).isEmpty());
            assertFalse(((List<?>) actual.get("tools")).isEmpty());
            assertFalse(((List<?>) actual.get("prompts")).isEmpty());
            assertFalse(((List<?>) actual.get("completionTargets")).isEmpty());
            assertFalse(((List<?>) actual.get("resourceNavigation")).isEmpty());
            Map<?, ?> protocolAvailability = (Map<?, ?>) actual.get("protocolAvailability");
            assertTrue((Boolean) protocolAvailability.get("resourceNavigation"));
            assertFalse(protocolAvailability.containsKey("sampling"));
            assertFalse(protocolAvailability.containsKey("roots"));
            assertTrue(((Map<?, ?>) actual.get("fingerprints")).containsKey("descriptorCatalog"));
            assertModelFirstSummary(actual);
            assertModelContract(actual);
            assertSurfaceSummary(actual);
            assertFieldNamingContract(actual);
            assertNextActionContract(actual);
            assertCommonFlows(actual);
            assertSecurityHints(actual);
            assertLegacyPayloadFieldsAbsent(actual);
            assertResourcePayloadContracts(actual);
            assertCoreToolSchemas(actual);
        }
    }
    
    private void assertGoldenTopLevelKeys(final Map<String, Object> capabilities) {
        assertThat(capabilities.keySet(), is(Set.of("response_mode", "model_first_summary", "supportedResources", "supportedTools", "supportedStatementClasses", "model_contract",
                "surface_summary", "field_naming_contract", "next_action_contract", "common_flows", "security_hints", "resources", "resourceTemplates", "tools", "prompts",
                "completionTargets", "resourceNavigation", "protocolAvailability", "fingerprints")));
    }
    
    private void assertModelFirstSummary(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_first_summary");
        assertThat(((Map<?, ?>) actual.get("official_discovery_methods")).get("tools"), is("tools/list"));
        assertTrue(String.valueOf(actual.get("catalog_resource_role")).contains("domain catalog resource"));
        assertThat(actual.get("safe_first_resource"), is("shardingsphere://capabilities"));
        Map<?, ?> metadataRule = (Map<?, ?>) actual.get("metadata_rule");
        assertThat(metadataRule.get("first_resource"), is("shardingsphere://databases"));
        assertThat(metadataRule.get("search_tool"), is("database_gateway_search_metadata"));
        Map<?, ?> sqlToolSelection = (Map<?, ?>) actual.get("sql_tool_selection");
        assertThat(((Map<?, ?>) sqlToolSelection.get("read_only")).get("tool"), is("database_gateway_execute_query"));
        assertThat(((Map<?, ?>) sqlToolSelection.get("side_effecting")).get("first_mode"), is("preview"));
        assertTrue(String.valueOf(actual.get("side_effect_rule")).contains("explicit user approval"));
        Map<?, ?> workflowRule = (Map<?, ?>) actual.get("workflow_rule");
        assertTrue(workflowRule.containsKey("planning_tools"));
        assertThat(((Map<?, ?>) workflowRule.get("preview_tool")).get("tool"), is("database_gateway_apply_workflow"));
        assertThat(workflowRule.get("validate_tool"), is("database_gateway_validate_workflow"));
        assertTrue(String.valueOf(actual.get("completion_rule")).contains("before guessing identifiers"));
        assertTrue(String.valueOf(actual.get("recovery_rule")).contains("recovery.next_actions"));
    }
    
    private void assertModelContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_contract");
        assertTrue(String.valueOf(actual.get("public_surface_source")).contains("tools/list"));
        assertThat(((Map<?, ?>) actual.get("official_discovery_methods")).get("resources"), is("resources/list"));
        assertThat(actual.get("safe_first_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(((Map<?, ?>) actual.get("sql_tool_selection")).containsKey("side_effecting"));
        assertTrue(actual.containsKey("workflow_session_rule"));
        assertTrue(actual.containsKey("next_action_rule"));
        assertTrue(actual.containsKey("recovery_rule"));
    }
    
    private void assertSurfaceSummary(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("surface_summary");
        assertThat(((Map<?, ?>) actual.get("first_protocol_methods")).get("resources"), is("resources/list"));
        assertThat(actual.get("catalog_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_search_tool"), is("database_gateway_search_metadata"));
        assertThat(actual.get("side_effect_sql_tool"), is("database_gateway_execute_update"));
    }
    
    private void assertFieldNamingContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("field_naming_contract");
        assertTrue(((List<?>) actual.get("protocol_methods")).contains("resources/templates/list"));
        assertTrue(((List<?>) actual.get("catalog_fields")).contains("resourceTemplates"));
        assertThat(actual.get("payload_fields"), is("ShardingSphere-owned structured payload fields use snake_case."));
        assertTrue(String.valueOf(actual.get("alias_rule")).contains("Do not assume"));
    }
    
    private void assertNextActionContract(final Map<String, Object> capabilities) {
        Map<?, ?> callTool = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "tool_call");
        assertThat(callTool.get("required_fields"), is(List.of("order", "type", "title", "tool_name", "arguments", "requires_user_approval")));
        Map<?, ?> readResource = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "resource_read");
        assertThat(readResource.get("required_fields"), is(List.of("order", "type", "title", "resource_uri", "requires_user_approval")));
        Map<?, ?> completion = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "completion");
        assertTrue(((List<?>) completion.get("required_fields")).contains("argument_name"));
        Map<?, ?> askUser = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "ask_user");
        assertThat(askUser.get("required_fields"), is(List.of("order", "type", "title", "question", "requires_user_approval")));
        Map<?, ?> stop = findByKey((List<?>) capabilities.get("next_action_contract"), "type", "terminal");
        assertThat(stop.get("required_fields"), is(List.of("order", "type", "title", "requires_user_approval")));
    }
    
    private void assertCommonFlows(final Map<String, Object> capabilities) {
        Collection<?> supportedTools = (Collection<?>) capabilities.get("supportedTools");
        Collection<?> supportedResources = (Collection<?>) capabilities.get("supportedResources");
        Map<?, ?> inspectMetadata = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "inspect_metadata");
        assertTrue(((List<?>) inspectMetadata.get("steps")).containsAll(List.of("resources/list", "resources/templates/list", "call_tool database_gateway_search_metadata")));
        assertReferencedFlowEntries(inspectMetadata, supportedTools, supportedResources);
        Map<?, ?> sideEffectingSql = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "side_effecting_sql");
        assertTrue(((List<?>) sideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=preview"));
        assertTrue(((List<?>) sideEffectingSql.get("steps")).contains("call_tool database_gateway_execute_update execution_mode=execute approved_by_user=true"));
        assertReferencedFlowEntries(sideEffectingSql, supportedTools, supportedResources);
        Map<?, ?> workflow = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "workflow_plan_apply_validate");
        assertTrue(((List<?>) workflow.get("steps")).contains("call_tool database_gateway_apply_workflow review-then-execute approved_by_user=true"));
        assertThat(workflow.get("stop_condition"), is("Reuse the same current-session plan_id and stop after validation succeeds."));
        assertReferencedFlowEntries(workflow, supportedTools, supportedResources);
    }
    
    private void assertReferencedFlowEntries(final Map<?, ?> flow, final Collection<?> supportedTools, final Collection<?> supportedResources) {
        for (Object each : (List<?>) flow.get("referenced_tools")) {
            assertTrue(supportedTools.contains(each), "Unknown flow tool: " + each);
        }
        for (Object each : (List<?>) flow.get("referenced_resources")) {
            assertTrue(supportedResources.contains(each), "Unknown flow resource: " + each);
        }
    }
    
    private void assertSecurityHints(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("security_hints");
        assertTrue(actual.containsKey("http_access_token"));
        assertTrue(actual.containsKey("remote_access"));
        assertTrue(actual.containsKey("stdio_stdout"));
        Map<?, ?> actualClientSafetyPolicy = (Map<?, ?>) actual.get("client_safety_policy");
        assertThat(actualClientSafetyPolicy.get("identity_scope"), is("mcp_session"));
        assertThat(((Map<?, ?>) actualClientSafetyPolicy.get("tool_call_limit")).get("scope"), is("session"));
        assertTrue(String.valueOf(actualClientSafetyPolicy.get("abuse_guard")).contains("counted before dispatch"));
    }
    
    private void assertLegacyPayloadFieldsAbsent(final Map<String, Object> capabilities) {
        String actual = JsonUtils.toJsonString(capabilities);
        for (String each : List.of("pending_questions", "parent_uri", "next_resource_uris", "read_resources_first", "empty_reason", "not_found_reason")) {
            assertFalse(actual.contains(each));
        }
    }
    
    private void assertResourcePayloadContracts(final Map<String, Object> capabilities) {
        Map<?, ?> capabilityCatalog = findResource(capabilities, "shardingsphere://capabilities");
        assertThat(getResourceMeta(capabilityCatalog).get(MCPShardingSphereMetadataKeys.RESOURCE_KIND), is("capability-catalog"));
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
        Map<?, ?> navigation = findByKey((List<?>) capabilities.get("resourceNavigation"), "from", "shardingsphere://capabilities");
        assertThat(navigation.get("from_type"), is("resource"));
        assertThat(navigation.get("to_type"), is("resource"));
    }
    
    private Map<?, ?> getResourceMeta(final Map<?, ?> resource) {
        return (Map<?, ?>) resource.get("meta");
    }
    
    private Map<?, ?> findUriVariable(final Map<?, ?> resource, final String variableName) {
        return ((List<?>) getResourceMeta(resource).get(MCPShardingSphereMetadataKeys.URI_VARIABLES)).stream()
                .map(each -> (Map<?, ?>) each).filter(each -> variableName.equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private void assertCoreToolSchemas(final Map<String, Object> capabilities) {
        Map<?, ?> searchMetadataTool = findTool(capabilities, "database_gateway_search_metadata");
        Map<?, ?> searchMetadataOutputProperties = (Map<?, ?>) ((Map<?, ?>) searchMetadataTool.get("outputSchema")).get("properties");
        assertTrue(searchMetadataOutputProperties.containsKey("total_match_count"));
        Map<?, ?> objectTypesSchema = findInputSchema(searchMetadataTool, "object_types");
        assertTrue(((List<?>) ((Map<?, ?>) objectTypesSchema.get("items")).get("enum")).containsAll(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        Map<?, ?> executeUpdateTool = findTool(capabilities, "database_gateway_execute_update");
        Map<?, ?> executeUpdateOutputProperties = (Map<?, ?>) ((Map<?, ?>) executeUpdateTool.get("outputSchema")).get("properties");
        assertTrue(executeUpdateOutputProperties.containsKey("response_mode"));
        assertTrue(((List<?>) ((Map<?, ?>) executeUpdateOutputProperties.get("result_kind")).get("enum")).containsAll(List.of("preview", "result_set", "update_count", "statement_ack")));
        assertTrue(executeUpdateOutputProperties.containsKey("preview_semantics"));
        assertTrue(executeUpdateOutputProperties.containsKey("approval_summary"));
        assertTrue(executeUpdateOutputProperties.containsKey("approval_question"));
        assertThat(getInputFieldNames(executeUpdateTool), is(List.of("database", "schema", "sql", "execution_mode", "approved_by_user", "max_rows", "timeout_ms")));
        assertThat(findInputSchema(executeUpdateTool, "approved_by_user").get("type"), is("boolean"));
        Map<?, ?> applyWorkflowTool = findTool(capabilities, "database_gateway_apply_workflow");
        assertThat(getInputFieldNames(applyWorkflowTool), is(List.of("plan_id", "execution_mode", "approved_steps", "approved_by_user")));
        assertTrue(hasCompletionTarget(capabilities, "shardingsphere://workflows/{plan_id}", "plan_id"));
        assertThat(findInputSchema(applyWorkflowTool, "approved_by_user").get("type"), is("boolean"));
        Map<?, ?> inspectMetadataPrompt = findPrompt(capabilities, "inspect_metadata");
        Map<?, ?> schemaArgument = findPromptArgument(inspectMetadataPrompt, "schema");
        assertThat(((Map<?, ?>) schemaArgument.get("completion")).get("required_context_arguments"), is(List.of("database")));
        assertNoLegacyPublicAliasFields(capabilities);
    }
    
    private void assertNoLegacyPublicAliasFields(final Object value) {
        if (value instanceof Map) {
            assertNoLegacyPublicAliasFieldMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                assertNoLegacyPublicAliasFields(each);
            }
        }
    }
    
    private void assertNoLegacyPublicAliasFieldMap(final Map<?, ?> value) {
        assertFalse(value.containsKey("recommended_next_tool"));
        assertFalse(value.containsKey("suggested_next_tool"));
        assertFalse(value.containsKey("suggested_next_tools"));
        assertFalse(value.containsKey("recommended_recovery"));
        assertFalse(value.containsKey("suggested_next_action"));
        for (Object each : value.values()) {
            assertNoLegacyPublicAliasFields(each);
        }
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
}
