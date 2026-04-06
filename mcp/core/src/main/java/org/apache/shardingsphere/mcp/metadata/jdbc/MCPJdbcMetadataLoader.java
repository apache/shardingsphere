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

package org.apache.shardingsphere.mcp.metadata.jdbc;

import org.apache.shardingsphere.mcp.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Load database metadata snapshots.
     *
     * @param runtimeDatabases runtime database configurations
     * @return loaded database metadata snapshots
     * @throws IllegalStateException when runtime metadata cannot be loaded from one configured database
     */
    public DatabaseMetadataSnapshots load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            String databaseName = entry.getKey();
            try (Connection connection = entry.getValue().openConnection(databaseName)) {
                databaseMetadataMap.put(databaseName, loadDatabaseSnapshot(databaseName, entry.getValue().getDatabaseType(), connection.getMetaData()));
            } catch (final SQLException ex) {
                throw new IllegalStateException(String.format("Failed to load metadata for database `%s`.", databaseName), ex);
            }
        }
        return new DatabaseMetadataSnapshots(databaseMetadataMap);
    }
    
    private MCPDatabaseMetadata loadDatabaseSnapshot(final String databaseName, final String databaseType, final DatabaseMetaData databaseMetaData) throws SQLException {
        String databaseVersion = Objects.toString(databaseMetaData.getDatabaseProductVersion(), "").trim();
        DatabaseMetadataAccumulator accumulator = new DatabaseMetadataAccumulator(databaseName, databaseType, databaseVersion);
        loadTables(accumulator, databaseMetaData);
        loadViews(accumulator, databaseMetaData);
        return accumulator.build();
    }
    
    private void loadTables(final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
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
                TableMetadataAccumulator tableMetadata = accumulator.getSchemaAccumulator(schemaName).getTableAccumulator(tableName);
                for (String each : loadColumns(databaseMetaData, schemaName, tableName)) {
                    tableMetadata.addColumn(each);
                }
                for (String each : loadIndexes(databaseMetaData, schemaName, tableName)) {
                    tableMetadata.addIndex(each);
                }
            }
        }
    }
    
    private void loadViews(final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
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
                ViewMetadataAccumulator viewMetadata = accumulator.getSchemaAccumulator(schemaName).getViewAccumulator(viewName);
                for (String each : loadColumns(databaseMetaData, schemaName, viewName)) {
                    viewMetadata.addColumn(each);
                }
            }
        }
    }
    
    private boolean isSystemSchema(final String schemaName) {
        return SYSTEM_SCHEMAS.contains(schemaName.toUpperCase(Locale.ENGLISH));
    }
    
    private List<String> loadColumns(final DatabaseMetaData databaseMetaData, final String schemaName, final String objectName) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet columns = databaseMetaData.getColumns(null, getSchemaPattern(schemaName), objectName, "%")) {
            while (columns.next()) {
                String columnName = Objects.toString(columns.getString("COLUMN_NAME"), "").trim();
                if (!columnName.isEmpty()) {
                    result.add(columnName);
                }
            }
        }
        return result;
    }
    
    private List<String> loadIndexes(final DatabaseMetaData databaseMetaData, final String schemaName, final String tableName) throws SQLException {
        List<String> result = new LinkedList<>();
        Set<String> loadedIndexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(null, getSchemaPattern(schemaName), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = Objects.toString(indexes.getString("INDEX_NAME"), "").trim();
                if (!indexName.isEmpty() && loadedIndexNames.add(indexName)) {
                    result.add(indexName);
                }
            }
        }
        return result;
    }
    
    private String getSchemaPattern(final String schemaName) {
        String result = Objects.toString(schemaName, "").trim();
        return result.isEmpty() ? null : result;
    }
    
    private static final class DatabaseMetadataAccumulator {
        
        private final String database;
        
        private final String databaseType;
        
        private final String databaseVersion;
        
        private final Map<String, SchemaMetadataAccumulator> schemaAccumulators = new LinkedHashMap<>(16, 1F);
        
        private DatabaseMetadataAccumulator(final String database, final String databaseType, final String databaseVersion) {
            this.database = database;
            this.databaseType = databaseType;
            this.databaseVersion = databaseVersion;
        }
        
        private SchemaMetadataAccumulator getSchemaAccumulator(final String schema) {
            SchemaMetadataAccumulator result = schemaAccumulators.get(schema);
            if (null == result) {
                result = new SchemaMetadataAccumulator(database, schema);
                schemaAccumulators.put(schema, result);
            }
            return result;
        }
        
        private MCPDatabaseMetadata build() {
            List<MCPSchemaMetadata> schemas = new LinkedList<>();
            for (SchemaMetadataAccumulator each : schemaAccumulators.values()) {
                schemas.add(each.build());
            }
            return new MCPDatabaseMetadata(database, databaseType, databaseVersion, schemas);
        }
    }
    
    private static final class SchemaMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final Map<String, TableMetadataAccumulator> tableAccumulators = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ViewMetadataAccumulator> viewAccumulators = new LinkedHashMap<>(16, 1F);
        
        private SchemaMetadataAccumulator(final String database, final String schema) {
            this.database = database;
            this.schema = schema;
        }
        
        private TableMetadataAccumulator getTableAccumulator(final String table) {
            TableMetadataAccumulator result = tableAccumulators.get(table);
            if (null == result) {
                result = new TableMetadataAccumulator(database, schema, table);
                tableAccumulators.put(table, result);
            }
            return result;
        }
        
        private ViewMetadataAccumulator getViewAccumulator(final String view) {
            ViewMetadataAccumulator result = viewAccumulators.get(view);
            if (null == result) {
                result = new ViewMetadataAccumulator(database, schema, view);
                viewAccumulators.put(view, result);
            }
            return result;
        }
        
        private MCPSchemaMetadata build() {
            List<MCPTableMetadata> tables = new LinkedList<>();
            for (TableMetadataAccumulator each : tableAccumulators.values()) {
                tables.add(each.build());
            }
            List<MCPViewMetadata> views = new LinkedList<>();
            for (ViewMetadataAccumulator each : viewAccumulators.values()) {
                views.add(each.build());
            }
            return new MCPSchemaMetadata(database, schema, tables, views);
        }
    }
    
    private static final class TableMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String table;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, MCPIndexMetadata> indexes = new LinkedHashMap<>(16, 1F);
        
        private TableMetadataAccumulator(final String database, final String schema, final String table) {
            this.database = database;
            this.schema = schema;
            this.table = table;
        }
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new MCPColumnMetadata(database, schema, table, "", column));
        }
        
        private void addIndex(final String index) {
            indexes.putIfAbsent(index, new MCPIndexMetadata(database, schema, table, index));
        }
        
        private MCPTableMetadata build() {
            return new MCPTableMetadata(database, schema, table, new LinkedList<>(columns.values()), new LinkedList<>(indexes.values()));
        }
    }
    
    private static final class ViewMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String view;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private ViewMetadataAccumulator(final String database, final String schema, final String view) {
            this.database = database;
            this.schema = schema;
            this.view = view;
        }
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new MCPColumnMetadata(database, schema, "", view, column));
        }
        
        private MCPViewMetadata build() {
            return new MCPViewMetadata(database, schema, view, new LinkedList<>(columns.values()));
        }
    }
}
