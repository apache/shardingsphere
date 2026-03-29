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

package org.apache.shardingsphere.mcp.bootstrap.transport.resource;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolution;

import java.util.Optional;

final class MCPResourcePayloadResolver {
    
    private final MCPRuntimeContext runtimeContext;
    
    MCPResourcePayloadResolver(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }
    
    Object resolve(final String resourceUri) {
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
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.map(runtimeContext.getPayloadBuilder()::createDatabaseCapabilityPayload)
                .orElseGet(() -> runtimeContext.getPayloadBuilder().createErrorPayload("not_found", "Database capability does not exist."));
    }
    
    private Object toResourcePayload(final ResourceLoadResult loadResult) {
        if (!loadResult.isSuccessful()) {
            return runtimeContext.getPayloadBuilder().createErrorPayload(
                    runtimeContext.getPayloadBuilder().toDomainErrorCode(loadResult.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), loadResult.getMessage());
        }
        return runtimeContext.getPayloadBuilder().createMetadataItemsPayload(loadResult.getMetadataObjects(), "");
    }
}
