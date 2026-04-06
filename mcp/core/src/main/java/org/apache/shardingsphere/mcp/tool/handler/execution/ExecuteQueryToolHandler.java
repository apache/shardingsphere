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

package org.apache.shardingsphere.mcp.tool.handler.execution;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.handler.MCPToolHandlerSupport;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;

import java.util.Map;

/**
 * Handler for execute-query tool.
 */
public final class ExecuteQueryToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPToolDescriptor.create("execute_query",
            MCPToolInputDefinition.create(
                    MCPToolHandlerSupport.requiredStringField("database", "Logical database name."),
                    MCPToolHandlerSupport.optionalStringField("schema", "Optional schema name."),
                    MCPToolHandlerSupport.requiredStringField("sql", "Single SQL statement."),
                    MCPToolHandlerSupport.optionalIntegerField("max_rows", "Optional maximum row count."),
                    MCPToolHandlerSupport.optionalIntegerField("timeout_ms", "Optional timeout in milliseconds.")));
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPRuntimeContext runtimeContext, final String sessionId, final Map<String, Object> arguments) {
        ExecutionRequest executionRequest = MCPToolHandlerSupport.createExecutionRequest(sessionId, arguments);
        if (executionRequest.getDatabase().isEmpty() || executionRequest.getSql().isEmpty()) {
            throw new MCPInvalidRequestException("Database and sql are required.");
        }
        return runtimeContext.getSqlExecutionFacade().execute(executionRequest);
    }
}
