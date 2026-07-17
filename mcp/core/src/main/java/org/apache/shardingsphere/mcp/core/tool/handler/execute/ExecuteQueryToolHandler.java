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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.tool.payload.SQLExecutionPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Execute read-only SQL query tool handler.
 */
public final class ExecuteQueryToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private static final String TOOL_NAME = "database_gateway_execute_query";
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        String sql = toolArguments.getStringArgument("sql");
        checkReadOnlyQuery(requestContext, toolArguments, sql);
        SQLExecutionToolHandlerSupport.checkExecutionArguments(toolArguments, TOOL_NAME);
        return SQLExecutionPayload.query(requestContext.getExecutionFacade().execute(SQLExecutionToolHandlerSupport.createReadOnlyExecutionRequest(requestContext.getSessionId(), toolArguments,
                SQLExecutionToolHandlerSupport.resolveSchema(requestContext, toolArguments), sql, TOOL_NAME)));
    }
    
    private void checkReadOnlyQuery(final MCPFeatureRequestContext requestContext, final MCPToolArguments toolArguments, final String sql) {
        ClassificationResult classificationResult = SQLExecutionToolHandlerSupport.analyze(requestContext, toolArguments, sql);
        if (!SQLExecutionToolHandlerSupport.isQueryStatement(classificationResult)) {
            throw new SQLToolMismatchException(
                    "database_gateway_execute_query only supports parser-approved QUERY statements. "
                            + "Use database_gateway_execute_explain_query for EXPLAIN diagnostics or database_gateway_execute_update for side-effecting SQL.",
                    TOOL_NAME, "database_gateway_execute_update", classificationResult,
                    createSuggestedArguments(toolArguments, classificationResult));
        }
    }
    
    private Map<String, Object> createSuggestedArguments(final MCPToolArguments toolArguments, final ClassificationResult classificationResult) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "database", toolArguments.getStringArgument("database"));
        SQLExecutionToolHandlerSupport.putIfNotEmpty(result, "schema", toolArguments.getStringArgument("schema"));
        result.put("sql", classificationResult.getNormalizedSql());
        result.put(MCPPayloadFieldNames.EXECUTION_MODE, "preview");
        return result;
    }
}
