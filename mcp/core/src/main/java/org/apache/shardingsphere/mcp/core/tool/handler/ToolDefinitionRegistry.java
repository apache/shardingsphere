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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.handler.MCPHandlerContexts;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tool definition registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolDefinitionRegistry {
    
    private static final Map<String, MCPToolDefinition> REGISTERED_TOOL_DEFINITIONS;
    
    static {
        REGISTERED_TOOL_DEFINITIONS = createRegisteredToolDefinitions(
                ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class).stream().flatMap(each -> each.getToolHandlers().stream()).toList());
        validateRegisteredToolDescriptors();
    }
    
    private static Map<String, MCPToolDefinition> createRegisteredToolDefinitions(final Collection<MCPToolHandler<?>> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No tool handlers are registered."));
        Map<String, MCPToolDefinition> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (MCPToolHandler<?> each : handlers) {
            String toolName = each.getToolName();
            ShardingSpherePreconditions.checkNotEmpty(toolName, () -> new IllegalArgumentException(String.format("Tool name is required for `%s`.", each.getClass().getName())));
            MCPHandlerContexts.validateContextType(each.getContextType(), each.getClass());
            MCPToolDefinition previousDefinition = result.get(toolName);
            ShardingSpherePreconditions.checkState(null == previousDefinition, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", toolName, previousDefinition.getHandler().getClass().getName(), each.getClass().getName())));
            result.put(toolName, createToolDefinition(toolName, each));
        }
        return result;
    }
    
    private static MCPToolDefinition createToolDefinition(final String toolName, final MCPToolHandler<?> handler) {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(toolName);
        ShardingSpherePreconditions.checkNotNull(descriptor.getAnnotations(),
                () -> new IllegalArgumentException(String.format("Tool `%s` MCP annotations are required for `%s`.", toolName, handler.getClass().getName())));
        return new MCPToolDefinition(descriptor, handler);
    }
    
    private static void validateRegisteredToolDescriptors() {
        for (MCPToolDescriptor each : MCPDescriptorCatalogIndex.getToolDescriptors()) {
            ShardingSpherePreconditions.checkState(REGISTERED_TOOL_DEFINITIONS.containsKey(each.getName()),
                    () -> new IllegalStateException(String.format("MCP tool descriptor `%s` has no registered handler.", each.getName())));
        }
    }
    
    /**
     * Get supported tools.
     *
     * @return supported tools
     */
    public static Collection<String> getSupportedTools() {
        return REGISTERED_TOOL_DEFINITIONS.keySet().stream().toList();
    }
    
    /**
     * Get supported tool descriptors.
     *
     * @return supported tool descriptors
     */
    public static List<MCPToolDescriptor> getSupportedToolDescriptors() {
        return REGISTERED_TOOL_DEFINITIONS.values().stream().map(MCPToolDefinition::getDescriptor).toList();
    }
    
    /**
     * Get tool definition.
     *
     * @param toolName tool name
     * @return tool definition
     */
    public static MCPToolDefinition getToolDefinition(final String toolName) {
        return Optional.ofNullable(REGISTERED_TOOL_DEFINITIONS.get(toolName)).orElseThrow(() -> new UnsupportedToolException(toolName));
    }
    
    /**
     * Dispatch tool call to tool definition.
     *
     * @param requestScope request scope
     * @param definition tool definition
     * @param sessionId session identifier
     * @param arguments tool arguments
     * @return tool response
     */
    public static MCPResponse dispatch(final MCPRequestScope requestScope, final MCPToolDefinition definition, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArgumentContract.create(definition.getDescriptor()).validate(arguments);
        return dispatch(requestScope, definition.getHandler(), new MCPToolCall(sessionId, arguments));
    }
    
    private static <T extends MCPHandlerContext> MCPResponse dispatch(final MCPRequestScope requestScope, final MCPToolHandler<T> handler, final MCPToolCall toolCall) {
        return handler.handle(handler.getContextType().cast(requestScope), toolCall);
    }
}
