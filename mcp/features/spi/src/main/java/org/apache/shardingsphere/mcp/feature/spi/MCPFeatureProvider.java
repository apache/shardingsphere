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

package org.apache.shardingsphere.mcp.feature.spi;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;

import java.util.Collection;
import java.util.List;

/**
 * MCP feature provider.
 */
@SingletonSPI
public interface MCPFeatureProvider extends ShardingSphereSPI {
    
    /**
     * Get tool handlers owned by the feature.
     *
     * @return tool handlers
     */
    default Collection<ToolHandler> getToolHandlers() {
        return List.of();
    }
    
    /**
     * Get resource handlers owned by the feature.
     *
     * @return resource handlers
     */
    default Collection<ResourceHandler> getResourceHandlers() {
        return List.of();
    }
}
