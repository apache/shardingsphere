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
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.tool.payload.SQLExecutionPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;

import java.util.Map;

/**
 * Execute model-assisted EXPLAIN SQL tool handler.
 */
public final class ExecuteExplainToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private static final String TOOL_NAME = "database_gateway_execute_explain_query";
    
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
        String explainSql = toolArguments.getStringArgument("explain_sql");
        SQLExecutionToolHandlerSupport.checkExecutionArguments(toolArguments, TOOL_NAME);
        String schema = SQLExecutionToolHandlerSupport.resolveSchema(requestContext, toolArguments);
        SQLExecutionRequest executionRequest = SQLExecutionToolHandlerSupport.createReadOnlyExecutionRequest(requestContext.getSessionIdentity().getSessionId(), toolArguments,
                schema, explainSql, TOOL_NAME);
        return SQLExecutionPayload.query(requestContext.getExecutionFacade().executeExplain(executionRequest, sql));
    }
}
