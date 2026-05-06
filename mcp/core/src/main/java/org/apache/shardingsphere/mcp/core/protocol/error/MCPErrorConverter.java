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
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;

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
        if (cause instanceof RuntimeDatabaseConnectionException) {
            return createError(UNAVAILABLE, cause, "Runtime database connection failed.");
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
        if (cause instanceof SQLToolMismatchException) {
            return createSQLToolMismatchRecovery((SQLToolMismatchException) cause);
        }
        if (cause instanceof MetadataIntrospectionSQLStatementException) {
            return createMetadataIntrospectionSQLRecovery((MetadataIntrospectionSQLStatementException) cause);
        }
        if (cause instanceof RuntimeDatabaseConnectionException) {
            return createRuntimeDatabaseConnectionRecovery((RuntimeDatabaseConnectionException) cause);
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
        if (INVALID_REQUEST.equals(errorCode) && "apply_workflow execution_mode is required.".equals(message)) {
            return createMissingWorkflowExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("execution_mode must be either")) {
            return createInvalidUpdateExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("execution_mode must be one of")) {
            return createInvalidWorkflowExecutionModeRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("approved_steps must contain only")) {
            return createInvalidApprovedStepsRecovery();
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
        if (INVALID_REQUEST.equals(errorCode) && "Invalid page token.".equals(message)) {
            return createInvalidPageTokenRecovery();
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("page_size must be an integer")) {
            return createInvalidIntegerArgumentRecovery("page_size", 100, "search_metadata");
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("max_rows must be an integer")) {
            return createInvalidIntegerArgumentRecovery("max_rows", 100, "");
        }
        if (INVALID_REQUEST.equals(errorCode) && message.startsWith("timeout_ms must be an integer")) {
            return createInvalidIntegerArgumentRecovery("timeout_ms", 0, "");
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
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP capabilities before choosing another tool.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createUnsupportedResourceRecovery(final String resourceUri) {
        Map<String, Object> result = createBaseRecovery("unsupported_resource_uri", "Read one of the supported resources or templates returned by resources/list and resources/templates/list.");
        result.put("resource_uri", resourceUri);
        result.put("matching_resource_templates", ResourceHandlerRegistry.getSupportedResources());
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP capabilities before choosing another resource.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createSQLToolMismatchRecovery(final SQLToolMismatchException cause) {
        boolean requiresUserApproval = "execute_update".equals(cause.getTargetTool());
        Map<String, Object> result = createBaseRecovery(createSQLToolMismatchCategory(cause),
                requiresUserApproval ? "Use execute_update in preview mode, then ask for approval before execution." : "Use execute_query for this read-only SQL.");
        result.put("source_tool", cause.getSourceTool());
        result.put("statement_class", cause.getClassificationResult().getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", cause.getClassificationResult().getStatementType());
        result.put("normalized_sql", cause.getClassificationResult().getNormalizedSql());
        cause.getClassificationResult().getTargetObjectName().ifPresent(optional -> result.put("target_object", optional));
        cause.getClassificationResult().getSavepointName().ifPresent(optional -> result.put("savepoint", optional));
        result.put("suggested_arguments", cause.getSuggestedArguments());
        result.put("next_actions", List.of(MCPNextActionUtils.callTool(cause.getTargetTool(), createSQLToolMismatchActionReason(cause), cause.getSuggestedArguments(), requiresUserApproval)));
        result.put("requires_user_approval", requiresUserApproval);
        result.put("ask_user_when_uncertain", requiresUserApproval);
        return result;
    }

    private static Map<String, Object> createMetadataIntrospectionSQLRecovery(final MetadataIntrospectionSQLStatementException cause) {
        Map<String, Object> result = createBaseRecovery("metadata_introspection_sql",
                "Use logical metadata resources or search_metadata instead of console-style metadata SQL.");
        result.put("statement_type", cause.getStatementType());
        result.put("read_resources_first", List.of("shardingsphere://databases"));
        result.put("suggested_arguments", Map.of("page_size", 100));
        result.put("next_actions", MCPNextActionUtils.ordered(
                MCPNextActionUtils.readResource("shardingsphere://databases", "Read logical databases before choosing a metadata scope."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.callTool("search_metadata",
                        "Search metadata with an explicit database, schema, query, or object_types scope instead of executing metadata SQL.", Map.of("page_size", 100), false), 1)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static String createSQLToolMismatchCategory(final SQLToolMismatchException cause) {
        return "execute_update".equals(cause.getTargetTool()) ? "unsafe_sql_attempted" : "read_only_sql_sent_to_update_tool";
    }

    private static String createSQLToolMismatchActionReason(final SQLToolMismatchException cause) {
        return "execute_update".equals(cause.getTargetTool())
                ? "Retry side-effecting SQL in preview mode with the normalized SQL and preserved context."
                : "Retry the read-only SQL with execute_query using the normalized SQL and preserved context.";
    }

    private static Map<String, Object> createUnsafeQueryRecovery() {
        Map<String, Object> result = createBaseRecovery("unsafe_sql_attempted", "Use execute_update only after user approval for side-effecting SQL.");
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions",
                List.of(MCPNextActionUtils.callTool("execute_update", "Retry side-effecting SQL in preview mode before asking for approval.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createReadOnlyUpdateRecovery() {
        Map<String, Object> result = createBaseRecovery("read_only_sql_sent_to_update_tool", "Use execute_query for read-only SELECT or EXPLAIN ANALYZE statements.");
        result.put("next_actions", List.of(MCPNextActionUtils.callTool("execute_query", "Retry the read-only SQL with execute_query.", Map.of(), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createMissingExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("missing_execution_mode",
                "Retry the same side-effecting tool with execution_mode=preview, review the preview, then ask the user for approval.");
        result.put("missing_fields", List.of("execution_mode"));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("execute_update", "Retry the same side-effecting tool in preview mode.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createMissingWorkflowExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("missing_execution_mode",
                "Retry apply_workflow with execution_mode=preview, review the preview, then use review-then-execute or manual-only after approval.");
        result.put("missing_fields", List.of("execution_mode"));
        result.put("field", "execution_mode");
        result.put("source_tool", "apply_workflow");
        result.put("target_tool", "apply_workflow");
        result.put("allowed_values", List.of("preview", "review-then-execute", "manual-only"));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("apply_workflow", "Retry apply_workflow with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidUpdateExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value", "Retry with execution_mode=preview or execution_mode=execute.");
        result.put("field", "execution_mode");
        result.put("allowed_values", List.of("preview", "execute"));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool("execute_update", "Retry execute_update with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidWorkflowExecutionModeRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value",
                "Retry apply_workflow with execution_mode=preview, review the preview, then use review-then-execute or manual-only after approval.");
        result.put("field", "execution_mode");
        result.put("allowed_values", List.of("preview", "review-then-execute", "manual-only"));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool("apply_workflow", "Retry apply_workflow with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidApprovedStepsRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value",
                "Retry apply_workflow with approved_steps copied from preview_artifacts.approval_step, or omit approved_steps to apply every artifact.");
        result.put("field", "approved_steps");
        result.put("allowed_values", List.of(WorkflowArtifactPayloadUtils.STEP_DDL, WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL));
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool("apply_workflow", "Preview again, then copy only visible approval_step values into approved_steps after user approval.",
                Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createMissingArgumentRecovery(final String argumentName) {
        boolean missingDatabase = "database".equals(argumentName);
        String category = missingDatabase ? "missing_database" : "missing_argument";
        String modelAction = missingDatabase
                ? "Read shardingsphere://databases, infer a single safe database when possible, or ask the user."
                : "Provide the missing argument, infer it from resources when safe, or ask the user.";
        Map<String, Object> result = createBaseRecovery(category, modelAction);
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
        List<String> allowedObjectTypes = getAllowedObjectTypes();
        result.put("field", "object_types");
        result.put("allowed_values", allowedObjectTypes);
        result.put("suggested_arguments", Map.of("object_types", allowedObjectTypes));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("search_metadata",
                "Retry search_metadata with allowed object_types values, or omit object_types to search every supported metadata type.",
                Map.of("object_types", allowedObjectTypes), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static List<String> getAllowedObjectTypes() {
        return Arrays.stream(SupportedMCPMetadataObjectType.values()).map(each -> each.name().toLowerCase(Locale.ENGLISH)).toList();
    }

    private static Map<String, Object> createInvalidPageTokenRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_page_token",
                "Retry without page_token, or use the next_page_token returned by the previous search page.");
        result.put("field", "page_token");
        result.put("suggested_arguments", Map.of("page_token", ""));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("search_metadata",
                "Retry the same search without page_token, or use a next_page_token returned by the previous page.",
                Map.of("page_token", ""), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createInvalidIntegerArgumentRecovery(final String fieldName, final int suggestedValue, final String targetTool) {
        Map<String, Object> result = createBaseRecovery("invalid_integer_argument", "Retry with an integer value inside the documented bounds.");
        result.put("field", fieldName);
        Map<String, Object> suggestedArguments = Map.of(fieldName, suggestedValue);
        result.put("suggested_arguments", suggestedArguments);
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool(targetTool, "Retry with a bounded integer argument.", suggestedArguments, false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createMultipleStatementsRecovery() {
        Map<String, Object> result = createBaseRecovery("multiple_sql_statements", "Split the user intent into separate MCP calls and handle each statement independently.");
        result.put("ask_user_when_uncertain", true);
        result.put("requires_user_approval", true);
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.askUser("Ask the user which single statement should be handled first.", List.of("single_sql_statement"), true)));
        return result;
    }

    private static Map<String, Object> createUnsupportedStatementRecovery() {
        Map<String, Object> result = createBaseRecovery("unsupported_sql_statement", "Ask the user for a supported SELECT, EXPLAIN ANALYZE, DML, DDL, DCL, transaction, or savepoint statement.");
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read supported statement classes before retrying.")));
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createBannedStatementRecovery() {
        Map<String, Object> result = createBaseRecovery("banned_sql_statement", "Do not execute this SQL through MCP; ask the user for a safer supported operation.");
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", List.of(MCPNextActionUtils.askUser("Ask for a safer supported operation instead of executing the banned SQL.", List.of("safe_sql_or_metadata_request"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createWorkflowStateRecovery() {
        Map<String, Object> result = createBaseRecovery("workflow_state_error", "Use current-session completion to select an available plan_id, or re-run the matching planning tool.");
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("completion_first", Map.of("argument", "plan_id", "scope", "current MCP session"));
        result.put("next_actions", MCPNextActionUtils.ordered(
                MCPNextActionUtils.completeArgument("plan_id", "Use MCP completion for plan_id to pick an available current-session workflow plan."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.readResource(
                        "shardingsphere://capabilities", "Read current workflow tools, then re-run the matching planning tool if completion has no usable plan."), 1)));
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createRuntimeDatabaseConnectionRecovery(final RuntimeDatabaseConnectionException cause) {
        Map<String, Object> result = createBaseRecovery(cause.getCategory(), createRuntimeDatabaseConnectionModelAction(cause));
        result.put("database", cause.getDatabaseName());
        result.put("read_resources_first", List.of("shardingsphere://capabilities"));
        result.put("next_actions", MCPNextActionUtils.ordered(
                MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP runtime capabilities and configured database names."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser("Ask the operator to fix the MCP runtime database configuration before retrying.",
                        List.of("runtime_database_configuration"), false), 1)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static String createRuntimeDatabaseConnectionModelAction(final RuntimeDatabaseConnectionException cause) {
        if ("missing_jdbc_driver".equals(cause.getCategory())) {
            return "Install or configure the JDBC driver for the MCP runtime database, then retry.";
        }
        if ("authentication_failed".equals(cause.getCategory())) {
            return "Check the runtime database credentials outside MCP, then retry.";
        }
        if ("connection_timeout".equals(cause.getCategory())) {
            return "Check database reachability and timeout settings, then retry.";
        }
        return "Check the runtime database availability and configuration, then retry.";
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
            return List.of(MCPNextActionUtils.askUser("Ask the user for the missing argument.", List.of(argumentName), false));
        }
        return List.of(MCPNextActionUtils.readResource(resources.iterator().next(), "Read a safe resource before retrying with the missing argument."));
    }
}
