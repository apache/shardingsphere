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

package org.apache.shardingsphere.mcp.tool.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tool handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolHandlerRegistry {
    
    private static final Map<String, ToolHandler> REGISTERED_HANDLERS;
    
    private static final List<String> SUPPORTED_TOOLS;
    
    private static final List<MCPToolDescriptor> SUPPORTED_TOOL_DESCRIPTORS;
    
    static {
        REGISTERED_HANDLERS = createRegisteredHandlers();
        SUPPORTED_TOOLS = List.copyOf(REGISTERED_HANDLERS.keySet());
        SUPPORTED_TOOL_DESCRIPTORS = REGISTERED_HANDLERS.values().stream().map(ToolHandler::getToolDescriptor).toList();
    }
    
    private static Map<String, ToolHandler> createRegisteredHandlers() {
        return createRegisteredHandlers(new ArrayList<>(ShardingSphereServiceLoader.getServiceInstances(ToolHandler.class)));
    }
    
    static Map<String, ToolHandler> createRegisteredHandlers(final Collection<ToolHandler> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No tool handlers are registered."));
        Map<String, ToolHandler> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (ToolHandler each : handlers) {
            MCPToolDescriptor descriptor = each.getToolDescriptor();
            ShardingSpherePreconditions.checkState(null != descriptor,
                    () -> new IllegalArgumentException(String.format("Tool descriptor is required for `%s`.", each.getClass().getName())));
            String toolName = descriptor.getName();
            ShardingSpherePreconditions.checkState(null != toolName && !toolName.isBlank(),
                    () -> new IllegalArgumentException(String.format("Tool name is required for `%s`.", each.getClass().getName())));
            ToolHandler previousHandler = result.putIfAbsent(toolName, each);
            ShardingSpherePreconditions.checkState(null == previousHandler, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", toolName, previousHandler.getClass().getName(), each.getClass().getName())));
        }
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Get supported tools.
     *
     * @return supported tools
     */
    public static List<String> getSupportedTools() {
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
     * Find registered handler.
     *
     * @param toolName tool name
     * @return registered handler
     */
    public static Optional<ToolHandler> findRegisteredHandler(final String toolName) {
        return Optional.ofNullable(REGISTERED_HANDLERS.get(toolName));
    }
    
    /**
     * Dispatch tool call to registered handler.
     *
     * @param requestContext feature context
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments tool arguments
     * @return handled response
     */
    public static Optional<MCPResponse> dispatch(final MCPFeatureContext requestContext, final String sessionId,
                                                 final String toolName, final Map<String, Object> arguments) {
        Optional<ToolHandler> toolHandler = findRegisteredHandler(toolName);
        if (toolHandler.isEmpty()) {
            return Optional.empty();
        }
        checkRequiredArguments(arguments, toolHandler.get().getToolDescriptor());
        return Optional.of(toolHandler.get().handle(requestContext, sessionId, arguments));
    }
    
    private static void checkRequiredArguments(final Map<String, Object> arguments, final MCPToolDescriptor toolDescriptor) {
        for (MCPToolFieldDefinition each : toolDescriptor.getFields()) {
            if (!each.isRequired()) {
                continue;
            }
            ShardingSpherePreconditions.checkContainsKey(arguments, each.getName(), () -> new MCPInvalidRequestException(String.format("%s is required.", each.getName())));
            if (Type.STRING == each.getValueDefinition().getType()) {
                checkRequiredTextArgument(arguments, each.getName());
            }
        }
    }
    
    private static void checkRequiredTextArgument(final Map<String, Object> arguments, final String argumentName) {
        String actualValue = Objects.toString(arguments.get(argumentName), "").trim();
        ShardingSpherePreconditions.checkState(!actualValue.isEmpty(), () -> new MCPInvalidRequestException(String.format("%s is required.", argumentName)));
    }
}
