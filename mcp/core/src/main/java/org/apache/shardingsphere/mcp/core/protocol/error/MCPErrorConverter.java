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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPNotFoundException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.api.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * MCP error converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPErrorConverter {
    
    private static final String INVALID_REQUEST = "invalid_request";
    
    private static final String NOT_FOUND = "not_found";
    
    private static final String UNSUPPORTED = "unsupported";
    
    private static final String TIMEOUT = "timeout";
    
    private static final String TRANSACTION_STATE_ERROR = "transaction_state_error";
    
    private static final String QUERY_FAILED = "query_failed";
    
    private static final String UNAVAILABLE = "unavailable";
    
    /**
     * Convert throwable to MCP error.
     *
     * @param cause throwable
     * @return MCP error
     */
    public static MCPErrorResponse convert(final Throwable cause) {
        if (cause instanceof UnsupportedToolException) {
            return createError(INVALID_REQUEST, cause, "Unsupported tool.");
        }
        if (cause instanceof UnsupportedResourceUriException) {
            return createError(INVALID_REQUEST, cause, "Unsupported resource URI.");
        }
        if (cause instanceof MCPInvalidRequestException) {
            return createError(INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof MCPNotFoundException) {
            return createError(NOT_FOUND, cause, "MCP operation not found.");
        }
        if (cause instanceof MCPUnsupportedException) {
            return createError(UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof MCPTimeoutException) {
            return createError(TIMEOUT, cause, "MCP operation timeout.");
        }
        if (cause instanceof MCPTransactionStateException) {
            return createError(TRANSACTION_STATE_ERROR, cause, "MCP transaction operation failed.");
        }
        if (cause instanceof MCPQueryFailedException) {
            return createError(QUERY_FAILED, cause, "MCP query failed.");
        }
        if (cause instanceof MCPUnavailableException) {
            return createError(UNAVAILABLE, cause, "Service is temporarily unavailable.");
        }
        if (cause instanceof SQLSyntaxErrorException) {
            return createError(INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof SQLTimeoutException) {
            return createError(TIMEOUT, cause, "MCP operation timeout.");
        }
        if (cause instanceof SQLFeatureNotSupportedException) {
            return createError(UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof UnsupportedOperationException) {
            return createError(UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof SQLException) {
            return createError(QUERY_FAILED, cause, "MCP query failed.");
        }
        if (cause instanceof IllegalArgumentException) {
            return createError(INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof IllegalStateException) {
            return createError(TRANSACTION_STATE_ERROR, cause, "MCP transaction operation failed.");
        }
        return createError(UNAVAILABLE, cause, "Service is temporarily unavailable.");
    }
    
    private static MCPErrorResponse createError(final String errorCode, final Throwable cause, final String defaultMessage) {
        String message = Objects.toString(cause.getMessage(), defaultMessage).trim();
        return new MCPErrorResponse(errorCode, message, createRecovery(errorCode, cause, message));
    }
    
    private static Map<String, Object> createRecovery(final String errorCode, final Throwable cause, final String message) {
        if (cause instanceof UnsupportedToolException) {
            return createUnsupportedToolRecovery(((UnsupportedToolException) cause).getToolName());
        }
        if (cause instanceof UnsupportedResourceUriException) {
            return createUnsupportedResourceRecovery(((UnsupportedResourceUriException) cause).getResourceUri());
        }
        if (UNSUPPORTED.equals(errorCode) && message.startsWith("execute_query only supports read-only")) {
            return createUnsafeQueryRecovery();
        }
        if (UNSUPPORTED.equals(errorCode) && message.startsWith("execute_update does not accept read-only SQL")) {
            return createReadOnlyUpdateRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && "execution_mode is required.".equals(message)) {
            return createMissingExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("execution_mode must be either")) {
            return createInvalidUpdateExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("execution_mode must be one of")) {
            return createInvalidWorkflowExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.endsWith(" is required.")) {
            return createMissingArgumentRecovery(message.substring(0, message.length() - " is required.".length()));
        }
        if (INVALID_REQUEST.equals(errorCode) && "Only one SQL statement is allowed.".equals(message)) {
            return createMultipleStatementsRecovery();
        }
        if ((INVALID_REQUEST.equals(errorCode) || UNSUPPORTED.equals(errorCode)) && message.contains("Statement is not supported")) {
            return createUnsupportedStatementRecovery();
        }
        if (UNSUPPORTED.equals(errorCode) && message.contains("Statement is banned")) {
            return createBannedStatementRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("Unsupported object_types value")) {
            return createInvalidObjectTypesRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && (message.startsWith("Unknown plan_id") || message.startsWith("Unknown or unavailable plan_id")
                || message.startsWith("Workflow kind is required"))) {
            return createWorkflowStateRecovery();
        }
        return Map.of();
    }
    
    private static Map<String, Object> createUnsupportedToolRecovery(final String toolName) {
        Map<String, Object> result = createBaseRecovery("unsupported_tool", "Call one of the supported tools returned by tools/list.");
        result.put("tool_name", toolName);
        result.put("supported_tools", ToolHandlerRegistry.getSupportedTools());
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(createReadResourceAction("shardingsphere://capabilities", "Read current MCP capabilities before choosing another tool.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createUnsupportedResourceRecovery(final String resourceUri) {
        Map<String, Object> result = createBaseRecovery("unsupported_resource_uri", "Read one of the supported resources or templates returned by resources/list and resources/templates/list.");
        result.put("resource_uri", resourceUri);
        result.put("matching_resource_templates", ResourceHandlerRegistry.getSupportedResources());
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(createReadResourceAction("shardingsphere://capabilities", "Read current MCP capabilities before choosing another resource.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createUnsafeQueryRecovery() {
        Map<String, Object> result = createBaseRecovery("unsafe_sql_attempted", "Use execute_update only after user approval for side-effecting SQL.");
        result.put("suggested_next_tool", "execute_update");
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(createToolAction("execute_update", "Retry side-effecting SQL in preview mode before asking for approval.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createReadOnlyUpdateRecovery() {
        Map<String, Object> result = createBaseRecovery("read_only_sql_sent_to_update_tool", "Use execute_query for read-only SELECT or EXPLAIN ANALYZE statements.");
        result.put("suggested_next_tool", "execute_query");
        result.put("next_actions", List.of(createToolAction("execute_query", "Retry the read-only SQL with execute_query.", Map.of(), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createMissingExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("missing_execution_mode",
                "Retry the same side-effecting tool with execution_mode=preview, review the preview, then ask the user for approval.");
        result.put("missing_fields", List.of("execution_mode"));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(createRetryAction("Retry the same side-effecting tool in preview mode.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createInvalidUpdateExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value", "Retry with execution_mode=preview or execution_mode=execute.");
        result.put("field", "execution_mode");
        result.put("allowed_values", List.of("preview", "execute"));
        result.put("suggested_next_tool", "execute_update");
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(createToolAction("execute_update", "Retry execute_update with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createInvalidWorkflowExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value",
                "Retry apply_workflow with execution_mode=preview, review the preview, then use review-then-execute or manual-only after approval.");
        result.put("field", "execution_mode");
        result.put("allowed_values", List.of("preview", "review-then-execute", "manual-only"));
        result.put("suggested_next_tool", "apply_workflow");
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(createToolAction("apply_workflow", "Retry apply_workflow with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createMissingArgumentRecovery(final String argumentName) {
        Map<String, Object> result = createBaseRecovery("missing_argument", "Provide the missing argument, infer it from resources when safe, or ask the user.");
        result.put("missing_fields", List.of(argumentName));
        result.put("read_resources_first", createReadResourcesFirst(argumentName));
        result.put("next_actions", createMissingArgumentNextActions(argumentName));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static List<String> createReadResourcesFirst(final String argumentName) {
        if ("database".equals(argumentName)) {
            return List.of("shardingsphere://databases");
        }
        if ("plan_id".equals(argumentName)) {
            return List.of("shardingsphere://capabilities");
        }
        return List.of();
    }
    
    private static Map<String, Object> createInvalidObjectTypesRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value", "Retry with one or more allowed object_types values.");
        result.put("field", "object_types");
        result.put("allowed_values", Arrays.stream(SupportedMCPMetadataObjectType.values()).map(each -> each.name().toLowerCase(Locale.ENGLISH)).toList());
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createMultipleStatementsRecovery() {
        Map<String, Object> result = createBaseRecovery("multiple_sql_statements", "Split the user intent into separate MCP calls and handle each statement independently.");
        result.put("ask_user_when_uncertain", true);
        result.put("requires_user_approval", true);
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(createAskUserAction("Ask the user which single statement should be handled first.", List.of("single_sql_statement"), true)));
        return result;
    }
    
    private static Map<String, Object> createUnsupportedStatementRecovery() {
        Map<String, Object> result = createBaseRecovery("unsupported_sql_statement", "Ask the user for a supported SELECT, EXPLAIN ANALYZE, DML, DDL, DCL, transaction, or savepoint statement.");
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(createReadResourceAction("shardingsphere://capabilities", "Read supported statement classes before retrying.")));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createBannedStatementRecovery() {
        Map<String, Object> result = createBaseRecovery("banned_sql_statement", "Do not execute this SQL through MCP; ask the user for a safer supported operation.");
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(createAskUserAction("Ask for a safer supported operation instead of executing the banned SQL.", List.of("safe_sql_or_metadata_request"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static Map<String, Object> createWorkflowStateRecovery() {
        Map<String, Object> result = createBaseRecovery("workflow_state_error", "Use the latest plan_id from a planning response, or re-run the planning tool.");
        result.put("suggested_next_tools", List.of("plan_encrypt_rule", "plan_mask_rule"));
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(createReadResourceAction("shardingsphere://capabilities", "Read current workflow tools, then re-run the matching planning tool in this session.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createBaseRecovery(final String category, final String modelAction) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("recoverable", true);
        result.put("category", category);
        result.put("model_action", modelAction);
        return result;
    }
    
    private static List<Map<String, Object>> createMissingArgumentNextActions(final String argumentName) {
        List<String> resources = createReadResourcesFirst(argumentName);
        if (resources.isEmpty()) {
            return List.of(createAskUserAction("Ask the user for the missing argument.", List.of(argumentName), false));
        }
        return List.of(createReadResourceAction(resources.iterator().next(), "Read a safe resource before retrying with the missing argument."));
    }
    
    private static Map<String, Object> createReadResourceAction(final String resourceUri, final String reason) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("action_kind", "read_resource");
        result.put("target_resource", resourceUri);
        result.put("reason", reason);
        result.put("requires_user_approval", false);
        return result;
    }
    
    private static Map<String, Object> createRetryAction(final String reason, final Map<String, Object> requiredArguments, final boolean requiresUserApproval) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("action_kind", "retry_tool");
        result.put("reason", reason);
        result.put("required_arguments", requiredArguments);
        result.put("requires_user_approval", requiresUserApproval);
        return result;
    }
    
    private static Map<String, Object> createToolAction(final String targetTool, final String reason, final Map<String, Object> requiredArguments, final boolean requiresUserApproval) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("action_kind", "call_tool");
        result.put("target_tool", targetTool);
        result.put("reason", reason);
        result.put("required_arguments", requiredArguments);
        result.put("requires_user_approval", requiresUserApproval);
        return result;
    }
    
    private static Map<String, Object> createAskUserAction(final String reason, final List<String> requiredInputs, final boolean requiresUserApproval) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("action_kind", "ask_user");
        result.put("reason", reason);
        result.put("required_inputs", requiredInputs);
        result.put("requires_user_approval", requiresUserApproval);
        return result;
    }
}
