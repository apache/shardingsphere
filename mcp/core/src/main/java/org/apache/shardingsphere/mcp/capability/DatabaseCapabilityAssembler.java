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
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Assemble MCP service-level and database-level capability views.
 */
public final class DatabaseCapabilityAssembler {
    
    private static final List<String> SUPPORTED_RESOURCES = List.of(
            "shardingsphere://capabilities",
            "shardingsphere://databases",
            "shardingsphere://databases/{database}",
            "shardingsphere://databases/{database}/capabilities",
            "shardingsphere://databases/{database}/schemas",
            "shardingsphere://databases/{database}/schemas/{schema}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables",
            "shardingsphere://databases/{database}/schemas/{schema}/views",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
            "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
            "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}");
    
    private static final List<String> SUPPORTED_TOOLS = List.of(
            "list_databases",
            "list_schemas",
            "list_tables",
            "list_views",
            "list_columns",
            "list_indexes",
            "search_metadata",
            "describe_table",
            "describe_view",
            "get_capabilities",
            "execute_query");
    
    private static final Set<StatementClass> SUPPORTED_STATEMENT_CLASSES = Set.of(StatementClass.values());
    
    private final MetadataCatalog metadataCatalog;
    
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
        return new ServiceCapability(SUPPORTED_RESOURCES, SUPPORTED_TOOLS, SUPPORTED_STATEMENT_CLASSES);
    }
    
    /**
     * Assemble the database-level capability view for one logical database.
     *
     * @param database logical database name
     * @param databaseType database type
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> assembleDatabaseCapability(final String database, final String databaseType) {
        Optional<RuntimeDatabaseDescriptor> runtimeDescriptor = metadataCatalog.findRuntimeDatabaseDescriptor(database);
        String actualDatabaseType = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseType).orElse(databaseType);
        String actualDatabaseVersion = runtimeDescriptor.map(RuntimeDatabaseDescriptor::getDatabaseVersion).orElse("");
        return DatabaseCapabilityCatalog.find(database, actualDatabaseType, actualDatabaseVersion);
    }
}
