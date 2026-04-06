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

package org.apache.shardingsphere.mcp.tool.handler.metadata;

import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolDispatchKind;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.ToolRequest;
import org.apache.shardingsphere.mcp.tool.handler.MCPToolHandlerSupport;

import java.util.Collections;
import java.util.Map;

/**
 * Handler for describe-view tool.
 */
public final class DescribeViewToolHandler extends AbstractMetadataToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPToolHandlerSupport.createDescriptor("describe_view", MCPToolDispatchKind.METADATA,
            MCPToolInputDefinition.create(
                    MCPToolHandlerSupport.requiredStringField("database", "Logical database name."),
                    MCPToolHandlerSupport.requiredStringField("schema", "Schema name."),
                    MCPToolHandlerSupport.requiredStringField("view", "View name.")));
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public ToolRequest createToolRequest(final Map<String, Object> arguments) {
        return MCPToolHandlerSupport.createToolRequest("describe_view", arguments,
                MCPToolHandlerSupport.getStringArgument(arguments, "database"),
                MCPToolHandlerSupport.getStringArgument(arguments, "schema"),
                MCPToolHandlerSupport.getStringArgument(arguments, "view"),
                "", Collections.emptySet(), 100, "");
    }
}
