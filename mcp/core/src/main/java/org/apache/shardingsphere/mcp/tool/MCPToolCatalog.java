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

package org.apache.shardingsphere.mcp.tool;

import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.tool.handler.MCPToolHandlerSupport;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.tool.handler.metadata.MetadataToolHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool catalog.
 */
public final class MCPToolCatalog {
    
    /**
     * Get supported tool names.
     *
     * @return supported tool names
     */
    public List<String> getSupportedTools() {
        return ToolHandlerRegistry.getSupportedTools();
    }
    
    /**
     * Get supported tool descriptors.
     *
     * @return supported tool descriptors
     */
    public List<MCPToolDescriptor> getToolDescriptors() {
        return ToolHandlerRegistry.getSupportedToolDescriptors();
    }
    
    /**
     * Find one supported tool descriptor.
     *
     * @param toolName tool name
     * @return matched tool descriptor
     */
    public Optional<MCPToolDescriptor> findToolDescriptor(final String toolName) {
        return ToolHandlerRegistry.findRegisteredHandler(toolName).map(ToolHandler::getToolDescriptor);
    }
    
    /**
     * Create a metadata tool request for the named tool.
     *
     * @param toolName tool name
     * @param arguments raw tool arguments
     * @return normalized metadata tool request
     * @throws UnsupportedToolException unsupported tool exception
     * @throws UnsupportedOperationException not a metadata tool exception
     */
    public ToolRequest createMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        ToolHandler toolHandler = ToolHandlerRegistry.findRegisteredHandler(toolName).orElseThrow(UnsupportedToolException::new);
        if (!(toolHandler instanceof MetadataToolHandler)) {
            throw new UnsupportedOperationException("Not a metadata tool.");
        }
        return ((MetadataToolHandler) toolHandler).createToolRequest(arguments);
    }
    
    /**
     * Resolve the database argument used by capability tools.
     *
     * @param arguments raw tool arguments
     * @return normalized database argument
     */
    public String getCapabilityDatabase(final Map<String, Object> arguments) {
        return MCPToolHandlerSupport.getCapabilityDatabase(arguments);
    }
    
    /**
     * Create an execute-query request.
     *
     * @param sessionId session identifier
     * @param arguments raw tool arguments
     * @return normalized execute-query request
     */
    public ExecutionRequest createExecutionRequest(final String sessionId, final Map<String, Object> arguments) {
        return MCPToolHandlerSupport.createExecutionRequest(sessionId, arguments);
    }
}
