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

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator.ValidationResponse;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification.Builder;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.api.protocol.error.MCPErrorCode;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportErrorFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {

    private static final int RESOURCE_LINK_LIMIT = 24;

    private final List<MCPToolDescriptor> toolDescriptors;

    private final MCPToolController toolController;

    private final MCPToolElicitationHandler elicitationHandler;

    private final JsonSchemaValidator outputSchemaValidator;

    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        toolDescriptors = ToolHandlerRegistry.getSupportedToolDescriptors();
        toolController = new MCPToolController(runtimeContext);
        elicitationHandler = new MCPToolElicitationHandler(toolController);
        outputSchemaValidator = new DefaultJsonSchemaValidator();
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
                .inputSchema(createInputSchema(toolDescriptor.getInputSchema()));
        if (!toolDescriptor.getOutputSchema().isEmpty()) {
            result.outputSchema(toolDescriptor.getOutputSchema());
        }
        result.annotations(createToolAnnotations(toolDescriptor.getAnnotations()));
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
        return new McpSchema.ToolAnnotations(annotations.getTitle(), annotations.isReadOnlyHint(), annotations.isDestructiveHint(), annotations.isIdempotentHint(),
                annotations.isOpenWorldHint(), null);
    }

    private McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        try {
            Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
            MCPResponse response = toolController.handle(exchange.sessionId(), request.name(), arguments);
            Map<String, Object> payload = response.toPayload();
            Optional<MCPToolDescriptor> toolDescriptor = findToolDescriptor(request.name());
            if (toolDescriptor.isPresent() && elicitationHandler.shouldElicit(exchange, toolDescriptor.get(), payload)) {
                return createCallToolResult(toolDescriptor.get(), elicitationHandler.handle(exchange, toolDescriptor.get(), arguments, response, payload));
            }
            return toolDescriptor.map(each -> createCallToolResult(each, response)).orElseGet(() -> createCallToolResult(response));
        } catch (final UnsupportedToolException ex) {
            throw createCallToolError(ex);
        }
    }

    private McpSchema.CallToolResult createCallToolResult(final MCPToolDescriptor toolDescriptor, final MCPResponse response) {
        if (response instanceof MCPErrorResponse) {
            return createCallToolResult(response);
        }
        Map<String, Object> payload = response.toPayload();
        if (toolDescriptor.getOutputSchema().isEmpty()) {
            return createCallToolResult(payload);
        }
        ValidationResponse validation = outputSchemaValidator.validate(toolDescriptor.getOutputSchema(), payload);
        if (validation.valid()) {
            return createCallToolResult(payload);
        }
        return createCallToolResult(new MCPErrorResponse(MCPErrorCode.INVALID_OUTPUT_SCHEMA, String.format(
                "Tool `%s` structuredContent does not match declared outputSchema: %s", toolDescriptor.getName(), Objects.toString(validation.errorMessage(), "validation failed"))));
    }

    private McpSchema.CallToolResult createCallToolResult(final MCPResponse response) {
        return createCallToolResult(response.toPayload(), response instanceof MCPErrorResponse);
    }

    private McpSchema.CallToolResult createCallToolResult(final Map<String, Object> payload) {
        return createCallToolResult(payload, false);
    }

    private McpSchema.CallToolResult createCallToolResult(final Map<String, Object> payload, final boolean error) {
        CallToolResult.Builder result = CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(error);
        appendResourceLinks(payload, result);
        return result.build();
    }

    private void appendResourceLinks(final Map<String, Object> payload, final CallToolResult.Builder result) {
        MCPResourceLinkContract.ResourceLinks resourceLinks = MCPResourceLinkContract.createResourceLinks(payload, RESOURCE_LINK_LIMIT);
        for (McpSchema.ResourceLink each : resourceLinks.links()) {
            result.addContent(each);
        }
        if (0 < resourceLinks.totalCount()) {
            result.meta(createResourceLinksMeta(resourceLinks));
        }
    }

    private Map<String, Object> createResourceLinksMeta(final MCPResourceLinkContract.ResourceLinks resourceLinks) {
        return Map.of(
                MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED, resourceLinks.links().size(),
                MCPShardingSphereMetadataKeys.RESOURCE_LINKS_OMITTED, resourceLinks.omittedCount(),
                MCPShardingSphereMetadataKeys.RESOURCE_LINK_LIMIT, RESOURCE_LINK_LIMIT);
    }

    private McpError createCallToolError(final UnsupportedToolException cause) {
        return MCPTransportErrorFactory.createToolNotFoundError(cause);
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
