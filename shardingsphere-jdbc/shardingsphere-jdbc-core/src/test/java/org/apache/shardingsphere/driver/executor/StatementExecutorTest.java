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

package org.apache.shardingsphere.driver.executor;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.SQLExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StatementExecutorTest extends AbstractBaseExecutorTest {
    
    private static final String DQL_SQL = "SELECT * FROM table_x";
    
    private static final String DML_SQL = "DELETE FROM table_x";
    
    private StatementExecutor actual;
    
    @Override
    public void setUp() throws SQLException {
        super.setUp();
        ShardingSphereConnection connection = getConnection();
        actual = spy(new StatementExecutor(connection.getDataSourceMap(), connection.getMetaDataContexts(), new SQLExecutor(getExecutorEngine(), false)));
    }
    
    @Test
    public void assertNoStatement() throws SQLException {
        assertFalse(actual.execute(Collections.emptyList(), mock(SQLStatement.class), null));
        assertThat(actual.executeUpdate(Collections.emptyList(), createSQLStatementContext(), null), is(0));
        assertThat(actual.executeQuery(Collections.emptyList()).size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementSuccess() throws SQLException {
        Statement statement = getStatement();
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnName(1)).thenReturn("column");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("column");
        when(resultSetMetaData.getTableName(1)).thenReturn("table_x");
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(statement.executeQuery(DQL_SQL)).thenReturn(resultSet);
        assertThat(actual.executeQuery(createExecutionGroups(Collections.singletonList(statement), true)).iterator().next().getValue(1, String.class), is("value"));
        verify(statement).executeQuery(DQL_SQL);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        ResultSet resultSet1 = mock(ResultSet.class);
        ResultSet resultSet2 = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnName(1)).thenReturn("column");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("column");
        when(resultSetMetaData.getTableName(1)).thenReturn("table_x");
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet1.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet2.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet1.getInt(1)).thenReturn(1);
        when(resultSet2.getInt(1)).thenReturn(2);
        when(statement1.executeQuery(DQL_SQL)).thenReturn(resultSet1);
        when(statement2.executeQuery(DQL_SQL)).thenReturn(resultSet2);
        List<QueryResult> result = actual.executeQuery(createExecutionGroups(Arrays.asList(statement1, statement2), true));
        assertThat(String.valueOf(result.get(0).getValue(1, int.class)), is("1"));
        assertThat(String.valueOf(result.get(1).getValue(1, int.class)), is("2"));
        verify(statement1).executeQuery(DQL_SQL);
        verify(statement2).executeQuery(DQL_SQL);
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementFailure() throws SQLException {
        Statement statement = getStatement();
        SQLException ex = new SQLException("");
        when(statement.executeQuery(DQL_SQL)).thenThrow(ex);
        assertThat(actual.executeQuery(createExecutionGroups(Collections.singletonList(statement), true)), is(Collections.singletonList((QueryResult) null)));
        verify(statement).executeQuery(DQL_SQL);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        SQLException ex = new SQLException("");
        when(statement1.executeQuery(DQL_SQL)).thenThrow(ex);
        when(statement2.executeQuery(DQL_SQL)).thenThrow(ex);
        List<QueryResult> actualResultSets = actual.executeQuery(createExecutionGroups(Arrays.asList(statement1, statement2), true));
        assertThat(actualResultSets, is(Arrays.asList((QueryResult) null, null)));
        verify(statement1).executeQuery(DQL_SQL);
        verify(statement2).executeQuery(DQL_SQL);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementSuccess() throws SQLException {
        Statement statement = getStatement();
        when(statement.executeUpdate(DML_SQL)).thenReturn(10);
        assertThat(actual.executeUpdate(createExecutionGroups(Collections.singletonList(statement), false), createSQLStatementContext(), null), is(10));
        verify(statement).executeUpdate(DML_SQL);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        when(statement1.executeUpdate(DML_SQL)).thenReturn(10);
        when(statement2.executeUpdate(DML_SQL)).thenReturn(20);
        assertThat(actual.executeUpdate(createExecutionGroups(Arrays.asList(statement1, statement2), false), createSQLStatementContext(), null), is(30));
        verify(statement1).executeUpdate(DML_SQL);
        verify(statement2).executeUpdate(DML_SQL);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementFailure() throws SQLException {
        Statement statement = getStatement();
        SQLException ex = new SQLException("");
        when(statement.executeUpdate(DML_SQL)).thenThrow(ex);
        assertThat(actual.executeUpdate(createExecutionGroups(Collections.singletonList(statement), false), createSQLStatementContext(), null), is(0));
        verify(statement).executeUpdate(DML_SQL);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        SQLException ex = new SQLException("");
        when(statement1.executeUpdate(DML_SQL)).thenThrow(ex);
        when(statement2.executeUpdate(DML_SQL)).thenThrow(ex);
        assertThat(actual.executeUpdate(createExecutionGroups(Arrays.asList(statement1, statement2), false), createSQLStatementContext(), null), is(0));
        verify(statement1).executeUpdate(DML_SQL);
        verify(statement2).executeUpdate(DML_SQL);
    }
    
    @Test
    public void assertExecuteUpdateWithAutoGeneratedKeys() throws SQLException {
        Statement statement = getStatement();
        when(statement.executeUpdate(DML_SQL, Statement.NO_GENERATED_KEYS)).thenReturn(10);
        assertThat(actual.executeUpdate(createExecutionGroups(Collections.singletonList(statement), false), createSQLStatementContext(), null, Statement.NO_GENERATED_KEYS), is(10));
        verify(statement).executeUpdate(DML_SQL, Statement.NO_GENERATED_KEYS);
    }
    
    @Test
    public void assertExecuteUpdateWithColumnIndexes() throws SQLException {
        Statement statement = getStatement();
        when(statement.executeUpdate(DML_SQL, new int[] {1})).thenReturn(10);
        assertThat(actual.executeUpdate(createExecutionGroups(Collections.singletonList(statement), false), createSQLStatementContext(), null, new int[] {1}), is(10));
        verify(statement).executeUpdate(DML_SQL, new int[] {1});
    }
    
    private Statement getStatement() throws SQLException {
        Statement result = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("jdbc:h2:mem:primary_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    @Test
    public void assertExecuteUpdateWithColumnNames() throws SQLException {
        Statement statement = getStatement();
        when(statement.executeUpdate(DML_SQL, new String[] {"col"})).thenReturn(10);
        assertThat(actual.executeUpdate(createExecutionGroups(Collections.singletonList(statement), false), createSQLStatementContext(), null, new String[] {"col"}), is(10));
        verify(statement).executeUpdate(DML_SQL, new String[] {"col"});
    }
    
    @Test
    public void assertExecuteForSingleStatementSuccessWithDML() throws SQLException {
        Statement statement = getStatement();
        when(statement.execute(DML_SQL)).thenReturn(false);
        assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null));
        verify(statement).execute(DML_SQL);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsSuccessWithDML() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        when(statement1.execute(DML_SQL)).thenReturn(false);
        when(statement2.execute(DML_SQL)).thenReturn(false);
        assertFalse(actual.execute(createExecutionGroups(Arrays.asList(statement1, statement2), false), mock(SQLStatement.class), null));
        verify(statement1).execute(DML_SQL);
        verify(statement2).execute(DML_SQL);
    }
    
    @Test
    public void assertExecuteForSingleStatementFailureWithDML() throws SQLException {
        Statement statement = getStatement();
        SQLException ex = new SQLException("");
        when(statement.execute(DML_SQL)).thenThrow(ex);
        assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null));
        verify(statement).execute(DML_SQL);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsFailureWithDML() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        SQLException ex = new SQLException("");
        when(statement1.execute(DML_SQL)).thenThrow(ex);
        when(statement2.execute(DML_SQL)).thenThrow(ex);
        assertFalse(actual.execute(createExecutionGroups(Arrays.asList(statement1, statement2), false), mock(SQLStatement.class), null));
        verify(statement1).execute(DML_SQL);
        verify(statement2).execute(DML_SQL);
    }
    
    @Test
    public void assertExecuteForSingleStatementWithDQL() throws SQLException {
        Statement statement = getStatement();
        when(statement.execute(DQL_SQL)).thenReturn(true);
        assertTrue(actual.execute(createExecutionGroups(Collections.singletonList(statement), true), mock(SQLStatement.class), null));
        verify(statement).execute(DQL_SQL);
    }
    
    @Test
    public void assertExecuteForMultipleStatements() throws SQLException {
        Statement statement1 = getStatement();
        Statement statement2 = getStatement();
        when(statement1.execute(DQL_SQL)).thenReturn(true);
        when(statement2.execute(DQL_SQL)).thenReturn(true);
        assertTrue(actual.execute(createExecutionGroups(Arrays.asList(statement1, statement2), true), mock(SQLStatement.class), null));
        verify(statement1).execute(DQL_SQL);
        verify(statement2).execute(DQL_SQL);
    }
    
    @Test
    public void assertExecuteWithAutoGeneratedKeys() throws SQLException {
        Statement statement = getStatement();
        when(statement.execute(DML_SQL, Statement.NO_GENERATED_KEYS)).thenReturn(false);
        assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null, Statement.NO_GENERATED_KEYS));
        verify(statement).execute(DML_SQL, Statement.NO_GENERATED_KEYS);
    }

    @Test
    public void assertExecuteWithColumnIndexes() throws SQLException {
        Statement statement = getStatement();
        when(statement.execute(DML_SQL, new int[] {1})).thenReturn(false);
        assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null, new int[] {1}));
        verify(statement).execute(DML_SQL, new int[] {1});
    }
    
    @Test
    public void assertExecuteWithColumnNames() throws SQLException {
        Statement statement = getStatement();
        when(statement.execute(DML_SQL, new String[] {"col"})).thenReturn(false);
        assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null, new String[] {"col"}));
        verify(statement).execute(DML_SQL, new String[] {"col"});
    }
    
    @Test
    public void assertOverallExceptionFailure() throws SQLException {
        ExecutorExceptionHandler.setExceptionThrown(true);
        Statement statement = getStatement();
        SQLException ex = new SQLException("");
        when(statement.execute(DML_SQL)).thenThrow(ex);
        try {
            assertFalse(actual.execute(createExecutionGroups(Collections.singletonList(statement), false), mock(SQLStatement.class), null));
        } catch (final SQLException ignored) {
        }
    }
    
    private Collection<ExecutionGroup<JDBCExecutionUnit>> createExecutionGroups(final List<Statement> statements, final boolean isQuery) {
        Collection<ExecutionGroup<JDBCExecutionUnit>> result = new LinkedList<>();
        List<JDBCExecutionUnit> executionUnits = new LinkedList<>();
        result.add(new ExecutionGroup<>(executionUnits));
        for (Statement each : statements) {
            executionUnits.add(
                    new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(isQuery ? DQL_SQL : DML_SQL, Collections.singletonList(1))), ConnectionMode.MEMORY_STRICTLY, each));
        }
        return result;
    }
}
