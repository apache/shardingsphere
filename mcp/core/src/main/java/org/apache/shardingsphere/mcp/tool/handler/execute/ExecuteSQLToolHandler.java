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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;

import java.util.Arrays;
import java.util.Map;

/**
 * Execute SQL tool handler.
 */
public final class ExecuteSQLToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("execute_query",
            Arrays.asList(
                    new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Logical database name.", null), true),
                    new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional schema name used as a namespace hint for unqualified object names.", null), false),
                    new MCPToolFieldDefinition("sql", new MCPToolValueDefinition(Type.STRING, "Single SQL statement.", null), true),
                    new MCPToolFieldDefinition("max_rows", new MCPToolValueDefinition(Type.INTEGER, "Optional maximum row count.", null), false),
                    new MCPToolFieldDefinition("timeout_ms", new MCPToolValueDefinition(Type.INTEGER, "Optional timeout in milliseconds.", null), false)));
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPRequestContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        MCPSQLExecutionFacade sqlExecutionFacade = new MCPSQLExecutionFacade(requestContext);
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        return sqlExecutionFacade.execute(new SQLExecutionRequest(sessionId,
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), toolArguments.getStringArgument("sql"),
                toolArguments.getIntegerArgument("max_rows", 0), toolArguments.getIntegerArgument("timeout_ms", 0)));
    }
}
