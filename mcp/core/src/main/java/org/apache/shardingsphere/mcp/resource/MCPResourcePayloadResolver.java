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

import java.util.Optional;

/**
 * Resolve one MCP resource URI into a transport-neutral payload.
 */
@RequiredArgsConstructor
public final class MCPResourcePayloadResolver {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Resolve one resource URI.
     *
     * @param resourceUri resource URI
     * @return resolved payload
     */
    public Object resolve(final String resourceUri) {
        Optional<ResourceUriResolution> resolution = runtimeContext.getResourceUriResolver().resolve(resourceUri);
        if (resolution.isEmpty()) {
            return runtimeContext.getPayloadBuilder().createErrorPayload("invalid_request", "Unsupported resource URI.");
        }
        switch (resolution.get().getType()) {
            case SERVICE_CAPABILITIES:
                return runtimeContext.getCapabilityAssembler().assembleServiceCapability();
            case DATABASE_CAPABILITIES:
                return resolveDatabaseCapabilityPayload(resolution.get().getDatabase().orElse(""));
            default:
                return toResourcePayload(runtimeContext.getMetadataResourceLoader().load(runtimeContext.getMetadataCatalog(), resolution.get().getResourceRequest().orElseThrow()));
        }
    }
    
    private Object resolveDatabaseCapabilityPayload(final String database) {
        MCPPayloadBuilder payloadBuilder = runtimeContext.getPayloadBuilder();
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.map(payloadBuilder::createDatabaseCapabilityPayload)
                .orElseGet(() -> payloadBuilder.createErrorPayload("not_found", "Database capability does not exist."));
    }
    
    private Object toResourcePayload(final ResourceLoadResult loadResult) {
        MCPPayloadBuilder payloadBuilder = runtimeContext.getPayloadBuilder();
        return loadResult.isSuccessful()
                ? payloadBuilder.createMetadataItemsPayload(loadResult.getMetadataObjects(), "")
                : payloadBuilder.createErrorPayload(payloadBuilder.toDomainErrorCode(loadResult.getErrorCode().orElse(MCPErrorCode.INVALID_REQUEST)), loadResult.getMessage());
    }
}
