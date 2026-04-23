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

package org.apache.shardingsphere.mcp.tool.handler;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;

import java.util.Map;

/**
 * Handler for one MCP tool.
 */
public interface ToolHandler {
    
    /**
     * Get tool descriptor.
     *
     * @return tool descriptor
     */
    MCPToolDescriptor getToolDescriptor();
    
    /**
     * Handle one tool call.
     *
     * @param requestContext request context
     * @param sessionId session identifier
     * @param arguments normalized tool arguments
     * @return tool response
     */
    MCPResponse handle(MCPFeatureContext requestContext, String sessionId, Map<String, Object> arguments);
}
