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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Model-first contract payload builder.
 */
final class MCPModelFirstContractPayloadBuilder {
    
    private static final String PLANNING_TOOL_NAME_PREFIX = "database_gateway_plan_";
    
    private final MCPDescriptorCatalog catalog;
    
    MCPModelFirstContractPayloadBuilder(final MCPDescriptorCatalog catalog) {
        this.catalog = catalog;
    }
    
    Map<String, Object> createModelFirstSummary() {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("safe_first_resource", "shardingsphere://capabilities");
        result.put("metadata_rule", createMetadataRule());
        result.put("sql_tool_selection", createSqlToolSelection());
        result.put("side_effect_rule", "Never execute side effects before preview and explicit user approval.");
        result.put("workflow_rule", createWorkflowRule());
        result.put("completion_rule", "Use MCP completion for missing database, schema, table, column, index, or sequence values before guessing identifiers.");
        result.put("recovery_rule", "Follow structured recovery.next_actions before inventing a replacement call.");
        return result;
    }
    
    Map<String, Object> createModelContract() {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("public_surface_source", "shardingsphere://capabilities");
        result.put("safe_first_resource", "shardingsphere://capabilities");
        result.put("metadata_first_resource", "shardingsphere://databases");
        result.put("sql_tool_selection", Map.of(
                "read_only", "Use database_gateway_execute_query for one SELECT or EXPLAIN ANALYZE statement.",
                "side_effecting", "Use database_gateway_execute_update with execution_mode=preview before asking for user approval."));
        result.put("workflow_session_rule", "Reuse the current-session plan_id returned by a planning tool; re-plan when the plan is unavailable.");
        result.put("side_effect_rule", "Preview before side effects and continue only after explicit user approval with approved_by_user=true.");
        result.put("next_action_rule", "Use canonical next_actions fields: type, tool_name, resource_uri, arguments, and requires_user_approval.");
        result.put("detail_resource_rule", "Read each resource payload_contract before assuming detail fields.");
        result.put("recovery_rule", "When a call fails with recovery.next_actions, follow those structured actions before inventing a new call.");
        return result;
    }
    
    Map<String, Object> createSurfaceSummary() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("first_resource", "shardingsphere://capabilities");
        result.put("metadata_resource", "shardingsphere://databases");
        result.put("metadata_search_tool", "database_gateway_search_metadata");
        result.put("read_only_sql_tool", "database_gateway_execute_query");
        result.put("side_effect_sql_tool", "database_gateway_execute_update");
        result.put("workflow_tools", List.of("database_gateway_apply_workflow", "database_gateway_validate_workflow"));
        result.put("approval_rule", "Preview first and continue only after explicit user approval when requires_user_approval is true.");
        result.put("completion_rule", "Use local completion hints on fields, prompts, and resources before guessing identifiers.");
        return result;
    }
    
    Map<String, Object> createFieldNamingContract() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("protocol_fields", List.of("supportedResources", "supportedTools", "resourceTemplates", "completionTargets", "resourceNavigation", "protocolAvailability"));
        result.put("payload_fields", "ShardingSphere-owned structured payload fields use snake_case.");
        result.put("descriptor_fields", "Descriptor-derived MCP schema fields keep the protocol or JSON Schema field names required by MCP clients.");
        result.put("alias_rule", "Do not assume camelCase and snake_case variants are aliases unless the same descriptor explicitly documents both.");
        result.put("cleanup_rule", "Prefer one canonical ShardingSphere payload field over parallel compatibility aliases.");
        return result;
    }
    
    List<Map<String, Object>> createNextActionContract() {
        return List.of(
                createNextActionContractEntry("resource_read", List.of("order", "type", "title", "resource_uri", "requires_user_approval"),
                        List.of("reason", "depends_on"), "Read an MCP resource before choosing another call."),
                createNextActionContractEntry("tool_call", List.of("order", "type", "title", "tool_name", "arguments", "requires_user_approval"),
                        List.of("reason", "depends_on"), "Call another MCP tool with server-suggested arguments."),
                createNextActionContractEntry("completion",
                        List.of("order", "type", "title", "reference_type", "reference", "argument_name", "context_arguments", "requires_user_approval"),
                        List.of("argument_prefix", "missing_context_arguments", "resume_target_type", "resume_target", "resume_arguments", "reason", "depends_on"),
                        "Use MCP completion for one argument before retrying."),
                createNextActionContractEntry("ask_user", List.of("order", "type", "title", "question", "requires_user_approval"),
                        List.of("required_inputs", "reason", "depends_on"), "Ask the user for missing input or approval."),
                createNextActionContractEntry("terminal", List.of("order", "type", "title", "requires_user_approval"),
                        List.of("reason", "depends_on"), "Stop the current MCP flow and report the result."));
    }
    
    List<Map<String, Object>> createCommonFlows() {
        return List.of(
                createCommonFlow("inspect_metadata", List.of("read_resource shardingsphere://capabilities", "read_resource shardingsphere://databases",
                        "call_tool database_gateway_search_metadata", "read_resource returned resource.uri"), "Stop when the requested metadata detail resource is read.",
                        List.of("database_gateway_search_metadata"), List.of("shardingsphere://capabilities", "shardingsphere://databases")),
                createCommonFlow("read_only_sql", List.of("read_resource shardingsphere://databases/{database}/capabilities", "call_tool database_gateway_execute_query"),
                        "Use one SELECT or EXPLAIN ANALYZE statement and stop after the result is reported.",
                        List.of("database_gateway_execute_query"), List.of("shardingsphere://databases/{database}/capabilities")),
                createCommonFlow("side_effecting_sql", List.of("call_tool database_gateway_execute_update execution_mode=preview", "ask_user approved_by_user",
                        "call_tool database_gateway_execute_update execution_mode=execute approved_by_user=true"),
                        "Never execute until the previewed SQL and side-effect scope are approved by the user.", List.of("database_gateway_execute_update"), List.of()),
                createCommonFlow("workflow_plan_apply_validate", List.of("call_tool descriptor-backed feature planning tool", "call_tool database_gateway_apply_workflow execution_mode=preview",
                        "ask_user approved_by_user", "call_tool database_gateway_apply_workflow review-then-execute approved_by_user=true", "call_tool database_gateway_validate_workflow"),
                        "Reuse the same current-session plan_id and stop after validation succeeds.",
                        List.of("database_gateway_apply_workflow", "database_gateway_validate_workflow"), List.of()),
                createCommonFlow("recover_from_error", List.of("follow recovery.next_actions", "read_resource shardingsphere://capabilities when suggested", "ask_user only when uncertain"),
                        "Do not invent replacement calls while structured recovery actions are present.", List.of(), List.of("shardingsphere://capabilities")));
    }
    
    Map<String, Object> createSecurityHints() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("http_access_token", "HTTP transport may require an Authorization bearer token; capabilities never exposes secrets.");
        result.put("remote_access", "Prefer loopback access unless the operator explicitly configures remote exposure.");
        result.put("stdio_stdout", "STDIO transport must keep MCP protocol frames on stdout and send logs to stderr or files.");
        result.put("client_safety_policy", MCPClientSafetyPolicy.createModelFacingPayload());
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
        sideEffecting.put("execute_requires", "approved_by_user=true");
        result.put("side_effecting", sideEffecting);
        return result;
    }
    
    private Map<String, Object> createWorkflowRule() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("planning_tools", catalog.getToolDescriptors().stream().map(MCPToolDescriptor::getName).filter(each -> each.startsWith(PLANNING_TOOL_NAME_PREFIX)).toList());
        Map<String, Object> previewTool = new LinkedHashMap<>(2, 1F);
        previewTool.put("tool", "database_gateway_apply_workflow");
        previewTool.put("execution_mode", "preview");
        result.put("preview_tool", previewTool);
        Map<String, Object> executeTool = new LinkedHashMap<>(3, 1F);
        executeTool.put("tool", "database_gateway_apply_workflow");
        executeTool.put("execution_mode", "review-then-execute");
        executeTool.put("execute_requires", "approved_by_user=true");
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
