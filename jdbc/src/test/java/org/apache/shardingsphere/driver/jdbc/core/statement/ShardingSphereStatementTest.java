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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereStatementTest {
    
    @Test
    void assertGetGeneratedKeysBeforeExecutionWithoutDialectColumn() throws SQLException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertFalse(actual.next());
            assertNull(actual.getMetaData().getColumnName(1));
            assertThat(actual.getStatement(), sameInstance(statement));
        }
    }
    
    @Test
    void assertGetGeneratedKeysBeforeExecutionWithDialectColumn() throws SQLException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertFalse(actual.next());
            assertThat(actual.getMetaData().getColumnName(1), is("GENERATED_KEY"));
            assertThat(actual.getStatement(), sameInstance(statement));
        }
    }
    
    @Test
    void assertGetGeneratedKeysByColumnIndexWhenColumnNameMissing() throws SQLException {
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("id")).thenThrow(new SQLException("Column not found"));
        when(generatedKeys.getObject(1)).thenReturn(2L);
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        setInsertStatementContext(statement);
        addStatement(statement, generatedKeys);
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertTrue(actual.next());
            assertThat(actual.getObject(1), is(2L));
        }
        verify(generatedKeys).getObject("id");
        verify(generatedKeys).getObject(1);
    }
    
    @Test
    void assertGetGeneratedKeysByDialectGeneratedKeyColumn() throws SQLException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("GENERATED_KEY")).thenReturn(3L);
        setInsertStatementContext(statement);
        addStatement(statement, generatedKeys);
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertTrue(actual.next());
            assertThat(actual.getObject(1), is(3L));
        }
        verify(generatedKeys).getObject("GENERATED_KEY");
        verify(generatedKeys, never()).getObject("id");
        verify(generatedKeys, never()).getObject(1);
    }
    
    @Test
    void assertCreateStatementAfterDatabaseRemoved() {
        ShardingSphereConnection connection = createConnection(createMetaData(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        UnknownDatabaseException expected = removeDatabase(connection);
        assertThat(assertThrows(UnknownDatabaseException.class, () -> new ShardingSphereStatement(connection)), sameInstance(expected));
        verify(connection, never()).registerStatementManager(any());
    }
    
    private UnknownDatabaseException removeDatabase(final ShardingSphereConnection connection) {
        UnknownDatabaseException result = new UnknownDatabaseException("foo_db");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
        when(connection.getContextManager().getDatabase("foo_db")).thenThrow(result);
        return result;
    }
    
    @Test
    void assertGetGeneratedKeysAfterDatabaseRemoved() {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        setInsertStatementContext(statement);
        UnknownDatabaseException expected = removeDatabase(statement.getConnection());
        assertThat(assertThrows(UnknownDatabaseException.class, statement::getGeneratedKeys), sameInstance(expected));
    }
    
    @Test
    void assertGetGeneratedKeysBeforeExecutionAfterDatabaseRemoved() {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        UnknownDatabaseException expected = removeDatabase(statement.getConnection());
        assertThat(assertThrows(UnknownDatabaseException.class, statement::getGeneratedKeys), sameInstance(expected));
    }
    
    @Test
    void assertExecuteQueryAfterDatabaseRemoved() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereStatement statement = createStatement(databaseType);
        removeDatabase(statement.getConnection());
        SQLException expected = new SQLException("Unknown database 'foo_db'", "42000", 1049);
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class, CALLS_REAL_METHODS)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(
                    argThat(ex -> ex instanceof UnknownDatabaseException && "foo_db".equals(((UnknownDatabaseException) ex).getDatabaseName())), same(databaseType))).thenReturn(expected);
            SQLException actual = assertThrows(SQLException.class, () -> statement.executeQuery("SELECT 1"));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getMessage(), is("Unknown database 'foo_db'"));
            assertThat(actual.getSQLState(), is("42000"));
            assertThat(actual.getErrorCode(), is(1049));
        }
    }
    
    @Test
    void assertExecuteQueryAfterDatabaseRemovedWithoutDialectMapper() {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        removeDatabase(statement.getConnection());
        SQLException actual = assertThrows(SQLException.class, () -> statement.executeQuery("SELECT 1"));
        assertThat(actual.getCause(), isA(UnknownDatabaseException.class));
        assertThat(((UnknownDatabaseException) actual.getCause()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertExecuteUpdateAfterDatabaseRemoved() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereStatement statement = createStatement(databaseType);
        removeDatabase(statement.getConnection());
        SQLException expected = new SQLException("Unknown database 'foo_db'", "42000", 1049);
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class, CALLS_REAL_METHODS)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(
                    argThat(ex -> ex instanceof UnknownDatabaseException && "foo_db".equals(((UnknownDatabaseException) ex).getDatabaseName())), same(databaseType))).thenReturn(expected);
            SQLException actual = assertThrows(SQLException.class, () -> statement.executeUpdate("UPDATE foo_table SET id = 1"));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getMessage(), is("Unknown database 'foo_db'"));
            assertThat(actual.getSQLState(), is("42000"));
            assertThat(actual.getErrorCode(), is(1049));
        }
    }
    
    @Test
    void assertExecuteAfterDatabaseRemoved() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereStatement statement = createStatement(databaseType);
        removeDatabase(statement.getConnection());
        SQLException expected = new SQLException("Unknown database 'foo_db'", "42000", 1049);
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class, CALLS_REAL_METHODS)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(
                    argThat(ex -> ex instanceof UnknownDatabaseException && "foo_db".equals(((UnknownDatabaseException) ex).getDatabaseName())), same(databaseType))).thenReturn(expected);
            SQLException actual = assertThrows(SQLException.class, () -> statement.execute("SELECT 1"));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getMessage(), is("Unknown database 'foo_db'"));
            assertThat(actual.getSQLState(), is("42000"));
            assertThat(actual.getErrorCode(), is(1049));
        }
    }
    
    @Test
    void assertGetGeneratedKeysWithoutColumnName() throws SQLException, ReflectiveOperationException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
        Plugins.getMemberAccessor().set(ShardingSphereStatement.class.getDeclaredField("queryContext"), statement, queryContext);
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertNull(actual.getMetaData().getColumnName(1));
            assertFalse(actual.next());
        }
    }
    
    @Test
    void assertGetGeneratedKeysWithDifferentGlobalProtocol() throws SQLException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(statement.getConnection().getContextManager().getMetaDataContexts().getMetaData().getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        setInsertStatementContext(statement);
        try (ResultSet actual = statement.getGeneratedKeys()) {
            assertThat(actual.getMetaData().getColumnName(1), is("GENERATED_KEY"));
            assertFalse(actual.next());
        }
    }
    
    @Test
    void assertGetGeneratedKeysWithBackendFailure() throws SQLException {
        ShardingSphereStatement statement = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        setInsertStatementContext(statement);
        Statement routedStatement = mock(Statement.class);
        SQLException expected = new SQLException("foo_failure", "42000", 123);
        when(routedStatement.getGeneratedKeys()).thenThrow(expected);
        statement.getRoutedStatements().add(routedStatement);
        assertThat(assertThrows(SQLException.class, statement::getGeneratedKeys), sameInstance(expected));
    }
    
    @Test
    void assertExecuteQueryWithDatabaseRemovedDuringFailure() throws SQLException {
        SQLException expected = new SQLException("foo_failure", "42000", 123);
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.executeQuery("SELECT 1")), sameInstance(expected));
    }
    
    private ShardingSphereStatement createFailingStatement(final Exception cause) throws SQLException {
        ShardingSphereStatement result = createStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        Statement routedStatement = mock(Statement.class);
        doAnswer(invocation -> {
            removeDatabase(result.getConnection());
            throw cause;
        }).when(routedStatement).close();
        getStatements(result).add(routedStatement);
        return result;
    }
    
    @Test
    void assertExecuteUpdateWithDatabaseRemovedDuringFailure() throws SQLException {
        SQLException expected = new SQLException("foo_failure", "42000", 123);
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1")), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWithGeneratedKeysDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1", Statement.RETURN_GENERATED_KEYS)), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWithColumnIndexesDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1", new int[]{1})), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWithColumnNamesDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1", new String[]{"id"})), sameInstance(expected));
    }
    
    @Test
    void assertExecuteWithDatabaseRemovedDuringFailure() throws SQLException {
        SQLException expected = new SQLException("foo_failure", "42000", 123);
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.execute("SELECT 1")), sameInstance(expected));
    }
    
    @Test
    void assertExecuteWithGeneratedKeysDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.execute("SELECT 1", Statement.RETURN_GENERATED_KEYS)), sameInstance(expected));
    }
    
    @Test
    void assertExecuteWithColumnIndexesDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.execute("SELECT 1", new int[]{1})), sameInstance(expected));
    }
    
    @Test
    void assertExecuteWithColumnNamesDuringDatabaseRemoval() throws SQLException {
        SQLException expected = new SQLException("foo_failure");
        ShardingSphereStatement statement = createFailingStatement(expected);
        assertThat(assertThrows(SQLException.class, () -> statement.execute("SELECT 1", new String[]{"id"})), sameInstance(expected));
    }
    
    @Test
    void assertExecuteDistSQLDuringDatabaseRemoval() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereStatement statement = createStatement(databaseType);
        ShardingSphereConnection connection = statement.getConnection();
        ShardingSphereMetaData metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        SQLParserRule parserRule = mock(SQLParserRule.class, RETURNS_DEEP_STUBS);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singletonList(parserRule));
        when(metaData.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ParseStatement sqlStatement = new ParseStatement("SELECT 1");
        UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
        when(parserRule.getSQLParserEngine(databaseType).parse("foo_distsql", false)).thenAnswer(invocation -> {
            when(metaData.getDatabase("foo_db")).thenReturn(null);
            when(connection.getContextManager().getDatabase("foo_db")).thenThrow(expected);
            return sqlStatement;
        });
        SQLException actual = assertThrows(SQLException.class, () -> statement.execute("foo_distsql"));
        assertThat(actual.getCause(), sameInstance(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideFailuresDuringDatabaseRemoval")
    void assertExecuteUpdateWithRuntimeFailureDuringDatabaseRemoval(final String name, final RuntimeException expected) throws SQLException {
        ShardingSphereStatement statement = createFailingStatement(expected);
        SQLException actual = assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1"));
        assertThat(actual.getCause(), sameInstance(expected));
    }
    
    private static Stream<Arguments> provideFailuresDuringDatabaseRemoval() {
        return Stream.of(
                Arguments.of("runtime failure", new IllegalStateException("foo_failure")),
                Arguments.of("database missing", new UnknownDatabaseException("foo_db")),
                Arguments.of("database not selected", new NoDatabaseSelectedException()));
    }
    
    @Test
    void assertExecuteUpdateWithProtocolFailureDuringDatabaseRemoval() throws SQLException {
        ShardingSphereStatement statement = createFailingStatement(new DatabaseProtocolException("foo_failure"));
        SQLException actual = assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1"));
        assertThat(actual.getErrorCode(), is(30002));
        assertThat(actual.getMessage(), is("Database protocol exception: foo_failure"));
    }
    
    @Test
    void assertExecuteUpdateWithSqlFailureDuringDatabaseRemoval() throws SQLException {
        ShardingSphereSQLException cause = mock(ShardingSphereSQLException.class);
        SQLException expected = new SQLException("foo_failure", "42000", 123);
        when(cause.toSQLException()).thenReturn(expected);
        ShardingSphereStatement statement = createFailingStatement(cause);
        assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1")), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWithDifferentGlobalProtocol() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereStatement statement = createStatement(databaseType);
        when(statement.getConnection().getContextManager().getMetaDataContexts().getMetaData().getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        Statement routedStatement = mock(Statement.class);
        UnknownDatabaseException cause = new UnknownDatabaseException("foo_db");
        doThrow(cause).when(routedStatement).close();
        statement.getRoutedStatements().add(routedStatement);
        SQLException expected = new SQLException("foo_failure");
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(cause, databaseType)).thenReturn(expected);
            assertThat(assertThrows(SQLException.class, () -> statement.executeUpdate("SELECT 1")), sameInstance(expected));
        }
    }
    
    private ShardingSphereStatement createStatement(final DatabaseType databaseType) {
        return new ShardingSphereStatement(createConnection(createMetaData(databaseType)));
    }
    
    private ShardingSphereConnection createConnection(final ShardingSphereMetaData metaData) {
        ShardingSphereConnection result = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(result.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(result.getCurrentDatabaseName()).thenReturn("foo_db");
        ShardingSphereDatabase database = metaData.getDatabase("foo_db");
        when(result.getContextManager().getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyList()))));
        when(result.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setInsertStatementContext(final ShardingSphereStatement statement) {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class);
        when(generatedKeyContext.getColumnName()).thenReturn("id");
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.emptyList());
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        Plugins.getMemberAccessor().set(ShardingSphereStatement.class.getDeclaredField("queryContext"), statement, queryContext);
        Plugins.getMemberAccessor().set(ShardingSphereStatement.class.getDeclaredField("returnGeneratedKeys"), statement, true);
    }
    
    private void addStatement(final ShardingSphereStatement shardingSphereStatement, final ResultSet generatedKeys) throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        getStatements(shardingSphereStatement).add(statement);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Collection<Statement> getStatements(final ShardingSphereStatement statement) {
        Field statementsField = ShardingSphereStatement.class.getDeclaredField("statements");
        return (Collection<Statement>) Plugins.getMemberAccessor().get(statementsField, statement);
    }
}
