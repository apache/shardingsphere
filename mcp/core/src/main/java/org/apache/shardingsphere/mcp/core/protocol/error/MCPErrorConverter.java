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
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidMetadataObjectTypesException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArgumentConflictException;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
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
        return new MCPErrorResponse(errorCode, message, createRecovery(cause));
    }

    private static Map<String, Object> createRecovery(final Throwable cause) {
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
        if (cause instanceof MCPInvalidToolArgumentException) {
            return createInvalidToolArgumentRecovery((MCPInvalidToolArgumentException) cause);
        }
        if (cause instanceof WorkflowArgumentConflictException) {
            return createWorkflowArgumentConflictRecovery((WorkflowArgumentConflictException) cause);
        }
        if (cause instanceof MCPExecutionModeRequiredException) {
            return createMissingExecutionModeRecovery((MCPExecutionModeRequiredException) cause);
        }
        if (cause instanceof MCPInvalidExecutionModeException) {
            return createInvalidExecutionModeRecovery((MCPInvalidExecutionModeException) cause);
        }
        if (cause instanceof MCPInvalidApprovedStepsException) {
            return createInvalidApprovedStepsRecovery((MCPInvalidApprovedStepsException) cause);
        }
        if (cause instanceof MCPMissingToolArgumentException) {
            return createMissingArgumentRecovery(((MCPMissingToolArgumentException) cause).getArgumentName());
        }
        if (cause instanceof MCPMultipleSQLStatementsException) {
            return createMultipleStatementsRecovery();
        }
        if (cause instanceof MCPUnsupportedSQLStatementException) {
            return createUnsupportedStatementRecovery();
        }
        if (cause instanceof MCPBannedSQLStatementException) {
            return createBannedStatementRecovery();
        }
        if (cause instanceof MCPInvalidMetadataObjectTypesException) {
            return createInvalidObjectTypesRecovery((MCPInvalidMetadataObjectTypesException) cause);
        }
        if (cause instanceof InvalidPageTokenException) {
            return createInvalidPageTokenRecovery();
        }
        if (cause instanceof MCPWorkflowStateException) {
            return createWorkflowStateRecovery();
        }
        return Map.of();
    }

    private static Map<String, Object> createUnsupportedToolRecovery(final String toolName) {
        Map<String, Object> result = createBaseRecovery("unsupported_tool", "Call one of the supported tools returned by tools/list.");
        result.put("tool_name", toolName);
        result.put("supported_tools", ToolHandlerRegistry.getSupportedTools());
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Discover supported MCP tools before choosing another tool."));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP capabilities before choosing another tool.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createUnsupportedResourceRecovery(final String resourceUri) {
        Map<String, Object> result = createBaseRecovery("unsupported_resource", "Read one of the supported resources or templates returned by resources/list and resources/templates/list.");
        result.put("resource", MCPResourceHintUtils.create(resourceUri, "resource", "inspect_detail", "Unsupported resource URI requested.", "recovery"));
        result.put("matching_resource_templates", ResourceHandlerRegistry.getSupportedResources());
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Discover supported resources and templates before choosing another resource."));
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
        result.put("resources_to_read", createResourceHintList("shardingsphere://databases", "logical-database", "Read logical databases before choosing a metadata scope."));
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

    private static Map<String, Object> createMissingExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        return "apply_workflow".equals(cause.getToolName()) ? createMissingWorkflowExecutionModeRecovery(cause) : createMissingUpdateExecutionModeRecovery(cause);
    }

    private static Map<String, Object> createMissingUpdateExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        Map<String, Object> result = createBaseRecovery("missing_execution_mode",
                "Retry the same side-effecting tool with execution_mode=preview, review the preview, then ask the user for approval.");
        result.put("missing_fields", List.of("execution_mode"));
        result.put("field", "execution_mode");
        result.put("source_tool", cause.getToolName());
        result.put("tool_name", cause.getToolName());
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool(cause.getToolName(), "Retry the same side-effecting tool in preview mode.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createMissingWorkflowExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        Map<String, Object> result = createBaseRecovery("missing_execution_mode",
                "Retry apply_workflow with execution_mode=preview, review the preview, then use review-then-execute or manual-only after approval.");
        result.put("missing_fields", List.of("execution_mode"));
        result.put("field", "execution_mode");
        result.put("source_tool", cause.getToolName());
        result.put("tool_name", cause.getToolName());
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool(cause.getToolName(), "Retry apply_workflow with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        return "apply_workflow".equals(cause.getToolName()) ? createInvalidWorkflowExecutionModeRecovery(cause) : createInvalidUpdateExecutionModeRecovery(cause);
    }

    private static Map<String, Object> createInvalidUpdateExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value", "Retry with execution_mode=preview or execution_mode=execute.");
        result.put("field", "execution_mode");
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool(cause.getToolName(), "Retry execute_update with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidWorkflowExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value",
                "Retry apply_workflow with execution_mode=preview, review the preview, then use review-then-execute or manual-only after approval.");
        result.put("field", "execution_mode");
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool(cause.getToolName(), "Retry apply_workflow with execution_mode=preview first.", Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createInvalidApprovedStepsRecovery(final MCPInvalidApprovedStepsException cause) {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value",
                "Retry apply_workflow with approved_steps copied from preview_artifacts.approval_step, or omit approved_steps to apply every artifact.");
        result.put("field", "approved_steps");
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("execution_mode", "preview"));
        result.put("next_actions", List.of(MCPNextActionUtils.callTool("apply_workflow", "Preview again, then copy only visible approval_step values into approved_steps after user approval.",
                Map.of("execution_mode", "preview"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createWorkflowArgumentConflictRecovery(final WorkflowArgumentConflictException cause) {
        Map<String, Object> result = createBaseRecovery("workflow_argument_conflict", "Ask the user which public workflow argument value to keep, then retry with only one value.");
        List<String> argumentFields = createWorkflowArgumentConflictFields(cause.getConflictingArguments());
        result.put("conflicting_arguments", cause.getConflictingArguments());
        result.put("clarification_questions", createWorkflowArgumentConflictQuestions(cause.getConflictingArguments()));
        result.put("next_actions", List.of(MCPNextActionUtils.askUser("Resolve conflicting workflow arguments before retrying the planning tool.",
                argumentFields, false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static List<String> createWorkflowArgumentConflictFields(final List<String> conflictingArguments) {
        return conflictingArguments.stream().map(MCPErrorConverter::getWorkflowArgumentConflictField).distinct().toList();
    }

    private static List<Map<String, Object>> createWorkflowArgumentConflictQuestions(final List<String> conflictingArguments) {
        return conflictingArguments.stream().map(MCPErrorConverter::createWorkflowArgumentConflictQuestion).toList();
    }

    private static Map<String, Object> createWorkflowArgumentConflictQuestion(final String conflict) {
        String field = getWorkflowArgumentConflictField(conflict);
        return Map.of(
                "field", field,
                "conflict", conflict,
                "input_type", "string",
                "display_message", String.format("Choose one value for `%s`, or remove the duplicate path.", field));
    }

    private static String getWorkflowArgumentConflictField(final String conflict) {
        int conflictSeparatorIndex = conflict.indexOf(" conflicts with ");
        return 0 < conflictSeparatorIndex ? conflict.substring(0, conflictSeparatorIndex) : conflict;
    }

    private static Map<String, Object> createMissingArgumentRecovery(final String argumentName) {
        boolean missingDatabase = "database".equals(argumentName);
        String category = missingDatabase ? "missing_database" : "missing_argument";
        String modelAction = missingDatabase
                ? "Read shardingsphere://databases, infer a single safe database when possible, or ask the user."
                : "Provide the missing argument, infer it from resources when safe, or ask the user.";
        Map<String, Object> result = createBaseRecovery(category, modelAction);
        result.put("missing_fields", List.of(argumentName));
        result.put("resources_to_read", createMissingArgumentResourcesToRead(argumentName));
        result.put("next_actions", createMissingArgumentNextActions(argumentName));
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static List<Map<String, Object>> createMissingArgumentResourcesToRead(final String argumentName) {
        if ("database".equals(argumentName)) {
            return createResourceHintList("shardingsphere://databases", "logical-database", "Read logical databases before retrying with a database argument.");
        }
        if ("plan_id".equals(argumentName)) {
            return createResourceHintList("shardingsphere://capabilities", "capability", "Read workflow-capable MCP tools before retrying with a plan_id argument.");
        }
        return List.of();
    }

    private static Map<String, Object> createInvalidObjectTypesRecovery(final MCPInvalidMetadataObjectTypesException cause) {
        Map<String, Object> result = createBaseRecovery("invalid_enum_value", "Retry with one or more allowed object_types values.");
        result.put("field", "object_types");
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("object_types", cause.getAllowedValues()));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("search_metadata",
                "Retry search_metadata with allowed object_types values, or omit object_types to search every supported metadata type.",
                Map.of("object_types", cause.getAllowedValues()), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createInvalidPageTokenRecovery() {
        Map<String, Object> result = createBaseRecovery("invalid_page_token",
                "Retry without page_token, or use the next_page_token returned by the previous search page.");
        result.put("field", "page_token");
        result.put("argument_path", "page_token");
        result.put("source_tool", "search_metadata");
        result.put("tool_name", "search_metadata");
        result.put("minimum_value", 0);
        result.put("suggested_value", "");
        result.put("suggested_arguments", Map.of("page_token", ""));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("search_metadata",
                "Retry the same search without page_token, or use a next_page_token returned by the previous page.",
                Map.of("page_token", ""), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createInvalidToolArgumentRecovery(final MCPInvalidToolArgumentException cause) {
        return createInvalidIntegerArgumentRecovery(cause.getArgumentPath(), cause.getSuggestedValue(), cause.getSourceTool(), cause.getTargetTool(), cause.getMinimumValue(),
                cause.getMaximumValue());
    }

    private static Map<String, Object> createInvalidIntegerArgumentRecovery(final String fieldName, final int suggestedValue, final String sourceTool, final String targetTool,
                                                                            final int minimumValue, final int maximumValue) {
        Map<String, Object> result = createBaseRecovery("invalid_integer_argument", "Retry with an integer value inside the documented bounds.");
        result.put("field", fieldName);
        result.put("argument_path", fieldName);
        if (!sourceTool.isEmpty()) {
            result.put("source_tool", sourceTool);
        }
        if (!targetTool.isEmpty()) {
            result.put("tool_name", targetTool);
        }
        result.put("minimum_value", minimumValue);
        result.put("maximum_value", maximumValue);
        result.put("suggested_value", suggestedValue);
        Map<String, Object> suggestedArguments = Map.of(fieldName, suggestedValue);
        result.put("suggested_arguments", suggestedArguments);
        result.put("next_actions", createInvalidIntegerArgumentNextActions(fieldName, targetTool, suggestedArguments));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static List<Map<String, Object>> createInvalidIntegerArgumentNextActions(final String fieldName, final String targetTool, final Map<String, Object> suggestedArguments) {
        if (targetTool.isEmpty()) {
            return List.of(MCPNextActionUtils.askUser("Ask the user for a bounded integer value before retrying.", List.of(fieldName), false));
        }
        return List.of(MCPNextActionUtils.retryTool(targetTool, "Retry with a bounded integer argument.", suggestedArguments, false));
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
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Read supported SQL statement classes before retrying."));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read supported statement classes before retrying.")));
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createBannedStatementRecovery() {
        Map<String, Object> result = createBaseRecovery("banned_sql_statement", "Do not execute this SQL through MCP; ask the user for a safer supported operation.");
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Read supported safe alternatives before asking the user."));
        result.put("next_actions", List.of(MCPNextActionUtils.askUser("Ask for a safer supported operation instead of executing the banned SQL.", List.of("safe_sql_or_metadata_request"), true)));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        return result;
    }

    private static Map<String, Object> createWorkflowStateRecovery() {
        Map<String, Object> result = createBaseRecovery("workflow_state_error", "Use current-session completion to select an available plan_id, or re-run the matching planning tool.");
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Read workflow-capable MCP tools before re-planning."));
        result.put("completion_first", Map.of("argument", "plan_id", "scope", "current MCP session"));
        result.put("next_actions", MCPNextActionUtils.ordered(
                MCPNextActionUtils.completeArgument("resource", "shardingsphere://workflows/{plan_id}", "plan_id", "", Map.of(), List.of(), "resource",
                        "shardingsphere://workflows/{plan_id}", Map.of(), "Use MCP completion for plan_id to pick an available current-session workflow plan."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.readResource(
                        "shardingsphere://capabilities", "Read current workflow tools, then re-run the matching planning tool if completion has no usable plan."), 1)));
        result.put("ask_user_when_uncertain", false);
        return result;
    }

    private static Map<String, Object> createRuntimeDatabaseConnectionRecovery(final RuntimeDatabaseConnectionException cause) {
        Map<String, Object> result = createBaseRecovery(cause.getCategory(), createRuntimeDatabaseConnectionModelAction(cause));
        result.put("database", cause.getDatabaseName());
        result.put("resources_to_read", createResourceHintList("shardingsphere://capabilities", "capability", "Read configured MCP runtime capabilities before retrying."));
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
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("recovery_category", normalizeRecoveryCategory(category));
        result.put("recoverable", true);
        result.put("category", category);
        result.put("model_action", modelAction);
        return result;
    }

    private static String normalizeRecoveryCategory(final String category) {
        if (category.startsWith("missing_")) {
            return "missing_context";
        }
        if (category.endsWith("_not_found")) {
            return "not_found";
        }
        if (category.contains("conflict")) {
            return "ambiguous";
        }
        if (category.contains("unavailable") || category.contains("authentication_failed") || category.contains("connection_timeout")) {
            return "terminal";
        }
        return "validation";
    }

    private static List<Map<String, Object>> createResourceHintList(final String uri, final String resourceKind, final String reason) {
        return List.of(MCPResourceHintUtils.create(uri, resourceKind, "read_first", reason, "resources_to_read"));
    }

    private static List<Map<String, Object>> createMissingArgumentNextActions(final String argumentName) {
        List<Map<String, Object>> resources = createMissingArgumentResourcesToRead(argumentName);
        if (resources.isEmpty()) {
            return List.of(MCPNextActionUtils.askUser("Ask the user for the missing argument.", List.of(argumentName), false));
        }
        return List.of(MCPNextActionUtils.readResource(Objects.toString(resources.iterator().next().get("uri"), ""), "Read a safe resource before retrying with the missing argument."));
    }
}
