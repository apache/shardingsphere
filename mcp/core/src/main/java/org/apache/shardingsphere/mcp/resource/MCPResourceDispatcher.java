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

package org.apache.shardingsphere.mcp.resource;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;
import org.apache.shardingsphere.mcp.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.uri.MCPUriVariables;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * MCP resource dispatcher.
 */
public final class MCPResourceDispatcher {
    
    private final ResourceHandlerRegistry handlerRegistry = new ResourceHandlerRegistry();
    
    /**
     * Get supported resource URI surfaces.
     *
     * @return supported resource URI surfaces
     */
    public List<String> getSupportedResources() {
        return handlerRegistry.getSupportedResources();
    }
    
    /**
     * Dispatch resource URI.
     *
     * @param resourceUri resource URI
     * @param runtimeContext runtime context
     * @return resource response
     */
    public Optional<MCPResourceResponse> dispatch(final String resourceUri, final MCPRuntimeContext runtimeContext) {
        for (Entry<MCPUriPattern, ResourceHandler> each : handlerRegistry.getRegisteredHandlers().entrySet()) {
            Optional<MCPUriVariables> matchedUriVariables = each.getKey().parse(resourceUri);
            if (matchedUriVariables.isPresent()) {
                return Optional.of(each.getValue().handle(runtimeContext, matchedUriVariables.get()));
            }
        }
        return Optional.empty();
    }
}
