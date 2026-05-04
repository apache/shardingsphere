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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

/**
 * Execute side-effecting SQL tool handler.
 */
public final class ExecuteUpdateToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPDescriptorRegistry.getRequiredToolDescriptor("execute_update");
    
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
        String sql = toolArguments.getStringArgument("sql");
        checkUpdateStatement(sql);
        return databaseContext.getExecutionFacade().execute(SQLExecutionToolHandlerSupport.createExecutionRequest(toolCall, toolArguments, sql));
    }
    
    private void checkUpdateStatement(final String sql) {
        ClassificationResult classificationResult = new StatementClassifier().classify(sql);
        if (SQLExecutionToolHandlerSupport.isReadOnlyStatement(classificationResult.getStatementClass())) {
            throw new MCPUnsupportedException("execute_update does not accept read-only SQL. Use execute_query for read-only SQL.");
        }
    }
}
