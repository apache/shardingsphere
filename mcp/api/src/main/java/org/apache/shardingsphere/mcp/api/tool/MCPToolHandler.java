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

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;

import java.util.Map;

/**
 * MCP tool handler.
 *
 * <p>Implementations must support concurrent invocations and must not retain request context or arguments after {@link #handle} returns.</p>
 *
 * @param <T> required request context type
 */
public interface MCPToolHandler<T extends MCPRequestContext> {
    
    /**
     * Get required request context type.
     *
     * @return required request context type
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
     * @param context request context
     * @param arguments tool arguments
     * @return successful tool payload
     * @throws ShardingSphereMCPException controlled failure to be converted by runtime
     */
    MCPSuccessPayload handle(T context, Map<String, Object> arguments);
}
