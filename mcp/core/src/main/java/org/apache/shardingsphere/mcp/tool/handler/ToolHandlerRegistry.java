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
import org.apache.shardingsphere.mcp.feature.MCPFeatureProviderRegistry;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        SUPPORTED_TOOLS = new ArrayList<>(REGISTERED_HANDLERS.keySet());
        SUPPORTED_TOOL_DESCRIPTORS = REGISTERED_HANDLERS.values().stream().map(ToolHandler::getToolDescriptor).toList();
    }
    
    private static Map<String, ToolHandler> createRegisteredHandlers() {
        Collection<ToolHandler> handlers = new ArrayList<>(ShardingSphereServiceLoader.getServiceInstances(ToolHandler.class));
        for (MCPFeatureProvider each : MCPFeatureProviderRegistry.getRegisteredProviders()) {
            handlers.addAll(each.getToolHandlers());
        }
        return createRegisteredHandlers(handlers);
    }
    
    static Map<String, ToolHandler> createRegisteredHandlers(final Collection<ToolHandler> handlers) {
        ShardingSpherePreconditions.checkNotEmpty(handlers, () -> new IllegalStateException("No tool handlers are registered."));
        Map<String, ToolHandler> result = new LinkedHashMap<>(handlers.size(), 1F);
        for (ToolHandler each : handlers) {
            ToolHandler previousHandler = result.putIfAbsent(each.getToolDescriptor().getName(), each);
            ShardingSpherePreconditions.checkState(null == previousHandler, () -> new IllegalArgumentException(
                    String.format("Duplicate tool name `%s` with `%s` and `%s`.", each.getToolDescriptor().getName(), previousHandler.getClass().getName(), each.getClass().getName())));
        }
        return result;
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
}
