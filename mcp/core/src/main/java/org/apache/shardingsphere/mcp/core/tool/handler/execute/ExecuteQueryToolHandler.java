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
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Execute read-only SQL query tool handler.
 */
public final class ExecuteQueryToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {

    private static final String TOOL_NAME = "database_gateway_execute_query";

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
        String sql = toolArguments.getStringArgument("sql");
        checkReadOnlyQuery(toolArguments, sql);
        SQLExecutionToolHandlerSupport.checkExecutionArguments(toolArguments, TOOL_NAME);
        return databaseContext.getExecutionFacade().execute(SQLExecutionToolHandlerSupport.createReadOnlyExecutionRequest(toolCall, toolArguments,
                resolveSchema(databaseContext, toolArguments), sql, TOOL_NAME));
    }

    private String resolveSchema(final MCPDatabaseHandlerContext databaseContext, final MCPToolArguments toolArguments) {
        String result = toolArguments.getStringArgument("schema");
        if (!result.isEmpty()) {
            return result;
        }
        String database = toolArguments.getStringArgument("database");
        if (database.isEmpty()) {
            return "";
        }
        List<MCPSchemaMetadata> schemas = databaseContext.getMetadataQueryFacade().querySchemas(database);
        return 1 == schemas.size() ? schemas.iterator().next().getSchema() : "";
    }

    private void checkReadOnlyQuery(final MCPToolArguments toolArguments, final String sql) {
        ClassificationResult classificationResult = new StatementClassifier().classify(sql);
        if (!SQLExecutionToolHandlerSupport.isReadOnlyStatement(classificationResult)) {
            throw new SQLToolMismatchException(
                    "database_gateway_execute_query only supports classifier-approved QUERY and EXPLAIN_ANALYZE statements. Use database_gateway_execute_update for side-effecting SQL.",
                    TOOL_NAME, "database_gateway_execute_update", classificationResult,
                    createSuggestedArguments(toolArguments, classificationResult));
        }
    }

    private Map<String, Object> createSuggestedArguments(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "database", toolArguments.getStringArgument("database"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "schema", toolArguments.getStringArgument("schema"));
        result.put("sql", classificationResult.getNormalizedSql());
        result.put("execution_mode", "preview");
        return result;
    }
}
