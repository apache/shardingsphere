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
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPHandlerDescriptorUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Tool handler registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolHandlerRegistry {
    
    private static final Map<String, MCPToolHandler<?>> REGISTERED_TOOL_HANDLERS;
    
    private static final Map<String, MCPToolDescriptor> REGISTERED_TOOL_DESCRIPTORS;
    
    private static final Collection<String> SUPPORTED_TOOLS;
    
    private static final List<MCPToolDescriptor> SUPPORTED_TOOL_DESCRIPTORS;
    
    static {
        REGISTERED_TOOL_HANDLERS = createRegisteredTools(
                ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class).stream().flatMap(each -> each.getToolHandlers().stream()).toList());
        REGISTERED_TOOL_DESCRIPTORS = createRegisteredToolDescriptors();
        validateRegisteredToolDescriptors();
        SUPPORTED_TOOLS = REGISTERED_TOOL_HANDLERS.keySet().stream().toList();
        SUPPORTED_TOOL_DESCRIPTORS = REGISTERED_TOOL_DESCRIPTORS.values().stream().toList();
    }
    
    private static Map<String, MCPToolHandler<?>> createRegisteredTools(final Collection<MCPToolHandler<?>> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No tool handlers are registered."));
        Map<String, MCPToolHandler<?>> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (MCPToolHandler<?> each : handlers) {
            String toolName = each.getToolName();
            ShardingSpherePreconditions.checkState(null != toolName && !toolName.isBlank(),
                    () -> new IllegalArgumentException(String.format("Tool name is required for `%s`.", each.getClass().getName())));
            MCPHandlerContexts.validateContextType(each.getContextType(), each.getClass());
            MCPToolHandler<?> previousHandler = result.putIfAbsent(toolName, each);
            ShardingSpherePreconditions.checkState(null == previousHandler, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", toolName, previousHandler.getClass().getName(), each.getClass().getName())));
        }
        return result;
    }
    
    private static Map<String, MCPToolDescriptor> createRegisteredToolDescriptors() {
        Map<String, MCPToolDescriptor> result = new LinkedHashMap<>(REGISTERED_TOOL_HANDLERS.size(), 1F);
        for (Entry<String, MCPToolHandler<?>> entry : REGISTERED_TOOL_HANDLERS.entrySet()) {
            MCPToolDescriptor descriptor = MCPHandlerDescriptorUtils.getRequiredToolDescriptor(entry.getValue());
            ShardingSpherePreconditions.checkState(null != descriptor.getAnnotations(),
                    () -> new IllegalArgumentException(String.format("Tool `%s` MCP annotations are required for `%s`.", entry.getKey(), entry.getValue().getClass().getName())));
            result.put(entry.getKey(), descriptor);
        }
        return result;
    }
    
    private static void validateRegisteredToolDescriptors() {
        for (MCPToolDescriptor each : MCPDescriptorCatalogIndex.getToolDescriptors()) {
            ShardingSpherePreconditions.checkState(REGISTERED_TOOL_HANDLERS.containsKey(each.getName()),
                    () -> new IllegalStateException(String.format("MCP tool descriptor `%s` has no registered handler.", each.getName())));
        }
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
    public static Optional<MCPResponse> dispatch(final MCPRequestScope requestScope, final String sessionId, final String toolName, final Map<String, Object> arguments) {
        Optional<MCPToolHandler<?>> toolHandler = findRegisteredTool(toolName);
        if (toolHandler.isEmpty()) {
            return Optional.empty();
        }
        MCPToolArgumentContract.create(REGISTERED_TOOL_DESCRIPTORS.get(toolName)).validate(arguments);
        return Optional.of(dispatch(requestScope, toolHandler.get(), new MCPToolCall(sessionId, arguments)));
    }
    
    private static <T extends MCPHandlerContext> MCPResponse dispatch(final MCPRequestScope requestScope, final MCPToolHandler<T> toolHandler, final MCPToolCall toolCall) {
        return toolHandler.handle(toolHandler.getContextType().cast(requestScope), toolCall);
    }
}
