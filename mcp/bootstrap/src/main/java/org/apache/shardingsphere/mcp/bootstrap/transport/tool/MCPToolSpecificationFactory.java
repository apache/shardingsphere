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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification.Builder;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.tool.MCPToolController;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final List<MCPToolDescriptor> toolDescriptors;
    
    private final MCPToolJsonSchemaAdapter mcpToolJsonSchemaAdapter;
    
    private final MCPToolController toolController;
    
    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        toolDescriptors = ToolHandlerRegistry.getSupportedToolDescriptors();
        mcpToolJsonSchemaAdapter = new MCPToolJsonSchemaAdapter();
        toolController = new MCPToolController(runtimeContext);
    }
    
    /**
     * Create MCP tool specifications.
     *
     * @return tool specifications
     */
    public List<SyncToolSpecification> createToolSpecifications() {
        return toolDescriptors.stream().map(each -> new Builder().tool(createTool(each)).callHandler(this::handle).build()).toList();
    }
    
    private McpSchema.Tool createTool(final MCPToolDescriptor toolDescriptor) {
        return McpSchema.Tool.builder()
                .name(toolDescriptor.getName())
                .title(toolDescriptor.getTitle())
                .description(toolDescriptor.getDescription())
                .inputSchema(mcpToolJsonSchemaAdapter.createInputSchema(toolDescriptor.getInputDefinition()))
                .build();
    }
    
    private McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
        Map<String, Object> payload = toolController.handle(exchange.sessionId(), request.name(), arguments).toPayload();
        return CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(payload.containsKey("error_code")).build();
    }
}
