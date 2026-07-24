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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.protocol.MCPModelFacingPayloadContract;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP guidance payload builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPGuidancePayloadBuilder {
    
    private static final String GUIDANCE_RESOURCE_URI = "shardingsphere://guidance";
    
    static Map<String, Object> build() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_mode", MCPResponseMode.GUIDANCE);
        result.put("discovery", createDiscovery());
        result.put("model_contract", createModelContract());
        result.put("next_action_contract", createNextActionContract());
        result.put("common_flows", createCommonFlows());
        result.put("security_hints", createSecurityHints());
        return result;
    }
    
    static Map<String, Object> createDiscovery() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", "completion/complete");
        result.put("guidance_resource_role", GUIDANCE_RESOURCE_URI + " complements MCP list methods with ShardingSphere domain guidance, workflow guidance, and side-effect notes.");
        return result;
    }
    
    static Map<String, Object> createModelContract() {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("metadata_first_resource", "shardingsphere://databases");
        result.put("preflight_rule", "Use database_gateway_validate_runtime_database with a configured database name before onboarding or troubleshooting runtime connectivity.");
        Map<String, Object> sqlToolSelection = new LinkedHashMap<>(3, 1F);
        sqlToolSelection.put("read_only", "Use database_gateway_execute_query for one parser-approved SELECT statement.");
        sqlToolSelection.put("explain", "Use database_gateway_execute_explain_query with the original SELECT and a model-generated database-native EXPLAIN SQL; do not use EXPLAIN ANALYZE.");
        sqlToolSelection.put("side_effecting", "Use database_gateway_execute_update with execution_mode=preview before execution.");
        result.put("sql_tool_selection", sqlToolSelection);
        result.put("workflow_session_rule", "Reuse the current-session plan_id returned by a planning tool; re-plan when the plan is unavailable.");
        result.put("side_effect_rule", "Preview before side effects and continue only when the requested side effect is still intended.");
        result.put("completion_rule", "Use completion/complete for one uncertain argument at a time; when completion reports missing context, follow meta.next_actions before guessing.");
        result.put("resource_template_rule", "Use resources/templates/list to discover URI variables, then read the nearest concrete resource before filling dependent completion context.");
        result.put("next_action_rule", "Use canonical next_actions fields: type, tool_name, resource_uri, and arguments.");
        result.put("detail_resource_rule", "Use resource descriptors, outputSchema, and returned payload keys before assuming detail fields.");
        result.put("recovery_rule", "When a call fails, follow top-level next_actions before inventing a new call.");
        result.put("payload_field_rule", "ShardingSphere-owned structured payload fields use snake_case.");
        result.put("descriptor_field_rule", "Descriptor-derived MCP schema fields keep the protocol or JSON Schema field names required by MCP clients.");
        return result;
    }
    
    static List<Map<String, Object>> createNextActionContract() {
        return List.of(
                createNextActionContractEntry("resource_read", "Read an MCP resource before choosing another call."),
                createNextActionContractEntry("tool_call", "Call another MCP tool with server-suggested arguments."),
                createNextActionContractEntry("completion", "Use MCP completion for one argument before retrying."),
                createNextActionContractEntry("ask_user", "Ask the user for missing input."),
                createNextActionContractEntry("terminal", "Stop the current MCP flow and report the result."));
    }
    
    static List<Map<String, Object>> createCommonFlows() {
        return List.of(
                createCommonFlow("inspect_metadata", List.of("resources/list", "resources/templates/list", "read_resource shardingsphere://databases",
                        "call_tool database_gateway_search_metadata", "read_resource returned resource.uri"),
                        "Stop when the requested metadata detail resource is read."),
                createCommonFlow("validate_runtime_database", List.of("read_resource shardingsphere://databases", "call_tool database_gateway_validate_runtime_database"),
                        "Stop after the configured runtime database reports ready or returns structured recovery guidance."),
                createCommonFlow("read_only_sql", List.of("read_resource shardingsphere://databases/{database}/capabilities", "call_tool database_gateway_execute_query"),
                        "Use one SELECT statement and stop after the result is reported."),
                createCommonFlow("explain_query", List.of("read_resource shardingsphere://databases/{database}/capabilities",
                        "generate database-native EXPLAIN SQL without ANALYZE", "call_tool database_gateway_execute_explain_query"),
                        "Use the original SELECT as sql, the generated EXPLAIN as explain_sql, and stop after the plan rows are reported."),
                createCommonFlow("side_effecting_sql", List.of("call_tool database_gateway_execute_update execution_mode=preview",
                        "call_tool database_gateway_execute_update execution_mode=execute"),
                        "Execute only after reviewing the previewed SQL and side-effect scope."),
                createCommonFlow("complete_uncertain_argument", List.of("resources/templates/list", "call completion/complete for one argument",
                        "follow completion meta.next_actions when diagnostic is missing_context, prefix_filtered_all_candidates, or no_candidates"),
                        "Stop when a completion value is selected or the nearest resource proves the argument is unavailable."),
                createCommonFlow("workflow_plan_apply_validate", List.of("call_tool descriptor-backed feature planning tool", "call_tool database_gateway_apply_workflow execution_mode=preview",
                        "call_tool database_gateway_apply_workflow execution_mode=review-then-execute approved_steps=<preview_artifacts.approval_step>",
                        "call_tool database_gateway_validate_workflow"),
                        "Reuse the same current-session plan_id and stop after validation succeeds."),
                createCommonFlow("recover_from_error", List.of("follow top-level next_actions", "prefer official list discovery methods when surface information is needed",
                        "read_resource shardingsphere://guidance only when the server suggests the guidance resource", "ask_user only when uncertain"),
                        "Do not invent replacement calls while structured recovery actions are present."));
    }
    
    static Map<String, Object> createSecurityHints() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("http_transport", "Streamable HTTP is unauthenticated by default; prefer loopback binding or put remote exposure behind a trusted gateway.");
        result.put("origin_header", "Present Origin headers must be valid loopback origins for loopback HTTP bindings; missing Origin is accepted.");
        result.put("stdio_stdout", "STDIO transport must keep MCP protocol frames on stdout and send logs to stderr or files.");
        result.put("client_safety_policy", MCPClientSafetyPolicy.createModelFacingPayload());
        return result;
    }
    
    private static Map<String, Object> createOfficialDiscoveryMethods() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("tools", "tools/list");
        result.put("resources", "resources/list");
        result.put("resource_templates", "resources/templates/list");
        result.put("prompts", "prompts/list");
        return result;
    }
    
    private static Map<String, Object> createNextActionContractEntry(final String actionType, final String description) {
        Collection<String> requiredFields = MCPModelFacingPayloadContract.getNextActionRequiredFields(actionType);
        List<String> optionalFields = MCPModelFacingPayloadContract.getNextActionAllowedFields(actionType).stream().filter(each -> !requiredFields.contains(each)).toList();
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", actionType);
        result.put("required_fields", requiredFields);
        result.put("optional_fields", optionalFields);
        result.put("description", description);
        return result;
    }
    
    private static Map<String, Object> createCommonFlow(final String flowId, final List<String> steps, final String stopCondition) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("flow_id", flowId);
        result.put("steps", steps);
        result.put("stop_condition", stopCondition);
        return result;
    }
}
