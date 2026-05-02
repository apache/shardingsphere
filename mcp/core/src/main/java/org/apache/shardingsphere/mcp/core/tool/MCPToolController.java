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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.api.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;

import java.util.Map;

/**
 * MCP tool controller.
 */
@RequiredArgsConstructor
public final class MCPToolController {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Handle tool call.
     *
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments tool arguments
     * @return MCP response
     */
    public MCPResponse handle(final String sessionId, final String toolName, final Map<String, Object> arguments) {
        try (MCPRequestScope requestScope = new MCPRequestScope(runtimeContext)) {
            return ToolHandlerRegistry.dispatch(requestScope, sessionId, toolName, arguments).orElseThrow(UnsupportedToolException::new);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return MCPErrorConverter.convert(ex);
        }
    }
}
