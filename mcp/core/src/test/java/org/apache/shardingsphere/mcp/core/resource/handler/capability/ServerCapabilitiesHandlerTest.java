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
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCapabilitiesHandlerTest {

    @Test
    void assertHandleReturnsCoreModelSurfaceContract() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext())) {
            Map<String, Object> actual = new ServerCapabilitiesHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertTrue(((Collection<?>) actual.get("supportedResources")).contains("shardingsphere://capabilities"));
            assertTrue(((Collection<?>) actual.get("supportedTools")).containsAll(List.of("search_metadata", "execute_query", "execute_update", "apply_workflow", "validate_workflow")));
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

    private void assertModelContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_contract");
        assertThat(actual.get("public_surface_source"), is("shardingsphere://capabilities"));
        assertThat(actual.get("safe_first_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(((Map<?, ?>) actual.get("sql_tool_selection")).containsKey("side_effecting"));
        assertTrue(actual.containsKey("workflow_session_rule"));
        assertTrue(actual.containsKey("legacy_compatibility_rule"));
        assertTrue(actual.containsKey("recovery_rule"));
    }

    private void assertSurfaceSummary(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("surface_summary");
        assertThat(actual.get("first_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_search_tool"), is("search_metadata"));
        assertThat(actual.get("side_effect_sql_tool"), is("execute_update"));
    }
    
    private void assertFieldNamingContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("field_naming_contract");
        assertTrue(((List<?>) actual.get("protocol_fields")).contains("resourceTemplates"));
        assertThat(actual.get("payload_fields"), is("ShardingSphere-owned structured payload fields use snake_case."));
        assertTrue(String.valueOf(actual.get("alias_rule")).contains("Do not assume"));
    }

    private void assertNextActionContract(final Map<String, Object> capabilities) {
        Map<?, ?> callTool = findByKey((List<?>) capabilities.get("next_action_contract"), "action_kind", "call_tool");
        assertThat(callTool.get("required_fields"), is(List.of("target_tool", "required_arguments", "reason", "requires_user_approval")));
        assertThat(callTool.get("optional_fields"), is(List.of("order", "depends_on")));
        Map<?, ?> retryTool = findByKey((List<?>) capabilities.get("next_action_contract"), "action_kind", "retry_tool");
        assertThat(retryTool.get("optional_fields"), is(List.of("target_tool", "source_tool", "order", "depends_on")));
        Map<?, ?> readResource = findByKey((List<?>) capabilities.get("next_action_contract"), "action_kind", "read_resource");
        assertThat(readResource.get("required_fields"), is(List.of("target_resource", "reason", "requires_user_approval")));
        Map<?, ?> askUser = findByKey((List<?>) capabilities.get("next_action_contract"), "action_kind", "ask_user");
        assertThat(askUser.get("required_fields"), is(List.of("required_inputs", "reason", "requires_user_approval")));
        Map<?, ?> stop = findByKey((List<?>) capabilities.get("next_action_contract"), "action_kind", "stop");
        assertThat(stop.get("required_fields"), is(List.of("reason", "requires_user_approval")));
    }

    private void assertCommonFlows(final Map<String, Object> capabilities) {
        Collection<?> supportedTools = (Collection<?>) capabilities.get("supportedTools");
        Collection<?> supportedResources = (Collection<?>) capabilities.get("supportedResources");
        Map<?, ?> inspectMetadata = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "inspect_metadata");
        assertTrue(((List<?>) inspectMetadata.get("steps")).containsAll(List.of("read_resource shardingsphere://capabilities", "call_tool search_metadata")));
        assertReferencedFlowEntries(inspectMetadata, supportedTools, supportedResources);
        Map<?, ?> sideEffectingSql = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "side_effecting_sql");
        assertTrue(((List<?>) sideEffectingSql.get("steps")).contains("call_tool execute_update execution_mode=preview"));
        assertReferencedFlowEntries(sideEffectingSql, supportedTools, supportedResources);
        Map<?, ?> workflow = findByKey((List<?>) capabilities.get("common_flows"), "flow_id", "workflow_plan_apply_validate");
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
    }

    private void assertLegacyPayloadFieldsAbsent(final Map<String, Object> capabilities) {
        String actual = JsonUtils.toJsonString(capabilities);
        for (String each : List.of("pending_questions", "resource_uri", "parent_uri", "next_resource_uris", "read_resources_first", "empty_reason", "not_found_reason")) {
            assertFalse(actual.contains(each));
        }
    }

    private void assertResourcePayloadContracts(final Map<String, Object> capabilities) {
        Map<?, ?> capabilityCatalog = findResource(capabilities, "shardingsphere://capabilities");
        assertThat(((Map<?, ?>) capabilityCatalog.get("payload_contract")).get("response_kind"), is("capability-catalog"));
        Map<?, ?> runtimeStatus = findResource(capabilities, "shardingsphere://runtime");
        Map<?, ?> runtimeStatusPayloadContract = (Map<?, ?>) runtimeStatus.get("payload_contract");
        assertThat(runtimeStatusPayloadContract.get("response_kind"), is("runtime-status"));
        assertTrue(((List<?>) runtimeStatusPayloadContract.get("stable_fields")).containsAll(List.of("active_transport", "databases")));
        Map<?, ?> databaseDetail = findResource(capabilities, "shardingsphere://databases/{database}");
        Map<?, ?> payloadContract = (Map<?, ?>) databaseDetail.get("payload_contract");
        assertThat(payloadContract.get("response_kind"), is("detail"));
        assertTrue(((List<?>) payloadContract.get("stable_fields")).containsAll(List.of("found", "items", "count")));
        assertTrue(((List<?>) payloadContract.get("optional_fields")).containsAll(List.of("item", "self_uri", "parent_resource", "next_resources")));
        Map<?, ?> databaseCapabilities = findResource(capabilities, "shardingsphere://databases/{database}/capabilities");
        assertThat(((Map<?, ?>) databaseCapabilities.get("payload_contract")).get("response_kind"), is("detail-object"));
        Map<?, ?> databases = findResource(capabilities, "shardingsphere://databases");
        Map<?, ?> databaseListPayloadContract = (Map<?, ?>) databases.get("payload_contract");
        assertThat(databaseListPayloadContract.get("stable_fields"), is(List.of("items", "count", "has_more")));
        assertThat(databaseListPayloadContract.get("optional_fields"), is(List.of(
                "next_page_token", "self_uri", "parent_resource", "next_resources", "empty_state", "large_result_guidance", "next_actions")));
        Map<?, ?> databaseParameter = ((List<?>) databaseDetail.get("parameters")).stream().map(each -> (Map<?, ?>) each).filter(each -> "database".equals(each.get("name"))).findFirst().orElseThrow();
        assertTrue((Boolean) ((Map<?, ?>) databaseParameter.get("completion")).get("available"));
        Map<?, ?> navigation = findByKey((List<?>) capabilities.get("resourceNavigation"), "from", "shardingsphere://capabilities");
        assertThat(navigation.get("from_type"), is("resource"));
        assertThat(navigation.get("to_type"), is("resource"));
    }

    private void assertCoreToolSchemas(final Map<String, Object> capabilities) {
        Map<?, ?> searchMetadataTool = findTool(capabilities, "search_metadata");
        Map<?, ?> objectTypesSchema = findInputSchema(searchMetadataTool, "object_types");
        assertTrue(((List<?>) ((Map<?, ?>) objectTypesSchema.get("items")).get("enum")).containsAll(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        Map<?, ?> executeUpdateTool = findTool(capabilities, "execute_update");
        Map<?, ?> executeUpdateOutputProperties = (Map<?, ?>) ((Map<?, ?>) executeUpdateTool.get("outputSchema")).get("properties");
        assertTrue(executeUpdateOutputProperties.containsKey("response_mode"));
        assertTrue(((List<?>) ((Map<?, ?>) executeUpdateOutputProperties.get("result_kind")).get("enum")).containsAll(List.of("preview", "result_set", "update_count", "statement_ack")));
        assertTrue(executeUpdateOutputProperties.containsKey("preview_semantics"));
        assertTrue(executeUpdateOutputProperties.containsKey("approval_summary"));
        assertTrue(executeUpdateOutputProperties.containsKey("approval_question"));
        Map<?, ?> applyWorkflowTool = findTool(capabilities, "apply_workflow");
        Map<?, ?> planIdField = findInputField(applyWorkflowTool, "plan_id");
        assertTrue((Boolean) ((Map<?, ?>) planIdField.get("completion")).get("available"));
        Map<?, ?> inspectMetadataPrompt = findPrompt(capabilities, "inspect_metadata");
        Map<?, ?> schemaArgument = findPromptArgument(inspectMetadataPrompt, "schema");
        assertThat(((Map<?, ?>) schemaArgument.get("completion")).get("required_context_arguments"), is(List.of("database")));
        assertNoLegacyRecommendationFields(capabilities);
    }

    private void assertNoLegacyRecommendationFields(final Object value) {
        if (value instanceof Map) {
            assertNoLegacyRecommendationFieldMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                assertNoLegacyRecommendationFields(each);
            }
        }
    }

    private void assertNoLegacyRecommendationFieldMap(final Map<?, ?> value) {
        assertFalse(value.containsKey("recommended_next_tool"));
        assertFalse(value.containsKey("suggested_next_tool"));
        assertFalse(value.containsKey("suggested_next_tools"));
        for (Object each : value.values()) {
            assertNoLegacyRecommendationFields(each);
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
        return (Map<?, ?>) findInputField(tool, fieldName).get("schema");
    }

    private Map<?, ?> findInputField(final Map<?, ?> tool, final String fieldName) {
        Map<?, ?> field = ((List<?>) tool.get("inputFields")).stream().map(each -> (Map<?, ?>) each).filter(each -> fieldName.equals(each.get("name"))).findFirst().orElseThrow();
        return field;
    }

    private Map<?, ?> findPrompt(final Map<String, Object> capabilities, final String promptName) {
        return ((List<?>) capabilities.get("prompts")).stream().map(each -> (Map<?, ?>) each).filter(each -> promptName.equals(each.get("name"))).findFirst().orElseThrow();
    }

    private Map<?, ?> findPromptArgument(final Map<?, ?> prompt, final String argumentName) {
        return ((List<?>) prompt.get("arguments")).stream().map(each -> (Map<?, ?>) each).filter(each -> argumentName.equals(each.get("name"))).findFirst().orElseThrow();
    }
}
