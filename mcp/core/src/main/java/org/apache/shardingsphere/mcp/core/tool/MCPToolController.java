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

package org.apache.shardingsphere.mcp.core.tool;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;

import java.util.Map;

/**
 * MCP tool controller.
 */
public final class MCPToolController {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPToolCallLimiter toolCallLimiter;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    public MCPToolController(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        toolCallLimiter = new MCPToolCallLimiter();
        runtimeContext.getSessionManager().addSessionCloseListener(toolCallLimiter::releaseSession);
        sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(runtimeContext.getSessionManager());
    }
    
    /**
     * Handle tool call.
     *
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments tool arguments
     * @return tool response
     * @throws UnsupportedToolException unsupported tool exception
     */
    public MCPResponse handle(final String sessionId, final String toolName, final Map<String, Object> arguments) {
        return handle(sessionId, ToolDefinitionRegistry.getToolDefinition(toolName), arguments);
    }
    
    /**
     * Handle tool call.
     *
     * @param sessionId session identifier
     * @param toolDefinition tool definition
     * @param arguments tool arguments
     * @return tool response
     */
    public MCPResponse handle(final String sessionId, final MCPToolDefinition toolDefinition, final Map<String, Object> arguments) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(sessionId, () -> {
                toolCallLimiter.acquire(sessionId, toolDefinition.getDescriptor().getName());
                return ToolDefinitionRegistry.dispatch(new MCPRequestScope(runtimeContext, sessionId), toolDefinition, arguments);
            });
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return MCPErrorConverter.convert(ex);
        }
    }
}
