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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseDialect;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata.Nullability;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

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
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, databaseProfile.getDatabaseType(), ex);
        }
    }
    
    /**
     * Load columns for one relation.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @param schemaName schema name
     * @param relationName table or view name
     * @return column metadata
     * @throws RuntimeDatabaseConnectionException when metadata loading fails
     */
    public List<MCPColumnMetadata> loadColumns(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig,
                                               final RuntimeDatabaseProfile databaseProfile, final String schemaName, final String relationName) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return loadColumnMetadata(connection, databaseMetaData, databaseProfile, schemaName, escapePattern(databaseMetaData, relationName));
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, databaseProfile.getDatabaseType(), ex);
        }
    }
    
    /**
     * Load all columns in one schema.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @param schemaName schema name
     * @return column metadata
     * @throws RuntimeDatabaseConnectionException when metadata loading fails
     */
    public List<MCPColumnMetadata> loadSchemaColumns(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig,
                                                     final RuntimeDatabaseProfile databaseProfile, final String schemaName) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            return loadColumnMetadata(connection, connection.getMetaData(), databaseProfile, schemaName, "%");
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, databaseProfile.getDatabaseType(), ex);
        }
    }
    
    /**
     * Load indexes for one table.
     *
     * @param databaseName database name
     * @param runtimeDatabaseConfig runtime database configuration
     * @param databaseProfile runtime database profile
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata
     * @throws RuntimeDatabaseConnectionException when metadata loading fails
     */
    public List<ShardingSphereIndex> loadIndexes(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfig,
                                                 final RuntimeDatabaseProfile databaseProfile, final String schemaName, final String tableName) {
        try (Connection connection = runtimeDatabaseConfig.openConnection(databaseName)) {
            return loadIndexMetadata(connection, connection.getMetaData(), databaseProfile, schemaName, tableName);
        } catch (final SQLException ex) {
            throw RuntimeDatabaseConnectionException.connectionFailed(databaseName, databaseProfile.getDatabaseType(), ex);
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
                accumulator.getSchemaAccumulator(normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName)).addRelation(tableName, TableType.TABLE);
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
                accumulator.getSchemaAccumulator(normalizeSchemaName(databaseName, defaultSchemaSemantics, schemaName)).addRelation(viewName, TableType.VIEW);
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
    
    private List<MCPColumnMetadata> loadColumnMetadata(final Connection connection, final DatabaseMetaData databaseMetaData, final RuntimeDatabaseProfile databaseProfile,
                                                       final String schemaName, final String relationNamePattern) throws SQLException {
        DialectSchemaSemantics schemaSemantics = MCPDatabaseDialect.of(databaseProfile.getDatabaseType()).getDefaultSchemaSemantics();
        String catalogName = DialectSchemaSemantics.DATABASE_AS_SCHEMA == schemaSemantics ? resolveCatalogName(connection, schemaName) : null;
        String schemaNamePattern = DialectSchemaSemantics.NATIVE_SCHEMA == schemaSemantics ? escapePattern(databaseMetaData, schemaName) : null;
        List<MCPColumnMetadata> result = queryColumnMetadata(databaseMetaData, catalogName, schemaNamePattern, relationNamePattern);
        if (result.isEmpty() && null != catalogName) {
            result = queryColumnMetadata(databaseMetaData, null, schemaNamePattern, relationNamePattern);
        }
        result.sort(Comparator.comparing(MCPColumnMetadata::getRelationName)
                .thenComparingInt(MCPColumnMetadata::getOrdinalPosition).thenComparing(MCPColumnMetadata::getName));
        return result;
    }
    
    private List<MCPColumnMetadata> queryColumnMetadata(final DatabaseMetaData databaseMetaData, final String catalogName,
                                                        final String schemaNamePattern, final String relationNamePattern) throws SQLException {
        List<MCPColumnMetadata> result = new LinkedList<>();
        try (ResultSet columns = databaseMetaData.getColumns(catalogName, schemaNamePattern, relationNamePattern, "%")) {
            while (columns.next()) {
                String columnName = Objects.toString(columns.getString("COLUMN_NAME"), "").trim();
                if (!columnName.isEmpty()) {
                    result.add(new MCPColumnMetadata(
                            Objects.toString(columns.getString("TABLE_NAME"), "").trim(), columnName, columns.getInt("ORDINAL_POSITION"),
                            columns.getInt("DATA_TYPE"), Objects.toString(columns.getString("TYPE_NAME"), "").trim(),
                            Nullability.fromJdbcValue(columns.getInt("NULLABLE"))));
                }
            }
        }
        return result;
    }
    
    private List<ShardingSphereIndex> loadIndexMetadata(final Connection connection, final DatabaseMetaData databaseMetaData, final RuntimeDatabaseProfile databaseProfile,
                                                        final String schemaName, final String tableName) throws SQLException {
        Map<String, IndexMetadataAccumulator> result = new LinkedHashMap<>(16, 1F);
        DialectSchemaSemantics schemaSemantics = MCPDatabaseDialect.of(databaseProfile.getDatabaseType()).getDefaultSchemaSemantics();
        String catalogName = DialectSchemaSemantics.DATABASE_AS_SCHEMA == schemaSemantics ? resolveCatalogName(connection, schemaName) : null;
        String jdbcSchemaName = DialectSchemaSemantics.NATIVE_SCHEMA == schemaSemantics ? trimToNull(schemaName) : null;
        try (ResultSet indexes = databaseMetaData.getIndexInfo(catalogName, jdbcSchemaName, tableName, false, false)) {
            while (indexes.next()) {
                String indexName = Objects.toString(indexes.getString("INDEX_NAME"), "").trim();
                if (indexName.isEmpty() || DatabaseMetaData.tableIndexStatistic == indexes.getShort("TYPE")) {
                    continue;
                }
                boolean unique = !indexes.getBoolean("NON_UNIQUE");
                IndexMetadataAccumulator accumulator = result.computeIfAbsent(indexName, ignored -> new IndexMetadataAccumulator(unique));
                accumulator.addColumn(indexes.getInt("ORDINAL_POSITION"), Objects.toString(indexes.getString("COLUMN_NAME"), "").trim());
            }
        } catch (final SQLFeatureNotSupportedException ignored) {
            return Collections.emptyList();
        }
        return result.entrySet().stream().map(each -> each.getValue().build(each.getKey())).toList();
    }
    
    private String resolveCatalogName(final Connection connection, final String schemaName) throws SQLException {
        String result = trimToNull(connection.getCatalog());
        return null == result ? trimToNull(schemaName) : result;
    }
    
    private String escapePattern(final DatabaseMetaData databaseMetaData, final String value) throws SQLException {
        String result = Objects.toString(value, "").trim();
        if (result.isEmpty()) {
            return null;
        }
        String escape = Objects.toString(databaseMetaData.getSearchStringEscape(), "");
        if (escape.isEmpty()) {
            return result;
        }
        return result.replace(escape, escape + escape).replace("%", escape + "%").replace("_", escape + "_");
    }
    
    private String trimToNull(final String value) {
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
        
        private final Map<String, ShardingSphereTable> relations = new LinkedHashMap<>(16, 1F);
        
        private final Map<String, ShardingSphereSequence> sequences = new LinkedHashMap<>(16, 1F);
        
        private void addRelation(final String name, final TableType type) {
            relations.putIfAbsent(name, new ShardingSphereTable(name, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), type));
        }
        
        private void addSequence(final String sequence) {
            sequences.putIfAbsent(sequence, new ShardingSphereSequence(sequence));
        }
        
        private ShardingSphereSchema build() {
            return new ShardingSphereSchema(schema, protocolType, relations.values(), Collections.emptyList(), sequences.values());
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class IndexMetadataAccumulator {
        
        private final boolean unique;
        
        private final Map<Integer, String> columns = new LinkedHashMap<>(4, 1F);
        
        private void addColumn(final int ordinalPosition, final String columnName) {
            if (!columnName.isEmpty()) {
                columns.putIfAbsent(ordinalPosition, columnName);
            }
        }
        
        private ShardingSphereIndex build(final String name) {
            return new ShardingSphereIndex(name, columns.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).toList(), unique);
        }
    }
}
