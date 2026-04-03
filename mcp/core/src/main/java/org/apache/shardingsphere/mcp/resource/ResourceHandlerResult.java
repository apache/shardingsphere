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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

import java.util.Optional;

/**
 * Resource handler result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceHandlerResult {
    
    @Getter
    private final ResourceHandlerResultType type;
    
    private final ServiceCapability serviceCapability;
    
    private final DatabaseCapability databaseCapability;
    
    private final MetadataResourceResult metadataResourceResult;
    
    private final MCPErrorCode errorCode;
    
    @Getter
    private final String message;
    
    /**
     * Create service capability result.
     *
     * @param capability service capability
     * @return resource handler result
     */
    public static ResourceHandlerResult serviceCapability(final ServiceCapability capability) {
        return new ResourceHandlerResult(ResourceHandlerResultType.SERVICE_CAPABILITY, capability, null, null, null, "");
    }
    
    /**
     * Create database capability result.
     *
     * @param capability database capability
     * @return resource handler result
     */
    public static ResourceHandlerResult databaseCapability(final DatabaseCapability capability) {
        return new ResourceHandlerResult(ResourceHandlerResultType.DATABASE_CAPABILITY, null, capability, null, null, "");
    }
    
    /**
     * Create metadata result.
     *
     * @param result metadata resource result
     * @return resource handler result
     */
    public static ResourceHandlerResult metadata(final MetadataResourceResult result) {
        return new ResourceHandlerResult(ResourceHandlerResultType.METADATA, null, null, result, null, "");
    }
    
    /**
     * Create error result.
     *
     * @param errorCode error code
     * @param message error message
     * @return resource handler result
     */
    public static ResourceHandlerResult error(final MCPErrorCode errorCode, final String message) {
        return new ResourceHandlerResult(ResourceHandlerResultType.ERROR, null, null, null, errorCode, message);
    }
    
    /**
     * Get service capability when present.
     *
     * @return optional service capability
     */
    public Optional<ServiceCapability> getServiceCapability() {
        return ResourceHandlerResultType.SERVICE_CAPABILITY == type ? Optional.of(serviceCapability) : Optional.empty();
    }
    
    /**
     * Get database capability when present.
     *
     * @return optional database capability
     */
    public Optional<DatabaseCapability> getDatabaseCapability() {
        return ResourceHandlerResultType.DATABASE_CAPABILITY == type ? Optional.of(databaseCapability) : Optional.empty();
    }
    
    /**
     * Get metadata resource result when present.
     *
     * @return optional metadata resource result
     */
    public Optional<MetadataResourceResult> getMetadataResourceResult() {
        return ResourceHandlerResultType.METADATA == type ? Optional.of(metadataResourceResult) : Optional.empty();
    }
    
    /**
     * Get error code when present.
     *
     * @return optional error code
     */
    public Optional<MCPErrorCode> getErrorCode() {
        return ResourceHandlerResultType.ERROR == type ? Optional.of(errorCode) : Optional.empty();
    }
    
    /**
     * Resource handler result type.
     */
    public enum ResourceHandlerResultType {
        
        SERVICE_CAPABILITY,
        
        DATABASE_CAPABILITY,
        
        METADATA,
        
        ERROR
    }
}
