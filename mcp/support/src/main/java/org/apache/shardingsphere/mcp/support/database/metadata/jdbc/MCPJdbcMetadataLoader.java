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
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.SequenceMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseDialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
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
     * Load schema metadata.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @return schema metadata
     * @throws RuntimeDatabaseConnectionException when metadata loading fails
     */
    public Collection<ShardingSphereSchema> load(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig, final RuntimeDatabaseProfile databaseProfile) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            return loadSchemas(databaseName, databaseProfile, connection, connection.getMetaData());
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, ex);
        }
    }
    
    private Collection<ShardingSphereSchema> loadSchemas(final String databaseName, final RuntimeDatabaseProfile databaseProfile,
                                                         final Connection connection, final DatabaseMetaData databaseMetaData) throws SQLException {
        DatabaseType protocolType = TypedSPILoader.getService(DatabaseType.class, databaseProfile.getDatabaseType());
        MCPDatabaseDialect databaseDialect = MCPDatabaseDialect.of(databaseProfile.getDatabaseType());
        DialectSchemaSemantics defaultSchemaSemantics = databaseDialect.getDefaultSchemaSemantics();
        DatabaseMetadataAccumulator accumulator = new DatabaseMetadataAccumulator(protocolType);
        loadTables(databaseName, defaultSchemaSemantics, databaseDialect, accumulator, databaseMetaData);
        loadViews(databaseName, defaultSchemaSemantics, databaseDialect, accumulator, databaseMetaData);
        loadSequences(databaseName, defaultSchemaSemantics, protocolType, accumulator, connection);
        return accumulator.build();
    }
    
    private void loadTables(final String databaseName, final DialectSchemaSemantics defaultSchemaSemantics, final MCPDatabaseDialect databaseDialect,
                            final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String schemaName = Objects.toString(tables.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(tables.getString("TABLE_CAT"), "").trim();
                if (databaseDialect.isSystemSchema(schemaName, catalogName, defaultSchemaSemantics)) {
                    continue;
                }
                String tableName = Objects.toString(tables.getString("TABLE_NAME"), "").trim();
                if (tableName.isEmpty()) {
                    continue;
                }
                TableMetadataAccumulator tableMetadata = accumulator.getSchemaAccumulator(
                        normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName)).getTableAccumulator(tableName, TableType.TABLE);
                for (String each : loadColumns(databaseMetaData, catalogName, schemaName, tableName)) {
                    tableMetadata.addColumn(each);
                }
                for (String each : loadIndexes(databaseMetaData, catalogName, schemaName, tableName)) {
                    tableMetadata.addIndex(each);
                }
            }
        }
    }
    
    private void loadViews(final String databaseName, final DialectSchemaSemantics defaultSchemaSemantics, final MCPDatabaseDialect databaseDialect,
                           final DatabaseMetadataAccumulator accumulator, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet views = databaseMetaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (views.next()) {
                String schemaName = Objects.toString(views.getString("TABLE_SCHEM"), "").trim();
                String catalogName = Objects.toString(views.getString("TABLE_CAT"), "").trim();
                if (databaseDialect.isSystemSchema(schemaName, catalogName, defaultSchemaSemantics)) {
                    continue;
                }
                String viewName = Objects.toString(views.getString("TABLE_NAME"), "").trim();
                if (viewName.isEmpty()) {
                    continue;
                }
                TableMetadataAccumulator viewMetadata = accumulator.getSchemaAccumulator(
                        normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName)).getTableAccumulator(viewName, TableType.VIEW);
                for (String each : loadColumns(databaseMetaData, catalogName, schemaName, viewName)) {
                    viewMetadata.addColumn(each);
                }
            }
        }
    }
    
    private void loadSequences(final String databaseName, final DialectSchemaSemantics defaultSchemaSemantics, final DatabaseType protocolType,
                               final DatabaseMetadataAccumulator accumulator, final Connection connection) throws SQLException {
        for (Entry<String, Collection<String>> entry : new SequenceMetaDataLoader(protocolType).load(connection).entrySet()) {
            SchemaMetadataAccumulator schema = accumulator.getSchemaAccumulator(normalizeSchemaName(databaseName, defaultSchemaSemantics, entry.getKey()));
            for (String each : entry.getValue()) {
                schema.addSequence(each);
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
        } catch (final SQLFeatureNotSupportedException ignored) {
            return result;
        }
        return result;
    }
    
    private String getPattern(final String value) {
        String result = Objects.toString(value, "").trim();
        return result.isEmpty() ? null : result;
    }
    
    private String normalizeSchemaName(final String databaseName, final DialectSchemaSemantics defaultSchemaSemantics, final String schemaName) {
        String result = Objects.toString(schemaName, "").trim();
        if (!result.isEmpty()) {
            return result;
        }
        return DialectSchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics ? databaseName : result;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class DatabaseMetadataAccumulator {
        
        private final DatabaseType protocolType;
        
        private final Map<String, SchemaMetadataAccumulator> schemaAccumulators = new LinkedHashMap<>(16, 1F);
        
        private SchemaMetadataAccumulator getSchemaAccumulator(final String schema) {
            SchemaMetadataAccumulator result = schemaAccumulators.get(schema);
            if (null == result) {
                result = new SchemaMetadataAccumulator(schema, protocolType);
                schemaAccumulators.put(schema, result);
            }
            return result;
        }
        
        private Collection<ShardingSphereSchema> build() {
            List<ShardingSphereSchema> result = new LinkedList<>();
            for (SchemaMetadataAccumulator each : schemaAccumulators.values()) {
                result.add(each.build());
            }
            return result;
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SchemaMetadataAccumulator {
        
        private final String schema;
        
        private final DatabaseType protocolType;
        
        private final Map<String, TableMetadataAccumulator> tableAccumulators = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ShardingSphereSequence> sequences = new LinkedHashMap<>(16, 1F);
        
        private TableMetadataAccumulator getTableAccumulator(final String name, final TableType type) {
            TableMetadataAccumulator result = tableAccumulators.get(name);
            if (null == result) {
                result = new TableMetadataAccumulator(name, type);
                tableAccumulators.put(name, result);
            }
            return result;
        }
        
        private void addSequence(final String sequence) {
            sequences.putIfAbsent(sequence, new ShardingSphereSequence(sequence));
        }
        
        private ShardingSphereSchema build() {
            List<ShardingSphereTable> tables = new LinkedList<>();
            for (TableMetadataAccumulator each : tableAccumulators.values()) {
                tables.add(each.build());
            }
            return new ShardingSphereSchema(schema, protocolType, tables, Collections.emptyList(), sequences.values());
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TableMetadataAccumulator {
        
        private final String name;
        
        private final TableType type;
        
        private final Map<String, ShardingSphereColumn> columns = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ShardingSphereIndex> indexes = new LinkedHashMap<>(16, 1F);
        
        private void addColumn(final String column) {
            columns.putIfAbsent(column, new ShardingSphereColumn(column, Types.OTHER, false, false, false, true, false, true));
        }
        
        private void addIndex(final String index) {
            indexes.putIfAbsent(index, new ShardingSphereIndex(index, Collections.emptyList(), false));
        }
        
        private ShardingSphereTable build() {
            Collection<ShardingSphereIndex> actualIndexes = TableType.TABLE == type ? new LinkedList<>(indexes.values()) : Collections.emptyList();
            return new ShardingSphereTable(name, new LinkedList<>(columns.values()), actualIndexes, Collections.emptyList(), type);
        }
    }
}
