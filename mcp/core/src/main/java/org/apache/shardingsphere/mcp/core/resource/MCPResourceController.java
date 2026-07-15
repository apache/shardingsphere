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

package org.apache.shardingsphere.mcp.core.resource;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceDefinitionRegistry;

/**
 * MCP resource controller.
 */
@RequiredArgsConstructor
public final class MCPResourceController {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Handle resource URI.
     *
     * @param sessionId session identifier
     * @param resourceUri resource URI
     * @return successful resource payload
     */
    public MCPSuccessPayload handle(final String sessionId, final String resourceUri) {
        return ResourceDefinitionRegistry.dispatch(new MCPRequestScope(runtimeContext, sessionId), resourceUri)
                .orElseThrow(() -> new UnsupportedResourceUriException(resourceUri));
    }
}
