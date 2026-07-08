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

package org.apache.shardingsphere.mcp.api.tool;

import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.protocol.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;

/**
 * MCP tool handler.
 *
 * @param <T> type of handler context
 */
public interface MCPToolHandler<T extends MCPHandlerContext> {
    
    /**
     * Get handler context type.
     *
     * @return handler context type
     */
    Class<T> getContextType();
    
    /**
     * Get canonical tool name.
     *
     * @return canonical tool name
     */
    String getToolName();
    
    /**
     * Handle one tool call.
     *
     * @param handlerContext handler context
     * @param toolCall tool call
     * @return successful tool response
     * @throws ShardingSphereMCPException controlled failure to be converted by runtime
     */
    MCPResponse handle(T handlerContext, MCPToolCall toolCall);
}
