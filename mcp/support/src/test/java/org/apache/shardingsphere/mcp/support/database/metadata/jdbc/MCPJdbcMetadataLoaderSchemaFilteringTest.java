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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderSchemaFilteringTest extends AbstractMCPJdbcMetadataLoaderTest {
    
    @Test
    void assertLoadWithoutSchemaObjects() throws SQLException {
        Driver mockDriver = new MockDriver("jdbc:mock:no-schema", createConnectionWithoutSchema("MySQL"));
        try (MockDriverRegistration ignored = MockDriverRegistration.register(mockDriver)) {
            LoadedMetadataCatalog actual = load(Map.of("logic_db", new RuntimeDatabaseConfiguration("jdbc:mock:no-schema", "", "", MockDriver.class.getName())));
            Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
            assertThat(schemas.size(), is(1));
            assertThat(schemas.iterator().next().getName(), is("logic_db"));
            assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.TABLE, "orders"));
            assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
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
                Map.of("MySQL", List.of("information_schema"))).findMetadata("logic_db").orElseThrow();
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
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertThat(actual.iterator().next().getName(), is("logic_db"));
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
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
    void assertLoadWithoutEmptyColumnName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")), List.of(), Map.of("orders", List.of("", "order_id")), Map.of(), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, "order_id"));
        assertFalse(containsMetadata(actual, SupportedMCPMetadataObjectType.COLUMN, ""));
    }
    
    @Test
    void assertLoadWithoutEmptyOrDuplicateIndexName() throws SQLException {
        Connection connection = createConnectionWithMetadata(
                List.of(Map.of("TABLE_SCHEM", "PUBLIC", "TABLE_NAME", "orders")), List.of(), Map.of("orders", List.of("order_id")),
                Map.of("orders", List.of("", "idx_orders_status", "idx_orders_status")), List.of());
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = createMockRuntimeDatabaseConfiguration(connection);
        Collection<ShardingSphereSchema> actual = load(Map.of("logic_db", runtimeDatabaseConfiguration)).findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
        assertThat(countMetadata(actual, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"), is(1));
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
}
