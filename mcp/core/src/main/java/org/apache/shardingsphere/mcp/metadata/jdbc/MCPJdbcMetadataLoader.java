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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.capability.database.SchemaSemantics;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * MCP JDBC metadata loader.
 */
public final class MCPJdbcMetadataLoader {
    
    private static final Set<String> SYSTEM_SCHEMAS = Set.of("INFORMATION_SCHEMA", "MYSQL", "PERFORMANCE_SCHEMA", "PG_CATALOG", "SHARDINGSPHERE", "SYS", "SYSTEM_LOBS");
    
    private static final String INFORMATION_SCHEMA_SEQUENCE_QUERY =
            "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences";
    
    private static final String SQL_SERVER_SEQUENCE_QUERY =
            "SELECT schemas.name AS SEQUENCE_SCHEMA, seq.name AS SEQUENCE_NAME FROM sys.sequences seq INNER JOIN sys.schemas schemas ON seq.schema_id = schemas.schema_id";
    
    private static final String ORACLE_SEQUENCE_QUERY = "SELECT USER AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM USER_SEQUENCES";
    
    private static final String MARIADB_SEQUENCE_QUERY =
            "SELECT TABLE_SCHEMA AS SEQUENCE_SCHEMA, TABLE_NAME AS SEQUENCE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'SEQUENCE'";
    
    private static final String FIREBIRD_SEQUENCE_QUERY =
            "SELECT '' AS SEQUENCE_SCHEMA, TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE COALESCE(RDB$SYSTEM_FLAG, 0) = 0";
    
    /**
     * Load database metadata.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @return database metadata
     * @throws IllegalStateException when metadata loading fails
     */
    public MCPDatabaseMetadata load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final RuntimeDatabaseProfile databaseProfile) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            return loadDatabaseMetadata(databaseName, databaseProfile, connection, connection.getMetaData());
        } catch (final SQLException ex) {
            throw new IllegalStateException(String.format("Failed to load metadata for database `%s`.", databaseName), ex);
        }
    }
    
    private MCPDatabaseMetadata loadDatabaseMetadata(final String databaseName, final RuntimeDatabaseProfile databaseProfile,
                                                     final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        SchemaSemantics defaultSchemaSemantics = getDefaultSchemaSemantics(databaseProfile.getDatabaseType());
        DatabaseMetadataAccumulator accumulator = new DatabaseMetadataAccumulator(databaseName, databaseProfile.getDatabaseType(), databaseProfile.getDatabaseVersion());
        loadTables(databaseName, defaultSchemaSemantics, accumulator, databaseMetaData);
        loadViews(databaseName, defaultSchemaSemantics, accumulator, databaseMetaData);
        loadSequences(databaseName, defaultSchemaSemantics, databaseProfile.getDatabaseType(), accumulator, connection);
        return accumulator.build();
    }
    
    private void loadTables(final String databaseName, final SchemaSemantics defaultSchemaSemantics,
                            final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schemaName = Objects.toString(tables.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(tables.getString("TABLE_CAT"), "").trim();
                if (isSystemSchema(defaultSchemaSemantics, schemaName, catalogName)) {
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
    
    private void loadViews(final String databaseName, final SchemaSemantics defaultSchemaSemantics,
                           final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schemaName = Objects.toString(views.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(views.getString("TABLE_CAT"), "").trim();
                if (isSystemSchema(defaultSchemaSemantics, schemaName, catalogName)) {
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
    
    private void loadSequences(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final String databaseType,
                               final DatabaseMetadataAccumulator accumulator, final Connection connection) throws SQLException {
        String sequenceQuery = getSequenceQuery(databaseType);
        if (null == sequenceQuery) {
            return;
        }
        try (Statement statement = connection.createStatement(); ResultSet sequences = statement.executeQuery(sequenceQuery)) {
            while (sequences.next()) {
                String schemaName = Objects.toString(sequences.getString("SEQUENCE_SCHEMA"), "").trim();
                if (isSystemSchema(schemaName)) {
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
    
    private String getSequenceQuery(final String databaseType) {
        if (null == databaseType || databaseType.isBlank()) {
            return null;
        }
        switch (databaseType.toUpperCase(Locale.ENGLISH)) {
            case "POSTGRESQL":
            case "OPENGAUSS":
                return INFORMATION_SCHEMA_SEQUENCE_QUERY;
            case "SQLSERVER":
                return SQL_SERVER_SEQUENCE_QUERY;
            case "ORACLE":
                return ORACLE_SEQUENCE_QUERY;
            case "MARIADB":
                return MARIADB_SEQUENCE_QUERY;
            case "FIREBIRD":
                return FIREBIRD_SEQUENCE_QUERY;
            case "H2":
                return "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES";
            default:
                return null;
        }
    }
    
    private boolean isSystemSchema(final String schemaName) {
        return SYSTEM_SCHEMAS.contains(schemaName.toUpperCase(Locale.ENGLISH));
    }
    
    private boolean isSystemSchema(final SchemaSemantics defaultSchemaSemantics, final String schemaName, final String catalogName) {
        return isSystemSchema(schemaName) || SchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics && isSystemSchema(catalogName);
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
    
    private SchemaSemantics getDefaultSchemaSemantics(final String databaseType) {
        return TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, databaseType)
                .map(MCPDatabaseCapabilityOption::getDefaultSchemaSemantics)
                .orElse(SchemaSemantics.NATIVE_SCHEMA);
    }
    
    private String normalizeSchemaName(final String databaseName, final SchemaSemantics defaultSchemaSemantics, final String schemaName) {
        String result = Objects.toString(schemaName, "").trim();
        if (!result.isEmpty()) {
            return result;
        }
        return SchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics ? databaseName : result;
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
        
        private final Map<String, MCPSequenceMetadata> sequences = new LinkedHashMap<>(16, 1F);
        
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
