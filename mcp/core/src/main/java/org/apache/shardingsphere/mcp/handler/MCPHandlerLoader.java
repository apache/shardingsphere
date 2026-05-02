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

package org.apache.shardingsphere.mcp.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MCP handler loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPHandlerLoader {
    
    /**
     * Load tool handlers from MCP handler providers.
     *
     * @return tool handlers
     */
    public static Collection<MCPToolHandler<?>> loadToolHandlers() {
        Collection<MCPToolHandler<?>> result = new LinkedList<>();
        for (MCPHandlerProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)) {
            result.addAll(createToolHandlers(each));
        }
        return List.copyOf(result);
    }
    
    /**
     * Load resource handlers from MCP handler providers.
     *
     * @return resource handlers
     */
    public static Collection<MCPResourceHandler<?>> loadResourceHandlers() {
        Collection<MCPResourceHandler<?>> result = new LinkedList<>();
        for (MCPHandlerProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)) {
            result.addAll(createResourceHandlers(each));
        }
        return List.copyOf(result);
    }
    
    static Collection<MCPToolHandler<?>> createToolHandlers(final MCPHandlerProvider provider) {
        Collection<MCPToolHandler<?>> handlers = Objects.requireNonNull(provider.getToolHandlers(),
                () -> String.format("Tool handlers are required for `%s`.", provider.getClass().getName()));
        handlers.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Tool handler is required for `%s`.", provider.getClass().getName())));
        return List.copyOf(handlers);
    }
    
    static Collection<MCPResourceHandler<?>> createResourceHandlers(final MCPHandlerProvider provider) {
        Collection<MCPResourceHandler<?>> handlers = Objects.requireNonNull(provider.getResourceHandlers(),
                () -> String.format("Resource handlers are required for `%s`.", provider.getClass().getName()));
        handlers.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Resource handler is required for `%s`.", provider.getClass().getName())));
        return List.copyOf(handlers);
    }
}
