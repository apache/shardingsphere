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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata.Nullability;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPMetadataSnapshot;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderTest extends AbstractMCPJdbcMetadataLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
        assertThat(schemas.size(), is(1));
        assertThat(schemas.iterator().next().getName(), is("PUBLIC"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTypedMetadataArguments")
    void assertLoadWithTypedMetadata(final String name, final SupportedMCPMetadataObjectType objectType, final String objectName) throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        assertTrue(containsMetadata(actual.findSnapshot("logic_db").orElseThrow(), objectType, objectName));
    }
    
    @Test
    void assertLoadWithMultipleLogicalDatabases() throws SQLException {
        Map<String, RuntimeDatabaseConfiguration> connectionConfigs = Map.of(
                "logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection()),
                "analytics_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection()));
        LoadedMetadataCatalog actual = load(connectionConfigs);
        assertThat(actual.getDatabaseMetadataMap().size(), is(2));
        assertTrue(actual.findMetadata("analytics_db").isPresent());
    }
    
    @Test
    void assertLoadWithSchemaRegisteredOnce() throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertThat(schemas.size(), is(1));
    }
    
    @Test
    void assertLoadWithoutSchemaObjects() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:no-schema", createConnectionWithoutSchema("MySQL"));
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            LoadedMetadataCatalog actual = loadWithDatabaseAsSchema(Map.of("logic_db", new RuntimeDatabaseConfiguration("jdbc:mock:no-schema", "", "", MockDriver.class.getName())));
            Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
            assertThat(schemas.size(), is(1));
            assertThat(schemas.iterator().next().getName(), is("logic_db"));
            assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.TABLE, "orders"));
            assertFalse(containsMetadata(schemas, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
        }
    }
    
    @Test
    void assertLoadWithoutSystemCatalogForDatabaseAsSchema() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("MySQL"));
        mockEmptyScalarQueries(connection);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return mockMultiRowResultSet("TABLE".equals(tableTypes[0])
                    ? List.of(
                            Map.of("TABLE_CAT", "information_schema", "TABLE_SCHEM", "", "TABLE_NAME", "GLOBAL_STATUS"),
                            Map.of("TABLE_CAT", "logic_db", "TABLE_SCHEM", "", "TABLE_NAME", "orders"))
                    : List.of());
        });
        ResultSet orderColumns = mockResultSet("COLUMN_NAME", "order_id");
        when(databaseMetaData.getColumns(eq("logic_db"), isNull(), eq("orders"), eq("%"))).thenReturn(orderColumns);
        ResultSet orderIndexes = mockResultSet("INDEX_NAME", "idx_orders_status");
        when(databaseMetaData.getIndexInfo(eq("logic_db"), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(orderIndexes);
        when(databaseMetaData.getColumns(eq("information_schema"), isNull(), eq("GLOBAL_STATUS"), eq("%")))
                .thenThrow(new SQLException("system catalog should be skipped"));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration), List.of(),
                Map.of("MySQL", List.of("information_schema")), Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "GLOBAL_STATUS"));
        assertThat(actual.iterator().next().getName(), is("logic_db"));
    }
    
    @Test
    void assertLoadWithCatalogBackedMetadataUsingLogicalSchemaName() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl("MySQL"));
        mockEmptyScalarQueries(connection);
        when(databaseMetaData.getTables(isNull(), isNull(), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3);
            return mockMultiRowResultSet("TABLE".equals(tableTypes[0])
                    ? List.of(Map.of("TABLE_CAT", "orders", "TABLE_SCHEM", "", "TABLE_NAME", "orders"))
                    : List.of());
        });
        ResultSet orderColumns = mockResultSet("COLUMN_NAME", "order_id");
        when(databaseMetaData.getColumns(eq("orders"), isNull(), eq("orders"), eq("%"))).thenReturn(orderColumns);
        ResultSet orderIndexes = mockResultSet("INDEX_NAME", "idx_orders_status");
        when(databaseMetaData.getIndexInfo(eq("orders"), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(orderIndexes);
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = loadWithDatabaseAsSchema(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(actual.iterator().next().getName(), is("logic_db"));
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
    }
    
    @Test
    void assertLoadWithoutEmptyTableName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", ""), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(), Map.of("orders", List.of("order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, ""));
    }
    
    @Test
    void assertLoadWithoutEmptyViewName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(), List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", ""), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders")),
                Map.of("active_orders", List.of("order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.VIEW, ""));
    }
    
    @Test
    void assertLoadCatalogWithoutEagerColumns() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")), List.of(), Map.of("orders", List.of("", "order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, ""));
    }
    
    @Test
    void assertLoadCatalogWithoutEagerIndexes() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")), List.of(), Map.of("orders", List.of("order_id")),
                Map.of("orders", List.of("", "idx_orders_status", "idx_orders_status")), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
    }
    
    @Test
    void assertLoadWithDuplicateTableRows() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders"), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")),
                List.of(), Map.of("orders", List.of("order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"), is(1));
    }
    
    @Test
    void assertLoadWithDuplicateViewRows() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(), List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders"), Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "active_orders")),
                Map.of("active_orders", List.of("order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.VIEW, "active_orders"), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithoutSequenceQueryArguments")
    void assertLoadWithoutSequenceQuery(final String name, final String databaseType) throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(createConnectionWithoutSchema(databaseType));
        MCPMetadataSnapshot actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findSnapshot("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    @Test
    void assertLoadWithoutSystemSchemaSequence() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL", List.of(), List.of(), Map.of(), Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "PG_CATALOG", "SEQUENCE_NAME", "order_seq")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        MCPMetadataSnapshot actual = load(Map.of("logic_db", runtimeDatabaseConfiguration), List.of("PostgreSQL"),
                Map.of("PostgreSQL", List.of("pg_catalog"))).findSnapshot("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, "order_seq"));
    }
    
    @Test
    void assertLoadWithoutEmptySequenceName() throws SQLException {
        Connection connection = createConnectionWithMetadata("PostgreSQL", List.of(), List.of(), Map.of(), Map.of(),
                List.of(Map.of("SEQUENCE_SCHEMA", "public", "SEQUENCE_NAME", "")));
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        MCPMetadataSnapshot actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findSnapshot("logic_db").orElseThrow();
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.SEQUENCE, ""));
    }
    
    @Test
    void assertLoadWithWhitespaceInSequenceName() throws SQLException {
        Connection connection = createConnectionWithSequenceMetadata("PostgreSQL", "public", " order_seq ");
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        MCPSequenceMetadata actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findSnapshot("logic_db").orElseThrow()
                .getSequences("public").iterator().next();
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getSequence(), is(" order_seq "));
    }
    
    @Test
    void assertLoadWithFailedSequenceMetadataQuery() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:failed-sequence-query", createConnectionWithFailedSequenceMetadataQuery());
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                    () -> load(Map.of("logic_db", new RuntimeDatabaseConfiguration("jdbc:mock:failed-sequence-query", "", "", MockDriver.class.getName()))));
            assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: connection_failed."));
            assertThat(actual.getCause().getMessage(), is("sequence metadata query failed"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadSequenceDatabaseArguments")
    void assertLoadWithDialectSequenceMetadata(final String name, final String databaseType, final String sequenceSchema,
                                               final String sequenceName) throws SQLException {
        String jdbcUrl = "jdbc:mock:sequence:" + name.replace(' ', '-');
        Driver mockDriver = new MockDriver(jdbcUrl, createConnectionWithSequenceMetadata(databaseType, sequenceSchema, sequenceName));
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            LoadedMetadataCatalog actual = load(Map.of("logic_db", new RuntimeDatabaseConfiguration(jdbcUrl, "", "", MockDriver.class.getName())), List.of(databaseType));
            assertTrue(containsMetadata(actual.findSnapshot("logic_db").orElseThrow(), SupportedMCPMetadataObjectType.SEQUENCE, sequenceName));
        }
    }
    
    @Test
    void assertLoadWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        SQLException expected = new SQLException("permission denied", "28000", 335544352);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(expected);
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().load("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile()));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
        assertThat(actual.getCause(), is(expected));
    }
    
    @Test
    void assertLoadColumns() throws SQLException {
        Connection connection = createMetadataDetailConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        ResultSet columns = mockMultiRowResultSet(List.of(
                Map.of("TABLE_NAME", "order_%\\archive", "COLUMN_NAME", "status", "ORDINAL_POSITION", 2,
                        "DATA_TYPE", Types.VARCHAR, "TYPE_NAME", "varchar", "NULLABLE", DatabaseMetaData.columnNullable),
                Map.of("TABLE_NAME", "order_%\\archive", "COLUMN_NAME", "order_id", "ORDINAL_POSITION", 1,
                        "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "int8", "NULLABLE", DatabaseMetaData.columnNoNulls),
                Map.of("TABLE_NAME", "order_%\\archive", "COLUMN_NAME", "", "ORDINAL_POSITION", 3,
                        "DATA_TYPE", Types.OTHER, "TYPE_NAME", "", "NULLABLE", DatabaseMetaData.columnNullableUnknown)));
        when(databaseMetaData.getColumns(isNull(), eq("PUBLIC"), eq("order\\_\\%\\\\archive"), eq("%"))).thenReturn(columns);
        List<MCPColumnMetadata> actual = loadColumns(createMockRuntimeDatabaseConfiguration(connection), "PUBLIC", "order_%\\archive");
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getName(), is("order_id"));
        assertThat(actual.getFirst().getOrdinalPosition(), is(1));
        assertThat(actual.getFirst().getJdbcType(), is(Types.BIGINT));
        assertThat(actual.getFirst().getNativeTypeName(), is("int8"));
        assertThat(actual.getFirst().getNullability(), is(Nullability.NOT_NULLABLE));
        assertThat(actual.get(1).getNullability(), is(Nullability.NULLABLE));
    }
    
    @Test
    void assertLoadColumnsWithDatabaseAsSchema() throws SQLException {
        Connection connection = createMetadataDetailConnection("MySQL");
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(connection.getCatalog()).thenReturn("orders");
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        ResultSet columns = mockMultiRowResultSet(List.of(
                Map.of("TABLE_NAME", "orders", "COLUMN_NAME", "order_id", "ORDINAL_POSITION", 1,
                        "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "BIGINT", "NULLABLE", DatabaseMetaData.columnNoNulls)));
        when(databaseMetaData.getColumns(eq("orders"), isNull(), eq("orders"), eq("%"))).thenReturn(columns);
        List<MCPColumnMetadata> actual = loadColumns(createMockRuntimeDatabaseConfiguration(connection), "logic_db", "orders",
                Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
    }
    
    @Test
    void assertLoadColumnsWithLogicalCatalogFallback() throws SQLException {
        Connection connection = createMetadataDetailConnection("MySQL");
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(connection.getCatalog()).thenReturn("logic_db");
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        ResultSet emptyColumns = mockMultiRowResultSet(List.of());
        ResultSet fallbackColumns = mockMultiRowResultSet(List.of(
                Map.of("TABLE_NAME", "orders", "COLUMN_NAME", "order_id", "ORDINAL_POSITION", 1,
                        "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "BIGINT", "NULLABLE", DatabaseMetaData.columnNoNulls)));
        when(databaseMetaData.getColumns(eq("logic_db"), isNull(), eq("orders"), eq("%"))).thenReturn(emptyColumns);
        when(databaseMetaData.getColumns(isNull(), isNull(), eq("orders"), eq("%"))).thenReturn(fallbackColumns);
        List<MCPColumnMetadata> actual = loadColumns(createMockRuntimeDatabaseConfiguration(connection), "logic_db", "orders",
                Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
    }
    
    @Test
    void assertLoadColumnsWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadColumns("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public", "t_order"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertLoadSchemaColumnsInOneQuery() throws SQLException {
        Connection connection = createMetadataDetailConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        ResultSet columns = mockMultiRowResultSet(List.of(
                Map.of("TABLE_NAME", "orders", "COLUMN_NAME", "order_id", "ORDINAL_POSITION", 1,
                        "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "int8", "NULLABLE", DatabaseMetaData.columnNoNulls),
                Map.of("TABLE_NAME", "active_orders", "COLUMN_NAME", "status", "ORDINAL_POSITION", 2,
                        "DATA_TYPE", Types.VARCHAR, "TYPE_NAME", "varchar", "NULLABLE", DatabaseMetaData.columnNullableUnknown)));
        when(databaseMetaData.getColumns(isNull(), eq("PUBLIC"), eq("%"), eq("%"))).thenReturn(columns);
        List<MCPColumnMetadata> actual = loadSchemaColumns(createMockRuntimeDatabaseConfiguration(connection), "PUBLIC");
        assertThat(actual.stream().map(MCPColumnMetadata::getRelationName).toList(), is(List.of("active_orders", "orders")));
        assertThat(actual.getFirst().getNullability(), is(Nullability.UNKNOWN));
        verify(databaseMetaData).getColumns(isNull(), eq("PUBLIC"), eq("%"), eq("%"));
    }
    
    @Test
    void assertLoadSchemaColumnsWithDatabaseAsSchema() throws SQLException {
        Connection connection = createMetadataDetailConnection("MySQL");
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(connection.getCatalog()).thenReturn("orders");
        ResultSet columns = mockMultiRowResultSet(List.of(
                Map.of("TABLE_NAME", "orders", "COLUMN_NAME", "order_id", "ORDINAL_POSITION", 1,
                        "DATA_TYPE", Types.BIGINT, "TYPE_NAME", "BIGINT", "NULLABLE", DatabaseMetaData.columnNoNulls)));
        when(databaseMetaData.getColumns(eq("orders"), isNull(), eq("%"), eq("%"))).thenReturn(columns);
        List<MCPColumnMetadata> actual = loadSchemaColumns(createMockRuntimeDatabaseConfiguration(connection), "logic_db",
                Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
    }
    
    @Test
    void assertLoadSchemaColumnsWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadSchemaColumns("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertLoadIndexes() throws SQLException {
        Connection connection = createMetadataDetailConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet indexes = mockMultiRowResultSet(List.of(
                Map.of("INDEX_NAME", "idx_orders", "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", false, "ORDINAL_POSITION", 2, "COLUMN_NAME", "status"),
                Map.of("INDEX_NAME", "statistics", "TYPE", DatabaseMetaData.tableIndexStatistic, "NON_UNIQUE", true, "ORDINAL_POSITION", 0, "COLUMN_NAME", ""),
                Map.of("INDEX_NAME", "", "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", true, "ORDINAL_POSITION", 1, "COLUMN_NAME", "ignored"),
                Map.of("INDEX_NAME", "idx_orders", "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", false, "ORDINAL_POSITION", 1, "COLUMN_NAME", "tenant_id"),
                Map.of("INDEX_NAME", "idx_status", "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", true, "ORDINAL_POSITION", 1, "COLUMN_NAME", "status")));
        when(databaseMetaData.getIndexInfo(isNull(), eq("PUBLIC"), eq("orders"), eq(false), eq(false))).thenReturn(indexes);
        List<ShardingSphereIndex> actual = loadIndexes(createMockRuntimeDatabaseConfiguration(connection), "PUBLIC", "orders");
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getName(), is("idx_orders"));
        assertThat(actual.getFirst().getColumns(), is(List.of("tenant_id", "status")));
        assertTrue(actual.getFirst().isUnique());
        assertThat(actual.get(1).getName(), is("idx_status"));
        assertFalse(actual.get(1).isUnique());
    }
    
    @Test
    void assertLoadIndexesWithDatabaseAsSchema() throws SQLException {
        Connection connection = createMetadataDetailConnection("MySQL");
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(connection.getCatalog()).thenReturn("orders");
        ResultSet indexes = mockMultiRowResultSet(List.of(
                Map.of("INDEX_NAME", "PRIMARY", "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", false,
                        "ORDINAL_POSITION", 1, "COLUMN_NAME", "order_id")));
        when(databaseMetaData.getIndexInfo(eq("orders"), isNull(), eq("orders"), eq(false), eq(false))).thenReturn(indexes);
        List<ShardingSphereIndex> actual = loadIndexes(createMockRuntimeDatabaseConfiguration(connection), "logic_db", "orders",
                Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.stream().map(ShardingSphereIndex::getName).toList(), is(List.of("PRIMARY")));
    }
    
    @Test
    void assertLoadIndexesWithoutMetadataSupport() throws SQLException {
        Connection connection = createMetadataDetailConnection();
        when(connection.getMetaData().getIndexInfo(isNull(), eq("PUBLIC"), eq("orders"), eq(false), eq(false)))
                .thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        assertTrue(loadIndexes(createMockRuntimeDatabaseConfiguration(connection), "PUBLIC", "orders").isEmpty());
    }
    
    @Test
    void assertLoadIndexesWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadIndexes("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public", "t_order"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    private LoadedMetadataCatalog loadWithDatabaseAsSchema(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return load(runtimeDatabases, List.of(), Map.of(), Map.of("MySQL", DialectSchemaSemantics.DATABASE_AS_SCHEMA));
    }
    
    private RuntimeDatabaseProfile createDatabaseProfile() {
        return new RuntimeDatabaseProfile("logic_db", "Firebird", "", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
    
    private Connection createMetadataDetailConnection() throws SQLException {
        return createMetadataDetailConnection("PostgreSQL");
    }
    
    private Connection createMetadataDetailConnection(final String databaseType) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("16.2");
        when(databaseMetaData.getURL()).thenReturn(getMetadataJdbcUrl(databaseType));
        return result;
    }
}
