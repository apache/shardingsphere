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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PostgreSQLPreparedStatementParameterTypeResolverTest {
    
    private static final String SQL = "SELECT id FROM foo_tbl WHERE id=?";
    
    private static final List<Object> PARAMETERS = Collections.singletonList(1);
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithWhereColumns() {
        PostgreSQLServerPreparedStatement preparedStatement = createBoundPreparedStatement("SELECT id FROM foo_tbl WHERE id=? OR k IN (?, ?) OR k BETWEEN ? AND ?", Arrays.asList(0, 1, 2, 3, 4));
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement, Arrays.asList(
                new PostgreSQLTypeUnspecifiedSQLParameter("1"), new PostgreSQLTypeUnspecifiedSQLParameter(" 2 "), new PostgreSQLTypeUnspecifiedSQLParameter("+3"),
                new PostgreSQLTypeUnspecifiedSQLParameter("-4"), new PostgreSQLTypeUnspecifiedSQLParameter("\t5\n")));
        assertThat(actual, is(Arrays.asList(1, 2L, 3L, -4L, 5L)));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithSetAssignment() {
        PostgreSQLServerPreparedStatement preparedStatement = createBoundPreparedStatement("UPDATE foo_tbl SET k=? WHERE id=?", Arrays.asList(0, 1));
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement,
                Arrays.asList(new PostgreSQLTypeUnspecifiedSQLParameter("1"), new PostgreSQLTypeUnspecifiedSQLParameter("2")));
        assertThat(actual, is(Arrays.asList(1L, 2)));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithReorderedParameters() {
        PostgreSQLServerPreparedStatement preparedStatement = createBoundPreparedStatement("SELECT id FROM foo_tbl WHERE id=? AND k=?", Arrays.asList(1, 0));
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement,
                Arrays.asList(new PostgreSQLTypeUnspecifiedSQLParameter("1"), new PostgreSQLTypeUnspecifiedSQLParameter("2")));
        assertThat(actual, is(Arrays.asList(1L, 2)));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithParameterOfDifferentColumnTypes() {
        PostgreSQLServerPreparedStatement preparedStatement = createBoundPreparedStatement("SELECT id FROM foo_tbl WHERE id=? OR k=?", Arrays.asList(0, 0));
        PostgreSQLTypeUnspecifiedSQLParameter expected = new PostgreSQLTypeUnspecifiedSQLParameter("1");
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement, Collections.singletonList(expected));
        assertThat(actual.get(0), sameInstance(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resolveUnspecifiedParameterValuesWithUnresolvedTextArguments")
    void assertResolveUnspecifiedParameterValuesWithUnresolvedText(final String name, final String text) {
        PostgreSQLServerPreparedStatement preparedStatement = createBoundPreparedStatement(SQL, Collections.singletonList(0));
        PostgreSQLTypeUnspecifiedSQLParameter expected = new PostgreSQLTypeUnspecifiedSQLParameter(text);
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement, Collections.singletonList(expected));
        assertThat(actual.get(0), sameInstance(expected));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithInsertValues() {
        PostgreSQLServerPreparedStatement preparedStatement = createInsertPreparedStatement("INSERT INTO foo_tbl (id, name, k) VALUES (?, ?, length(?))", Arrays.asList(0, 1, 2));
        prepareInsertTableMetaData();
        PostgreSQLTypeUnspecifiedSQLParameter expectedName = new PostgreSQLTypeUnspecifiedSQLParameter("foo_name");
        PostgreSQLTypeUnspecifiedSQLParameter expectedLength = new PostgreSQLTypeUnspecifiedSQLParameter("bar");
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement,
                Arrays.asList(new PostgreSQLTypeUnspecifiedSQLParameter("7"), expectedName, expectedLength));
        assertThat(actual.get(0), is(7));
        assertThat(actual.get(1), sameInstance(expectedName));
        assertThat(actual.get(2), sameInstance(expectedLength));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithInsertTableNotFound() {
        PostgreSQLServerPreparedStatement preparedStatement = createInsertPreparedStatement("INSERT INTO foo_tbl (id) VALUES (?)", Collections.singletonList(0));
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        PostgreSQLTypeUnspecifiedSQLParameter expected = new PostgreSQLTypeUnspecifiedSQLParameter("7");
        List<Object> actual = PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement, Collections.singletonList(expected));
        assertThat(actual.get(0), sameInstance(expected));
    }
    
    @Test
    void assertResolveUnspecifiedParameterValuesWithoutUnspecified() {
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(0));
        assertThat(PostgreSQLPreparedStatementParameterTypeResolver.resolveUnspecifiedParameterValues(connectionSession, preparedStatement, PARAMETERS), sameInstance(PARAMETERS));
        verifyNoInteractions(connectionSession);
    }
    
    @Test
    void assertResolveParameterTypesWithConnectionSession() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse(SQL, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("postgres");
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        when(connectionSession.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        prepareJDBCBackendConnectionWithParamTypes();
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), new ArrayList<>(Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED)), Collections.singletonList(0));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement, PARAMETERS);
        assertThat(preparedStatement.getParameterTypes(), is(Collections.singletonList(PostgreSQLBinaryColumnType.INT4)));
    }
    
    @Test
    void assertResolveParameterTypesWithConnectionSessionWithoutUnspecified() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(0));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement, PARAMETERS);
        verifyNoInteractions(connectionSession);
    }
    
    @Test
    void assertResolveParameterTypesWithConnectionSessionWithRepeatedParameter() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                "SELECT id FROM foo_tbl WHERE id=? OR id=?", sqlStatementContext, new HintValueContext(), Arrays.asList(PostgreSQLBinaryColumnType.INT4, PostgreSQLBinaryColumnType.UNSPECIFIED),
                Arrays.asList(0, 0));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(connectionSession, preparedStatement, Arrays.asList(1, 1));
        verifyNoInteractions(connectionSession);
    }
    
    @Test
    void assertResolveParameterTypesWithPreparedStatement() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse("SELECT id FROM foo_tbl WHERE id=? AND k=?", false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.SMALLINT);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int2");
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        when(actualPreparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("SELECT id FROM foo_tbl WHERE id=? AND k=?",
                sqlStatementContext, new HintValueContext(), Arrays.asList(PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.INT4), Arrays.asList(0, 1));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(preparedStatement, actualPreparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.INT2, PostgreSQLBinaryColumnType.INT4)));
    }
    
    @Test
    void assertResolveParameterTypesWithPreparedStatementWithReorderedParameters() throws SQLException {
        SQLStatement sqlStatement = sqlParserEngine.parse("SELECT id FROM foo_tbl WHERE id=? AND k=?", false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.SMALLINT);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int2");
        when(parameterMetaData.getParameterType(2)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterTypeName(2)).thenReturn("int4");
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        when(actualPreparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("SELECT id FROM foo_tbl WHERE id=? AND k=?",
                sqlStatementContext, new HintValueContext(), Arrays.asList(PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.UNSPECIFIED), Arrays.asList(1, 0));
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(preparedStatement, actualPreparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.INT4, PostgreSQLBinaryColumnType.INT2)));
    }
    
    @Test
    void assertResolveParameterTypesWithPreparedStatementWithoutUnspecified() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(0));
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(preparedStatement, actualPreparedStatement);
        verifyNoInteractions(actualPreparedStatement);
    }
    
    @Test
    void assertResolveInsertParameterTypes() {
        PostgreSQLServerPreparedStatement preparedStatement = createInsertPreparedStatement("INSERT INTO foo_tbl (id, uuid_col, created_at) VALUES (?, ?, ?)", Arrays.asList(0, 1, 2));
        prepareInsertTableMetaData();
        PostgreSQLPreparedStatementParameterTypeResolver.resolveInsertParameterTypes(connectionSession, preparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.INT4, PostgreSQLBinaryColumnType.JSON, PostgreSQLBinaryColumnType.TIMESTAMP)));
    }
    
    @Test
    void assertResolveInsertParameterTypesWithReorderedParameters() {
        PostgreSQLServerPreparedStatement preparedStatement = createInsertPreparedStatement("INSERT INTO foo_tbl (id, name, k) VALUES (?, ?, ?)", Arrays.asList(1, 0, 2));
        prepareInsertTableMetaData();
        PostgreSQLPreparedStatementParameterTypeResolver.resolveInsertParameterTypes(connectionSession, preparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.VARCHAR, PostgreSQLBinaryColumnType.INT4, PostgreSQLBinaryColumnType.INT8)));
    }
    
    @Test
    void assertResolveInsertParameterTypesWithNestedParameter() {
        PostgreSQLServerPreparedStatement preparedStatement = createInsertPreparedStatement("INSERT INTO foo_tbl (id, name, k) VALUES (length(?), ?, ?)", Arrays.asList(0, 1, 2));
        prepareInsertTableMetaData();
        PostgreSQLPreparedStatementParameterTypeResolver.resolveInsertParameterTypes(connectionSession, preparedStatement);
        assertThat(preparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLBinaryColumnType.UNSPECIFIED, PostgreSQLBinaryColumnType.VARCHAR, PostgreSQLBinaryColumnType.INT8)));
    }
    
    private static Stream<Arguments> resolveUnspecifiedParameterValuesWithUnresolvedTextArguments() {
        return Stream.of(
                Arguments.of("hexadecimal", "0x1F"),
                Arguments.of("underscore separated", "1_000"),
                Arguments.of("decimal fraction", "1.5"),
                Arguments.of("out of range", "3000000000"),
                Arguments.of("non ascii digit", "١"),
                Arguments.of("empty", ""));
    }
    
    private PostgreSQLServerPreparedStatement createBoundPreparedStatement(final String sql, final List<Integer> actualParameterMarkerIndexes) {
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(contextManager.getMetaDataContexts().getMetaData(), "postgres", new HintValueContext())
                .bind(sqlParserEngine.parse(sql, false));
        List<PostgreSQLBinaryColumnType> parameterTypes = new ArrayList<>(Collections.nCopies(actualParameterMarkerIndexes.size(), PostgreSQLBinaryColumnType.UNSPECIFIED));
        return new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, actualParameterMarkerIndexes);
    }
    
    private PostgreSQLServerPreparedStatement createInsertPreparedStatement(final String sql, final List<Integer> actualParameterMarkerIndexes) {
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlParserEngine.parse(sql, false));
        List<PostgreSQLBinaryColumnType> parameterTypes = new ArrayList<>(Collections.nCopies(actualParameterMarkerIndexes.size(), PostgreSQLBinaryColumnType.UNSPECIFIED));
        return new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, actualParameterMarkerIndexes);
    }
    
    private void prepareInsertTableMetaData() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase("postgres");
        when(database.getDefaultSchemaName()).thenReturn("public");
        lenient().when(database.containsSchema("public")).thenReturn(true);
        lenient().when(database.getSchema("public").containsTable("foo_tbl")).thenReturn(true);
        when(database.getSchema("public").getTable("foo_tbl")).thenReturn(new ShardingSphereTable("foo_tbl", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("k", Types.BIGINT, false, false, false, true, false, false),
                new ShardingSphereColumn("uuid_col", Types.OTHER, false, false, false, true, false, false),
                new ShardingSphereColumn("created_at", Types.TIMESTAMP, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        lenient().when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        lenient().when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        lenient().when(result.getMetaDataContexts().getMetaData().containsDatabase("postgres")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("postgres"))).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        lenient().when(storageUnit.getStorageType()).thenReturn(databaseType);
        lenient().when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getName()).thenReturn("public");
        lenient().when(database.getDefaultSchemaName()).thenReturn("public");
        lenient().when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(database.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        lenient().when(database.getSchema("public")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("k", Types.BIGINT, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        when(schema.containsTable(new IdentifierValue("foo_tbl"))).thenReturn(true);
        when(schema.getTable(new IdentifierValue("foo_tbl"))).thenReturn(table);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("postgres"))).thenReturn(database);
        when(result.getDatabase("postgres")).thenReturn(database);
        return result;
    }
    
    private void prepareJDBCBackendConnectionWithParamTypes() throws SQLException {
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int4");
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
}
