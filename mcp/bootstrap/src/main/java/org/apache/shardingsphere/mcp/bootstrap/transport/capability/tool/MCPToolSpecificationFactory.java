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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportErrorFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;

import java.sql.SQLException;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final List<MCPToolDescriptor> descriptors;
    
    private final MCPToolController controller;
    
    private final MCPToolElicitationHandler elicitationHandler;
    
    private final MCPCallToolResultFactory callToolResultFactory;
    
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        descriptors = ToolDefinitionRegistry.getSupportedToolDescriptors();
        controller = new MCPToolController(runtimeContext);
        elicitationHandler = new MCPToolElicitationHandler(controller, runtimeContext.getActiveTransport(), Clock.systemUTC());
        callToolResultFactory = new MCPCallToolResultFactory();
    }
    
    /**
     * Create MCP tool specifications.
     *
     * @return tool specifications
     */
    public List<SyncToolSpecification> createToolSpecifications() {
        return descriptors.stream().map(each -> new SyncToolSpecification(createTool(each), this::handle)).toList();
    }
    
    private McpSchema.Tool createTool(final MCPToolDescriptor descriptor) {
        McpSchema.Tool.Builder result = McpSchema.Tool.builder().name(descriptor.getName()).title(descriptor.getTitle()).description(descriptor.getDescription())
                .inputSchema(createInputSchema(descriptor.getInputSchema()));
        if (!descriptor.getOutputSchema().isEmpty()) {
            result.outputSchema(descriptor.getOutputSchema());
        }
        MCPToolAnnotations annotations = descriptor.getAnnotations();
        result.annotations(
                new ToolAnnotations(annotations.getTitle(), annotations.isReadOnlyHint(), annotations.isDestructiveHint(), annotations.isIdempotentHint(), annotations.isOpenWorldHint(), null));
        if (!descriptor.getMeta().isEmpty()) {
            result.meta(descriptor.getMeta());
        }
        return result.build();
    }
    
    @SuppressWarnings("unchecked")
    private McpSchema.JsonSchema createInputSchema(final Map<String, Object> inputSchema) {
        String type = String.valueOf(inputSchema.get("type"));
        Map<String, Object> props = (Map<String, Object>) inputSchema.get("properties");
        List<String> required = (List<String>) inputSchema.get("required");
        boolean additionalProps = Boolean.TRUE.equals(inputSchema.get("additionalProperties"));
        return new McpSchema.JsonSchema(type, props, required, additionalProps, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        try {
            Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Collections.emptyMap());
            MCPToolDefinition definition = ToolDefinitionRegistry.getToolDefinition(request.name());
            MCPSuccessPayload payload = controller.handle(exchange.sessionId(), definition, arguments);
            return callToolResultFactory.create(definition.getDescriptor(), getEffectivePayload(exchange, payload, definition, arguments));
        } catch (final UnsupportedToolException ignored) {
            throw MCPTransportErrorFactory.createError(new UnsupportedToolException(request.name()));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            if (isToolExecutionError(ex)) {
                return callToolResultFactory.create(MCPErrorConverter.convert(ex));
            }
            throw MCPTransportErrorFactory.createError(ex);
        }
    }
    
    private boolean isToolExecutionError(final Exception cause) {
        return cause instanceof ShardingSphereMCPException || cause instanceof RuntimeDatabaseConnectionException || cause instanceof SQLException;
    }
    
    private MCPSuccessPayload getEffectivePayload(final McpSyncServerExchange exchange, final MCPSuccessPayload successPayload, final MCPToolDefinition definition,
                                                  final Map<String, Object> arguments) {
        Map<String, Object> payload = successPayload.toPayload();
        return elicitationHandler.shouldHandle(definition.getDescriptor(), payload)
                ? elicitationHandler.handle(exchange, definition, arguments, payload).orElse(successPayload)
                : successPayload;
    }
}
