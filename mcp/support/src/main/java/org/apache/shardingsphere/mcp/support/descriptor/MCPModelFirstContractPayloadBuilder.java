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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Model-first contract payload builder.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MCPModelFirstContractPayloadBuilder {
    
    private static final String PLANNING_TOOL_NAME_PREFIX = "database_gateway_plan_";
    
    private static final String CATALOG_RESOURCE_URI = "shardingsphere://capabilities";
    
    private static final String ARGUMENT_COMPLETION_METHOD = "completion/complete";
    
    private static final String OFFICIAL_DISCOVERY_SOURCE = "Official MCP list methods: tools/list, resources/list, resources/templates/list, prompts/list.";
    
    private final MCPDescriptorCatalog catalog;
    
    Map<String, Object> createModelFirstSummary() {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("catalog_resource_role", CATALOG_RESOURCE_URI + " is an optional ShardingSphere domain catalog resource, not the MCP protocol discovery source.");
        result.put("optional_catalog_resource", CATALOG_RESOURCE_URI);
        result.put("metadata_rule", createMetadataRule());
        result.put("sql_tool_selection", createSqlToolSelection());
        result.put("side_effect_rule", "Preview side effects first; execute only when the requested side effect is still intended.");
        result.put("workflow_rule", createWorkflowRule());
        result.put("completion_rule", "Use MCP completion for missing database, schema, table, column, index, or sequence values before guessing identifiers.");
        result.put("recovery_rule", "Follow structured recovery.next_actions before inventing a replacement call.");
        return result;
    }
    
    Map<String, Object> createModelContract() {
        Map<String, Object> result = new LinkedHashMap<>(11, 1F);
        result.put("public_surface_source", OFFICIAL_DISCOVERY_SOURCE);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("optional_catalog_resource", CATALOG_RESOURCE_URI);
        result.put("metadata_first_resource", "shardingsphere://databases");
        result.put("sql_tool_selection", Map.of(
                "read_only", "Use database_gateway_execute_query for one classifier-approved SELECT or EXPLAIN ANALYZE statement.",
                "side_effecting", "Use database_gateway_execute_update with execution_mode=preview before execution."));
        result.put("workflow_session_rule", "Reuse the current-session plan_id returned by a planning tool; re-plan when the plan is unavailable.");
        result.put("side_effect_rule", "Preview before side effects and continue only when the requested side effect is still intended.");
        result.put("next_action_rule", "Use canonical next_actions fields: type, tool_name, resource_uri, and arguments.");
        result.put("detail_resource_rule", "Read each resource payload_contract before assuming detail fields.");
        result.put("recovery_rule", "When a call fails with recovery.next_actions, follow those structured actions before inventing a new call.");
        return result;
    }
    
    Map<String, Object> createSurfaceSummary() {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("optional_catalog_resource", CATALOG_RESOURCE_URI);
        result.put("metadata_resource", "shardingsphere://databases");
        result.put("metadata_search_tool", "database_gateway_search_metadata");
        result.put("read_only_sql_tool", "database_gateway_execute_query");
        result.put("side_effect_sql_tool", "database_gateway_execute_update");
        result.put("workflow_tools", List.of("database_gateway_apply_workflow", "database_gateway_validate_workflow"));
        result.put("side_effect_rule", "Preview first and continue only when the requested side effect is still intended.");
        result.put("completion_rule", "Use local completion hints on fields, prompts, and resources before guessing identifiers.");
        return result;
    }
    
    Map<String, Object> createFieldNamingContract() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("official_discovery_methods", List.of("tools/list", "resources/list", "resources/templates/list", "prompts/list"));
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("catalog_fields", List.of("supportedResources", "supportedTools", "resourceTemplates", "completionTargets", "resourceNavigation", "protocolAvailability"));
        result.put("payload_fields", "ShardingSphere-owned structured payload fields use snake_case.");
        result.put("descriptor_fields", "Descriptor-derived MCP schema fields keep the protocol or JSON Schema field names required by MCP clients.");
        result.put("alias_rule", "Do not assume camelCase and snake_case variants are aliases unless the same descriptor explicitly documents both.");
        result.put("cleanup_rule", "Prefer one canonical ShardingSphere payload field over parallel compatibility aliases.");
        return result;
    }
    
    List<Map<String, Object>> createNextActionContract() {
        return List.of(
                createNextActionContractEntry("resource_read", List.of("order", "type", "title", "resource_uri"),
                        List.of("reason", "depends_on"), "Read an MCP resource before choosing another call."),
                createNextActionContractEntry("tool_call", List.of("order", "type", "title", "tool_name", "arguments"),
                        List.of("reason", "depends_on"), "Call another MCP tool with server-suggested arguments."),
                createNextActionContractEntry("completion",
                        List.of("order", "type", "title", "reference_type", "reference", "argument_name", "context_arguments"),
                        List.of("argument_prefix", "missing_context_arguments", "resume_target_type", "resume_target", "resume_arguments", "reason", "depends_on"),
                        "Use MCP completion for one argument before retrying."),
                createNextActionContractEntry("ask_user", List.of("order", "type", "title", "question"),
                        List.of("required_inputs", "reason", "depends_on"), "Ask the user for missing input."),
                createNextActionContractEntry("terminal", List.of("order", "type", "title"),
                        List.of("reason", "depends_on"), "Stop the current MCP flow and report the result."));
    }
    
    List<Map<String, Object>> createCommonFlows() {
        return List.of(
                createCommonFlow("inspect_metadata", List.of("resources/list", "resources/templates/list", "read_resource shardingsphere://databases",
                        "call_tool database_gateway_search_metadata", "read_resource returned resource.uri"),
                        "Stop when the requested metadata detail resource is read.",
                        List.of("database_gateway_search_metadata"), List.of("shardingsphere://databases")),
                createCommonFlow("read_only_sql", List.of("read_resource shardingsphere://databases/{database}/capabilities", "call_tool database_gateway_execute_query"),
                        "Use one SELECT or EXPLAIN ANALYZE statement and stop after the result is reported.",
                        List.of("database_gateway_execute_query"), List.of("shardingsphere://databases/{database}/capabilities")),
                createCommonFlow("side_effecting_sql", List.of("call_tool database_gateway_execute_update execution_mode=preview",
                        "call_tool database_gateway_execute_update execution_mode=execute"),
                        "Execute only after reviewing the previewed SQL and side-effect scope.", List.of("database_gateway_execute_update"), List.of()),
                createCommonFlow("workflow_plan_apply_validate", List.of("call_tool descriptor-backed feature planning tool", "call_tool database_gateway_apply_workflow execution_mode=preview",
                        "call_tool database_gateway_apply_workflow review-then-execute", "call_tool database_gateway_validate_workflow"),
                        "Reuse the same current-session plan_id and stop after validation succeeds.",
                        List.of("database_gateway_apply_workflow", "database_gateway_validate_workflow"), List.of()),
                createCommonFlow("recover_from_error", List.of("follow recovery.next_actions", "prefer official list discovery methods when surface information is needed",
                        "read_resource shardingsphere://capabilities only when the server suggests the catalog resource", "ask_user only when uncertain"),
                        "Do not invent replacement calls while structured recovery actions are present.", List.of(), List.of(CATALOG_RESOURCE_URI)));
    }
    
    Map<String, Object> createSecurityHints() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("http_transport", "Streamable HTTP is unauthenticated by default; prefer loopback binding or put remote exposure behind a trusted gateway.");
        result.put("origin_header", "Present Origin headers must be valid loopback origins for loopback HTTP bindings; missing Origin is accepted.");
        result.put("stdio_stdout", "STDIO transport must keep MCP protocol frames on stdout and send logs to stderr or files.");
        result.put("client_safety_policy", MCPClientSafetyPolicy.createModelFacingPayload());
        return result;
    }
    
    private Map<String, Object> createOfficialDiscoveryMethods() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("tools", "tools/list");
        result.put("resources", "resources/list");
        result.put("resource_templates", "resources/templates/list");
        result.put("prompts", "prompts/list");
        return result;
    }
    
    private Map<String, Object> createNextActionContractEntry(final String actionType, final List<String> requiredFields, final List<String> optionalFields, final String description) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", actionType);
        result.put("required_fields", requiredFields);
        result.put("optional_fields", optionalFields);
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createMetadataRule() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("first_resource", "shardingsphere://databases");
        result.put("search_tool", "database_gateway_search_metadata");
        result.put("detail_rule", "Read the returned resource.uri when the list or search response points to a detail resource.");
        return result;
    }
    
    private Map<String, Object> createSqlToolSelection() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        Map<String, Object> readOnly = new LinkedHashMap<>(2, 1F);
        readOnly.put("tool", "database_gateway_execute_query");
        readOnly.put("statement_rule", "Use for one SELECT or EXPLAIN ANALYZE statement.");
        result.put("read_only", readOnly);
        Map<String, Object> sideEffecting = new LinkedHashMap<>(3, 1F);
        sideEffecting.put("tool", "database_gateway_execute_update");
        sideEffecting.put("first_mode", "preview");
        sideEffecting.put("execute_requires", "execution_mode=execute");
        result.put("side_effecting", sideEffecting);
        return result;
    }
    
    private Map<String, Object> createWorkflowRule() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("planning_tools", catalog.getToolDescriptors().stream().map(MCPToolDescriptor::getName).filter(each -> each.startsWith(PLANNING_TOOL_NAME_PREFIX)).toList());
        Map<String, Object> previewTool = new LinkedHashMap<>(2, 1F);
        previewTool.put("tool", "database_gateway_apply_workflow");
        previewTool.put(MCPPayloadFieldNames.EXECUTION_MODE, "preview");
        result.put("preview_tool", previewTool);
        Map<String, Object> executeTool = new LinkedHashMap<>(3, 1F);
        executeTool.put("tool", "database_gateway_apply_workflow");
        executeTool.put(MCPPayloadFieldNames.EXECUTION_MODE, "review-then-execute");
        executeTool.put("execute_requires", "execution_mode=review-then-execute");
        result.put("execute_tool", executeTool);
        result.put("validate_tool", "database_gateway_validate_workflow");
        return result;
    }
    
    private Map<String, Object> createCommonFlow(final String flowId, final List<String> steps, final String stopCondition, final List<String> referencedTools,
                                                 final List<String> referencedResources) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("flow_id", flowId);
        result.put("steps", steps);
        result.put("stop_condition", stopCondition);
        result.put("referenced_tools", referencedTools);
        result.put("referenced_resources", referencedResources);
        return result;
    }
}
