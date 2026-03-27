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
        Set<String> discoveredSchemas = new LinkedHashSet<>();
        supportedObjectTypes.add(SupportedObjectType.DATABASE);
        loadMetadataObjects(databaseName, databaseMetaData, metadataObjects, supportedObjectTypes, discoveredSchemas);
        return new RuntimeMetadataSnapshot(metadataObjects,
                new RuntimeDatabaseDescriptor(databaseName, databaseType, resolveDatabaseVersion(databaseMetaData), supportedObjectTypes, resolveDefaultSchema(connection, discoveredSchemas)));
    }
    
    private void loadMetadataObjects(final String databaseName, final DatabaseMetaData databaseMetaData, final List<MetadataObject> metadataObjects,
                                     final Set<SupportedObjectType> supportedObjectTypes, final Set<String> discoveredSchemas) throws SQLException {
        loadTables(databaseName, databaseMetaData, metadataObjects, supportedObjectTypes, discoveredSchemas);
        loadViews(databaseName, databaseMetaData, metadataObjects, supportedObjectTypes, discoveredSchemas);
    }
    
    private boolean isSystemSchema(final String schemaName) {
        String upperSchemaName = schemaName.toUpperCase();
        return "INFORMATION_SCHEMA".equals(upperSchemaName) || "PG_CATALOG".equals(upperSchemaName) || "SYSTEM_LOBS".equals(upperSchemaName);
    }
    
    private void loadTables(final String databaseName, final DatabaseMetaData databaseMetaData,
                            final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes,
                            final Set<String> discoveredSchemas) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schema = normalizeSchema(tables.getString("TABLE_SCHEM"));
                if (isSystemSchema(schema)) {
                    continue;
                }
                String tableName = tables.getString("TABLE_NAME");
                if (null == tableName || tableName.trim().isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schema, metadataObjects, supportedObjectTypes, discoveredSchemas);
                String actualTableName = tableName.trim();
                metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.TABLE, actualTableName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.TABLE);
                loadColumns(databaseName, databaseMetaData, schema, actualTableName, "TABLE", metadataObjects, supportedObjectTypes);
                loadIndexes(databaseName, databaseMetaData, schema, actualTableName, metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private void loadViews(final String databaseName, final DatabaseMetaData databaseMetaData,
                           final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes, final Set<String> discoveredSchemas) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schema = normalizeSchema(views.getString("TABLE_SCHEM"));
                if (isSystemSchema(schema)) {
                    continue;
                }
                String viewName = views.getString("TABLE_NAME");
                if (null == viewName || viewName.trim().isEmpty()) {
                    continue;
                }
                registerSchema(databaseName, schema, metadataObjects, supportedObjectTypes, discoveredSchemas);
                String actualViewName = viewName.trim();
                metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.VIEW, actualViewName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.VIEW);
                loadColumns(databaseName, databaseMetaData, schema, actualViewName, "VIEW", metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private void loadColumns(final String databaseName, final DatabaseMetaData databaseMetaData, final String schema,
                             final String objectName, final String parentObjectType, final List<MetadataObject> metadataObjects,
                             final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        try (ResultSet columns = databaseMetaData.getColumns(null, getSchemaPattern(schema), objectName, "%")) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (null == columnName || columnName.trim().isEmpty()) {
                    continue;
                }
                metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.COLUMN, columnName.trim(),
                        parentObjectType, objectName));
                supportedObjectTypes.add(SupportedObjectType.COLUMN);
            }
        }
    }
    
    private void loadIndexes(final String databaseName, final DatabaseMetaData databaseMetaData, final String schema,
                             final String tableName, final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, getSchemaPattern(schema), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if (null == indexName || indexName.trim().isEmpty()) {
                    continue;
                }
                if (!indexNames.add(indexName.trim())) {
                    continue;
                }
                metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.INDEX, indexName.trim(), "TABLE", tableName));
                supportedObjectTypes.add(SupportedObjectType.INDEX);
            }
        }
    }
    
    private void registerSchema(final String databaseName,
                                final String schema, final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes, final Set<String> discoveredSchemas) {
        if (schema.isEmpty() || !discoveredSchemas.add(schema)) {
            return;
        }
        supportedObjectTypes.add(SupportedObjectType.SCHEMA);
        metadataObjects.add(new MetadataObject(databaseName, schema, MetadataObjectType.SCHEMA, schema, "", ""));
    }
    
    private String resolveDefaultSchema(final Connection connection, final Set<String> schemas) throws SQLException {
        String currentSchema = normalizeSchema(connection.getSchema());
        if (!currentSchema.isEmpty()) {
            return resolveMatchingSchema(currentSchema, schemas);
        }
        return schemas.isEmpty() ? "" : schemas.iterator().next();
    }
    
    private String resolveMatchingSchema(final String currentSchema, final Set<String> availableSchemas) {
        for (String each : availableSchemas) {
            if (currentSchema.equalsIgnoreCase(each)) {
                return each;
            }
        }
        return currentSchema;
    }
    
    private String resolveDatabaseVersion(final DatabaseMetaData databaseMetaData) throws SQLException {
        String result = databaseMetaData.getDatabaseProductVersion();
        return null == result ? "" : result.trim();
    }
    
    private String normalizeSchema(final String schema) {
        return null == schema ? "" : schema.trim();
    }
    
    private String getSchemaPattern(final String schema) {
        String result = normalizeSchema(schema);
        return result.isEmpty() ? null : result;
    }
    
    @RequiredArgsConstructor
    private static final class RuntimeMetadataSnapshot {
        
        private final List<MetadataObject> metadataObjects;
        
        private final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor;
        
        private List<MetadataObject> getMetadataObjects() {
            return metadataObjects;
        }
        
        private RuntimeDatabaseDescriptor getRuntimeDatabaseDescriptor() {
            return runtimeDatabaseDescriptor;
        }
    }
}
