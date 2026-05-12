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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadUtils;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final List<MCPToolDescriptor> toolDescriptors;
    
    private final MCPToolController toolController;
    
    private final MCPToolElicitationHandler elicitationHandler;
    
    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        toolDescriptors = ToolHandlerRegistry.getSupportedToolDescriptors();
        toolController = new MCPToolController(runtimeContext);
        elicitationHandler = new MCPToolElicitationHandler(toolController);
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
        McpSchema.Tool.Builder result = McpSchema.Tool.builder()
                .name(toolDescriptor.getName())
                .title(toolDescriptor.getTitle())
                .description(toolDescriptor.getDescription())
                .inputSchema(createInputSchema(toolDescriptor.toInputSchema()));
        if (!toolDescriptor.getOutputSchema().isEmpty()) {
            result.outputSchema(toolDescriptor.getOutputSchema());
        }
        if (!toolDescriptor.getAnnotations().isEmpty()) {
            result.annotations(createToolAnnotations(toolDescriptor.getAnnotations()));
        }
        if (!toolDescriptor.getMeta().isEmpty()) {
            result.meta(toolDescriptor.getMeta());
        }
        return result.build();
    }
    
    @SuppressWarnings("unchecked")
    private McpSchema.JsonSchema createInputSchema(final Map<String, Object> inputSchema) {
        Map<String, Object> properties = (Map<String, Object>) inputSchema.get("properties");
        List<String> required = (List<String>) inputSchema.get("required");
        boolean additionalProperties = Boolean.TRUE.equals(inputSchema.get("additionalProperties"));
        return new McpSchema.JsonSchema(String.valueOf(inputSchema.get("type")), properties, required, additionalProperties, Collections.emptyMap(),
                Collections.emptyMap());
    }
    
    private McpSchema.ToolAnnotations createToolAnnotations(final MCPToolAnnotations annotations) {
        return new McpSchema.ToolAnnotations(annotations.getTitle(), annotations.getReadOnlyHint(), annotations.getDestructiveHint(), annotations.getIdempotentHint(),
                annotations.getOpenWorldHint(), annotations.getReturnDirect());
    }
    
    private McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
        MCPResponse response = toolController.handle(exchange.sessionId(), request.name(), arguments);
        Map<String, Object> payload = response.toPayload();
        Optional<MCPToolDescriptor> toolDescriptor = findToolDescriptor(request.name());
        if (toolDescriptor.isPresent() && elicitationHandler.shouldElicit(exchange, toolDescriptor.get(), payload)) {
            return MCPTransportPayloadUtils.createCallToolResult(elicitationHandler.handle(exchange, toolDescriptor.get(), arguments, response, payload));
        }
        return MCPTransportPayloadUtils.createCallToolResult(response);
    }
    
    private Optional<MCPToolDescriptor> findToolDescriptor(final String toolName) {
        for (MCPToolDescriptor each : toolDescriptors) {
            if (toolName.equals(each.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
