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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.MCPToolController;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP tool elicitation handler.
 */
@RequiredArgsConstructor
final class MCPToolElicitationHandler {
    
    private static final String PLANNING_WORKFLOW_ROLE = "plan";
    
    private final MCPToolController toolController;
    
    boolean shouldElicit(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> payload) {
        return isPlanningTool(toolDescriptor) && supportsFormElicitation(exchange) && hasClarificationQuestions(payload);
    }
    
    MCPResponse handle(final McpSyncServerExchange exchange, final MCPToolDescriptor toolDescriptor, final Map<String, Object> arguments,
                       final MCPResponse fallbackResponse, final Map<String, Object> payload) {
        McpSchema.ElicitResult elicitedResult = exchange.createElicitation(createElicitRequest(toolDescriptor.getName(), payload));
        return McpSchema.ElicitResult.Action.ACCEPT == elicitedResult.action() && null != elicitedResult.content()
                ? toolController.handle(exchange.sessionId(), toolDescriptor.getName(), mergeElicitedArguments(arguments, payload, elicitedResult.content(), toolDescriptor))
                : fallbackResponse;
    }
    
    private boolean isPlanningTool(final MCPToolDescriptor toolDescriptor) {
        return MCPDescriptorRegistry.findToolRuntimeDescriptor(toolDescriptor.getName())
                .map(runtimeDescriptor -> PLANNING_WORKFLOW_ROLE.equals(runtimeDescriptor.getWorkflowRole())).orElse(false);
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
    
    private McpSchema.ElicitRequest createElicitRequest(final String toolName, final Map<String, Object> payload) {
        return McpSchema.ElicitRequest.builder()
                .message(String.format("Provide missing ShardingSphere workflow inputs for `%s`.", toolName))
                .requestedSchema(createElicitRequestedSchema(payload))
                .meta(Map.of(MCPShardingSphereMetadataKeys.TOOL, toolName, MCPShardingSphereMetadataKeys.PLAN_ID, Objects.toString(payload.get("plan_id"), "")))
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
        if (!field.isEmpty()) {
            properties.put(field, createElicitPropertySchema(question));
            required.add(field);
        }
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
    
    private Map<String, Object> mergeElicitedArguments(final Map<String, Object> arguments, final Map<String, Object> payload, final Map<String, Object> elicitedContent,
                                                       final MCPToolDescriptor toolDescriptor) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        if (!result.containsKey("plan_id") && payload.containsKey("plan_id")) {
            result.put("plan_id", payload.get("plan_id"));
        }
        for (Entry<String, Object> entry : elicitedContent.entrySet()) {
            putElicitedArgument(result, entry.getKey(), entry.getValue(), toolDescriptor);
        }
        return result;
    }
    
    private void putElicitedArgument(final Map<String, Object> arguments, final String field, final Object value, final MCPToolDescriptor toolDescriptor) {
        if (null == value || value instanceof String stringValue && stringValue.trim().isEmpty()) {
            return;
        }
        int separatorIndex = field.indexOf('.');
        if (0 < separatorIndex && separatorIndex < field.length() - 1 && isObjectArgument(toolDescriptor, field.substring(0, separatorIndex))) {
            putNestedArgument(arguments, field.substring(0, separatorIndex), field.substring(separatorIndex + 1), value);
            return;
        }
        if (hasArgument(toolDescriptor, field)) {
            arguments.put(field, value);
            return;
        }
        Optional<String> objectArgumentName = findObjectArgumentName(toolDescriptor, field);
        if (objectArgumentName.isPresent()) {
            putNestedArgument(arguments, objectArgumentName.get(), field, value);
            return;
        }
        arguments.put(field, value);
    }
    
    private boolean hasArgument(final MCPToolDescriptor toolDescriptor, final String argumentName) {
        return getInputProperties(toolDescriptor).containsKey(argumentName);
    }
    
    private boolean isObjectArgument(final MCPToolDescriptor toolDescriptor, final String argumentName) {
        Object property = getInputProperties(toolDescriptor).get(argumentName);
        return property instanceof Map && "object".equals(((Map<?, ?>) property).get("type"));
    }
    
    private Optional<String> findObjectArgumentName(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        String result = null;
        for (Entry<String, Object> entry : getInputProperties(toolDescriptor).entrySet()) {
            if (!(entry.getValue() instanceof Map) || !"object".equals(((Map<?, ?>) entry.getValue()).get("type")) || !hasObjectProperty((Map<?, ?>) entry.getValue(), fieldName)) {
                continue;
            }
            if (null != result) {
                return Optional.empty();
            }
            result = entry.getKey();
        }
        return Optional.ofNullable(result);
    }
    
    private boolean hasObjectProperty(final Map<?, ?> objectProperty, final String fieldName) {
        Object properties = objectProperty.get("properties");
        return properties instanceof Map && ((Map<?, ?>) properties).containsKey(fieldName);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getInputProperties(final MCPToolDescriptor toolDescriptor) {
        Object properties = toolDescriptor.getInputSchema().get("properties");
        return properties instanceof Map ? (Map<String, Object>) properties : Map.of();
    }
    
    private void putNestedArgument(final Map<String, Object> arguments, final String argumentName, final String fieldName, final Object value) {
        Map<String, Object> nestedArguments = createNestedArguments(arguments.get(argumentName));
        nestedArguments.put(fieldName, value);
        arguments.put(argumentName, nestedArguments);
    }
    
    private Map<String, Object> createNestedArguments(final Object rawValue) {
        Map<String, Object> result = new LinkedHashMap<>(rawValue instanceof Map<?, ?> ? ((Map<?, ?>) rawValue).size() + 1 : 4, 1F);
        if (rawValue instanceof Map<?, ?>) {
            for (Entry<?, ?> entry : ((Map<?, ?>) rawValue).entrySet()) {
                result.put(Objects.toString(entry.getKey(), ""), entry.getValue());
            }
        }
        return result;
    }
}
