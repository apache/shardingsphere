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
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        Collection<ToolHandler> result = new ArrayList<>();
        for (MCPFeatureProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)) {
            result.addAll(null == each.getToolHandlers() ? List.of() : each.getToolHandlers());
        }
        return Collections.unmodifiableList(new ArrayList<>(result));
    }
    
    /**
     * Load registered resource handlers from MCP feature providers.
     *
     * @return resource handlers
     */
    public static Collection<ResourceHandler> loadResourceHandlers() {
        Collection<ResourceHandler> result = new ArrayList<>();
        for (MCPFeatureProvider each : ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)) {
            result.addAll(null == each.getResourceHandlers() ? List.of() : each.getResourceHandlers());
        }
        return Collections.unmodifiableList(new ArrayList<>(result));
    }
}
