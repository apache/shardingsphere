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
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP guidance payload builder.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class MCPGuidancePayloadBuilder {
    
    private static final String PLANNING_TOOL_NAME_PREFIX = "database_gateway_plan_";
    
    private static final String PREFLIGHT_TOOL_NAME = "database_gateway_validate_runtime_database";
    
    private static final String GUIDANCE_RESOURCE_URI = "shardingsphere://guidance";
    
    private static final String ARGUMENT_COMPLETION_METHOD = "completion/complete";
    
    private static final String MCP_LIST_METHODS_SOURCE = "MCP list methods expose the protocol surface: tools/list, resources/list, resources/templates/list, prompts/list.";
    
    private final MCPDescriptorCatalog catalog;
    
    static Map<String, Object> build(final MCPDescriptorCatalog catalog) {
        return new MCPGuidancePayloadBuilder(catalog).build();
    }
    
    private Map<String, Object> build() {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("response_mode", MCPResponseMode.GUIDANCE);
        result.put("guidance_resource", GUIDANCE_RESOURCE_URI);
        result.put("model_first_summary", createModelFirstSummary());
        result.put("model_contract", createModelContract());
        result.put("surface_summary", createSurfaceSummary());
        result.put("field_naming_contract", createFieldNamingContract());
        result.put("next_action_contract", createNextActionContract());
        result.put("common_flows", createCommonFlows());
        result.put("security_hints", createSecurityHints());
        return result;
    }
    
    Map<String, Object> createModelFirstSummary() {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("guidance_resource_role", GUIDANCE_RESOURCE_URI + " complements MCP list methods with ShardingSphere domain guidance, workflow guidance, and side-effect notes.");
        result.put("guidance_resource", GUIDANCE_RESOURCE_URI);
        result.put("first_call_routes", createFirstCallRoutes());
        result.put("metadata_rule", createMetadataRule());
        result.put("preflight_rule", createPreflightRule());
        result.put("sql_tool_selection", createSqlToolSelection());
        result.put("side_effect_rule", "Preview side effects first; execute only when the requested side effect is still intended.");
        result.put("workflow_rule", createWorkflowRule());
        result.put("completion_rule", "Use MCP completion for missing database, schema, table, column, index, sequence, or storage unit values before guessing identifiers.");
        result.put("recovery_rule", "Follow structured recovery.next_actions before inventing a replacement call.");
        return result;
    }
    
    private List<Map<String, Object>> createFirstCallRoutes() {
        return List.of(
                createFirstCallRoute("inspect_metadata", "read_resource shardingsphere://databases", "call_tool database_gateway_search_metadata or read returned resource.uri",
                        "Stop after the requested detail resource is read."),
                createFirstCallRoute("validate_runtime", "read_resource shardingsphere://runtime", "call_tool database_gateway_validate_runtime_database with a configured database",
                        "Follow top-level next_actions when validation fails."),
                createFirstCallRoute("read_only_sql", "read_resource shardingsphere://databases/{database}/capabilities", "call_tool database_gateway_execute_query",
                        "Stop after reporting the result rows."),
                createFirstCallRoute("rule_workflow", "call_tool matching database_gateway_plan_* workflow tool without plan_id for a new plan",
                        "call_tool database_gateway_apply_workflow execution_mode=preview with the returned plan_id",
                        "Use workflow apply and validation for natural-language rule changes before considering raw side-effect SQL."),
                createFirstCallRoute("side_effect_sql", "call_tool database_gateway_execute_update execution_mode=preview", "call_tool database_gateway_execute_update execution_mode=execute",
                        "Execute only after preview review confirms the intended side effect."),
                createFirstCallRoute("complete_uncertain_argument", "call completion/complete for one uncertain argument",
                        "follow completion meta.next_actions when context is missing or no candidates match", "Stop after the argument is selected or the nearest resource proves it is unavailable."),
                createFirstCallRoute("recover_error", "follow top-level next_actions", "fallback to recovery.next_actions when top-level actions are absent",
                        "Ask the user only when no deterministic resource, completion, or tool action is available."));
    }
    
    private Map<String, Object> createFirstCallRoute(final String intent, final String firstAction, final String nextStep, final String stopRule) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("intent", intent);
        result.put("first_action", firstAction);
        result.put("next_step", nextStep);
        result.put("stop_rule", stopRule);
        return result;
    }
    
    Map<String, Object> createModelContract() {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("public_surface_source", MCP_LIST_METHODS_SOURCE);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("guidance_resource", GUIDANCE_RESOURCE_URI);
        result.put("metadata_first_resource", "shardingsphere://databases");
        result.put("preflight_rule", "Use database_gateway_validate_runtime_database with a configured database name before onboarding or troubleshooting runtime connectivity.");
        Map<String, Object> sqlToolSelection = new LinkedHashMap<>(2, 1F);
        sqlToolSelection.put("read_only", "Use database_gateway_execute_query for one classifier-approved SELECT or EXPLAIN ANALYZE statement.");
        sqlToolSelection.put("side_effecting", "Use database_gateway_execute_update with execution_mode=preview before execution.");
        result.put("sql_tool_selection", sqlToolSelection);
        result.put("workflow_session_rule", "Reuse the current-session plan_id returned by a planning tool; re-plan when the plan is unavailable.");
        result.put("side_effect_rule", "Preview before side effects and continue only when the requested side effect is still intended.");
        result.put("completion_rule", "Use completion/complete for one uncertain argument at a time; when completion reports missing context, follow meta.next_actions before guessing.");
        result.put("resource_template_rule", "Use resources/templates/list to discover URI variables, then read the nearest concrete resource before filling dependent completion context.");
        result.put("next_action_rule", "Use canonical next_actions fields: type, tool_name, resource_uri, and arguments.");
        result.put("detail_resource_rule", "Use resource descriptors, outputSchema, and returned payload keys before assuming detail fields.");
        result.put("recovery_rule", "When a call fails with recovery.next_actions, follow those structured actions before inventing a new call.");
        return result;
    }
    
    Map<String, Object> createSurfaceSummary() {
        Map<String, Object> result = new LinkedHashMap<>(11, 1F);
        result.put("official_discovery_methods", createOfficialDiscoveryMethods());
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("guidance_resource", GUIDANCE_RESOURCE_URI);
        result.put("metadata_resource", "shardingsphere://databases");
        result.put("metadata_search_tool", "database_gateway_search_metadata");
        result.put("preflight_validation_tool", PREFLIGHT_TOOL_NAME);
        result.put("read_only_sql_tool", "database_gateway_execute_query");
        result.put("side_effect_sql_tool", "database_gateway_execute_update");
        result.put("workflow_tools", List.of(WorkflowToolDescriptors.APPLY_TOOL_NAME, WorkflowToolDescriptors.VALIDATE_TOOL_NAME));
        result.put("side_effect_rule", "Preview first and continue only when the requested side effect is still intended.");
        result.put("completion_rule", "Use local completion hints on fields, prompts, and resources before guessing identifiers.");
        return result;
    }
    
    Map<String, Object> createFieldNamingContract() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("official_discovery_methods", List.of("tools/list", "resources/list", "resources/templates/list", "prompts/list"));
        result.put("argument_completion_method", ARGUMENT_COMPLETION_METHOD);
        result.put("catalog_fields", List.of("supportedResources", "supportedTools", "supportedStatementClasses", "guidanceResource", "resourceTemplates", "completionTargets",
                "resourceNavigation", "protocolAvailability"));
        result.put("payload_fields", "ShardingSphere-owned structured payload fields use snake_case.");
        result.put("descriptor_fields", "Descriptor-derived MCP schema fields keep the protocol or JSON Schema field names required by MCP clients.");
        return result;
    }
    
    List<Map<String, Object>> createNextActionContract() {
        return List.of(
                createNextActionContractEntry("resource_read", List.of("order", "type", "title", "resource_uri"),
                        List.of("reason", "depends_on"), "Read an MCP resource before choosing another call."),
                createNextActionContractEntry("tool_call", List.of("order", "type", "title", "tool_name", "arguments"),
                        List.of("reason", "depends_on"), "Call another MCP tool with server-suggested arguments."),
                createNextActionContractEntry("completion",
                        List.of("order", "type", "title", "ref", "argument"),
                        List.of("context", "missing_context_arguments", "resume_ref", "resume_arguments", "reason", "depends_on"),
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
                createCommonFlow("validate_runtime_database", List.of("read_resource shardingsphere://databases", "call_tool database_gateway_validate_runtime_database"),
                        "Stop after the configured runtime database reports ready or returns structured recovery guidance.",
                        List.of(PREFLIGHT_TOOL_NAME), List.of("shardingsphere://databases")),
                createCommonFlow("read_only_sql", List.of("read_resource shardingsphere://databases/{database}/capabilities", "call_tool database_gateway_execute_query"),
                        "Use one SELECT or EXPLAIN ANALYZE statement and stop after the result is reported.",
                        List.of("database_gateway_execute_query"), List.of("shardingsphere://databases/{database}/capabilities")),
                createCommonFlow("side_effecting_sql", List.of("call_tool database_gateway_execute_update execution_mode=preview",
                        "call_tool database_gateway_execute_update execution_mode=execute"),
                        "Execute only after reviewing the previewed SQL and side-effect scope.", List.of("database_gateway_execute_update"), List.of()),
                createCommonFlow("complete_uncertain_argument", List.of("resources/templates/list", "call completion/complete for one argument",
                        "follow completion meta.next_actions when diagnostic is missing_context, prefix_filtered_all_candidates, or no_candidates"),
                        "Stop when a completion value is selected or the nearest resource proves the argument is unavailable.", List.of(), List.of()),
                createCommonFlow("workflow_plan_apply_validate", List.of("call_tool descriptor-backed feature planning tool", "call_tool database_gateway_apply_workflow execution_mode=preview",
                        "call_tool database_gateway_apply_workflow execution_mode=review-then-execute approved_steps=<preview_artifacts.approval_step>",
                        "call_tool database_gateway_validate_workflow"),
                        "Reuse the same current-session plan_id and stop after validation succeeds.",
                        List.of(WorkflowToolDescriptors.APPLY_TOOL_NAME, WorkflowToolDescriptors.VALIDATE_TOOL_NAME), List.of()),
                createCommonFlow("recover_from_error", List.of("follow recovery.next_actions", "prefer official list discovery methods when surface information is needed",
                        "read_resource shardingsphere://guidance only when the server suggests the guidance resource", "ask_user only when uncertain"),
                        "Do not invent replacement calls while structured recovery actions are present.", List.of(), List.of(GUIDANCE_RESOURCE_URI)));
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
    
    private Map<String, Object> createPreflightRule() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("tool", PREFLIGHT_TOOL_NAME);
        result.put("input_rule", "Pass only a configured runtime database name.");
        result.put("secret_rule", "Connection details stay in administrator runtime configuration and are not tool arguments.");
        return result;
    }
    
    private Map<String, Object> createSqlToolSelection() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        Map<String, Object> readOnly = new LinkedHashMap<>(2, 1F);
        readOnly.put("tool", "database_gateway_execute_query");
        readOnly.put("statement_rule", "Use for one SELECT or EXPLAIN ANALYZE statement.");
        result.put("read_only", readOnly);
        Map<String, Object> sideEffecting = new LinkedHashMap<>(4, 1F);
        sideEffecting.put("tool", "database_gateway_execute_update");
        sideEffecting.put("first_mode", "preview");
        sideEffecting.put("execute_requires", "execution_mode=execute");
        sideEffecting.put("rule_change_preference",
                "For natural-language rule changes, use the matching database_gateway_plan_* workflow tool before raw SQL execution; "
                        + "omit plan_id for a new plan.");
        result.put("side_effecting", sideEffecting);
        return result;
    }
    
    private Map<String, Object> createWorkflowRule() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("selection_rule",
                "For natural-language rule changes, use the matching database_gateway_plan_* workflow tool before raw side-effect SQL; "
                        + "omit plan_id for a new plan and reuse only returned plan_id values.");
        result.put("planning_tools", catalog.getProtocolDescriptors().getToolDescriptors().stream()
                .map(MCPToolDescriptor::getName).filter(each -> each.startsWith(PLANNING_TOOL_NAME_PREFIX)).toList());
        Map<String, Object> previewTool = new LinkedHashMap<>(2, 1F);
        previewTool.put("tool", WorkflowToolDescriptors.APPLY_TOOL_NAME);
        previewTool.put(MCPPayloadFieldNames.EXECUTION_MODE, "preview");
        result.put("preview_tool", previewTool);
        Map<String, Object> executeTool = new LinkedHashMap<>(3, 1F);
        executeTool.put("tool", WorkflowToolDescriptors.APPLY_TOOL_NAME);
        executeTool.put(MCPPayloadFieldNames.EXECUTION_MODE, "review-then-execute");
        executeTool.put("execute_requires", "execution_mode=review-then-execute and explicit approved_steps copied from preview_artifacts.approval_step");
        result.put("execute_tool", executeTool);
        result.put("validate_tool", WorkflowToolDescriptors.VALIDATE_TOOL_NAME);
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
