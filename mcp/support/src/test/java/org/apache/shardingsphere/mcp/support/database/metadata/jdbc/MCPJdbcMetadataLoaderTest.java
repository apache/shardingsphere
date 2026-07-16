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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata.Nullability;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), objectType, objectName));
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
