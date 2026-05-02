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
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;

import java.util.Arrays;

/**
 * Execute SQL tool handler.
 */
public final class ExecuteSQLToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("execute_query", "Execute Query",
            "Execute one SQL statement against a logical database.",
            Arrays.asList(
                    new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Logical database name.", null), true),
                    new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional schema name used as a namespace hint for unqualified object names.", null), false),
                    new MCPToolFieldDefinition("sql", new MCPToolValueDefinition(Type.STRING, "Single SQL statement.", null), true),
                    new MCPToolFieldDefinition("max_rows", new MCPToolValueDefinition(Type.INTEGER, "Optional maximum row count.", null), false),
                    new MCPToolFieldDefinition("timeout_ms", new MCPToolValueDefinition(Type.INTEGER, "Optional timeout in milliseconds.", null), false)));
    
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
        return databaseContext.getExecutionFacade().execute(new SQLExecutionRequest(toolCall.getSessionId(),
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), toolArguments.getStringArgument("sql"),
                toolArguments.getIntegerArgument("max_rows", 0), toolArguments.getIntegerArgument("timeout_ms", 0)));
    }
}
