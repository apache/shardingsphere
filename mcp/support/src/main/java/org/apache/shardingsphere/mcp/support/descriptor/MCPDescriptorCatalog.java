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

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceParameterDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * MCP descriptor catalog.
 */
@Getter
public final class MCPDescriptorCatalog {
    
    private final Collection<MCPResourceDescriptor> resourceDescriptors;
    
    private final Collection<MCPToolDescriptor> toolDescriptors;
    
    private final Collection<MCPPromptDescriptor> promptDescriptors;
    
    private final Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors;
    
    private final Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors;
    
    public MCPDescriptorCatalog(final Collection<MCPResourceDescriptor> resourceDescriptors, final Collection<MCPToolDescriptor> toolDescriptors,
                                final Collection<MCPPromptDescriptor> promptDescriptors, final Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors,
                                final Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors) {
        this.resourceDescriptors = null == resourceDescriptors ? Collections.emptyList() : resourceDescriptors;
        this.toolDescriptors = null == toolDescriptors ? Collections.emptyList() : toolDescriptors;
        this.promptDescriptors = null == promptDescriptors ? Collections.emptyList() : promptDescriptors;
        this.completionTargetDescriptors = null == completionTargetDescriptors ? Collections.emptyList() : completionTargetDescriptors;
        this.resourceNavigationDescriptors = null == resourceNavigationDescriptors ? Collections.emptyList() : resourceNavigationDescriptors;
    }
    
    /**
     * Convert descriptors to model-facing capability payload.
     *
     * @param supportedResources supported resource URI templates
     * @param supportedTools supported tool names
     * @param supportedStatements supported statement classes
     * @return capability payload
     */
    public Map<String, Object> toPayload(final Collection<String> supportedResources, final Collection<String> supportedTools, final Collection<?> supportedStatements) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        List<Map<String, Object>> resources = resourceDescriptors.stream().filter(each -> !each.isTemplated()).map(this::toResourcePayload).toList();
        List<Map<String, Object>> resourceTemplates = resourceDescriptors.stream().filter(MCPResourceDescriptor::isTemplated).map(this::toResourcePayload).toList();
        List<Map<String, Object>> tools = toolDescriptors.stream().map(this::toToolPayload).toList();
        List<Map<String, Object>> prompts = promptDescriptors.stream().map(this::toPromptPayload).toList();
        List<Map<String, Object>> completionTargets = completionTargetDescriptors.stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = resourceNavigationDescriptors.stream().map(this::toResourceNavigationPayload).toList();
        result.put("response_mode", MCPResponseMode.CATALOG);
        result.put("supportedResources", supportedResources);
        result.put("supportedTools", supportedTools);
        result.put("supportedStatementClasses", supportedStatements);
        result.put("model_contract", createModelContract());
        result.put("surface_summary", createSurfaceSummary());
        result.put("field_naming_contract", createFieldNamingContract());
        result.put("next_action_contract", createNextActionContract());
        result.put("common_flows", createCommonFlows());
        result.put("security_hints", createSecurityHints());
        result.put("resources", resources);
        result.put("resourceTemplates", resourceTemplates);
        result.put("tools", tools);
        result.put("prompts", prompts);
        result.put("completionTargets", completionTargets);
        result.put("resourceNavigation", resourceNavigation);
        result.put("protocolAvailability", createProtocolAvailability(!resourceNavigation.isEmpty()));
        result.put("fingerprints", createFingerprints(resources, resourceTemplates, tools, prompts, completionTargets, resourceNavigation));
        return result;
    }
    
    /**
     * Get descriptor catalog fingerprint.
     *
     * @return descriptor catalog fingerprint
     */
    public String getDescriptorCatalogFingerprint() {
        List<Map<String, Object>> resources = resourceDescriptors.stream().filter(each -> !each.isTemplated()).map(this::toResourcePayload).toList();
        List<Map<String, Object>> resourceTemplates = resourceDescriptors.stream().filter(MCPResourceDescriptor::isTemplated).map(this::toResourcePayload).toList();
        List<Map<String, Object>> tools = toolDescriptors.stream().map(this::toToolPayload).toList();
        List<Map<String, Object>> prompts = promptDescriptors.stream().map(this::toPromptPayload).toList();
        List<Map<String, Object>> completionTargets = completionTargetDescriptors.stream().map(this::toCompletionTargetPayload).toList();
        List<Map<String, Object>> resourceNavigation = resourceNavigationDescriptors.stream().map(this::toResourceNavigationPayload).toList();
        return String.valueOf(createFingerprints(resources, resourceTemplates, tools, prompts, completionTargets, resourceNavigation).get("descriptorCatalog"));
    }
    
    private List<Map<String, Object>> createNextActionContract() {
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
    
    private Map<String, Object> createNextActionContractEntry(final String actionType, final List<String> requiredFields, final List<String> optionalFields, final String description) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", actionType);
        result.put("required_fields", requiredFields);
        result.put("optional_fields", optionalFields);
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createSurfaceSummary() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("first_resource", "shardingsphere://capabilities");
        result.put("metadata_resource", "shardingsphere://databases");
        result.put("metadata_search_tool", "search_metadata");
        result.put("read_only_sql_tool", "execute_query");
        result.put("side_effect_sql_tool", "execute_update");
        result.put("workflow_tools", List.of("apply_workflow", "validate_workflow"));
        result.put("approval_rule", "Preview first and continue only after explicit user approval when requires_user_approval is true.");
        result.put("completion_rule", "Use local completion hints on fields, prompts, and resources before guessing identifiers.");
        return result;
    }
    
    private Map<String, Object> createFieldNamingContract() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("protocol_fields", List.of("supportedResources", "supportedTools", "resourceTemplates", "completionTargets", "resourceNavigation", "protocolAvailability"));
        result.put("payload_fields", "ShardingSphere-owned structured payload fields use snake_case.");
        result.put("descriptor_fields", "Descriptor-derived MCP schema fields keep the protocol or JSON Schema field names required by MCP clients.");
        result.put("alias_rule", "Do not assume camelCase and snake_case variants are aliases unless the same descriptor explicitly documents both.");
        result.put("cleanup_rule", "Prefer one canonical ShardingSphere payload field over parallel compatibility aliases.");
        return result;
    }
    
    private List<Map<String, Object>> createCommonFlows() {
        return List.of(
                createCommonFlow("inspect_metadata", List.of("read_resource shardingsphere://capabilities", "read_resource shardingsphere://databases",
                        "call_tool search_metadata", "read_resource returned resource.uri"), "Stop when the requested metadata detail resource is read.",
                        List.of("search_metadata"), List.of("shardingsphere://capabilities", "shardingsphere://databases")),
                createCommonFlow("read_only_sql", List.of("read_resource shardingsphere://databases/{database}/capabilities", "call_tool execute_query"),
                        "Use one SELECT or EXPLAIN ANALYZE statement and stop after the result is reported.",
                        List.of("execute_query"), List.of("shardingsphere://databases/{database}/capabilities")),
                createCommonFlow("side_effecting_sql", List.of("call_tool execute_update execution_mode=preview", "ask_user approval", "call_tool execute_update execution_mode=execute"),
                        "Never execute until the previewed SQL and side-effect scope are approved by the user.", List.of("execute_update"), List.of()),
                createCommonFlow("workflow_plan_apply_validate", List.of("call_tool descriptor-backed feature planning tool", "call_tool apply_workflow execution_mode=preview",
                        "ask_user approval", "call_tool apply_workflow approved mode", "call_tool validate_workflow"),
                        "Reuse the same current-session plan_id and stop after validation succeeds.",
                        List.of("apply_workflow", "validate_workflow"), List.of()),
                createCommonFlow("recover_from_error", List.of("follow recovery.next_actions", "read_resource shardingsphere://capabilities when suggested", "ask_user only when uncertain"),
                        "Do not invent replacement calls while structured recovery actions are present.", List.of(), List.of("shardingsphere://capabilities")));
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
    
    private Map<String, Object> toResourcePayload(final MCPResourceDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(14, 1F);
        result.put(descriptor.isTemplated() ? "uriTemplate" : "uri", descriptor.getUriTemplate());
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("mimeType", descriptor.getMimeType());
        result.put("parameters", descriptor.getParameters().stream().map(this::toParameterPayload).toList());
        putIfPresent(result, "resourceKind", descriptor.getResourceKind());
        putIfPresent(result, "objectScope", descriptor.getObjectScope());
        putIfPresent(result, "feature", descriptor.getFeature());
        putIfNotEmpty(result, "relatedTools", descriptor.getRelatedTools());
        putIfNotEmpty(result, "relatedResources", descriptor.getRelatedResources());
        putIfNotEmpty(result, "useBefore", descriptor.getUseBefore());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toResourceAnnotationsPayload(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        Map<String, Object> payloadContract = createResourcePayloadContract(descriptor);
        if (!payloadContract.isEmpty()) {
            result.put("payload_contract", payloadContract);
        }
        return result;
    }
    
    private Map<String, Object> createModelContract() {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("public_surface_source", "shardingsphere://capabilities");
        result.put("safe_first_resource", "shardingsphere://capabilities");
        result.put("metadata_first_resource", "shardingsphere://databases");
        result.put("sql_tool_selection", Map.of(
                "read_only", "Use execute_query for one SELECT or EXPLAIN ANALYZE statement.",
                "side_effecting", "Use execute_update with execution_mode=preview before asking for user approval."));
        result.put("workflow_session_rule", "Reuse the current-session plan_id returned by a planning tool; re-plan when the plan is unavailable.");
        result.put("side_effect_rule", "Preview before side effects and continue only after explicit user approval.");
        result.put("next_action_rule", "Use canonical next_actions fields: type, tool_name, resource_uri, arguments, and requires_user_approval.");
        result.put("detail_resource_rule", "Read each resource payload_contract before assuming detail fields.");
        result.put("recovery_rule", "When a call fails with recovery.next_actions, follow those structured actions before inventing a new call.");
        return result;
    }
    
    private Map<String, Object> createSecurityHints() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("http_access_token", "HTTP transport may require an Authorization bearer token; capabilities never exposes secrets.");
        result.put("remote_access", "Prefer loopback access unless the operator explicitly configures remote exposure.");
        result.put("stdio_stdout", "STDIO transport must keep MCP protocol frames on stdout and send logs to stderr or files.");
        return result;
    }
    
    private Map<String, Object> createResourcePayloadContract(final MCPResourceDescriptor descriptor) {
        Object resourceKind = descriptor.getResourceKind();
        if ("list".equals(resourceKind)) {
            return createListResourcePayloadContract(descriptor);
        }
        if ("detail".equals(resourceKind)) {
            return createDetailResourcePayloadContract(descriptor);
        }
        if ("capability-catalog".equals(resourceKind)) {
            return createCapabilityResourcePayloadContract();
        }
        return Map.of();
    }
    
    private Map<String, Object> createListResourcePayloadContract(final MCPResourceDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_kind", "list");
        result.put("item_scope", createObjectScope(descriptor));
        result.put("stable_fields", List.of("response_mode", "items", "count", "has_more", "continuation_mode", "total_count", "returned_count", "truncated"));
        result.put("optional_fields", List.of("next_page_token", "self_uri", "parent_resource", "next_resources", "empty_state", "large_result_guidance", "next_actions"));
        result.put("empty_state", Map.of("items", List.of(), "count", 0, "has_more", false));
        result.put("continuation", "continuation_mode is none, pagination, or search_metadata. next_page_token is present only for pagination.");
        return result;
    }
    
    private Map<String, Object> createDetailResourcePayloadContract(final MCPResourceDescriptor descriptor) {
        if ("runtime-status".equals(descriptor.getName())) {
            return createRuntimeStatusResourcePayloadContract();
        }
        if ("database-capabilities".equals(descriptor.getName())) {
            return createDatabaseCapabilityResourcePayloadContract();
        }
        if (null == descriptor.getObjectScope()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("response_kind", "detail");
        result.put("item_scope", createObjectScope(descriptor));
        result.put("stable_fields", List.of("response_mode", "resource_kind", "found", "items", "count"));
        result.put("optional_fields", List.of("item", "self_uri", "parent_resource", "next_resources"));
        result.put("found_state", Map.of("found", true, "count", 1, "item", "single object payload"));
        result.put("not_found_state", Map.of("found", false, "count", 0, "items", List.of()));
        result.put("pagination", "Detail resources are not paginated.");
        return result;
    }
    
    private Map<String, Object> createCapabilityResourcePayloadContract() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("response_kind", "capability-catalog");
        result.put("stable_fields", List.of("response_mode", "model_contract", "surface_summary", "field_naming_contract", "next_action_contract", "common_flows", "security_hints", "resources",
                "resourceTemplates", "tools", "prompts", "completionTargets", "resourceNavigation", "protocolAvailability"));
        result.put("model_first_hop", "Read model_contract before choosing metadata, SQL, or workflow calls.");
        result.put("fingerprints", "Use fingerprints to compare descriptor payload shape across calls.");
        return result;
    }
    
    private Map<String, Object> createDatabaseCapabilityResourcePayloadContract() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("response_kind", "detail-object");
        result.put("item_scope", "database-capability");
        result.put("stable_fields", List.of("response_mode", "database", "databaseType", "supportedObjectTypes", "supportedStatementClasses", "supportsTransactionControl", "supportsSavepoint",
                "defaultSchemaSemantics", "schemaExecutionSemantics", "supportsCrossSchemaSql", "supportsExplainAnalyze", "explainAnalyzeExecutionRisk"));
        result.put("not_found_behavior", "The resource returns a not_found error when the logical database capability is unavailable.");
        return result;
    }
    
    private Map<String, Object> createRuntimeStatusResourcePayloadContract() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("response_kind", "runtime-status");
        result.put("stable_fields", List.of("response_mode", "server_status", "status", "transport", "active_transport", "configured_database_count", "databases",
                "capability_fingerprint", "resources_to_read", "next_actions"));
        result.put("database_fields", List.of("database", "database_type", "driver_category", "schema_count", "metadata_visibility", "capability_visibility",
                "feature_visibility", "capabilities", "resource"));
        result.put("secret_rule", "Runtime status never exposes JDBC credentials, bearer tokens, raw environment variables, or stack traces.");
        return result;
    }
    
    private String createObjectScope(final MCPResourceDescriptor descriptor) {
        if (null != descriptor.getObjectScope()) {
            return descriptor.getObjectScope();
        }
        return Objects.toString(descriptor.getFeature(), "resource");
    }
    
    private Map<String, Object> toParameterPayload(final MCPResourceParameterDescriptor parameter) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("name", parameter.getName());
        result.put("title", parameter.getTitle());
        result.put("description", parameter.getDescription());
        result.put("required", parameter.isRequired());
        result.put("scope", parameter.getScope());
        Map<String, Object> completionHint = createCompletionHint(parameter.getName());
        if (!completionHint.isEmpty()) {
            result.put("completion", completionHint);
        }
        return result;
    }
    
    private Map<String, Object> toToolPayload(final MCPToolDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("inputFields", descriptor.getFields().stream().map(this::toFieldPayload).toList());
        result.put("outputSchema", descriptor.getOutputSchema());
        if (!descriptor.getAnnotations().isEmpty()) {
            result.put("annotations", toToolAnnotationsPayload(descriptor.getAnnotations()));
        }
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toPromptPayload(final MCPPromptDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("arguments", descriptor.getArguments().stream().map(this::toPromptArgumentPayload).toList());
        result.put("templateResource", descriptor.getTemplateResource());
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toPromptArgumentPayload(final MCPPromptArgumentDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("name", descriptor.getName());
        result.put("title", descriptor.getTitle());
        result.put("description", descriptor.getDescription());
        result.put("required", descriptor.isRequired());
        Map<String, Object> completionHint = createCompletionHint(descriptor.getName());
        if (!completionHint.isEmpty()) {
            result.put("completion", completionHint);
        }
        return result;
    }
    
    private Map<String, Object> toCompletionTargetPayload(final MCPCompletionTargetDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("arguments", descriptor.getArguments());
        result.put("maxValues", descriptor.getMaxValues());
        if (!descriptor.getMeta().isEmpty()) {
            result.put("meta", descriptor.getMeta());
        }
        return result;
    }
    
    private Map<String, Object> toResourceNavigationPayload(final MCPResourceNavigationDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1F);
        result.put("from", descriptor.getFrom());
        result.put("from_type", resolveReferenceType(descriptor.getFrom()));
        result.put("to", descriptor.getTo());
        result.put("to_type", resolveReferenceType(descriptor.getTo()));
        result.put("requiredArguments", descriptor.getRequiredArguments());
        result.put("carriedArguments", descriptor.getCarriedArguments());
        result.put("description", descriptor.getDescription());
        return result;
    }
    
    private Map<String, Object> toFieldPayload(final MCPToolFieldDefinition field) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("name", field.getName());
        result.put("required", field.isRequired());
        result.put("schema", field.getValueDefinition().toSchemaFragment());
        Map<String, Object> completionHint = createCompletionHint(field.getName());
        if (!completionHint.isEmpty()) {
            result.put("completion", completionHint);
        }
        return result;
    }
    
    private Map<String, Object> createCompletionHint(final String argumentName) {
        List<Map<String, Object>> references = completionTargetDescriptors.stream()
                .filter(each -> each.getArguments().contains(argumentName)).map(this::createCompletionReferenceHint).toList();
        if (references.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("available", true);
        result.put("references", references);
        List<String> requiredContextArguments = createCompletionRequiredContextArguments(argumentName);
        if (!requiredContextArguments.isEmpty()) {
            result.put("required_context_arguments", requiredContextArguments);
        }
        return result;
    }
    
    private Map<String, Object> createCompletionReferenceHint(final MCPCompletionTargetDescriptor descriptor) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("referenceType", descriptor.getReferenceType());
        result.put("reference", descriptor.getReference());
        result.put("maxValues", descriptor.getMaxValues());
        return result;
    }
    
    private List<String> createCompletionRequiredContextArguments(final String argumentName) {
        if ("schema".equals(argumentName)) {
            return List.of("database");
        }
        if ("table".equals(argumentName) || "sequence".equals(argumentName)) {
            return List.of("database", "schema");
        }
        if ("column".equals(argumentName) || "index".equals(argumentName)) {
            return List.of("database", "schema", "table");
        }
        return List.of();
    }
    
    private String resolveReferenceType(final String reference) {
        if (resourceDescriptors.stream().anyMatch(each -> each.getUriTemplate().equals(reference))) {
            return reference.contains("{") ? "resource_template" : "resource";
        }
        if (toolDescriptors.stream().anyMatch(each -> each.getName().equals(reference))) {
            return "tool";
        }
        if (promptDescriptors.stream().anyMatch(each -> each.getName().equals(reference))) {
            return "prompt";
        }
        return "unknown";
    }
    
    private Map<String, Object> toResourceAnnotationsPayload(final MCPResourceAnnotations annotations) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        if (!annotations.getAudience().isEmpty()) {
            result.put("audience", annotations.getAudience());
        }
        if (null != annotations.getPriority()) {
            result.put("priority", annotations.getPriority());
        }
        if (null != annotations.getLastModified() && !annotations.getLastModified().isBlank()) {
            result.put("lastModified", annotations.getLastModified());
        }
        return result;
    }
    
    private Map<String, Object> toToolAnnotationsPayload(final MCPToolAnnotations annotations) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        putIfPresent(result, "title", annotations.getTitle());
        putIfPresent(result, "readOnlyHint", annotations.getReadOnlyHint());
        putIfPresent(result, "destructiveHint", annotations.getDestructiveHint());
        putIfPresent(result, "idempotentHint", annotations.getIdempotentHint());
        putIfPresent(result, "openWorldHint", annotations.getOpenWorldHint());
        putIfPresent(result, "returnDirect", annotations.getReturnDirect());
        return result;
    }
    
    private void putIfPresent(final Map<String, Object> target, final String key, final Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }
    
    private void putIfNotEmpty(final Map<String, Object> target, final String key, final Collection<?> values) {
        if (!values.isEmpty()) {
            target.put(key, values);
        }
    }
    
    private Map<String, Object> createProtocolAvailability(final boolean hasResourceNavigation) {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("resources", true);
        result.put("resourceTemplates", true);
        result.put("tools", true);
        result.put("toolAnnotations", true);
        result.put("toolOutputSchemas", true);
        result.put("prompts", !promptDescriptors.isEmpty());
        result.put("completions", !completionTargetDescriptors.isEmpty());
        result.put("resourceNavigation", hasResourceNavigation);
        return result;
    }
    
    private Map<String, Object> createFingerprints(final List<Map<String, Object>> resources, final List<Map<String, Object>> resourceTemplates,
                                                   final List<Map<String, Object>> tools, final List<Map<String, Object>> prompts,
                                                   final List<Map<String, Object>> completionTargets, final List<Map<String, Object>> resourceNavigation) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("algorithm", "sha256");
        result.put("descriptorCatalog", createFingerprint(Map.of(
                "resources", resources,
                "resourceTemplates", resourceTemplates,
                "tools", tools,
                "prompts", prompts,
                "completionTargets", completionTargets,
                "resourceNavigation", resourceNavigation)));
        result.put("promptSet", createFingerprint(prompts));
        result.put("resourceNavigation", createFingerprint(resourceNavigation));
        result.put("modelFacingSchemas", createFingerprint(createModelFacingSchemas(resources, resourceTemplates, tools)));
        return result;
    }
    
    private List<Map<String, Object>> createModelFacingSchemas(final List<Map<String, Object>> resources, final List<Map<String, Object>> resourceTemplates,
                                                               final List<Map<String, Object>> tools) {
        List<Map<String, Object>> result = new LinkedList<>();
        resources.stream().map(this::createResourceSchema).forEach(result::add);
        resourceTemplates.stream().map(this::createResourceSchema).forEach(result::add);
        tools.stream().map(this::createToolSchema).forEach(result::add);
        return result;
    }
    
    private Map<String, Object> createResourceSchema(final Map<String, Object> resource) {
        return resource.containsKey("uriTemplate")
                ? Map.of("uriTemplate", resource.get("uriTemplate"), "parameters", resource.get("parameters"))
                : Map.of("uri", resource.get("uri"), "parameters", resource.get("parameters"));
    }
    
    private Map<String, Object> createToolSchema(final Map<String, Object> tool) {
        return Map.of("name", tool.get("name"), "inputFields", tool.get("inputFields"), "outputSchema", tool.get("outputSchema"));
    }
    
    private String createFingerprint(final Object value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonicalize(value).getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
    
    private String toHex(final byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte each : bytes) {
            int value = each & 0xFF;
            if (value < 16) {
                result.append('0');
            }
            result.append(Integer.toHexString(value));
        }
        return result.toString();
    }
    
    private String canonicalize(final Object value) {
        if (null == value) {
            return "null";
        }
        if (value instanceof Map) {
            return canonicalizeMap((Map<?, ?>) value);
        }
        if (value instanceof Collection) {
            return canonicalizeCollection((Collection<?>) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }
    
    private String canonicalizeMap(final Map<?, ?> value) {
        StringBuilder result = new StringBuilder("{");
        List<? extends Entry<?, ?>> entries = value.entrySet().stream()
                .sorted(Comparator.comparing(each -> String.valueOf(each.getKey()))).toList();
        for (int index = 0; index < entries.size(); index++) {
            if (0 < index) {
                result.append(',');
            }
            Entry<?, ?> entry = entries.get(index);
            result.append(canonicalize(String.valueOf(entry.getKey()))).append(':').append(canonicalize(entry.getValue()));
        }
        return result.append('}').toString();
    }
    
    private String canonicalizeCollection(final Collection<?> value) {
        StringBuilder result = new StringBuilder("[");
        int index = 0;
        for (Object each : value) {
            if (0 < index++) {
                result.append(',');
            }
            result.append(canonicalize(each));
        }
        return result.append(']').toString();
    }
    
    private String escape(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
