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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Execute side-effecting SQL tool handler.
 */
public final class ExecuteUpdateToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final String TOOL_NAME = "database_gateway_execute_update";
    
    private static final String EXECUTION_MODE_EXECUTE = "execute";
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final List<String> EXECUTION_MODES = List.of(EXECUTION_MODE_EXECUTE, EXECUTION_MODE_PREVIEW);
    
    private static final String RESULT_KIND_PREVIEW = "preview";
    
    private static final String PREVIEW_REVIEW_GUIDANCE = "Review normalized_sql and side_effect_scope before execution. "
            + "This preview is classification-only; it does not guarantee parsing, rule validation, algorithm initialization, affected rows, or runtime success.";
    
    private static final String PREVIEW_EXECUTION_REASON = "Execute only after reviewing normalized_sql and side_effect_scope; preview did not validate runtime executability.";
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
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
        return databaseContext.getExecutionFacade().execute(SQLExecutionToolHandlerSupport.createExecutionRequest(toolCall, toolArguments, sql, TOOL_NAME))
                .withExecutionMode(EXECUTION_MODE_EXECUTE);
    }
    
    private ClassificationResult checkUpdateStatement(final MCPToolArguments toolArguments, final String sql) {
        ClassificationResult classificationResult = new StatementClassifier().classify(sql);
        if (SQLExecutionToolHandlerSupport.isReadOnlyStatement(classificationResult)) {
            throw new SQLToolMismatchException("database_gateway_execute_update does not accept read-only SQL. Use database_gateway_execute_query for read-only SQL.",
                    TOOL_NAME, "database_gateway_execute_query", classificationResult,
                    createQuerySuggestedArguments(toolArguments, classificationResult));
        }
        return classificationResult;
    }
    
    private String resolveExecutionMode(final MCPToolArguments toolArguments) {
        String result = toolArguments.getStringArgument(MCPPayloadFieldNames.EXECUTION_MODE);
        if (result.isEmpty()) {
            throw new MCPExecutionModeRequiredException(TOOL_NAME, EXECUTION_MODES, createPreviewSuggestedArguments(toolArguments));
        }
        if (EXECUTION_MODE_EXECUTE.equals(result) || EXECUTION_MODE_PREVIEW.equals(result)) {
            return result;
        }
        throw new MCPInvalidExecutionModeException(TOOL_NAME, EXECUTION_MODES, createPreviewSuggestedArguments(toolArguments));
    }
    
    private MCPResponse createPreviewResponse(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("response_mode", MCPResponseMode.PREVIEW);
        result.put("result_kind", RESULT_KIND_PREVIEW);
        result.put(MCPPayloadFieldNames.EXECUTION_MODE, EXECUTION_MODE_PREVIEW);
        result.put("preview_semantics", "classification_only");
        result.put("affected_rows_estimated", false);
        result.put("status", "PREVIEWED");
        result.put("would_execute", false);
        result.put("statement_class", classificationResult.getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", classificationResult.getStatementType());
        result.put("normalized_sql", classificationResult.getNormalizedSql());
        result.put("side_effect_scope", createSideEffectScope(classificationResult));
        classificationResult.getTargetObjectName().ifPresent(optional -> result.put("target_object", optional));
        classificationResult.getSavepointName().ifPresent(optional -> result.put("savepoint", optional));
        result.put("review_guidance", PREVIEW_REVIEW_GUIDANCE);
        result.put("review_summary", createReviewSummary(classificationResult));
        Map<String, Object> suggestedArguments = createSuggestedArguments(toolArguments, classificationResult);
        result.put("suggested_arguments", suggestedArguments);
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createResourcesToRead(toolArguments));
        result.put("argument_provenance", createArgumentProvenance(suggestedArguments));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.callTool(TOOL_NAME, PREVIEW_EXECUTION_REASON, suggestedArguments)));
        return new MCPMapResponse(result);
    }
    
    private String createReviewSummary(final ClassificationResult classificationResult) {
        return String.format("Previewed %s statement with side-effect scope %s. It has not been executed.", classificationResult.getStatementType(),
                String.join(", ", createSideEffectScope(classificationResult)));
    }
    
    private List<String> createSideEffectScope(final ClassificationResult classificationResult) {
        return switch (classificationResult.getAnalyzedStatementClass().orElse(classificationResult.getStatementClass())) {
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
        arguments.put(MCPPayloadFieldNames.EXECUTION_MODE, EXECUTION_MODE_EXECUTE);
        return arguments;
    }
    
    private Map<String, Object> createPreviewSuggestedArguments(final MCPToolArguments toolArguments) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "database", toolArguments.getStringArgument("database"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "schema", toolArguments.getStringArgument("schema"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "sql", toolArguments.getStringArgument("sql"));
        result.put(MCPPayloadFieldNames.EXECUTION_MODE, EXECUTION_MODE_PREVIEW);
        return result;
    }
    
    private Map<String, Object> createArgumentProvenance(final Map<String, Object> suggestedArguments) {
        Map<String, Object> result = new LinkedHashMap<>(suggestedArguments.size(), 1F);
        if (suggestedArguments.containsKey("database")) {
            result.put("database", "user_provided");
        }
        if (suggestedArguments.containsKey("schema")) {
            result.put("schema", "user_provided");
        }
        result.put("sql", "server_generated");
        result.put(MCPPayloadFieldNames.EXECUTION_MODE, "server_defaulted");
        return result;
    }
    
    private Map<String, Object> createQuerySuggestedArguments(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "database", toolArguments.getStringArgument("database"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "schema", toolArguments.getStringArgument("schema"));
        result.put("sql", classificationResult.getNormalizedSql());
        return result;
    }
    
    private List<Map<String, Object>> createResourcesToRead(final MCPToolArguments toolArguments) {
        String database = toolArguments.getStringArgument("database");
        if (database.isEmpty()) {
            return List.of(MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "read_first", "Read logical databases before execution.",
                    MCPPayloadFieldNames.RESOURCES_TO_READ));
        }
        return List.of(MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s/capabilities", MCPUriPathSegmentUtils.encodePathSegment(database)), "logical-database-capability",
                "read_first", "Read logical database capabilities before execution.", MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
}
