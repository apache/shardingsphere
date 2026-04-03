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
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceDispatcher;

import java.util.Map;

/**
 * MCP resource controller.
 */
public final class MCPResourceController {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final ResourceDispatcher resourceDispatcher;
    
    private final MCPPayloadBuilder payloadBuilder;
    
    /**
     * Create MCP resource controller.
     *
     * @param runtimeContext runtime context
     */
    public MCPResourceController(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        resourceDispatcher = new ResourceDispatcher();
        payloadBuilder = new MCPPayloadBuilder();
    }
    
    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return payload
     */
    public Map<String, Object> handle(final String resourceUri) {
        return resourceDispatcher.dispatch(resourceUri, runtimeContext)
                .map(this::toPayload).orElseGet(() -> payloadBuilder.createErrorPayload("invalid_request", "Unsupported resource URI."));
    }
    
    private Map<String, Object> toPayload(final MCPResourceResult resourceResult) {
        switch (resourceResult.getType()) {
            case SERVICE_CAPABILITY:
                return payloadBuilder.createServiceCapabilityPayload(resourceResult.getServiceCapability());
            case DATABASE_CAPABILITY:
                return payloadBuilder.createDatabaseCapabilityPayload(resourceResult.getDatabaseCapability());
            case METADATA:
                return payloadBuilder.createMetadataItemsPayload(resourceResult.getMetadataObjects(), "");
            case ERROR:
                return payloadBuilder.createErrorPayload(payloadBuilder.toDomainErrorCode(resourceResult.getErrorCode()), resourceResult.getMessage());
            default:
                throw new IllegalStateException(String.format("Unsupported resource result type `%s`.", resourceResult.getType()));
        }
    }
}
