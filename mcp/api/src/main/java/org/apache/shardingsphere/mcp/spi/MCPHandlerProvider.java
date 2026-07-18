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

package org.apache.shardingsphere.mcp.spi;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;

import java.util.Collection;

/**
 * MCP handler provider.
 *
 * <p>The provider is loaded as a singleton SPI. Handler instances returned by this provider are retained by the runtime and may be invoked concurrently.
 * Implementations must not retain request-scoped state.</p>
 */
@SingletonSPI
public interface MCPHandlerProvider extends ShardingSphereSPI {
    
    /**
     * Get resource handlers.
     *
     * <p>The returned collection may be empty, but must not be {@code null} or contain {@code null} elements.</p>
     *
     * @return resource handlers
     */
    Collection<MCPResourceHandler<?>> getResourceHandlers();
    
    /**
     * Get tool handlers.
     *
     * <p>The returned collection may be empty, but must not be {@code null} or contain {@code null} elements.</p>
     *
     * @return tool handlers
     */
    Collection<MCPToolHandler<?>> getToolHandlers();
    
    /**
     * Get completion handlers.
     *
     * <p>The returned collection may be empty, but must not be {@code null} or contain {@code null} elements.</p>
     *
     * @return completion handlers
     */
    Collection<MCPCompletionHandler<?>> getCompletionHandlers();
}
