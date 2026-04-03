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
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceDispatcher;

import java.util.Map;

/**
 * MCP resource controller.
 */
public final class MCPResourceController {
    
    private final ResourceDispatcher resourceDispatcher = new ResourceDispatcher();
    
    private final ResourceHandlerContext resourceHandlerContext;
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    /**
     * Create MCP resource controller.
     *
     * @param runtimeContext runtime context
     */
    public MCPResourceController(final MCPRuntimeContext runtimeContext) {
        resourceHandlerContext = new ResourceHandlerContext(runtimeContext);
    }
    
    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return payload
     */
    public Map<String, Object> handle(final String resourceUri) {
        return resourceDispatcher.dispatch(resourceUri)
                .map(each -> each.getHandler().handle(resourceHandlerContext, each.getUriMatch()))
                .map(this::toPayload)
                .orElseGet(() -> payloadBuilder.createErrorPayload("invalid_request", "Unsupported resource URI."));
    }
    
    private Map<String, Object> toPayload(final ResourceHandlerResult resourceHandlerResult) {
        switch (resourceHandlerResult.getType()) {
            case SERVICE_CAPABILITY:
                return payloadBuilder.createServiceCapabilityPayload(resourceHandlerResult.getServiceCapability().orElseThrow());
            case DATABASE_CAPABILITY:
                return payloadBuilder.createDatabaseCapabilityPayload(resourceHandlerResult.getDatabaseCapability().orElseThrow());
            case METADATA:
                return toResourcePayload(resourceHandlerResult.getMetadataResourceResult().orElseThrow());
            case ERROR:
                return payloadBuilder.createErrorPayload(
                        payloadBuilder.toDomainErrorCode(resourceHandlerResult.getErrorCode().orElse(MCPErrorCode.INVALID_REQUEST)),
                        resourceHandlerResult.getMessage());
            default:
                throw new IllegalStateException(String.format("Unsupported resource handler result type `%s`.", resourceHandlerResult.getType()));
        }
    }
    
    private Map<String, Object> toResourcePayload(final MetadataResourceResult metadataResourceResult) {
        return metadataResourceResult.isSuccessful()
                ? payloadBuilder.createMetadataItemsPayload(metadataResourceResult.getMetadataObjects(), "")
                : payloadBuilder.createErrorPayload(payloadBuilder.toDomainErrorCode(metadataResourceResult.getErrorCode().orElse(MCPErrorCode.INVALID_REQUEST)), metadataResourceResult.getMessage());
    }
}
