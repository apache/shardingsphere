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
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Resolved MCP resource URI contract.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUriResolution {
    
    private final ResourceUriResolutionType type;
    
    private final String database;
    
    private final ResourceRequest resourceRequest;
    
    /**
     * Create one service capability resolution.
     *
     * @return resource URI resolution
     */
    public static ResourceUriResolution serviceCapabilities() {
        return new ResourceUriResolution(ResourceUriResolutionType.SERVICE_CAPABILITIES, "", null);
    }
    
    /**
     * Create one database capability resolution.
     *
     * @param databaseName logical database name
     * @return resource URI resolution
     */
    public static ResourceUriResolution databaseCapabilities(final String databaseName) {
        return new ResourceUriResolution(ResourceUriResolutionType.DATABASE_CAPABILITIES, databaseName, null);
    }
    
    /**
     * Create one metadata resource resolution.
     *
     * @param resourceRequest metadata resource request
     * @return resource URI resolution
     */
    public static ResourceUriResolution metadata(final ResourceRequest resourceRequest) {
        return new ResourceUriResolution(ResourceUriResolutionType.METADATA, "", resourceRequest);
    }
    
    /**
     * Get resolution type.
     *
     * @return resolution type
     */
    public ResourceUriResolutionType getType() {
        return type;
    }
    
    /**
     * Get database name when resolving database capability.
     *
     * @return database name when present
     */
    public Optional<String> getDatabase() {
        return ResourceUriResolutionType.DATABASE_CAPABILITIES == type ? Optional.of(database) : Optional.empty();
    }
    
    /**
     * Get metadata resource request when resolving metadata resources.
     *
     * @return metadata resource request when present
     */
    public Optional<ResourceRequest> getResourceRequest() {
        return ResourceUriResolutionType.METADATA == type ? Optional.of(resourceRequest) : Optional.empty();
    }
    
    /**
     * Resource URI resolution type.
     */
    public enum ResourceUriResolutionType {
        
        SERVICE_CAPABILITIES,
        
        DATABASE_CAPABILITIES,
        
        METADATA
    }
}
