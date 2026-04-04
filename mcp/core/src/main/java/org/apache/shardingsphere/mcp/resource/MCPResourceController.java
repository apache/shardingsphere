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
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;

import java.util.Map;

/**
 * MCP resource controller.
 */
@RequiredArgsConstructor
public final class MCPResourceController {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final ResourceDispatcher dispatcher = new ResourceDispatcher();
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return payload
     */
    public Map<String, Object> handle(final String resourceUri) {
        return dispatcher.dispatch(resourceUri, runtimeContext)
                .map(each -> each.toPayload(payloadBuilder)).orElseGet(() -> payloadBuilder.createErrorPayload("invalid_request", "Unsupported resource URI."));
    }
}
