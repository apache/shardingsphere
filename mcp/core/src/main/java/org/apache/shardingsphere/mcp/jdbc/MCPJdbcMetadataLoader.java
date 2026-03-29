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

import lombok.Getter;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * MCP JDBC metadata loader.
 */
public final class MCPJdbcMetadataLoader {
    
    private static final Set<String> SYSTEM_SCHEMAS = Set.of("INFORMATION_SCHEMA", "PG_CATALOG", "SYSTEM_LOBS");
    
    /**
     * Load metadata catalog.
     *
     * @param runtimeDatabases runtime database configurations
     * @return metadata catalog
     * @throws IllegalStateException when runtime metadata cannot be loaded from one configured database
     */
    public MetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            String databaseName = entry.getKey();
            try (Connection connection = entry.getValue().openConnection(databaseName)) {
                databaseSnapshots.put(databaseName, loadDatabaseSnapshot(databaseName, entry.getValue().getDatabaseType(), connection.getSchema(), connection.getMetaData()));
            } catch (final SQLException ex) {
                throw new IllegalStateException(String.format("Failed to load metadata for database `%s`.", databaseName), ex);
            }
        }
        return new MetadataCatalog(databaseSnapshots);
    }
    
    private DatabaseMetadataSnapshot loadDatabaseSnapshot(final String databaseName, final String databaseType, final String schemaName, final DatabaseMetaData databaseMetaData) throws SQLException {
        String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
        MetadataAccumulator accumulator = loadTables(databaseName, databaseMetaData);
        accumulator.merge(loadViews(databaseName, databaseMetaData));
        return new DatabaseMetadataSnapshot(databaseType, databaseVersion, accumulator.getMetadataObjects(), resolveDefaultSchema(schemaName, accumulator.getFoundSchemas()));
    }
    
    private MetadataAccumulator loadTables(final String databaseName, final DatabaseMetaData databaseMetaData) throws SQLException {
        MetadataAccumulator result = new MetadataAccumulator();
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
                result.registerSchema(databaseName, schemaName);
                result.addMetadataObject(new MetadataObject(databaseName, schemaName, MetadataObjectType.TABLE, tableName, "", ""));
                result.merge(loadColumns(databaseName, databaseMetaData, schemaName, tableName, "TABLE"));
                result.merge(loadIndexes(databaseName, databaseMetaData, schemaName, tableName));
            }
        }
        return result;
    }
    
    private MetadataAccumulator loadViews(final String databaseName, final DatabaseMetaData databaseMetaData) throws SQLException {
        MetadataAccumulator result = new MetadataAccumulator();
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
                result.registerSchema(databaseName, schemaName);
                result.addMetadataObject(new MetadataObject(databaseName, schemaName, MetadataObjectType.VIEW, viewName, "", ""));
                result.merge(loadColumns(databaseName, databaseMetaData, schemaName, viewName, "VIEW"));
            }
        }
        return result;
    }
    
    private boolean isSystemSchema(final String schemaName) {
        return SYSTEM_SCHEMAS.contains(schemaName.toUpperCase(Locale.ENGLISH));
    }
    
    private MetadataAccumulator loadColumns(final String databaseName, final DatabaseMetaData databaseMetaData,
                                            final String schemaName, final String objectName, final String parentObjectType) throws SQLException {
        MetadataAccumulator result = new MetadataAccumulator();
        try (ResultSet columns = databaseMetaData.getColumns(null, getSchemaPattern(schemaName), objectName, "%")) {
            while (columns.next()) {
                String columnName = Objects.toString(columns.getString("COLUMN_NAME"), "").trim();
                if (!columnName.isEmpty()) {
                    result.addMetadataObject(new MetadataObject(databaseName, schemaName, MetadataObjectType.COLUMN, columnName, parentObjectType, objectName));
                }
            }
        }
        return result;
    }
    
    private MetadataAccumulator loadIndexes(final String databaseName, final DatabaseMetaData databaseMetaData, final String schemaName, final String tableName) throws SQLException {
        MetadataAccumulator result = new MetadataAccumulator();
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, getSchemaPattern(schemaName), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = Objects.toString(indexes.getString("INDEX_NAME"), "").trim();
                if (!indexName.isEmpty() && indexNames.add(indexName)) {
                    result.addMetadataObject(new MetadataObject(databaseName, schemaName, MetadataObjectType.INDEX, indexName, "TABLE", tableName));
                }
            }
        }
        return result;
    }
    
    private String getSchemaPattern(final String schemaName) {
        String result = Objects.toString(schemaName, "").trim();
        return result.isEmpty() ? null : result;
    }
    
    private String resolveDefaultSchema(final String currentSchema, final Collection<String> schemas) {
        String schema = Objects.toString(currentSchema, "").trim();
        if (schema.isEmpty()) {
            return schemas.isEmpty() ? "" : schemas.iterator().next();
        }
        return schemas.stream().filter(schema::equalsIgnoreCase).findFirst().orElse(schema);
    }
    
    @Getter
    private static final class MetadataAccumulator {
        
        private final List<MetadataObject> metadataObjects = new LinkedList<>();
        
        private final Set<String> foundSchemas = new LinkedHashSet<>();
        
        private void registerSchema(final String databaseName, final String schemaName) {
            if (schemaName.isEmpty() || !foundSchemas.add(schemaName)) {
                return;
            }
            metadataObjects.add(new MetadataObject(databaseName, schemaName, MetadataObjectType.SCHEMA, schemaName, "", ""));
        }
        
        private void addMetadataObject(final MetadataObject metadataObject) {
            metadataObjects.add(metadataObject);
        }
        
        private void merge(final MetadataAccumulator accumulator) {
            for (MetadataObject each : accumulator.getMetadataObjects()) {
                if (MetadataObjectType.SCHEMA != each.getObjectType() || foundSchemas.add(each.getSchema())) {
                    metadataObjects.add(each);
                }
            }
        }
    }
}
