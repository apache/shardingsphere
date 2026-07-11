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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

abstract class AbstractMCPJdbcMetadataLoaderTest {
    
    private static final String SEQUENCE_METADATA_QUERY = "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES";
    
    protected LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return load(runtimeDatabases, List.of("PostgreSQL"));
    }
    
    protected LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final Collection<String> sequenceSupportedDatabaseTypes) {
        return load(runtimeDatabases, sequenceSupportedDatabaseTypes, Collections.emptyMap());
    }
    
    protected LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final Collection<String> sequenceSupportedDatabaseTypes,
                                         final Map<String, Collection<String>> systemSchemas) {
        return load(runtimeDatabases, sequenceSupportedDatabaseTypes, systemSchemas, Collections.emptyMap());
    }
    
    protected LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, final Collection<String> sequenceSupportedDatabaseTypes,
                                         final Map<String, Collection<String>> systemSchemas, final Map<String, DialectSchemaSemantics> schemaSemantics) {
        try (
                MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata();
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            MCPJdbcDatabaseProfileLoader databaseProfileLoader = new MCPJdbcDatabaseProfileLoader();
            MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
            Map<String, RuntimeDatabaseProfile> databaseProfiles = databaseProfileLoader.load(runtimeDatabases);
            mockDatabaseTypedSPI(databaseProfiles.values(), sequenceSupportedDatabaseTypes, systemSchemas, schemaSemantics, typedSPILoader, databaseTypedSPILoader);
            Map<String, Collection<ShardingSphereSchema>> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
            for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
                result.put(entry.getKey(), metadataLoader.load(entry.getKey(), entry.getValue(), databaseProfiles.get(entry.getKey())));
            }
            return new LoadedMetadataCatalog(result);
        }
    }
    
    private void mockDatabaseTypedSPI(final Collection<RuntimeDatabaseProfile> databaseProfiles, final Collection<String> sequenceSupportedDatabaseTypes,
                                      final Map<String, Collection<String>> systemSchemas, final Map<String, DialectSchemaSemantics> schemaSemantics,
                                      final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        for (RuntimeDatabaseProfile each : databaseProfiles) {
            mockDatabaseTypedSPI(each.getDatabaseType(), sequenceSupportedDatabaseTypes, systemSchemas, schemaSemantics, typedSPILoader, databaseTypedSPILoader);
        }
    }
    
    private void mockDatabaseTypedSPI(final String databaseType, final Collection<String> sequenceSupportedDatabaseTypes,
                                      final Map<String, Collection<String>> systemSchemas, final Map<String, DialectSchemaSemantics> schemaSemantics,
                                      final MockedStatic<TypedSPILoader> typedSPILoader, final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getType()).thenReturn(databaseType);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        typedSPILoader.when(() -> TypedSPILoader.getService(DatabaseType.class, databaseType)).thenReturn(databaseTypeFromSPI);
        mockDialectDatabaseMetaData(databaseTypeFromSPI, sequenceSupportedDatabaseTypes.contains(databaseType),
                schemaSemantics.getOrDefault(databaseType, DialectSchemaSemantics.NATIVE_SCHEMA), databaseTypedSPILoader);
        mockDialectSystemDatabase(databaseTypeFromSPI, systemSchemas.getOrDefault(databaseType, List.of()), databaseTypedSPILoader);
    }
    
    private void mockDialectDatabaseMetaData(final DatabaseType databaseType, final boolean sequenceSupported, final DialectSchemaSemantics schemaSemantics,
                                             final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null, schemaSemantics));
        when(result.getExplainOption()).thenReturn(() -> false);
        when(result.getSequenceOption()).thenReturn(sequenceSupported ? Optional.of(new DialectSequenceOption(SEQUENCE_METADATA_QUERY)) : Optional.empty());
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.of(result));
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(result);
    }
    
    private void mockDialectSystemDatabase(final DatabaseType databaseType, final Collection<String> systemSchemas,
                                           final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        if (systemSchemas.isEmpty()) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.empty());
            return;
        }
        DialectSystemDatabase result = mock(DialectSystemDatabase.class);
        when(result.getSystemSchemas()).thenReturn(systemSchemas);
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(result));
    }
    
    protected static Stream<Arguments> loadTypedMetadataArguments() {
        return Stream.of(
                Arguments.of("table orders", SupportedMCPMetadataObjectType.TABLE, "orders"),
                Arguments.of("table order_items", SupportedMCPMetadataObjectType.TABLE, "order_items"),
                Arguments.of("view active_orders", SupportedMCPMetadataObjectType.VIEW, "active_orders"),
                Arguments.of("column status", SupportedMCPMetadataObjectType.COLUMN, "status"),
                Arguments.of("index idx_orders_status", SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"),
                Arguments.of("sequence order_seq", SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    protected static Stream<Arguments> loadSequenceDatabaseArguments() {
        return Stream.of(
                Arguments.of("postgresql", "PostgreSQL", "public", "order_seq"),
                Arguments.of("open gauss", "openGauss", "public", "order_seq"),
                Arguments.of("sql server", "SQLServer", "dbo", "order_seq"),
                Arguments.of("oracle", "Oracle", "APP", "ORDER_SEQ"),
                Arguments.of("mariadb", "MariaDB", "logic_db", "order_seq"),
                Arguments.of("firebird", "Firebird", "", "ORDER_SEQ"));
    }
    
    protected static Stream<Arguments> loadWithoutSequenceQueryArguments() {
        return Stream.of(Arguments.of("unsupported database type", "MySQL"));
    }
    
    protected Connection createConnectionWithoutSchema(final String databaseType) throws SQLException {
        return createConnectionWithoutSchemaByURL(getMetadataJdbcUrl(databaseType));
    }
    
    protected Connection createConnectionWithoutSchemaByURL(final String jdbcUrl) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME", "orders");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet columnResultSet = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet indexResultSet = mockResultSet("INDEX_NAME");
        ResultSet emptyQueryResultSet = mockMultiRowResultSet(List.of());
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
        when(statement.executeQuery(anyString())).thenReturn(emptyQueryResultSet);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(databaseMetaData.getColumns(isNull(), isNull(), eq("orders"), eq("%"))).thenReturn(columnResultSet);
        when(databaseMetaData.getIndexInfo(isNull(), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(indexResultSet);
        return result;
    }
    
    protected Connection createConnectionWithSequenceMetadata(final String databaseType, final String sequenceSchema,
                                                              final String sequenceName) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet sequenceResultSet = mockSingleRowResultSet(Map.of("SEQUENCE_SCHEMA", sequenceSchema, "SEQUENCE_NAME", sequenceName));
        ResultSet emptyQueryResultSet = mockMultiRowResultSet(List.of());
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl(databaseType));
        when(statement.executeQuery(anyString())).thenReturn(emptyQueryResultSet);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(statement.executeQuery(getSequenceMetadataQuery())).thenReturn(sequenceResultSet);
        return result;
    }
    
    protected Connection createConnectionWithFailedSequenceMetadataQuery() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("PostgreSQL"));
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(statement.executeQuery(getSequenceMetadataQuery())).thenThrow(new SQLException("sequence metadata query failed"));
        return result;
    }
    
    protected Connection createStandardPostgreSQLMetadataConnection() throws SQLException {
        Connection result = createConnectionWithMetadata(
                List.of(
                        Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders"),
                        Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "order_items")),
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders")),
                Map.of(
                        "orders", List.of("order_id", "status", "amount"),
                        "order_items", List.of("item_id", "order_id", "sku"),
                        "active_orders", List.of("order_id", "status")),
                Map.of(
                        "orders", List.of("PRIMARY_KEY_C", "idx_orders_status"),
                        "order_items", List.of("PRIMARY_KEY_C")),
                List.of(Map.of("SEQUENCE_SCHEMA", "PUBLIC", "SEQUENCE_NAME", "order_seq")));
        DatabaseMetaData databaseMetaData = result.getMetaData();
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("16.2");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("PostgreSQL"));
        return result;
    }
    
    protected RuntimeDatabaseConfiguration createMockRuntimeDatabaseConfiguration(final Connection connection) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        when(result.openConnection(anyString())).thenReturn(connection);
        return result;
    }
    
    protected Connection createConnectionWithMetadata(final List<Map<String, String>> tableRows, final List<Map<String, String>> viewRows, final Map<String, List<String>> columns,
                                                      final Map<String, List<String>> indexes, final List<Map<String, String>> sequenceRows) throws SQLException {
        return createConnectionWithMetadata("PostgreSQL", tableRows, viewRows, columns, indexes, sequenceRows);
    }
    
    protected Connection createConnectionWithMetadata(final String databaseType, final List<Map<String, String>> tableRows, final List<Map<String, String>> viewRows,
                                                      final Map<String, List<String>> columns, final Map<String, List<String>> indexes,
                                                      final List<Map<String, String>> sequenceRows) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet sequenceResultSet = mockMultiRowResultSet(sequenceRows);
        ResultSet emptyQueryResultSet = mockMultiRowResultSet(List.of());
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl(databaseType));
        when(statement.executeQuery(anyString())).thenReturn(emptyQueryResultSet);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return mockMultiRowResultSet("TABLE".equals(tableTypes[0]) ? tableRows : viewRows);
        });
        when(databaseMetaData.getColumns(isNull(), nullable(String.class), anyString(), eq("%"))).thenAnswer(invocation -> {
            String objectName = invocation.getArgument(2);
            return mockResultSet("COLUMN_NAME", columns.getOrDefault(objectName, List.of()).toArray(new String[0]));
        });
        when(databaseMetaData.getIndexInfo(isNull(), nullable(String.class), anyString(), eq(false), eq(false))).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(2);
            return mockResultSet("INDEX_NAME", indexes.getOrDefault(tableName, List.of()).toArray(new String[0]));
        });
        when(statement.executeQuery(getSequenceMetadataQuery())).thenReturn(sequenceResultSet);
        return result;
    }
    
    protected void mockEmptyScalarQueries(final Connection connection) throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mockMultiRowResultSet(List.of());
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }
    
    protected String getSequenceMetadataQuery() {
        return SEQUENCE_METADATA_QUERY;
    }
    
    protected ResultSet mockResultSet(final String columnName, final String... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger valueIndex = new AtomicInteger();
        when(result.next()).thenAnswer(invocation -> nextIndex.getAndIncrement() < values.length);
        when(result.getString(columnName)).thenAnswer(invocation -> values[valueIndex.getAndIncrement()]);
        return result;
    }
    
    protected ResultSet mockSingleRowResultSet(final Map<String, String> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        for (Entry<String, String> entry : values.entrySet()) {
            when(result.getString(entry.getKey())).thenReturn(entry.getValue());
        }
        return result;
    }
    
    protected ResultSet mockMultiRowResultSet(final List<Map<String, String>> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < values.size());
        when(result.getString(anyString())).thenAnswer(invocation -> {
            int currentRowIndex = rowIndex.get();
            return 0 <= currentRowIndex && currentRowIndex < values.size() ? values.get(currentRowIndex).get(invocation.getArgument(0)) : null;
        });
        return result;
    }
    
    protected String getMetadataJdbcUrl(final String databaseType) {
        return SupportDatabaseTypeFactoryMocker.createJdbcUrl(databaseType);
    }
    
    protected int countMetadata(final Collection<ShardingSphereSchema> schemas, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (ShardingSphereSchema each : schemas) {
            result += countSchemaMetadata(each, objectType, objectName);
        }
        return result;
    }
    
    private int countSchemaMetadata(final ShardingSphereSchema schemaMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = SupportedMCPMetadataObjectType.SCHEMA == objectType && objectName.equals(schemaMetadata.getName()) ? 1 : 0;
        result += countTableMetadata(schemaMetadata.getAllTables(), objectType, objectName);
        result += countSequenceMetadata(schemaMetadata.getAllSequences(), objectType, objectName);
        return result;
    }
    
    private int countTableMetadata(final Collection<ShardingSphereTable> tables, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (ShardingSphereTable each : tables) {
            if (SupportedMCPMetadataObjectType.TABLE == objectType && TableType.TABLE == each.getType() && objectName.equals(each.getName())) {
                result++;
            }
            if (SupportedMCPMetadataObjectType.VIEW == objectType && TableType.VIEW == each.getType() && objectName.equals(each.getName())) {
                result++;
            }
            result += countColumnMetadata(each.getAllColumns(), objectType, objectName);
            result += countIndexMetadata(each.getAllIndexes(), objectType, objectName);
        }
        return result;
    }
    
    private int countColumnMetadata(final Collection<ShardingSphereColumn> columns, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (ShardingSphereColumn each : columns) {
            if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(each.getName())) {
                result++;
            }
        }
        return result;
    }
    
    private int countIndexMetadata(final Collection<ShardingSphereIndex> indexes, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (ShardingSphereIndex each : indexes) {
            if (SupportedMCPMetadataObjectType.INDEX == objectType && objectName.equals(each.getName())) {
                result++;
            }
        }
        return result;
    }
    
    private int countSequenceMetadata(final Collection<ShardingSphereSequence> sequences, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (ShardingSphereSequence each : sequences) {
            if (SupportedMCPMetadataObjectType.SEQUENCE == objectType && objectName.equals(each.getName())) {
                result++;
            }
        }
        return result;
    }
    
    protected boolean containsMetadata(final Collection<ShardingSphereSchema> schemas, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        return 0 < countMetadata(schemas, objectType, objectName);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(AccessLevel.PROTECTED)
    protected static final class LoadedMetadataCatalog {
        
        private final Map<String, Collection<ShardingSphereSchema>> databaseMetadataMap;
        
        protected Optional<Collection<ShardingSphereSchema>> findMetadata(final String databaseName) {
            return Optional.ofNullable(databaseMetadataMap.get(databaseName));
        }
    }
    
    protected static final class MockDriverRegistration implements AutoCloseable {
        
        private final Driver driver;
        
        private MockDriverRegistration(final Driver driver) throws SQLException {
            this.driver = driver;
            DriverManager.registerDriver(driver);
        }
        
        protected static MockDriverRegistration register(final Driver driver) throws SQLException {
            return new MockDriverRegistration(driver);
        }
        
        @Override
        public void close() throws SQLException {
            DriverManager.deregisterDriver(driver);
        }
    }
    
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    protected static final class MockDriver implements Driver {
        
        private final String jdbcUrl;
        
        private final Connection connection;
        
        @Override
        public Connection connect(final String url, final Properties info) {
            return acceptsURL(url) ? connection : null;
        }
        
        @Override
        public boolean acceptsURL(final String url) {
            return jdbcUrl.equals(url);
        }
        
        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
            return new DriverPropertyInfo[0];
        }
        
        @Override
        public int getMajorVersion() {
            return 1;
        }
        
        @Override
        public int getMinorVersion() {
            return 0;
        }
        
        @Override
        public boolean jdbcCompliant() {
            return false;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Mock driver does not expose a parent logger.");
        }
    }
}
