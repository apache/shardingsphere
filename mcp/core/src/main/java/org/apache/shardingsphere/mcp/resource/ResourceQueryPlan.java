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

import java.util.Optional;

/**
 * Resource query plan.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ResourceQueryPlan {
    
    private final ResourceReadPlanType type;
    
    private final String database;
    
    private final MetadataResourceQuery metadataResourceQuery;
    
    /**
     * Create one service capability plan.
     *
     * @return resource read plan
     */
    public static ResourceQueryPlan serviceCapabilities() {
        return new ResourceQueryPlan(ResourceReadPlanType.SERVICE_CAPABILITIES, "", null);
    }
    
    /**
     * Create one database capability plan.
     *
     * @param databaseName logical database name
     * @return resource read plan
     */
    public static ResourceQueryPlan databaseCapabilities(final String databaseName) {
        return new ResourceQueryPlan(ResourceReadPlanType.DATABASE_CAPABILITIES, databaseName, null);
    }
    
    /**
     * Create one metadata resource plan.
     *
     * @param metadataResourceQuery metadata resource query
     * @return resource read plan
     */
    public static ResourceQueryPlan metadata(final MetadataResourceQuery metadataResourceQuery) {
        return new ResourceQueryPlan(ResourceReadPlanType.METADATA, "", metadataResourceQuery);
    }
    
    /**
     * Get database name when resolving database capability.
     *
     * @return database name when present
     */
    public Optional<String> getDatabase() {
        return ResourceReadPlanType.DATABASE_CAPABILITIES == type ? Optional.of(database) : Optional.empty();
    }
    
    /**
     * Get metadata resource query when reading metadata resources.
     *
     * @return metadata resource query when present
     */
    public Optional<MetadataResourceQuery> getMetadataResourceQuery() {
        return ResourceReadPlanType.METADATA == type ? Optional.of(metadataResourceQuery) : Optional.empty();
    }
    
    /**
     * Resource read plan type.
     */
    public enum ResourceReadPlanType {
        
        SERVICE_CAPABILITIES,
        
        DATABASE_CAPABILITIES,
        
        METADATA
    }
}
