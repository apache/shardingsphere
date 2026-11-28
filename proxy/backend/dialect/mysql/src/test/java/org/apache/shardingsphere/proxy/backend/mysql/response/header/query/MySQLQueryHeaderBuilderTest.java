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

package org.apache.shardingsphere.proxy.backend.mysql.response.header.query;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, DatabaseTypeFactory.class})
class MySQLQueryHeaderBuilderTest {
    
    private final QueryHeaderBuilder queryHeaderBuilder = DatabaseTypedSPILoader.getService(QueryHeaderBuilder.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("timePrecisionArguments")
    void assertBuildWithTimePrecision(final String name, final int columnType, final int columnLength, final int decimals, final int expected) throws SQLException {
        ShardingSphereResultSetMetaData metaData = mock(ShardingSphereResultSetMetaData.class);
        when(metaData.getColumnType(1)).thenReturn(columnType);
        when(metaData.getColumnDisplaySize(1)).thenReturn(columnLength);
        when(metaData.getScale(1)).thenReturn(decimals);
        assertThat(queryHeaderBuilder.build(metaData, null, "foo_time", "foo_time", 1).getDecimals(), is(expected));
    }
    
    private static Stream<Arguments> timePrecisionArguments() {
        return Stream.of(
                Arguments.of("whole-second", Types.TIME, 10, 0, 0),
                Arguments.of("milliseconds", Types.TIME, 14, 0, 3),
                Arguments.of("microseconds", Types.TIME, 17, 0, 6),
                Arguments.of("explicit-scale", Types.TIME, 17, 3, 3),
                Arguments.of("unknown-width", Types.TIME, 0, 0, 0),
                Arguments.of("excessive-width", Types.TIME, 18, 0, 0),
                Arguments.of("non-time", Types.INTEGER, 14, 0, 0));
    }
    
    @Test
    void assertBuild() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), "order_id", "order_id", 1);
        assertThat(actual.getSchema(), is("foo_db"));
        assertThat(actual.getTable(), is("t_logic_order"));
        assertThat(actual.getColumnLabel(), is("order_id"));
        assertThat(actual.getColumnName(), is("order_id"));
        assertThat(actual.getColumnLength(), is(1));
        assertThat(actual.getColumnType(), is(Types.INTEGER));
        assertThat(actual.getDecimals(), is(1));
        assertTrue(actual.isSigned());
        assertTrue(actual.isPrimaryKey());
        assertTrue(actual.isNotNull());
        assertTrue(actual.isAutoIncrement());
    }
    
    private ShardingSphereResultSetMetaData createResultSetMetaData() throws SQLException {
        return createResultSetMetaData("t_logic_order", 1);
    }
    
    private ShardingSphereResultSetMetaData createResultSetMetaData(final String tableName, final int columnIndex) throws SQLException {
        ShardingSphereResultSetMetaData result = mock(ShardingSphereResultSetMetaData.class);
        when(result.getTableName(columnIndex)).thenReturn(tableName);
        when(result.getColumnType(columnIndex)).thenReturn(Types.INTEGER);
        when(result.isSigned(columnIndex)).thenReturn(true);
        when(result.isAutoIncrement(columnIndex)).thenReturn(true);
        when(result.getColumnDisplaySize(columnIndex)).thenReturn(1);
        when(result.getScale(columnIndex)).thenReturn(1);
        when(result.isNullable(columnIndex)).thenReturn(ResultSetMetaData.columnNoNulls);
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getTable("t_logic_order")).thenReturn(new ShardingSphereTable(
                "t_logic_order", Collections.singleton(column), Collections.singleton(new ShardingSphereIndex("order_id", Collections.emptyList(), false)), Collections.emptyList()));
        when(result.getSchema("foo_db")).thenReturn(schema);
        when(result.getName()).thenReturn("foo_db");
        return result;
    }
    
    @Test
    void assertBuildWithoutPrimaryKeyColumn() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData("t_logic_order", 2);
        assertFalse(new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), "expr", "expr", 2).isPrimaryKey());
    }
    
    @Test
    void assertBuildWithAliasColumnAndOriginalPrimaryKey() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        when(resultSetMetaData.getColumnName(1)).thenReturn("order_id");
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), "order_id_alias", "order_id_alias", 1);
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    void assertBuildWithNullDatabase() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createActualResultSetMetaData("t_order");
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, null, "order_id", "order_id", 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    private ShardingSphereResultSetMetaData createActualResultSetMetaData(final String tableName) throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getTableName(1)).thenReturn(tableName);
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.getColumnDisplaySize(1)).thenReturn(1);
        when(result.getScale(1)).thenReturn(1);
        when(result.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        return new ShardingSphereResultSetMetaData(result, null, null);
    }
    
    @Test
    void assertBuildWithNullSchema() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData("t_order", 1);
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, database, "order_id", "order_id", 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    @Test
    void assertBuildWithoutDataNodeContainedRule() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), "order_id", "order_id", 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is(actual.getTable()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("datetimeStorageArguments")
    void assertAppendProtocolAttributesWithDatetime(final String name, final DatabaseType storageType, final Map<String, Object> expected) throws SQLException {
        mockFederationRule();
        QueryHeader actual = new QueryHeader("foo_db", "foo_table", "foo_datetime", "foo_datetime", Types.TIMESTAMP, "DATETIME", 26, 6, true, false, false, false);
        ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(DatabaseTypeFactory.get(resultSet.getStatement().getConnection().getMetaData())).thenReturn(storageType);
        queryHeaderBuilder.appendProtocolAttributes(Collections.singleton(actual), resultSet);
        assertThat(actual.getProtocolAttributes(), is(expected));
    }
    
    private SQLFederationRule mockFederationRule() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SQLFederationRule result = mock(SQLFederationRule.class);
        RuleMetaData ruleMetaData = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(result).when(ruleMetaData).getSingleRule(SQLFederationRule.class);
        return result;
    }
    
    private static Stream<Arguments> datetimeStorageArguments() {
        DatabaseType sqlserver = mock(DatabaseType.class);
        when(sqlserver.getType()).thenReturn("SQLServer");
        return Stream.of(
                Arguments.of("mysql", TypedSPILoader.getService(DatabaseType.class, "MySQL"), Collections.singletonMap(MySQLQueryHeaderBuilder.MYSQL_DATETIME, true)),
                Arguments.of("sqlserver", sqlserver, Collections.emptyMap()),
                Arguments.of("mysql-trunk-only", TypedSPILoader.getService(DatabaseType.class, "H2"), Collections.emptyMap()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonDatetimeArguments")
    void assertAppendProtocolAttributesWithoutDatetime(final String name, final int jdbcType, final String typeName) throws SQLException {
        mockFederationRule();
        QueryHeader actual = new QueryHeader("foo_db", "foo_table", "foo_column", "foo_column", jdbcType, typeName, 26, 6, true, false, false, false);
        ResultSet resultSet = mock(ResultSet.class);
        queryHeaderBuilder.appendProtocolAttributes(Collections.singleton(actual), resultSet);
        assertTrue(actual.getProtocolAttributes().isEmpty());
        verifyNoInteractions(resultSet);
    }
    
    private static Stream<Arguments> nonDatetimeArguments() {
        return Stream.of(Arguments.of("non-temporal", Types.INTEGER, "INTEGER"), Arguments.of("timestamp", Types.TIMESTAMP, "TIMESTAMP"), Arguments.of("missing-type-name", Types.TIMESTAMP, ""));
    }
    
    @Test
    void assertAppendProtocolAttributesWithMultipleDatetimeColumns() throws SQLException {
        mockFederationRule();
        QueryHeader first = new QueryHeader("foo_db", "foo_table", "foo_first", "foo_first", Types.TIMESTAMP, "DATETIME", 26, 6, true, false, false, false);
        QueryHeader second = new QueryHeader("foo_db", "foo_table", "foo_second", "foo_second", Types.TIMESTAMP, "datetime", 26, 6, true, false, false, false);
        ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(resultSet.getStatement().getConnection().getMetaData()).thenReturn(metaData);
        when(DatabaseTypeFactory.get(metaData)).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        queryHeaderBuilder.appendProtocolAttributes(Arrays.asList(first, second), resultSet);
        assertThat(first.getProtocolAttributes(), is(Collections.singletonMap(MySQLQueryHeaderBuilder.MYSQL_DATETIME, true)));
        assertThat(second.getProtocolAttributes(), is(Collections.singletonMap(MySQLQueryHeaderBuilder.MYSQL_DATETIME, true)));
        verify(resultSet.getStatement().getConnection()).getMetaData();
    }
    
    @Test
    void assertAppendProtocolAttributesWithFederationEnabled() throws SQLException {
        SQLFederationRule federationRule = mockFederationRule();
        QueryHeader actual = new QueryHeader("foo_db", "foo_table", "foo_datetime", "foo_datetime", Types.TIMESTAMP, "DATETIME", 26, 6, true, false, false, false);
        when(federationRule.isSqlFederationEnabled()).thenReturn(true);
        ResultSet resultSet = mock(ResultSet.class);
        queryHeaderBuilder.appendProtocolAttributes(Collections.singleton(actual), resultSet);
        assertTrue(actual.getProtocolAttributes().isEmpty());
        verifyNoInteractions(resultSet);
    }
    
    @Test
    void assertAppendProtocolAttributesWithSingleHeader() throws SQLException {
        mockFederationRule();
        QueryHeader actual = new QueryHeader("foo_db", "foo_table", "foo_datetime", "foo_datetime", Types.TIMESTAMP, "DATETIME", 26, 6, true, false, false, false);
        ResultSet resultSet = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(DatabaseTypeFactory.get(resultSet.getStatement().getConnection().getMetaData())).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        queryHeaderBuilder.appendProtocolAttributes(actual, resultSet);
        assertThat(actual.getProtocolAttributes(), is(Collections.singletonMap(MySQLQueryHeaderBuilder.MYSQL_DATETIME, true)));
    }
}
