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

package org.apache.shardingsphere.mcp.support.database.metadata.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Request-scoped metadata context.
 */
@RequiredArgsConstructor
public final class RequestScopedMetadataContext {
    
    private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
    
    private final Map<String, Collection<ShardingSphereSchema>> loadedSchemas = new LinkedHashMap<>(4, 1F);
    
    private final Map<RelationKey, List<MCPColumnMetadata>> loadedColumns = new LinkedHashMap<>(16, 1F);
    
    private final Map<SchemaKey, List<MCPColumnMetadata>> loadedSchemaColumns = new LinkedHashMap<>(4, 1F);
    
    private final Map<RelationKey, List<ShardingSphereIndex>> loadedIndexes = new LinkedHashMap<>(16, 1F);
    
    /**
     * Load schema metadata lazily within the current request.
     *
     * @param databaseName database name
     * @return schema metadata
     */
    public Optional<Collection<ShardingSphereSchema>> loadSchemas(final String databaseName) {
        Collection<ShardingSphereSchema> loadedMetadata = loadedSchemas.get(databaseName);
        if (null != loadedMetadata) {
            return Optional.of(loadedMetadata);
        }
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = runtimeDatabases.get(databaseName);
        Optional<RuntimeDatabaseProfile> databaseProfile = databaseCapabilityProvider.findDatabaseProfile(databaseName);
        if (null == runtimeDatabaseConfig || databaseProfile.isEmpty()) {
            return Optional.empty();
        }
        Collection<ShardingSphereSchema> result = metadataLoader.load(databaseName, runtimeDatabaseConfig, databaseProfile.get());
        loadedSchemas.put(databaseName, result);
        return Optional.of(result);
    }
    
    /**
     * Load columns for one relation lazily within the current request.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param relationName table or view name
     * @return column metadata
     */
    public Optional<List<MCPColumnMetadata>> loadColumns(final String databaseName, final String schemaName, final String relationName) {
        RelationKey key = new RelationKey(databaseName, schemaName, relationName);
        List<MCPColumnMetadata> loadedMetadata = loadedColumns.get(key);
        if (null != loadedMetadata) {
            return Optional.of(loadedMetadata);
        }
        Optional<RuntimeDatabaseBinding> binding = findRuntimeDatabaseBinding(databaseName);
        if (binding.isEmpty()) {
            return Optional.empty();
        }
        List<MCPColumnMetadata> result = metadataLoader.loadColumns(
                databaseName, binding.get().configuration(), binding.get().profile(), schemaName, relationName);
        loadedColumns.put(key, result);
        return Optional.of(result);
    }
    
    /**
     * Load all columns in one schema lazily within the current request.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return column metadata
     */
    public Optional<List<MCPColumnMetadata>> loadSchemaColumns(final String databaseName, final String schemaName) {
        SchemaKey key = new SchemaKey(databaseName, schemaName);
        List<MCPColumnMetadata> loadedMetadata = loadedSchemaColumns.get(key);
        if (null != loadedMetadata) {
            return Optional.of(loadedMetadata);
        }
        Optional<RuntimeDatabaseBinding> binding = findRuntimeDatabaseBinding(databaseName);
        if (binding.isEmpty()) {
            return Optional.empty();
        }
        List<MCPColumnMetadata> result = metadataLoader.loadSchemaColumns(databaseName, binding.get().configuration(), binding.get().profile(), schemaName);
        loadedSchemaColumns.put(key, result);
        return Optional.of(result);
    }
    
    /**
     * Load indexes for one table lazily within the current request.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata
     */
    public Optional<List<ShardingSphereIndex>> loadIndexes(final String databaseName, final String schemaName, final String tableName) {
        RelationKey key = new RelationKey(databaseName, schemaName, tableName);
        List<ShardingSphereIndex> loadedMetadata = loadedIndexes.get(key);
        if (null != loadedMetadata) {
            return Optional.of(loadedMetadata);
        }
        Optional<RuntimeDatabaseBinding> binding = findRuntimeDatabaseBinding(databaseName);
        if (binding.isEmpty()) {
            return Optional.empty();
        }
        List<ShardingSphereIndex> result = metadataLoader.loadIndexes(databaseName, binding.get().configuration(), binding.get().profile(), schemaName, tableName);
        loadedIndexes.put(key, result);
        return Optional.of(result);
    }
    
    private Optional<RuntimeDatabaseBinding> findRuntimeDatabaseBinding(final String databaseName) {
        RuntimeDatabaseConfiguration configuration = runtimeDatabases.get(databaseName);
        Optional<RuntimeDatabaseProfile> profile = databaseCapabilityProvider.findDatabaseProfile(databaseName);
        return null == configuration || profile.isEmpty() ? Optional.empty() : Optional.of(new RuntimeDatabaseBinding(configuration, profile.get()));
    }
    
    private record SchemaKey(String databaseName, String schemaName) {
    }
    
    private record RelationKey(String databaseName, String schemaName, String relationName) {
    }
    
    private record RuntimeDatabaseBinding(RuntimeDatabaseConfiguration configuration, RuntimeDatabaseProfile profile) {
    }
    
}
