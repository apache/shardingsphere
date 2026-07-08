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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseDialect;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPViewMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * MCP JDBC metadata loader.
 */
public final class MCPJdbcMetadataLoader {
    
    /**
     * Load database metadata.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @return database metadata
     * @throws RuntimeDatabaseConnectionException when metadata loading fails
     */
    public MCPDatabaseMetadata load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final RuntimeDatabaseProfile databaseProfile) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            return loadDatabaseMetadata(databaseName, databaseProfile, connection, connection.getMetaData());
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, ex);
        }
    }
    
    private MCPDatabaseMetadata loadDatabaseMetadata(final String databaseName, final RuntimeDatabaseProfile databaseProfile,
                                                     final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        MCPDatabaseDialect databaseDialect = MCPDatabaseDialect.of(databaseProfile.getDatabaseType());
        SchemaSemantics defaultSchemaSemantics = databaseDialect.getDefaultSchemaSemantics();
        DatabaseMetadataAccumulator accumulator = new DatabaseMetadataAccumulator(databaseName, databaseProfile.getDatabaseType(), databaseProfile.getDatabaseVersion());
        loadTables(databaseName, defaultSchemaSemantics, databaseDialect, accumulator, databaseMetaData);
        loadViews(databaseName, defaultSchemaSemantics, databaseDialect, accumulator, databaseMetaData);
        loadSequences(databaseName, defaultSchemaSemantics, databaseDialect, accumulator, connection);
        return accumulator.build();
    }
    
    private void loadTables(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final MCPDatabaseDialect databaseDialect,
                            final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schemaName = Objects.toString(tables.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(tables.getString("TABLE_CAT"), "").trim();
                if (databaseDialect.isSystemSchema(schemaName, catalogName, defaultSchemaSemantics)) {
                    continue;
                }
                String normalizedSchemaName = normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName);
                String tableName = Objects.toString(tables.getString("TABLE_NAME"), "").trim();
                if (tableName.isEmpty()) {
                    continue;
                }
                TableMetadataAccumulator tableMetadata = accumulator.getSchemaAccumulator(normalizedSchemaName).getTableAccumulator(tableName);
                for (String each : loadColumns(databaseMetaData, catalogName, schemaName, tableName)) {
                    tableMetadata.addColumn(each);
                }
                for (String each : loadIndexes(databaseMetaData, catalogName, schemaName, tableName)) {
                    tableMetadata.addIndex(each);
                }
            }
        }
    }
    
    private void loadViews(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final MCPDatabaseDialect databaseDialect,
                           final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schemaName = Objects.toString(views.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(views.getString("TABLE_CAT"), "").trim();
                if (databaseDialect.isSystemSchema(schemaName, catalogName, defaultSchemaSemantics)) {
                    continue;
                }
                String normalizedSchemaName = normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName);
                String viewName = Objects.toString(views.getString("TABLE_NAME"), "").trim();
                if (viewName.isEmpty()) {
                    continue;
                }
                ViewMetadataAccumulator viewMetadata = accumulator.getSchemaAccumulator(normalizedSchemaName).getViewAccumulator(viewName);
                for (String each : loadColumns(databaseMetaData, catalogName, schemaName, viewName)) {
                    viewMetadata.addColumn(each);
                }
            }
        }
    }
    
    private void loadSequences(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final MCPDatabaseDialect databaseDialect,
                               final DatabaseMetadataAccumulator accumulator, final Connection connection) throws SQLException {
        Optional<String> sequenceQuery = databaseDialect.getSequenceQuery();
        if (sequenceQuery.isEmpty()) {
            return;
        }
        try (Statement statement = connection.createStatement(); ResultSet sequences = statement.executeQuery(sequenceQuery.get())) {
            while (sequences.next()) {
                String schemaName = Objects.toString(sequences.getString("SEQUENCE_SCHEMA"), "").trim();
                if (databaseDialect.isSystemSchema(schemaName)) {
                    continue;
                }
                String normalizedSchemaName = normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName);
                String sequenceName = Objects.toString(sequences.getString("SEQUENCE_NAME"), "").trim();
                if (!sequenceName.isEmpty()) {
                    accumulator.getSchemaAccumulator(normalizedSchemaName).addSequence(sequenceName);
                }
            }
        }
    }
    
    private List<String> loadColumns(final DatabaseMetaData databaseMetaData, final String catalogName, final String schemaName, final String objectName) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet columns = databaseMetaData.getColumns(getPattern(catalogName), getPattern(schemaName), objectName, "%")) {
            while (columns.next()) {
                String columnName = Objects.toString(columns.getString("COLUMN_NAME"), "").trim();
                if (!columnName.isEmpty()) {
                    result.add(columnName);
                }
            }
        }
        return result;
    }
    
    private List<String> loadIndexes(final DatabaseMetaData databaseMetaData, final String catalogName, final String schemaName, final String tableName) throws SQLException {
        List<String> result = new LinkedList<>();
        Set<String> loadedIndexNames = new LinkedHashSet<>();
        try (ResultSet indexes = databaseMetaData.getIndexInfo(getPattern(catalogName), getPattern(schemaName), tableName, false, false)) {
            while (indexes.next()) {
                String indexName = Objects.toString(indexes.getString("INDEX_NAME"), "").trim();
                if (!indexName.isEmpty() && loadedIndexNames.add(indexName)) {
                    result.add(indexName);
                }
            }
        }
        return result;
    }
    
    private String getPattern(final String value) {
        String result = Objects.toString(value, "").trim();
        return result.isEmpty() ? null : result;
    }
    
    private String normalizeSchemaName(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final String schemaName) {
        String result = Objects.toString(schemaName, "").trim();
        if (!result.isEmpty()) {
            return result;
        }
        return SchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics ? databaseName : result;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class DatabaseMetadataAccumulator {
        
        private final String database;
        
        private final String databaseType;
        
        private final String databaseVersion;
        
        private final Map<String, SchemaMetadataAccumulator> schemaAccumulators = new LinkedHashMap<>(16, 1F);
        
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
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SchemaMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final Map<String, TableMetadataAccumulator> tableAccumulators = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ViewMetadataAccumulator> viewAccumulators = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, MCPSequenceMetadata> sequences = new LinkedHashMap<>(16, 1F);
        
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
        
        private void addSequence(final String sequence) {
            sequences.putIfAbsent(sequence, new MCPSequenceMetadata(database, schema, sequence));
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
            return new MCPSchemaMetadata(database, schema, tables, views, new LinkedList<>(sequences.values()));
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TableMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String table;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, MCPIndexMetadata> indexes = new LinkedHashMap<>(16, 1F);
        
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
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ViewMetadataAccumulator {
        
        private final String database;
        
        private final String schema;
        
        private final String view;
        
        private final Map<String, MCPColumnMetadata> columns = new LinkedHashMap<>(16, 1F);
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new MCPColumnMetadata(database, schema, "", view, column));
        }
        
        private MCPViewMetadata build() {
            return new MCPViewMetadata(database, schema, view, new LinkedList<>(columns.values()));
        }
    }
}
