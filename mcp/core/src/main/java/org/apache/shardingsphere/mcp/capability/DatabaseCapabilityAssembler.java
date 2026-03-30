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

package org.apache.shardingsphere.mcp.capability;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolver;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;

import java.util.Optional;
import java.util.Set;

/**
 * Assemble MCP service-level and database-level capability views.
 */
@RequiredArgsConstructor
public final class DatabaseCapabilityAssembler {
    
    private static final Set<StatementClass> SUPPORTED_STATEMENT_CLASSES = Set.of(StatementClass.values());
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    private final ResourceUriResolver resourceUriResolver = new ResourceUriResolver();
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    /**
     * Assemble the service-level capability surface.
     *
     * @return service-level capability
     */
    public ServiceCapability assembleServiceCapability() {
        return new ServiceCapability(resourceUriResolver.getSupportedResources(), toolCatalog.getSupportedTools(), SUPPORTED_STATEMENT_CLASSES);
    }
    
    /**
     * Assemble the database-level capability view for one logical database.
     *
     * @param databaseName logical database name
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> assembleDatabaseCapability(final String databaseName) {
        if (null == databaseMetadataSnapshots) {
            return Optional.empty();
        }
        Optional<String> databaseType = databaseMetadataSnapshots.findDatabaseType(databaseName);
        return databaseType.isPresent() ? assembleDatabaseCapability(databaseName, databaseType.get()) : Optional.empty();
    }
    
    /**
     * Assemble the database-level capability view for one logical database.
     *
     * @param databaseName logical database name
     * @param databaseType database type
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> assembleDatabaseCapability(final String databaseName, final String databaseType) {
        return DatabaseCapabilityCatalog.find(databaseName, databaseType, getDatabaseVersion(databaseName));
    }
    
    private String getDatabaseVersion(final String databaseName) {
        if (null == databaseMetadataSnapshots) {
            return "";
        }
        Optional<DatabaseMetadataSnapshot> databaseSnapshot = databaseMetadataSnapshots.findDatabaseSnapshot(databaseName);
        return databaseSnapshot.map(DatabaseMetadataSnapshot::getDatabaseVersion).orElse("");
    }
}
