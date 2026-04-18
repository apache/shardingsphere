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

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void assertLoad() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertThat(actual.findMetadata("logic_db").map(MCPDatabaseMetadata::getDatabaseType).orElseThrow(), is("H2"));
        assertFalse(actual.findMetadata("logic_db").orElseThrow().getDatabaseVersion().isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTypedMetadataArguments")
    void assertLoadWithTypedMetadata(final String name, final SupportedMCPMetadataObjectType objectType, final String objectName) throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-" + objectName);
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), objectType, objectName));
    }

    @Test
    void assertLoadWithMultipleLogicalDatabases() throws SQLException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        Map<String, RuntimeDatabaseConfiguration> connectionConfigs = Map.of(
                "logic_db", createRuntimeDatabaseConfiguration(firstJdbcUrl), "analytics_db", createRuntimeDatabaseConfiguration(secondJdbcUrl));
        LoadedMetadataCatalog actual = load(connectionConfigs);
        assertThat(actual.getDatabaseMetadataMap().size(), is(2));
        assertTrue(actual.findMetadata("analytics_db").isPresent());
    }

    @Test
    void assertLoadWithoutSchemaObjects() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:no-schema", createConnectionWithoutSchema("MySQL"));
        DriverManager.registerDriver(mockDriver);
        try {
            LoadedMetadataCatalog actual = load(Map.of("logic_db", new RuntimeDatabaseConfiguration("MySQL", "jdbc:mock:no-schema", "", "", "")));
            MCPDatabaseMetadata databaseMetadata = actual.findMetadata("logic_db").orElseThrow();
            assertThat(databaseMetadata.getSchemas().size(), is(1));
            assertThat(databaseMetadata.getSchemas().get(0).getSchema(), is("logic_db"));
            assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.TABLE, "orders"));
            assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
            assertThat(databaseMetadata.getDatabaseVersion(), is(""));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }

    @Test
    void assertLoadWithoutSystemCatalogForDatabaseAsSchema() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet orderColumns = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet orderIndexes = mockResultSet("INDEX_NAME", "idx_orders_status");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("MySQL");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("MySQL"));
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return mockMultiRowResultSet("TABLE".equals(tableTypes[0])
                    ? List.of(
                            Map.of("TABLE_CAT", "information_schema", "TABLE_SCHEM", "", "TABLE_NAME", "GLOBAL_STATUS"),
                            Map.of("TABLE_CAT", "logic_db", "TABLE_SCHEM", "", "TABLE_NAME", "orders"))
                    : List.of());
        });
        when(databaseMetaData.getColumns(eq("logic_db"), isNull(), eq("orders"), eq("%"))).thenReturn(orderColumns);
        when(databaseMetaData.getIndexInfo(eq("logic_db"), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(orderIndexes);
        when(databaseMetaData.getColumns(eq("information_schema"), isNull(), eq("GLOBAL_STATUS"), eq("%")))
                .thenThrow(new SQLException("system catalog should be skipped"));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("MySQL", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "GLOBAL_STATUS"));
        assertThat(actual.getSchemas().get(0).getSchema(), is("logic_db"));
    }

    @Test
    void assertLoadWithCatalogBackedMetadataUsingLogicalSchemaName() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet orderColumns = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet orderIndexes = mockResultSet("INDEX_NAME", "idx_orders_status");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("MySQL");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("MySQL"));
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return mockMultiRowResultSet("TABLE".equals(tableTypes[0])
                    ? List.of(Map.of("TABLE_CAT", "orders", "TABLE_SCHEM", "", "TABLE_NAME", "orders"))
                    : List.of());
        });
        when(databaseMetaData.getColumns(eq("orders"), isNull(), eq("orders"), eq("%"))).thenReturn(orderColumns);
        when(databaseMetaData.getIndexInfo(eq("orders"), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(orderIndexes);
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("MySQL", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(actual.getSchemas().get(0).getSchema(), is("logic_db"));
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
    }

    @Test
    void assertLoadWithSchemaRegisteredOnce() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "metadata-loader-shared-schema");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createRuntimeDatabaseConfiguration(jdbcUrl)));
        MCPDatabaseMetadata databaseMetadata = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertThat(databaseMetadata.getSchemas().size(), is(1));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithoutSequenceQueryArguments")
    void assertLoadWithoutSequenceQuery(final String name, final String databaseType) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(databaseType, createConnectionWithoutSchema(databaseType));
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }

    @Test
    void assertLoadWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        SQLException expected = new SQLException("connection failed");
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(expected);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> metadataLoader.load("logic_db", runtimeDatabaseConfiguration, new RuntimeDatabaseProfile("logic_db", "H2", "")));
        assertThat(actual.getMessage(), is("Failed to load metadata for database `logic_db`."));
        assertThat(actual.getCause(), is(expected));
    }

    @Test
    void assertLoadWithoutEmptyTableName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", ""), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(),
                Map.of("orders", List.of("order_id")),
                Map.of(),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, ""));
    }

    @Test
    void assertLoadWithoutEmptyViewName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(),
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", ""), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders")),
                Map.of("active_orders", List.of("order_id")),
                Map.of(),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.VIEW, ""));
    }

    @Test
    void assertLoadWithoutEmptyColumnName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(),
                Map.of("orders", List.of("", "order_id")),
                Map.of(),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, ""));
    }

    @Test
    void assertLoadWithoutEmptyOrDuplicateIndexName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(),
                Map.of("orders", List.of("order_id")),
                Map.of("orders", List.of("", "idx_orders_status", "idx_orders_status")),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"), is(1));
    }

    @Test
    void assertLoadWithoutSystemSchemaSequence() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL",
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "PG_CATALOG", "SEQUENCE_NAME", "order_seq")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("PostgreSQL", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }

    @Test
    void assertLoadWithoutEmptySequenceName() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL",
                List.of(),
                List.of(),
                Map.of(),
                Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "public", "SEQUENCE_NAME", "")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("PostgreSQL", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, ""));
    }

    @Test
    void assertLoadWithDuplicateTableRows() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders"), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(),
                Map.of("orders", List.of("order_id")),
                Map.of(),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"), is(1));
    }

    @Test
    void assertLoadWithDuplicateViewRows() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(),
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders"), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders")),
                Map.of("active_orders", List.of("order_id")),
                Map.of(),
                List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("H2", connection);
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.VIEW, "active_orders"), is(1));
    }

    @Test
    void assertLoadWithFailedSequenceMetadataQuery() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:failed-sequence-query", createConnectionWithFailedSequenceMetadataQuery());
        DriverManager.registerDriver(mockDriver);
        try {
            IllegalStateException actual = assertThrows(IllegalStateException.class,
                    () -> load(Map.of("logic_db", new RuntimeDatabaseConfiguration("PostgreSQL", "jdbc:mock:failed-sequence-query", "", "", ""))));
            assertThat(actual.getMessage(), is("Failed to load metadata for database `logic_db`."));
            assertThat(actual.getCause().getMessage(), is("sequence metadata query failed"));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }

    @Test
    void assertLoadWithMismatchedDatabaseType() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration("MySQL", createConnectionWithoutSchema("H2"));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> load(Map.of("logic_db", runtimeDatabaseConfiguration)));
        assertThat(actual.getMessage(), is("Configured databaseType `MySQL` does not match actual database type `H2` for database `logic_db`."));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadCompatibleMySQLBranchDatabaseTypeArguments")
    void assertLoadWithCompatibleMySQLBranchDatabaseType(final String name, final String configuredDatabaseType, final String productName,
                                                         final String probeQuery, final String probeResult) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(configuredDatabaseType,
                createConnectionWithoutSchema(productName, "jdbc:mysql://metadata-loader/test", Map.of(probeQuery, probeResult)));
        MCPDatabaseMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(actual.getDatabaseType(), is(configuredDatabaseType));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadRejectMySQLMismatchArguments")
    void assertLoadRejectsMySQLMismatchForBranchDatabase(final String name, final String configuredDatabaseType, final String probeQuery,
                                                         final String probeResult) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(configuredDatabaseType,
                createConnectionWithoutSchema("MySQL", "jdbc:mysql://metadata-loader/test", Map.of(probeQuery, probeResult)));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> load(Map.of("logic_db", runtimeDatabaseConfiguration)));
        assertThat(actual.getMessage(), is(String.format("Configured databaseType `%s` does not match actual database type `MySQL` for database `logic_db`.", configuredDatabaseType)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loadSequenceDialectArguments")
    void assertLoadWithDialectSequenceMetadata(final String name, final String databaseType, final String sequenceSchema,
                                               final String sequenceName, final String sequenceQuery) throws SQLException {
        String jdbcUrl = "jdbc:mock:sequence:" + databaseType.toLowerCase(Locale.ENGLISH);
        Driver mockDriver = new MockDriver(jdbcUrl, createConnectionWithSequenceMetadata(databaseType, sequenceSchema, sequenceName, sequenceQuery));
        DriverManager.registerDriver(mockDriver);
        try {
            LoadedMetadataCatalog actual = load(Map.of("logic_db", new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", "")));
            assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), SupportedMCPMetadataObjectType.SEQUENCE, sequenceName));
        } finally {
            DriverManager.deregisterDriver(mockDriver);
        }
    }

    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }

    private LoadedMetadataCatalog load(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        MCPJdbcDatabaseProfileLoader databaseProfileLoader = new MCPJdbcDatabaseProfileLoader();
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        Map<String, RuntimeDatabaseProfile> databaseProfiles = databaseProfileLoader.load(runtimeDatabases);
        Map<String, MCPDatabaseMetadata> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            result.put(entry.getKey(), metadataLoader.load(entry.getKey(), entry.getValue(), databaseProfiles.get(entry.getKey())));
        }
        return new LoadedMetadataCatalog(result);
    }

    private static Stream<Arguments> loadTypedMetadataArguments() {
        return Stream.of(
                Arguments.of("table orders", SupportedMCPMetadataObjectType.TABLE, "orders"),
                Arguments.of("table order_items", SupportedMCPMetadataObjectType.TABLE, "order_items"),
                Arguments.of("view active_orders", SupportedMCPMetadataObjectType.VIEW, "active_orders"),
                Arguments.of("column status", SupportedMCPMetadataObjectType.COLUMN, "status"),
                Arguments.of("index idx_orders_status", SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"),
                Arguments.of("sequence order_seq", SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }

    private static Stream<Arguments> loadSequenceDialectArguments() {
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
                        "SELECT '' AS SEQUENCE_SCHEMA, TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE COALESCE(RDB$SYSTEM_FLAG, 0) = 0"),
                Arguments.of("h2", "H2", "PUBLIC", "ORDER_SEQ",
                        "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES"));
    }

    private static Stream<Arguments> loadWithoutSequenceQueryArguments() {
        return Stream.of(
                Arguments.of("unsupported database type", "MySQL"));
    }

    private static Stream<Arguments> loadCompatibleMySQLBranchDatabaseTypeArguments() {
        return Stream.of(
                Arguments.of("doris version comment probe", "Doris", "MySQL", "SELECT @@version_comment", "Doris version doris-3.0.3"),
                Arguments.of("mariadb version probe", "MariaDB", "MySQL", "SELECT VERSION()", "10.4.7-MariaDB"));
    }

    private static Stream<Arguments> loadRejectMySQLMismatchArguments() {
        return Stream.of(
                Arguments.of("reject mysql backend configured as doris", "Doris", "SELECT @@version_comment", "MySQL Community Server - GPL"),
                Arguments.of("reject mysql backend configured as mariadb", "MariaDB", "SELECT VERSION()", "8.0.36"));
    }

    private Connection createConnectionWithoutSchema(final String databaseType) throws SQLException {
        return createConnectionWithoutSchema(databaseType, getMetadataJdbcUrl(databaseType));
    }

    private Connection createConnectionWithoutSchema(final String databaseProductName, final String jdbcUrl) throws SQLException {
        return createConnectionWithoutSchema(databaseProductName, jdbcUrl, Map.of());
    }

    private Connection createConnectionWithoutSchema(final String databaseProductName, final String jdbcUrl, final Map<String, String> scalarQueries) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME", "orders");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet columnResultSet = mockResultSet("COLUMN_NAME", "order_id");
        ResultSet indexResultSet = mockResultSet("INDEX_NAME");
        final ResultSet emptyQueryResultSet = mockMultiRowResultSet(List.of());
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseProductName);
        when(databaseMetaData.getURL()).thenReturn(jdbcUrl);
        when(statement.executeQuery(anyString())).thenReturn(emptyQueryResultSet);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(databaseMetaData.getColumns(isNull(), isNull(), eq("orders"), eq("%"))).thenReturn(columnResultSet);
        when(databaseMetaData.getIndexInfo(isNull(), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(indexResultSet);
        for (Entry<String, String> entry : scalarQueries.entrySet()) {
            final ResultSet scalarResultSet = mockScalarResultSet(entry.getValue());
            when(statement.executeQuery(entry.getKey())).thenReturn(scalarResultSet);
        }
        return result;
    }

    private Connection createConnectionWithSequenceMetadata(final String databaseType, final String sequenceSchema,
                                                            final String sequenceName, final String sequenceQuery) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        ResultSet sequenceResultSet = mockSingleRowResultSet(Map.of("SEQUENCE_SCHEMA", sequenceSchema, "SEQUENCE_NAME", sequenceName));
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseType);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl(databaseType));
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return "TABLE".equals(tableTypes[0]) ? tableResultSet : viewResultSet;
        });
        when(statement.executeQuery(sequenceQuery)).thenReturn(sequenceResultSet);
        return result;
    }

    private Connection createConnectionWithFailedSequenceMetadataQuery() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tableResultSet = mockResultSet("TABLE_NAME");
        ResultSet viewResultSet = mockResultSet("TABLE_NAME");
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
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

    private RuntimeDatabaseConfiguration createMockRuntimeDatabaseConfiguration(final String databaseType, final Connection connection) throws SQLException {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.openConnection(anyString())).thenReturn(connection);
        return result;
    }

    private Connection createConnectionWithMetadata(final List<Map<String, String>> tableRows, final List<Map<String, String>> viewRows, final Map<String, List<String>> columns,
                                                    final Map<String, List<String>> indexes, final List<Map<String, String>> sequenceRows) throws SQLException {
        return createConnectionWithMetadata("H2", tableRows, viewRows, columns, indexes, sequenceRows);
    }

    private Connection createConnectionWithMetadata(final String databaseType, final List<Map<String, String>> tableRows, final List<Map<String, String>> viewRows,
                                                    final Map<String, List<String>> columns, final Map<String, List<String>> indexes,
                                                    final List<Map<String, String>> sequenceRows) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet sequenceResultSet = mockMultiRowResultSet(sequenceRows);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseType);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl(databaseType));
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

    private String getSequenceQuery(final String databaseType) {
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
            case "H2":
                return "SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES";
            default:
                return "";
        }
    }

    private ResultSet mockResultSet(final String columnName, final String... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger valueIndex = new AtomicInteger();
        when(result.next()).thenAnswer(invocation -> nextIndex.getAndIncrement() < values.length);
        when(result.getString(columnName)).thenAnswer(invocation -> values[valueIndex.getAndIncrement()]);
        return result;
    }

    private ResultSet mockSingleRowResultSet(final Map<String, String> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        for (Entry<String, String> entry : values.entrySet()) {
            when(result.getString(entry.getKey())).thenReturn(entry.getValue());
        }
        return result;
    }

    private ResultSet mockScalarResultSet(final String value) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString(1)).thenReturn(value);
        return result;
    }

    private ResultSet mockMultiRowResultSet(final List<Map<String, String>> values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < values.size());
        when(result.getString(anyString())).thenAnswer(invocation -> {
            int currentRowIndex = rowIndex.get();
            return 0 <= currentRowIndex && currentRowIndex < values.size() ? values.get(currentRowIndex).get(invocation.getArgument(0)) : null;
        });
        return result;
    }

    private String getMetadataJdbcUrl(final String databaseType) {
        if ("MySQL".equals(databaseType)) {
            return "jdbc:mysql://metadata-loader/test";
        }
        if ("Doris".equals(databaseType)) {
            return "jdbc:mysql://metadata-loader/test";
        }
        if ("PostgreSQL".equals(databaseType)) {
            return "jdbc:postgresql://metadata-loader/test";
        }
        if ("openGauss".equals(databaseType)) {
            return "jdbc:opengauss://metadata-loader/test";
        }
        if ("SQLServer".equals(databaseType)) {
            return "jdbc:sqlserver://metadata-loader";
        }
        if ("Oracle".equals(databaseType)) {
            return "jdbc:oracle:thin:@metadata-loader";
        }
        if ("MariaDB".equals(databaseType)) {
            return "jdbc:mariadb://metadata-loader/test";
        }
        if ("Firebird".equals(databaseType)) {
            return "jdbc:firebirdsql://metadata-loader/test";
        }
        return "jdbc:h2:mem:metadata-loader";
    }

    private int countMetadata(final MCPDatabaseMetadata databaseMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        int result = 0;
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            if (SupportedMCPMetadataObjectType.SCHEMA == objectType && objectName.equals(each.getSchema())) {
                result++;
            }
            for (MCPTableMetadata table : each.getTables()) {
                if (SupportedMCPMetadataObjectType.TABLE == objectType && objectName.equals(table.getTable())) {
                    result++;
                }
                for (MCPColumnMetadata column : table.getColumns()) {
                    if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        result++;
                    }
                }
                for (MCPIndexMetadata index : table.getIndexes()) {
                    if (SupportedMCPMetadataObjectType.INDEX == objectType && objectName.equals(index.getIndex())) {
                        result++;
                    }
                }
            }
            for (MCPViewMetadata view : each.getViews()) {
                if (SupportedMCPMetadataObjectType.VIEW == objectType && objectName.equals(view.getView())) {
                    result++;
                }
                for (MCPColumnMetadata column : view.getColumns()) {
                    if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        result++;
                    }
                }
            }
            for (MCPSequenceMetadata sequence : each.getSequences()) {
                if (SupportedMCPMetadataObjectType.SEQUENCE == objectType && objectName.equals(sequence.getSequence())) {
                    result++;
                }
            }
        }
        return result;
    }

    private boolean containsMetadata(final MCPDatabaseMetadata databaseMetadata, final SupportedMCPMetadataObjectType objectType, final String objectName) {
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            if (SupportedMCPMetadataObjectType.SCHEMA == objectType && objectName.equals(each.getSchema())) {
                return true;
            }
            for (MCPTableMetadata table : each.getTables()) {
                if (SupportedMCPMetadataObjectType.TABLE == objectType && objectName.equals(table.getTable())) {
                    return true;
                }
                for (MCPColumnMetadata column : table.getColumns()) {
                    if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        return true;
                    }
                }
                for (MCPIndexMetadata index : table.getIndexes()) {
                    if (SupportedMCPMetadataObjectType.INDEX == objectType && objectName.equals(index.getIndex())) {
                        return true;
                    }
                }
            }
            for (MCPViewMetadata view : each.getViews()) {
                if (SupportedMCPMetadataObjectType.VIEW == objectType && objectName.equals(view.getView())) {
                    return true;
                }
                for (MCPColumnMetadata column : view.getColumns()) {
                    if (SupportedMCPMetadataObjectType.COLUMN == objectType && objectName.equals(column.getColumn())) {
                        return true;
                    }
                }
            }
            for (MCPSequenceMetadata sequence : each.getSequences()) {
                if (SupportedMCPMetadataObjectType.SEQUENCE == objectType && objectName.equals(sequence.getSequence())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class LoadedMetadataCatalog {

        private final Map<String, MCPDatabaseMetadata> databaseMetadataMap;

        private LoadedMetadataCatalog(final Map<String, MCPDatabaseMetadata> databaseMetadataMap) {
            this.databaseMetadataMap = databaseMetadataMap;
        }

        private Optional<MCPDatabaseMetadata> findMetadata(final String databaseName) {
            return Optional.ofNullable(databaseMetadataMap.get(databaseName));
        }

        private Map<String, MCPDatabaseMetadata> getDatabaseMetadataMap() {
            return databaseMetadataMap;
        }
    }

    private static final class MockDriver implements Driver {

        private final String jdbcUrl;

        private final Connection connection;

        private MockDriver(final String jdbcUrl, final Connection connection) {
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
