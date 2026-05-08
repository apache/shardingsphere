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
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadUtils;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final List<MCPToolDescriptor> toolDescriptors;
    
    private final MCPToolController toolController;
    
    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        toolDescriptors = ToolHandlerRegistry.getSupportedToolDescriptors();
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
        McpSchema.Tool.Builder result = McpSchema.Tool.builder()
                .name(toolDescriptor.getName())
                .title(toolDescriptor.getTitle())
                .description(toolDescriptor.getDescription())
                .inputSchema(createInputSchema(toolDescriptor.getFields()));
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
    
    private McpSchema.JsonSchema createInputSchema(final List<MCPToolFieldDefinition> fields) {
        Map<String, Object> properties = new LinkedHashMap<>(fields.size(), 1F);
        List<String> required = new ArrayList<>(fields.size());
        for (MCPToolFieldDefinition each : fields) {
            properties.put(each.getName(), each.getValueDefinition().toSchemaFragment());
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        return new McpSchema.JsonSchema("object", properties, required, false, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private McpSchema.ToolAnnotations createToolAnnotations(final MCPToolAnnotations annotations) {
        return new McpSchema.ToolAnnotations(annotations.getTitle(), annotations.getReadOnlyHint(), annotations.getDestructiveHint(), annotations.getIdempotentHint(),
                annotations.getOpenWorldHint(), annotations.getReturnDirect());
    }
    
    private McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
        MCPResponse response = toolController.handle(exchange.sessionId(), request.name(), arguments);
        Map<String, Object> payload = response.toPayload();
        if (shouldElicit(exchange, request.name(), payload)) {
            return MCPTransportPayloadUtils.createCallToolResult(handleElicitation(exchange, request.name(), arguments, response, payload));
        }
        return MCPTransportPayloadUtils.createCallToolResult(response);
    }
    
    private boolean shouldElicit(final McpSyncServerExchange exchange, final String toolName, final Map<String, Object> payload) {
        return isPlanningTool(toolName) && supportsFormElicitation(exchange) && hasClarificationQuestions(payload);
    }
    
    private boolean isPlanningTool(final String toolName) {
        return "plan_encrypt_rule".equals(toolName) || "plan_mask_rule".equals(toolName);
    }
    
    private boolean supportsFormElicitation(final McpSyncServerExchange exchange) {
        McpSchema.ClientCapabilities clientCapabilities = exchange.getClientCapabilities();
        if (null == clientCapabilities || null == clientCapabilities.elicitation()) {
            return false;
        }
        McpSchema.ClientCapabilities.Elicitation elicitation = clientCapabilities.elicitation();
        return null != elicitation.form() || null == elicitation.url();
    }
    
    private boolean hasClarificationQuestions(final Map<String, Object> payload) {
        Object clarificationQuestions = payload.get("clarification_questions");
        return clarificationQuestions instanceof List<?> && !((List<?>) clarificationQuestions).isEmpty();
    }
    
    private MCPResponse handleElicitation(final McpSyncServerExchange exchange, final String toolName, final Map<String, Object> arguments,
                                          final MCPResponse fallbackResponse, final Map<String, Object> payload) {
        McpSchema.ElicitResult elicitedResult = exchange.createElicitation(createElicitRequest(toolName, payload));
        if (McpSchema.ElicitResult.Action.ACCEPT != elicitedResult.action() || null == elicitedResult.content()) {
            return fallbackResponse;
        }
        return toolController.handle(exchange.sessionId(), toolName, mergeElicitedArguments(arguments, payload, elicitedResult.content()));
    }
    
    private McpSchema.ElicitRequest createElicitRequest(final String toolName, final Map<String, Object> payload) {
        return McpSchema.ElicitRequest.builder()
                .message(String.format("Provide missing ShardingSphere workflow inputs for `%s`.", toolName))
                .requestedSchema(createElicitRequestedSchema(payload))
                .meta(Map.of("tool", toolName, "plan_id", Objects.toString(payload.get("plan_id"), "")))
                .build();
    }
    
    private Map<String, Object> createElicitRequestedSchema(final Map<String, Object> payload) {
        List<?> clarificationQuestions = (List<?>) payload.get("clarification_questions");
        Map<String, Object> properties = new LinkedHashMap<>(clarificationQuestions.size(), 1F);
        List<String> required = new LinkedList<>();
        for (Object each : clarificationQuestions) {
            if (each instanceof Map<?, ?>) {
                addElicitProperty(properties, required, (Map<?, ?>) each);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }
    
    private void addElicitProperty(final Map<String, Object> properties, final List<String> required, final Map<?, ?> question) {
        String field = Objects.toString(question.get("field"), "").trim();
        if (field.isEmpty()) {
            return;
        }
        properties.put(field, createElicitPropertySchema(question));
        required.add(field);
    }
    
    private Map<String, Object> createElicitPropertySchema(final Map<?, ?> question) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("type", "boolean".equals(Objects.toString(question.get("input_type"), "")) ? "boolean" : "string");
        result.put("description", Objects.toString(question.get("display_message"), ""));
        if (Boolean.TRUE.equals(question.get("secret"))) {
            result.put("format", "password");
        }
        return result;
    }
    
    private Map<String, Object> mergeElicitedArguments(final Map<String, Object> arguments, final Map<String, Object> payload, final Map<String, Object> elicitedContent) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        if (!result.containsKey("plan_id") && payload.containsKey("plan_id")) {
            result.put("plan_id", payload.get("plan_id"));
        }
        for (Entry<String, Object> entry : elicitedContent.entrySet()) {
            putElicitedArgument(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private void putElicitedArgument(final Map<String, Object> arguments, final String field, final Object value) {
        if (null == value || value instanceof String stringValue && stringValue.trim().isEmpty()) {
            return;
        }
        int separatorIndex = field.indexOf('.');
        if (0 < separatorIndex && isAlgorithmPropertiesArgument(field.substring(0, separatorIndex))) {
            putNestedArgument(arguments, field.substring(0, separatorIndex), field.substring(separatorIndex + 1), value);
        } else if (field.startsWith("requires_") || "field_semantics".equals(field)) {
            putNestedArgument(arguments, "structured_intent_evidence", field, value);
        } else {
            arguments.put(field, value);
        }
    }
    
    private boolean isAlgorithmPropertiesArgument(final String argumentName) {
        return "primary_algorithm_properties".equals(argumentName) || "assisted_query_algorithm_properties".equals(argumentName) || "like_query_algorithm_properties".equals(argumentName);
    }
    
    private void putNestedArgument(final Map<String, Object> arguments, final String argumentName, final String fieldName, final Object value) {
        Map<String, Object> nestedArguments = createNestedArguments(arguments.get(argumentName));
        nestedArguments.put(fieldName, value);
        arguments.put(argumentName, nestedArguments);
    }
    
    private Map<String, Object> createNestedArguments(final Object rawValue) {
        Map<String, Object> result = new LinkedHashMap<>(rawValue instanceof Map<?, ?> ? ((Map<?, ?>) rawValue).size() : 4, 1F);
        if (rawValue instanceof Map<?, ?>) {
            for (Entry<?, ?> entry : ((Map<?, ?>) rawValue).entrySet()) {
                result.put(Objects.toString(entry.getKey(), ""), entry.getValue());
            }
        }
        return result;
    }
}
