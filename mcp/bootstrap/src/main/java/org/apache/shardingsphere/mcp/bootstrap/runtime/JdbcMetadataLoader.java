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

import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory.DatabaseConnectionConfiguration;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SupportedObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.RuntimeDatabaseDescriptor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Load runtime metadata from one real JDBC-backed source.
 */
public final class JdbcMetadataLoader {
    
    private final JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory();
    
    /**
     * Load one metadata catalog from runtime connections.
     *
     * @param connectionConfigurations connection configurations
     * @return metadata catalog
     * @throws IllegalStateException when runtime metadata cannot be loaded from one configured database
     */
    public MetadataCatalog load(final Map<String, DatabaseConnectionConfiguration> connectionConfigurations) {
        Map<String, String> databaseTypes = new LinkedHashMap<>(connectionConfigurations.size(), 1F);
        List<MetadataObject> metadataObjects = new LinkedList<>();
        Map<String, RuntimeDatabaseDescriptor> runtimeDatabaseDescriptors = new LinkedHashMap<>(connectionConfigurations.size(), 1F);
        for (DatabaseConnectionConfiguration each : Objects.requireNonNull(connectionConfigurations, "connectionConfigurations cannot be null").values()) {
            try (Connection connection = openConnection(each)) {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                databaseTypes.put(each.getDatabase(), each.getDatabaseType());
                RuntimeMetadataSnapshot runtimeMetadataSnapshot = loadRuntimeMetadataSnapshot(each, connection, databaseMetaData);
                metadataObjects.addAll(runtimeMetadataSnapshot.getMetadataObjects());
                runtimeDatabaseDescriptors.put(each.getDatabase(), runtimeMetadataSnapshot.getRuntimeDatabaseDescriptor());
            } catch (final SQLException ex) {
                throw new IllegalStateException(String.format("Failed to load runtime metadata for database `%s`.", each.getDatabase()), ex);
            }
        }
        return new MetadataCatalog(databaseTypes, metadataObjects, runtimeDatabaseDescriptors);
    }
    
    private RuntimeMetadataSnapshot loadRuntimeMetadataSnapshot(final DatabaseConnectionConfiguration connectionConfiguration,
                                                                final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        LinkedList<MetadataObject> metadataObjects = new LinkedList<>();
        Set<SupportedObjectType> supportedObjectTypes = new LinkedHashSet<>();
        supportedObjectTypes.add(SupportedObjectType.DATABASE);
        List<String> schemas = resolveSchemas(connectionConfiguration, connection, databaseMetaData);
        if (!schemas.isEmpty()) {
            supportedObjectTypes.add(SupportedObjectType.SCHEMA);
        }
        for (String each : schemas) {
            String schema = normalizeSchema(each);
            metadataObjects.add(new MetadataObject(connectionConfiguration.getDatabase(), schema, MetadataObjectType.SCHEMA, schema, "", ""));
            loadTables(connectionConfiguration, databaseMetaData, schema, metadataObjects, supportedObjectTypes);
            loadViews(connectionConfiguration, databaseMetaData, schema, metadataObjects, supportedObjectTypes);
        }
        RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = new RuntimeDatabaseDescriptor(connectionConfiguration.getDatabase(),
                connectionConfiguration.getDatabaseType(), supportedObjectTypes, resolveDefaultSchema(connectionConfiguration, connection, schemas),
                connectionConfiguration.isSupportsCrossSchemaSql(), connectionConfiguration.isSupportsExplainAnalyze());
        return new RuntimeMetadataSnapshot(metadataObjects, runtimeDatabaseDescriptor);
    }
    
    private List<String> resolveSchemas(final DatabaseConnectionConfiguration connectionConfiguration,
                                        final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        if (!connectionConfiguration.getSchemaPattern().isEmpty()) {
            return Collections.singletonList(connectionConfiguration.getSchemaPattern());
        }
        String currentSchema = connection.getSchema();
        if (null != currentSchema && !currentSchema.trim().isEmpty()) {
            return Collections.singletonList(currentSchema.trim());
        }
        LinkedList<String> result = new LinkedList<>();
        try (ResultSet schemas = databaseMetaData.getSchemas()) {
            while (schemas.next()) {
                String schemaName = schemas.getString("TABLE_SCHEM");
                if (null == schemaName || schemaName.trim().isEmpty()) {
                    continue;
                }
                String actualSchema = schemaName.trim();
                if (isSystemSchema(actualSchema)) {
                    continue;
                }
                result.add(actualSchema);
            }
        }
        return result.isEmpty() ? Collections.singletonList(connectionConfiguration.getDefaultSchema()) : result;
    }
    
    private boolean isSystemSchema(final String schemaName) {
        String upperSchemaName = schemaName.toUpperCase();
        return "INFORMATION_SCHEMA".equals(upperSchemaName) || "PG_CATALOG".equals(upperSchemaName) || "SYSTEM_LOBS".equals(upperSchemaName);
    }
    
    private void loadTables(final DatabaseConnectionConfiguration connectionConfiguration, final DatabaseMetaData databaseMetaData, final String schema,
                            final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, schema, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (null == tableName || tableName.trim().isEmpty()) {
                    continue;
                }
                String actualTableName = tableName.trim();
                metadataObjects.add(new MetadataObject(connectionConfiguration.getDatabase(), schema, MetadataObjectType.TABLE, actualTableName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.TABLE);
                loadColumns(connectionConfiguration, databaseMetaData, schema, actualTableName, "TABLE", metadataObjects, supportedObjectTypes);
                loadIndexes(connectionConfiguration, databaseMetaData, schema, actualTableName, metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private void loadViews(final DatabaseConnectionConfiguration connectionConfiguration, final DatabaseMetaData databaseMetaData, final String schema,
                           final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, schema, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String viewName = views.getString("TABLE_NAME");
                if (null == viewName || viewName.trim().isEmpty()) {
                    continue;
                }
                String actualViewName = viewName.trim();
                metadataObjects.add(new MetadataObject(connectionConfiguration.getDatabase(), schema, MetadataObjectType.VIEW, actualViewName, "", ""));
                supportedObjectTypes.add(SupportedObjectType.VIEW);
                loadColumns(connectionConfiguration, databaseMetaData, schema, actualViewName, "VIEW", metadataObjects, supportedObjectTypes);
            }
        }
    }
    
    private void loadColumns(final DatabaseConnectionConfiguration connectionConfiguration, final DatabaseMetaData databaseMetaData, final String schema,
                             final String objectName, final String parentObjectType, final List<MetadataObject> metadataObjects,
                             final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        try (ResultSet columns = databaseMetaData.getColumns(null, schema, objectName, "%")) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (null == columnName || columnName.trim().isEmpty()) {
                    continue;
                }
                metadataObjects.add(new MetadataObject(connectionConfiguration.getDatabase(), schema, MetadataObjectType.COLUMN, columnName.trim(),
                        parentObjectType, objectName));
                supportedObjectTypes.add(SupportedObjectType.COLUMN);
            }
        }
    }
    
    private void loadIndexes(final DatabaseConnectionConfiguration connectionConfiguration, final DatabaseMetaData databaseMetaData, final String schema,
                             final String tableName, final List<MetadataObject> metadataObjects, final Set<SupportedObjectType> supportedObjectTypes) throws SQLException {
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, schema, tableName, false, false)) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if (null == indexName || indexName.trim().isEmpty()) {
                    continue;
                }
                if (!indexNames.add(indexName.trim())) {
                    continue;
                }
                metadataObjects.add(new MetadataObject(connectionConfiguration.getDatabase(), schema, MetadataObjectType.INDEX, indexName.trim(), "TABLE", tableName));
                supportedObjectTypes.add(SupportedObjectType.INDEX);
            }
        }
    }
    
    private Connection openConnection(final DatabaseConnectionConfiguration connectionConfiguration) throws SQLException {
        return jdbcConnectionFactory.openConnection(connectionConfiguration);
    }
    
    private String resolveDefaultSchema(final DatabaseConnectionConfiguration connectionConfiguration, final Connection connection, final List<String> schemas) throws SQLException {
        if (!connectionConfiguration.getDefaultSchema().isEmpty()) {
            return connectionConfiguration.getDefaultSchema();
        }
        String currentSchema = connection.getSchema();
        if (null != currentSchema && !currentSchema.trim().isEmpty()) {
            return currentSchema.trim();
        }
        return schemas.isEmpty() ? "" : normalizeSchema(schemas.get(0));
    }
    
    private String normalizeSchema(final String schema) {
        return null == schema ? "" : schema.trim();
    }
    
    private static final class RuntimeMetadataSnapshot {
        
        private final List<MetadataObject> metadataObjects;
        
        private final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor;
        
        private RuntimeMetadataSnapshot(final List<MetadataObject> metadataObjects, final RuntimeDatabaseDescriptor runtimeDatabaseDescriptor) {
            this.metadataObjects = metadataObjects;
            this.runtimeDatabaseDescriptor = runtimeDatabaseDescriptor;
        }
        
        private List<MetadataObject> getMetadataObjects() {
            return metadataObjects;
        }
        
        private RuntimeDatabaseDescriptor getRuntimeDatabaseDescriptor() {
            return runtimeDatabaseDescriptor;
        }
    }
}
