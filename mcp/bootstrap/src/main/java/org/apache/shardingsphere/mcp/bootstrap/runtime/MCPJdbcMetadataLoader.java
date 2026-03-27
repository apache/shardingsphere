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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.capability.SupportedObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * MCP JDBC metadata loader.
 */
public final class MCPJdbcMetadataLoader {
    
    private final MCPJdbcConnectionFactory jdbcConnectionFactory = new MCPJdbcConnectionFactory();
    
    /**
     * Load metadata catalog.
     *
     * @param connectionConfigs connection configurations
     * @return metadata catalog
     * @throws IllegalStateException when runtime metadata cannot be loaded from one configured database
     */
    public MetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> connectionConfigs) {
        Map<String, String> databaseTypes = new LinkedHashMap<>(connectionConfigs.size(), 1F);
        List<MetadataObject> metadataObjects = new LinkedList<>();
        Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors = new LinkedHashMap<>(connectionConfigs.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : connectionConfigs.entrySet()) {
            String databaseName = entry.getKey();
            String databaseType = entry.getValue().getDatabaseType();
            try (Connection connection = jdbcConnectionFactory.openConnection(databaseName, entry.getValue())) {
                databaseTypes.put(databaseName, databaseType);
                RuntimeMetadataSnapshot runtimeMetadataSnapshot = loadRuntimeMetadataSnapshot(databaseName, databaseType, connection, connection.getMetaData());
                metadataObjects.addAll(runtimeMetadataSnapshot.getMetadataObjects());
                runtimeDatabaseDescriptors.put(databaseName, runtimeMetadataSnapshot.getRuntimeDatabaseDescriptor());
            } catch (final SQLException ex) {
                throw new IllegalStateException(String.format("Failed to load runtime metadata for database `%s`.", databaseName), ex);
            }
        }
        return new MetadataCatalog(databaseTypes, metadataObjects, runtimeDatabaseDescriptors);
    }
    
    private RuntimeMetadataSnapshot loadRuntimeMetadataSnapshot(final String databaseName,
                                                                final String databaseType, final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        List<MetadataObject> metadataObjects = new LinkedList<>();
        Set<SupportedObjectType> supportedObjectTypes = new LinkedHashSet<>();
        Set<String> foundSchemas = new LinkedHashSet<>();
        supportedObjectTypes.add(SupportedObjectType.DATABASE);
        loadTables(databaseName, databaseMetaData, metadataObjects, supportedObjectTypes, foundSchemas);
        loadViews(databaseName, databaseMetaData, metadataObjects, supportedObjectTypes, foundSchemas);
        String databaseVersion = normalize(databaseMetaData.getDatabaseProductVersion());
        return new RuntimeMetadataSnapshot(
                metadataObjects, new RuntimeDatabaseDescriptor(databaseName, databaseType, databaseVersion, supportedObjectTypes, resolveDefaultSchema(connection, foundSchemas)));
    }
    
    private void loadTables(final String databaseName, final DatabaseMetaData databaseMetaData,
                            final Collection<MetadataObject> metadataObjects, final Collection<SupportedObjectType> supportedObjectTypes,
                            final Collection<String> foundSchemas) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schemaName = normalize(tables.getString("TABLE_SCHEM"));
                if (isSystemSchema(schemaName)) {
                    continue;
                }
                String tableName = normalize(tables.getString("TABLE_NAME"));
                if (tableName.isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schemaName, metadataObjects, supportedObjectTypes, foundSchemas);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.TABLE, tableName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.TABLE);
                loadColumns(databaseName, databaseMetaData, schemaName, tableName, "TABLE", metadataObjects, supportedObjectTypes);
                loadIndexes(databaseName, databaseMetaData, schemaName, tableName, metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private void loadViews(final String databaseName, final DatabaseMetaData databaseMetaData,
                           final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes, final Set<String> foundSchemas) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schemaName = normalize(views.getString("TABLE_SCHEM"));
                if (isSystemSchema(schemaName)) {
                    continue;
                }
                String viewName = normalize(views.getString("TABLE_NAME"));
                if (viewName.trim().isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schemaName, metadataObjects, supportedObjectTypes, foundSchemas);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.VIEW, viewName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.VIEW);
                loadColumns(databaseName, databaseMetaData, schemaName, viewName, "VIEW", metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private boolean isSystemSchema(final String schemaName) {
        String upperSchemaName = schemaName.toUpperCase();
        return "INFORMATION_SCHEMA".equals(upperSchemaName) || "PG_CATALOG".equals(upperSchemaName) || "SYSTEM_LOBS".equals(upperSchemaName);
    }
    
    private void registerSchema(final String databaseName, final String schema,
                                final Collection<MetadataObject> metadataObjects, final Collection<SupportedObjectType> supportedObjectTypes, final Collection<String> foundSchemas) {
        if (schema.isEmpty() || !foundSchemas.add(schema)) {
            return;
        }
        supportedObjectTypes.add(SupportedObjectType.SCHEMA);
        metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.SCHEMA, schema, "", ""));
    }
    
    private void loadColumns(final String databaseName, final DatabaseMetaData databaseMetaData, final String schemaName,
                             final String objectName, final String parentObjectType, final Collection<MetadataObject> metadataObjects,
                             final Collection<SupportedObjectType> supportedObjectTypes) throws SQLException {
        try (ResultSet columns = databaseMetaData.getColumns(null, getSchemaPattern(schemaName), objectName, "%")) {
            while (columns.next()) {
                String columnName = normalize(columns.getString("COLUMN_NAME"));
                if (columnName.isEmpty()) {
                    continue;
                }
                supportedObjectTypes.add(SupportedObjectType.COLUMN);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.COLUMN, columnName, parentObjectType, objectName));
            }
        }
    }
    
    private void loadIndexes(final String databaseName, final DatabaseMetaData databaseMetaData, final String schemaName,
                             final String tableName, final Collection<MetadataObject> metadataObjects, final Collection<SupportedObjectType> supportedObjectTypes) throws SQLException {
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, getSchemaPattern(schemaName), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = normalize(indexes.getString("INDEX_NAME"));
                if (indexName.isEmpty()) {
                    continue;
                }
                if (!indexNames.add(indexName)) {
                    continue;
                }
                supportedObjectTypes.add(SupportedObjectType.INDEX);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.INDEX, indexName.trim(), "TABLE", tableName));
            }
        }
    }
    
    private String getSchemaPattern(final String schema) {
        String result = normalize(schema);
        return result.isEmpty() ? null : result;
    }
    
    private String resolveDefaultSchema(final Connection connection, final Collection<String> schemas) throws SQLException {
        String currentSchema = normalize(connection.getSchema());
        if (currentSchema.isEmpty()) {
            return schemas.isEmpty() ? "" : schemas.iterator().next();
        }
        return schemas.stream().filter(currentSchema::equalsIgnoreCase).findFirst().orElse(currentSchema);
    }
    
    private String normalize(final String value) {
        return null == value ? "" : value.trim();
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private static final class RuntimeMetadataSnapshot {
        
        private final List<MetadataObject> metadataObjects;
        
        private final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor;
    }
}
