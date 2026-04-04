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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.MCPErrorPayload;
import org.apache.shardingsphere.mcp.protocol.MCPErrorPayload.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;
import org.apache.shardingsphere.mcp.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.uri.MCPUriVariables;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * MCP resource controller.
 */
@RequiredArgsConstructor
public final class MCPResourceController {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return payload
     */
    public Map<String, Object> handle(final String resourceUri) {
        return dispatch(resourceUri, runtimeContext)
                .map(MCPResourceResponse::toPayload).orElseGet(() -> new MCPErrorPayload(MCPErrorCode.INVALID_REQUEST, "Unsupported resource URI.").toPayload());
    }
    
    private Optional<MCPResourceResponse> dispatch(final String resourceUri, final MCPRuntimeContext runtimeContext) {
        for (Entry<MCPUriPattern, ResourceHandler> each : ResourceHandlerRegistry.getRegisteredHandlers().entrySet()) {
            Optional<MCPUriVariables> matchedUriVariables = each.getKey().parse(resourceUri);
            if (matchedUriVariables.isPresent()) {
                return Optional.of(each.getValue().handle(runtimeContext, matchedUriVariables.get()));
            }
        }
        return Optional.empty();
    }
}
