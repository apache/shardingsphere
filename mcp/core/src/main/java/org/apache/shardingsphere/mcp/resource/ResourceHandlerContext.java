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
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Resource handler context.
 */
@RequiredArgsConstructor
public final class ResourceHandlerContext {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Get service capability.
     *
     * @return service capability
     */
    public ServiceCapability getServiceCapability() {
        return runtimeContext.getCapabilityBuilder().buildServiceCapability();
    }
    
    /**
     * Find database capability.
     *
     * @param databaseName logical database name
     * @return database capability when present
     */
    public Optional<DatabaseCapability> findDatabaseCapability(final String databaseName) {
        return runtimeContext.getCapabilityBuilder().buildDatabaseCapability(databaseName);
    }
    
    /**
     * Get database metadata snapshots.
     *
     * @return database metadata snapshots
     */
    public DatabaseMetadataSnapshots getDatabaseMetadataSnapshots() {
        return runtimeContext.getDatabaseMetadataSnapshots();
    }
    
    /**
     * Get supported metadata object types.
     *
     * @param databaseName logical database name
     * @return supported metadata object types
     */
    public Set<MetadataObjectType> getSupportedMetadataObjectTypes(final String databaseName) {
        getDatabaseMetadataSnapshots().findDatabaseType(databaseName).orElseThrow(() -> new IllegalStateException("Database does not exist."));
        return findDatabaseCapability(databaseName).map(DatabaseCapability::getSupportedMetadataObjectTypes).orElseGet(Collections::emptySet);
    }
}
