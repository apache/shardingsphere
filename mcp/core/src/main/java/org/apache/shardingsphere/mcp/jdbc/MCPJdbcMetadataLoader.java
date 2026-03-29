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

package org.apache.shardingsphere.mcp.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.Objects;
import java.util.Set;

/**
 * MCP JDBC metadata loader.
 */
public final class MCPJdbcMetadataLoader {
    
    /**
     * Load metadata catalog.
     *
     * @param runtimeDatabases runtime database configurations
     * @return metadata catalog
     * @throws IllegalStateException when runtime metadata cannot be loaded from one configured database
     */
    public MetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, String> databaseTypes = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        List<MetadataObject> metadataObjects = new LinkedList<>();
        Map<String, RuntimeDatabaseDescriptor> databaseDescriptors = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            String databaseName = entry.getKey();
            databaseTypes.put(databaseName, entry.getValue().getDatabaseType());
            try (Connection connection = entry.getValue().openConnection(databaseName)) {
                RuntimeMetadataSnapshot metadataSnapshot = loadRuntimeMetadataSnapshot(databaseName, connection, connection.getMetaData());
                metadataObjects.addAll(metadataSnapshot.getMetadataObjects());
                databaseDescriptors.put(databaseName, metadataSnapshot.getRuntimeDatabaseDescriptor());
            } catch (final SQLException ex) {
                throw new IllegalStateException(String.format("Failed to load metadata for database `%s`.", databaseName), ex);
            }
        }
        return new MetadataCatalog(databaseTypes, metadataObjects, databaseDescriptors);
    }
    
    private RuntimeMetadataSnapshot loadRuntimeMetadataSnapshot(final String databaseName, final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        List<MetadataObject> metadataObjects = new LinkedList<>();
        Set<MetadataObjectType> discoveredMetadataObjectTypes = new LinkedHashSet<>();
        Set<String> foundSchemas = new LinkedHashSet<>();
        loadTables(databaseName, databaseMetaData, metadataObjects, discoveredMetadataObjectTypes, foundSchemas);
        loadViews(databaseName, databaseMetaData, metadataObjects, discoveredMetadataObjectTypes, foundSchemas);
        String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
        return new RuntimeMetadataSnapshot(metadataObjects, new RuntimeDatabaseDescriptor(databaseVersion, discoveredMetadataObjectTypes, resolveDefaultSchema(connection, foundSchemas)));
    }
    
    private void loadTables(final String databaseName, final DatabaseMetaData databaseMetaData,
                            final Collection<MetadataObject> metadataObjects, final Collection<MetadataObjectType> discoveredMetadataObjectTypes,
                            final Collection<String> foundSchemas) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schemaName = Objects.toString(tables.getString("TABLE_SCHEM"), "").trim();
                if (isSystemSchema(schemaName)) {
                    continue;
                }
                String tableName = Objects.toString(tables.getString("TABLE_NAME"), "").trim();
                if (tableName.isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schemaName, metadataObjects, discoveredMetadataObjectTypes, foundSchemas);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.TABLE, tableName, "", ""));
                discoveredMetadataObjectTypes.add(MetadataObjectType.TABLE);
                loadColumns(databaseName, databaseMetaData, schemaName, tableName, "TABLE", metadataObjects, discoveredMetadataObjectTypes);
                loadIndexes(databaseName, databaseMetaData, schemaName, tableName, metadataObjects, discoveredMetadataObjectTypes);
            }
        }
    }
    
    private void loadViews(final String databaseName, final DatabaseMetaData databaseMetaData,
                           final List<MetadataObject> metadataObjects, final Set<MetadataObjectType> discoveredMetadataObjectTypes, final Set<String> foundSchemas) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schemaName = Objects.toString(views.getString("TABLE_SCHEM"), "").trim();
                if (isSystemSchema(schemaName)) {
                    continue;
                }
                String viewName = Objects.toString(views.getString("TABLE_NAME"), "").trim();
                if (viewName.isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schemaName, metadataObjects, discoveredMetadataObjectTypes, foundSchemas);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.VIEW, viewName, "", ""));
                discoveredMetadataObjectTypes.add(MetadataObjectType.VIEW);
                loadColumns(databaseName, databaseMetaData, schemaName, viewName, "VIEW", metadataObjects, discoveredMetadataObjectTypes);
            }
        }
    }
    
    private boolean isSystemSchema(final String schemaName) {
        String upperSchemaName = schemaName.toUpperCase();
        return "INFORMATION_SCHEMA".equals(upperSchemaName) || "PG_CATALOG".equals(upperSchemaName) || "SYSTEM_LOBS".equals(upperSchemaName);
    }
    
    private void registerSchema(final String databaseName, final String schema,
                                final Collection<MetadataObject> metadataObjects, final Collection<MetadataObjectType> discoveredMetadataObjectTypes, final Collection<String> foundSchemas) {
        if (schema.isEmpty() || !foundSchemas.add(schema)) {
            return;
        }
        discoveredMetadataObjectTypes.add(MetadataObjectType.SCHEMA);
        metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.SCHEMA, schema, "", ""));
    }
    
    private void loadColumns(final String databaseName, final DatabaseMetaData databaseMetaData, final String schemaName,
                             final String objectName, final String parentObjectType, final Collection<MetadataObject> metadataObjects,
                             final Collection<MetadataObjectType> discoveredMetadataObjectTypes) throws SQLException {
        try (ResultSet columns = databaseMetaData.getColumns(null, getSchemaPattern(schemaName), objectName, "%")) {
            while (columns.next()) {
                String columnName = Objects.toString(columns.getString("COLUMN_NAME"), "").trim();
                if (columnName.isEmpty()) {
                    continue;
                }
                discoveredMetadataObjectTypes.add(MetadataObjectType.COLUMN);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.COLUMN, columnName, parentObjectType, objectName));
            }
        }
    }
    
    private void loadIndexes(final String databaseName, final DatabaseMetaData databaseMetaData, final String schemaName,
                             final String tableName, final Collection<MetadataObject> metadataObjects, final Collection<MetadataObjectType> discoveredMetadataObjectTypes) throws SQLException {
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, getSchemaPattern(schemaName), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = Objects.toString(indexes.getString("INDEX_NAME"), "").trim();
                if (indexName.isEmpty()) {
                    continue;
                }
                if (!indexNames.add(indexName)) {
                    continue;
                }
                discoveredMetadataObjectTypes.add(MetadataObjectType.INDEX);
                metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.INDEX, indexName, "TABLE", tableName));
            }
        }
    }
    
    private String getSchemaPattern(final String schema) {
        String result = Objects.toString(schema, "").trim();
        return result.isEmpty() ? null : result;
    }
    
    private String resolveDefaultSchema(final Connection connection, final Collection<String> schemas) throws SQLException {
        String currentSchema = Objects.toString(connection.getSchema(), "").trim();
        if (currentSchema.isEmpty()) {
            return schemas.isEmpty() ? "" : schemas.iterator().next();
        }
        return schemas.stream().filter(currentSchema::equalsIgnoreCase).findFirst().orElse(currentSchema);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private static final class RuntimeMetadataSnapshot {
        
        private final List<MetadataObject> metadataObjects;
        
        private final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor;
    }
}
