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

package org.apache.shardingsphere.mcp.core.tool.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.handler.MCPHandlerContexts;
import org.apache.shardingsphere.mcp.core.handler.MCPHandlerLoader;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tool handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolHandlerRegistry {
    
    private static final Map<String, MCPToolHandler<?>> REGISTERED_TOOL_HANDLERS;
    
    private static final Collection<String> SUPPORTED_TOOLS;
    
    private static final List<MCPToolDescriptor> SUPPORTED_TOOL_DESCRIPTORS;
    
    static {
        REGISTERED_TOOL_HANDLERS = createRegisteredTools();
        SUPPORTED_TOOLS = REGISTERED_TOOL_HANDLERS.keySet();
        SUPPORTED_TOOL_DESCRIPTORS = REGISTERED_TOOL_HANDLERS.values().stream().map(MCPToolHandler::getToolDescriptor).collect(Collectors.toList());
    }
    
    private static Map<String, MCPToolHandler<?>> createRegisteredTools() {
        return createRegisteredTools(MCPHandlerLoader.loadToolHandlers());
    }
    
    static Map<String, MCPToolHandler<?>> createRegisteredTools(final Collection<MCPToolHandler<?>> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No tool handlers are registered."));
        Map<String, MCPToolHandler<?>> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (MCPToolHandler<?> each : handlers) {
            MCPToolDescriptor descriptor = each.getToolDescriptor();
            ShardingSpherePreconditions.checkState(null != descriptor,
                    () -> new IllegalArgumentException(String.format("Tool descriptor is required for `%s`.", each.getClass().getName())));
            String toolName = descriptor.getName();
            ShardingSpherePreconditions.checkState(null != toolName && !toolName.isBlank(),
                    () -> new IllegalArgumentException(String.format("Tool name is required for `%s`.", each.getClass().getName())));
            MCPHandlerContexts.validateContextType(each.getContextType(), each.getClass());
            MCPToolHandler<?> previousHandler = result.putIfAbsent(toolName, each);
            ShardingSpherePreconditions.checkState(null == previousHandler, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", toolName, previousHandler.getClass().getName(), each.getClass().getName())));
        }
        return result;
    }
    
    /**
     * Get supported tools.
     *
     * @return supported tools
     */
    public static Collection<String> getSupportedTools() {
        return SUPPORTED_TOOLS;
    }
    
    /**
     * Get supported tool descriptors.
     *
     * @return supported tool descriptors
     */
    public static List<MCPToolDescriptor> getSupportedToolDescriptors() {
        return SUPPORTED_TOOL_DESCRIPTORS;
    }
    
    /**
     * Find registered tool.
     *
     * @param toolName tool name
     * @return registered tool
     */
    public static Optional<MCPToolHandler<?>> findRegisteredTool(final String toolName) {
        return Optional.ofNullable(REGISTERED_TOOL_HANDLERS.get(toolName));
    }
    
    /**
     * Dispatch tool call to registered tool.
     *
     * @param requestScope request scope
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments tool arguments
     * @return handled response
     */
    public static Optional<MCPResponse> dispatch(final MCPRequestScope requestScope, final String sessionId,
                                                 final String toolName, final Map<String, Object> arguments) {
        Optional<MCPToolHandler<?>> toolHandler = findRegisteredTool(toolName);
        if (toolHandler.isEmpty()) {
            return Optional.empty();
        }
        checkRequiredArguments(arguments, toolHandler.get().getToolDescriptor());
        return Optional.of(dispatch(requestScope, toolHandler.get(), new MCPToolCall(sessionId, arguments)));
    }
    
    private static <T extends MCPHandlerContext> MCPResponse dispatch(final MCPRequestScope requestScope, final MCPToolHandler<T> toolHandler, final MCPToolCall toolCall) {
        return toolHandler.handle(MCPHandlerContexts.resolve(requestScope, toolHandler.getContextType(), toolHandler.getClass()), toolCall);
    }
    
    private static void checkRequiredArguments(final Map<String, Object> arguments, final MCPToolDescriptor toolDescriptor) {
        for (MCPToolFieldDefinition each : toolDescriptor.getFields()) {
            if (!each.isRequired()) {
                continue;
            }
            ShardingSpherePreconditions.checkContainsKey(arguments, each.getName(), () -> createMissingArgumentException(arguments, toolDescriptor, each));
            if (Type.STRING == each.getValueDefinition().getType()) {
                checkRequiredTextArgument(arguments, toolDescriptor, each);
            }
        }
    }
    
    private static void checkRequiredTextArgument(final Map<String, Object> arguments, final MCPToolDescriptor toolDescriptor, final MCPToolFieldDefinition fieldDefinition) {
        String actualValue = Objects.toString(arguments.get(fieldDefinition.getName()), "").trim();
        ShardingSpherePreconditions.checkState(!actualValue.isEmpty(), () -> createMissingArgumentException(arguments, toolDescriptor, fieldDefinition));
    }
    
    private static RuntimeException createMissingArgumentException(final Map<String, Object> arguments, final MCPToolDescriptor toolDescriptor,
                                                                   final MCPToolFieldDefinition fieldDefinition) {
        return "execution_mode".equals(fieldDefinition.getName())
                ? new MCPExecutionModeRequiredException(toolDescriptor.getName(), fieldDefinition.getValueDefinition().getEnumValues().stream().toList(),
                        createExecutionModeSuggestedArguments(arguments))
                : new MCPMissingToolArgumentException(fieldDefinition.getName());
    }
    
    private static Map<String, Object> createExecutionModeSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove("execution_mode");
        result.put("execution_mode", "preview");
        return result;
    }
}
