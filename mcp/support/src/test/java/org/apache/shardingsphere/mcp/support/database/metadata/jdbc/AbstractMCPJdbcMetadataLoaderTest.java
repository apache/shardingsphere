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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPViewMetadata;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractMCPJdbcMetadataLoaderTest {
    
    protected static final Map<String, String> METADATA_JDBC_URLS = Map.of(
            "MySQL", "jdbc:mysql://metadata-loader/test",
            "PostgreSQL", "jdbc:postgresql://metadata-loader/test",
            "openGauss", "jdbc:opengauss://metadata-loader/test",
            "SQLServer", "jdbc:sqlserver://metadata-loader",
            "Oracle", "jdbc:oracle:thin:@metadata-loader",
            "MariaDB", "jdbc:mariadb://metadata-loader/test",
            "Firebird", "jdbc:firebirdsql://metadata-loader/test");
    
    protected LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        try (MockedStatic<DatabaseTypeFactory> ignored = SupportDatabaseTypeFactoryMocker.mockByConnectionMetadata()) {
            MCPJdbcDatabaseProfileLoader databaseProfileLoader = new MCPJdbcDatabaseProfileLoader();
            MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
            Map<String, RuntimeDatabaseProfile> databaseProfiles = databaseProfileLoader.load(runtimeDatabases);
            Map<String, MCPDatabaseMetadata> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
            for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
                result.put(entry.getKey(), metadataLoader.load(entry.getKey(), entry.getValue(), databaseProfiles.get(entry.getKey())));
            }
            return new LoadedMetadataCatalog(result);
        }
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
    
    protected static Stream<Arguments> loadSequenceDialectArguments() {
        return Stream.of(
                Arguments.of("postgresql", "PostgreSQL", "public", "order_seq",
                        "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"),
                Arguments.of("open gauss", "openGauss", "public", "order_seq",
                        "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"),
                Arguments.of("sql server", "SQLServer", "dbo", "order_seq",
                        "SELECT schemas.name AS SEQUENCE_SCHEMA, seq.name AS SEQUENCE_NAME FROM sys.sequences seq INNER JOIN sys.schemas schemas ON seq.schema_id = schemas.schema_id"),
                Arguments.of("oracle", "Oracle", "APP", "ORDER_SEQ",
                        "SELECT USER AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM USER_SEQUENCES"),
                Arguments.of("mariadb", "MariaDB", "logic_db", "order_seq",
                        "SELECT TABLE_SCHEMA AS SEQUENCE_SCHEMA, TABLE_NAME AS SEQUENCE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'SEQUENCE'"),
                Arguments.of("firebird", "Firebird", "", "ORDER_SEQ",
                        "SELECT '' AS SEQUENCE_SCHEMA, TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE COALESCE(RDB$SYSTEM_FLAG, 0) = 0"));
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
                                                              final String sequenceName, final String sequenceQuery) throws SQLException {
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
        when(statement.executeQuery(sequenceQuery)).thenReturn(sequenceResultSet);
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
        when(statement.executeQuery("SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences"))
                .thenThrow(new SQLException("sequence metadata query failed"));
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
        when(databaseMetaData.getURL()).thenReturn("jdbc:postgresql://metadata-loader/test");
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
        when(statement.executeQuery(getSequenceQuery(databaseType))).thenReturn(sequenceResultSet);
        return result;
    }
    
    protected void mockEmptyScalarQueries(final Connection connection) throws SQLException {
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mockMultiRowResultSet(List.of());
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }
    
    protected String getSequenceQuery(final String databaseType) {
        switch (databaseType) {
            case "PostgreSQL":
            case "openGauss":
                return "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences";
            case "SQLServer":
                return "SELECT schemas.name AS SEQUENCE_SCHEMA, seq.name AS SEQUENCE_NAME FROM sys.sequences seq INNER JOIN sys.schemas schemas ON seq.schema_id = schemas.schema_id";
            case "Oracle":
                return "SELECT USER AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM USER_SEQUENCES";
            case "MariaDB":
                return "SELECT TABLE_SCHEMA AS SEQUENCE_SCHEMA, TABLE_NAME AS SEQUENCE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'SEQUENCE'";
            case "Firebird":
                return "SELECT '' AS SEQUENCE_SCHEMA, TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE COALESCE(RDB$SYSTEM_FLAG, 0) = 0";
            default:
                return "";
        }
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
        return METADATA_JDBC_URLS.getOrDefault(databaseType, METADATA_JDBC_URLS.get("PostgreSQL"));
    }
    
    protected int countMetadata(final MCPDatabaseMetadata databaseMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            result += countSchemaMetadata(each, objectType, objectName);
        }
        return result;
    }
    
    private int countSchemaMetadata(final MCPSchemaMetadata schemaMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = SupportedMCPMetadataObjectType.SCHEMA == objectType && objectName.equals(schemaMetadata.getSchema()) ? 1 : 0;
        result += countTableMetadata(schemaMetadata.getTables(), objectType, objectName);
        result += countViewMetadata(schemaMetadata.getViews(), objectType, objectName);
        result += countSequenceMetadata(schemaMetadata.getSequences(), objectType, objectName);
        return result;
    }
    
    private int countTableMetadata(final Collection<MCPTableMetadata> tables, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPTableMetadata each : tables) {
            if (SupportedMCPMetadataObjectType.TABLE == objectType && objectName.equals(each.getTable())) {
                result++;
            }
            result += countColumnMetadata(each.getColumns(), objectType, objectName);
            result += countIndexMetadata(each.getIndexes(), objectType, objectName);
        }
        return result;
    }
    
    private int countViewMetadata(final Collection<MCPViewMetadata> views, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPViewMetadata each : views) {
            if (SupportedMCPMetadataObjectType.VIEW == objectType && objectName.equals(each.getView())) {
                result++;
            }
            result += countColumnMetadata(each.getColumns(), objectType, objectName);
        }
        return result;
    }
    
    private int countColumnMetadata(final Collection<MCPColumnMetadata> columns, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPColumnMetadata each : columns) {
            if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(each.getColumn())) {
                result++;
            }
        }
        return result;
    }
    
    private int countIndexMetadata(final Collection<MCPIndexMetadata> indexes, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPIndexMetadata each : indexes) {
            if (SupportedMCPMetadataObjectType.INDEX == objectType && objectName.equals(each.getIndex())) {
                result++;
            }
        }
        return result;
    }
    
    private int countSequenceMetadata(final Collection<MCPSequenceMetadata> sequences, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPSequenceMetadata each : sequences) {
            if (SupportedMCPMetadataObjectType.SEQUENCE == objectType && objectName.equals(each.getSequence())) {
                result++;
            }
        }
        return result;
    }
    
    protected boolean containsMetadata(final MCPDatabaseMetadata databaseMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        return 0 < countMetadata(databaseMetadata, objectType, objectName);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    protected static final class LoadedMetadataCatalog {
        
        private final Map<String, MCPDatabaseMetadata> databaseMetadataMap;
        
        protected Optional<MCPDatabaseMetadata> findMetadata(final String databaseName) {
            return Optional.ofNullable(databaseMetadataMap.get(databaseName));
        }
        
        protected Map<String, MCPDatabaseMetadata> getDatabaseMetadataMap() {
            return databaseMetadataMap;
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
    
    protected static final class MockDriver implements Driver {
        
        private final String jdbcUrl;
        
        private final Connection connection;
        
        protected MockDriver(final String jdbcUrl, final Connection connection) {
            this.jdbcUrl = jdbcUrl;
            this.connection = connection;
        }
        
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
