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

import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolver;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;

import java.util.Optional;
import java.util.Set;

/**
 * Assemble MCP service-level and database-level capability views.
 */
public final class DatabaseCapabilityAssembler {
    
    private static final Set<StatementClass> SUPPORTED_STATEMENT_CLASSES = Set.of(StatementClass.values());
    
    private final MetadataCatalog metadataCatalog;
    
    private final ResourceUriResolver resourceUriResolver = new ResourceUriResolver();
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    /**
     * Construct an assembler with runtime metadata facts.
     *
     * @param metadataCatalog metadata catalog
     */
    public DatabaseCapabilityAssembler(final MetadataCatalog metadataCatalog) {
        this.metadataCatalog = metadataCatalog;
    }
    
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
     * @param database logical database name
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> assembleDatabaseCapability(final String database) {
        if (null == metadataCatalog) {
            return Optional.empty();
        }
        Optional<String> databaseType = metadataCatalog.findDatabaseType(database);
        return databaseType.isPresent() ? assembleDatabaseCapability(database, databaseType.get()) : Optional.empty();
    }
    
    /**
     * Assemble the database-level capability view for one logical database.
     *
     * @param database logical database name
     * @param databaseType database type
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> assembleDatabaseCapability(final String database, final String databaseType) {
        return DatabaseCapabilityCatalog.find(database, databaseType, getDatabaseVersion(database));
    }
    
    private String getDatabaseVersion(final String database) {
        if (null == metadataCatalog) {
            return "";
        }
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(database);
        return runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseVersion).orElse("");
    }
}
