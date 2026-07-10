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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatementManagerTest {
    
    private static final String SQL = "SELECT 1";
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "SQL92");
    
    private static final StatementOption STATEMENT_OPTION = new StatementOption(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    
    private final StatementManager statementManager = new StatementManager();
    
    @Test
    void assertCreateStorageResource() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement expected = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenReturn(expected);
        Statement actual = statementManager.createStorageResource(connection, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWithCreateStatementFallback() throws SQLException {
        Connection connection = mock(Connection.class);
        Statement expected = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenThrow(SQLFeatureNotSupportedException.class);
        when(connection.createStatement()).thenReturn(expected);
        Statement actual = statementManager.createStorageResource(connection, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateStorageResourceThrowsSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        SQLException expected = new SQLException("foo_error");
        when(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenThrow(expected);
        SQLException actual = assertThrows(SQLException.class, () -> statementManager.createStorageResource(connection, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWithExecutionUnit() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement expected = mockPreparedStatement(connection);
        Statement actual = statementManager.createStorageResource(createExecutionUnit(), connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWithPrepareStatementFallback() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement expected = mock(PreparedStatement.class);
        when(connection.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenThrow(SQLFeatureNotSupportedException.class);
        when(connection.prepareStatement(SQL)).thenReturn(expected);
        Statement actual = statementManager.createStorageResource(createExecutionUnit(), connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE);
        assertThat(actual, is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("generatedKeysOptions")
    void assertCreateStorageResourceWithGeneratedKeys(final String name, final Connection connection, final StatementOption option, final PreparedStatement expected) throws SQLException {
        Statement actual = statementManager.createStorageResource(createExecutionUnit(), connection, 0, ConnectionMode.CONNECTION_STRICTLY, option, DATABASE_TYPE);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWithCachedStatement() throws SQLException {
        Connection connection = mock(Connection.class);
        ExecutionUnit executionUnit = createExecutionUnit();
        PreparedStatement expected = mockPreparedStatement(connection);
        when(expected.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);
        when(expected.isClosed()).thenReturn(false);
        Statement actual = statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE);
        assertThat(actual, is(expected));
        assertThat(statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWhenCachedConnectionClosed() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement cached = mock(PreparedStatement.class);
        PreparedStatement expected = mock(PreparedStatement.class);
        when(connection.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenReturn(cached, expected);
        when(cached.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(true);
        ExecutionUnit executionUnit = createExecutionUnit();
        assertThat(statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(cached));
        assertThat(statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(expected));
    }
    
    @Test
    void assertCreateStorageResourceWhenCachedStatementClosed() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement cached = mock(PreparedStatement.class);
        PreparedStatement expected = mock(PreparedStatement.class);
        when(connection.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenReturn(cached, expected);
        when(cached.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);
        when(cached.isClosed()).thenReturn(true);
        ExecutionUnit executionUnit = createExecutionUnit();
        assertThat(statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(cached));
        assertThat(statementManager.createStorageResource(executionUnit, connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(expected));
    }
    
    @Test
    void assertClose() throws SQLException, ReflectiveOperationException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mockPreparedStatement(connection);
        assertThat(statementManager.createStorageResource(createExecutionUnit(), connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(statement));
        statementManager.close();
        assertTrue(getCachedStatements().isEmpty());
    }
    
    @Test
    void assertCloseWithSQLException() throws SQLException, ReflectiveOperationException {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mockPreparedStatement(connection);
        SQLException expected = new SQLException("foo_error");
        doThrow(expected).when(statement).close();
        assertThat(statementManager.createStorageResource(createExecutionUnit(), connection, 0, ConnectionMode.CONNECTION_STRICTLY, STATEMENT_OPTION, DATABASE_TYPE), is(statement));
        SQLException actual = assertThrows(SQLException.class, statementManager::close);
        assertThat(actual.getNextException(), is(expected));
        assertTrue(getCachedStatements().isEmpty());
    }
    
    private PreparedStatement mockPreparedStatement(final Connection connection) throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        when(connection.prepareStatement(SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenReturn(result);
        return result;
    }
    
    private static Stream<Arguments> generatedKeysOptions() throws SQLException {
        PreparedStatement generatedKeysStatement = mock(PreparedStatement.class);
        PreparedStatement emptyColumnsStatement = mock(PreparedStatement.class);
        PreparedStatement columnsStatement = mock(PreparedStatement.class);
        String[] columns = {"id"};
        return Stream.of(
                Arguments.of("generated keys", mockGeneratedKeysConnection(generatedKeysStatement), new StatementOption(true), generatedKeysStatement),
                Arguments.of("generated keys with empty columns", mockGeneratedKeysConnection(emptyColumnsStatement), new StatementOption(true, new String[0]), emptyColumnsStatement),
                Arguments.of("generated keys with columns", mockGeneratedKeysConnection(columnsStatement, columns), new StatementOption(true, columns), columnsStatement));
    }
    
    private static Connection mockGeneratedKeysConnection(final PreparedStatement statement) throws SQLException {
        Connection result = mock(Connection.class);
        when(result.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)).thenReturn(statement);
        return result;
    }
    
    private static Connection mockGeneratedKeysConnection(final PreparedStatement statement, final String[] columns) throws SQLException {
        Connection result = mock(Connection.class);
        when(result.prepareStatement(SQL, columns)).thenReturn(statement);
        return result;
    }
    
    private ExecutionUnit createExecutionUnit() {
        return new ExecutionUnit("foo_ds", new SQLUnit(SQL, Collections.emptyList()));
    }
    
    @SuppressWarnings("unchecked")
    private Map<?, Statement> getCachedStatements() throws ReflectiveOperationException {
        return (Map<?, Statement>) Plugins.getMemberAccessor().get(StatementManager.class.getDeclaredField("cachedStatements"), statementManager);
    }
}
