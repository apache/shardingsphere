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

package org.apache.shardingsphere.mcp.feature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MCP feature provider registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPFeatureProviderRegistry {
    
    /**
     * Load registered tool handlers from MCP feature providers.
     *
     * @return tool handlers
     */
    public static Collection<ToolHandler> loadToolHandlers() {
        Collection<ToolHandler> result = new LinkedList<>();
        for (MCPFeatureProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)) {
            result.addAll(createToolHandlers(each));
        }
        return List.copyOf(result);
    }
    
    /**
     * Load registered resource handlers from MCP feature providers.
     *
     * @return resource handlers
     */
    public static Collection<ResourceHandler> loadResourceHandlers() {
        Collection<ResourceHandler> result = new LinkedList<>();
        for (MCPFeatureProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)) {
            result.addAll(createResourceHandlers(each));
        }
        return List.copyOf(result);
    }
    
    static Collection<ToolHandler> createToolHandlers(final MCPFeatureProvider featureProvider) {
        Collection<ToolHandler> handlers = Objects.requireNonNull(featureProvider.getToolHandlers(),
                () -> String.format("Tool handlers are required for `%s`.", featureProvider.getClass().getName()));
        handlers.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Tool handler is required for `%s`.", featureProvider.getClass().getName())));
        return List.copyOf(handlers);
    }
    
    static Collection<ResourceHandler> createResourceHandlers(final MCPFeatureProvider featureProvider) {
        Collection<ResourceHandler> handlers = Objects.requireNonNull(featureProvider.getResourceHandlers(),
                () -> String.format("Resource handlers are required for `%s`.", featureProvider.getClass().getName()));
        handlers.forEach(each -> Objects.requireNonNull(each,
                () -> String.format("Resource handler is required for `%s`.", featureProvider.getClass().getName())));
        return List.copyOf(handlers);
    }
}
