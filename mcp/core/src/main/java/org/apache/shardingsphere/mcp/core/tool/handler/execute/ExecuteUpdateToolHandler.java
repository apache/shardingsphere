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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Execute side-effecting SQL tool handler.
 */
public final class ExecuteUpdateToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPDescriptorRegistry.getRequiredToolDescriptor("execute_update");
    
    private static final String EXECUTION_MODE_EXECUTE = "execute";
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final String RESULT_KIND_PREVIEW = "preview";
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        String executionMode = resolveExecutionMode(toolArguments);
        String sql = toolArguments.getStringArgument("sql");
        ClassificationResult classificationResult = checkUpdateStatement(toolArguments, sql);
        if (EXECUTION_MODE_PREVIEW.equals(executionMode)) {
            return createPreviewResponse(toolArguments, classificationResult);
        }
        return databaseContext.getExecutionFacade().execute(SQLExecutionToolHandlerSupport.createExecutionRequest(toolCall, toolArguments, sql));
    }
    
    private ClassificationResult checkUpdateStatement(final MCPToolArguments toolArguments, final String sql) {
        ClassificationResult classificationResult = new StatementClassifier().classify(sql);
        if (SQLExecutionToolHandlerSupport.isReadOnlyStatement(classificationResult.getStatementClass())) {
            throw new SQLToolMismatchException("execute_update does not accept read-only SQL. Use execute_query for read-only SQL.", "execute_update", "execute_query",
                    classificationResult, createQuerySuggestedArguments(toolArguments, classificationResult));
        }
        return classificationResult;
    }
    
    private String resolveExecutionMode(final MCPToolArguments toolArguments) {
        String result = toolArguments.getStringArgument("execution_mode");
        if (result.isEmpty()) {
            throw new MCPInvalidRequestException("execution_mode is required.");
        }
        if (EXECUTION_MODE_EXECUTE.equals(result) || EXECUTION_MODE_PREVIEW.equals(result)) {
            return result;
        }
        throw new MCPInvalidRequestException("execution_mode must be either `execute` or `preview`.");
    }
    
    private MCPResponse createPreviewResponse(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(11, 1F);
        result.put("result_kind", RESULT_KIND_PREVIEW);
        result.put("execution_mode", EXECUTION_MODE_PREVIEW);
        result.put("status", "AWAITING_APPROVAL");
        result.put("would_execute", false);
        result.put("statement_class", classificationResult.getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", classificationResult.getStatementType());
        result.put("normalized_sql", classificationResult.getNormalizedSql());
        result.put("side_effect_scope", createSideEffectScope(classificationResult.getStatementClass()));
        classificationResult.getTargetObjectName().ifPresent(optional -> result.put("target_object", optional));
        classificationResult.getSavepointName().ifPresent(optional -> result.put("savepoint", optional));
        result.put("requires_user_approval", true);
        result.put("ask_user_when_uncertain", true);
        result.put("approval_guidance", "Review normalized_sql and side_effect_scope before calling execute_update with execution_mode=execute.");
        Map<String, Object> suggestedArguments = createSuggestedArguments(toolArguments, classificationResult);
        result.put("suggested_arguments", suggestedArguments);
        result.put("read_resources_first", createReadResourcesFirst(toolArguments));
        result.put("next_actions", List.of(
                createAskUserAction("Review normalized_sql and side_effect_scope with the user before execution.", List.of("approval")),
                createToolAction("execute_update", "After explicit approval, call execute_update with suggested_arguments.", suggestedArguments)));
        return new MCPMapResponse(result);
    }
    
    private List<String> createSideEffectScope(final SupportedMCPStatement statementClass) {
        return switch (statementClass) {
            case DML -> List.of("physical-data");
            case DDL -> List.of("physical-structure");
            case DCL -> List.of("privilege-metadata");
            case TRANSACTION_CONTROL, SAVEPOINT -> List.of("transaction-state");
            default -> List.of("unknown-side-effect");
        };
    }
    
    private Map<String, Object> createSuggestedArguments(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> arguments = new LinkedHashMap<>(6, 1F);
        arguments.put("database", toolArguments.getStringArgument("database"));
        String schema = toolArguments.getStringArgument("schema");
        if (!schema.isEmpty()) {
            arguments.put("schema", schema);
        }
        arguments.put("sql", classificationResult.getNormalizedSql());
        arguments.put("execution_mode", EXECUTION_MODE_EXECUTE);
        return arguments;
    }
    
    private Map<String, Object> createQuerySuggestedArguments(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "database", toolArguments.getStringArgument("database"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "schema", toolArguments.getStringArgument("schema"));
        result.put("sql", classificationResult.getNormalizedSql());
        return result;
    }
    
    private List<String> createReadResourcesFirst(final MCPToolArguments toolArguments) {
        String database = toolArguments.getStringArgument("database");
        return database.isEmpty() ? List.of("shardingsphere://databases") : List.of("shardingsphere://databases/" + database + "/capabilities");
    }
    
    private Map<String, Object> createAskUserAction(final String reason, final List<String> requiredInputs) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("action_kind", "ask_user");
        result.put("reason", reason);
        result.put("required_inputs", requiredInputs);
        result.put("requires_user_approval", true);
        return result;
    }
    
    private Map<String, Object> createToolAction(final String targetTool, final String reason, final Map<String, Object> requiredArguments) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("action_kind", "call_tool");
        result.put("target_tool", targetTool);
        result.put("reason", reason);
        result.put("required_arguments", requiredArguments);
        result.put("requires_user_approval", true);
        return result;
    }
}
