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
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceDispatcher;

import java.util.Map;
import java.util.Optional;

/**
 * MCP resource controller.
 */
@RequiredArgsConstructor
public final class MCPResourceController {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final ResourceDispatcher resourceDispatcher = new ResourceDispatcher();
    
    private final MetadataResourceReader metadataResourceReader = new MetadataResourceReader();
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    /**
     * Handle resource URI.
     *
     * @param resourceUri resource URI
     * @return payload
     */
    public Map<String, Object> handle(final String resourceUri) {
        Optional<ResourceReadPlan> readPlan = resourceDispatcher.dispatch(resourceUri);
        return readPlan.map(this::handle).orElseGet(() -> payloadBuilder.createErrorPayload("invalid_request", "Unsupported resource URI."));
    }
    
    private Map<String, Object> handle(final ResourceReadPlan readPlan) {
        switch (readPlan.getType()) {
            case SERVICE_CAPABILITIES:
                return payloadBuilder.createServiceCapabilityPayload(runtimeContext.getCapabilityBuilder().buildServiceCapability());
            case DATABASE_CAPABILITIES:
                return createDatabaseCapabilityPayload(readPlan.getDatabase().orElse(""));
            default:
                return toResourcePayload(metadataResourceReader.read(runtimeContext.getDatabaseMetadataSnapshots(), readPlan.getMetadataResourceQuery().orElseThrow()));
        }
    }
    
    private Map<String, Object> createDatabaseCapabilityPayload(final String databaseName) {
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityBuilder().buildDatabaseCapability(databaseName);
        return capability.map(payloadBuilder::createDatabaseCapabilityPayload).orElseGet(() -> payloadBuilder.createErrorPayload("not_found", "Database capability does not exist."));
    }
    
    private Map<String, Object> toResourcePayload(final MetadataResourceResult metadataResourceResult) {
        return metadataResourceResult.isSuccessful()
                ? payloadBuilder.createMetadataItemsPayload(metadataResourceResult.getMetadataObjects(), "")
                : payloadBuilder.createErrorPayload(payloadBuilder.toDomainErrorCode(metadataResourceResult.getErrorCode().orElse(MCPErrorCode.INVALID_REQUEST)), metadataResourceResult.getMessage());
    }
}
