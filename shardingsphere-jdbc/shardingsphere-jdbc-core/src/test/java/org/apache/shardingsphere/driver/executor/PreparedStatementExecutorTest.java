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
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultSet;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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

public final class PreparedStatementExecutorTest extends AbstractBaseExecutorTest {
    
    private static final String DQL_SQL = "SELECT * FROM table_x";
    
    private static final String DML_SQL = "DELETE FROM table_x";
    
    private PreparedStatementExecutor actual;
    
    @Override
    public void setUp() throws SQLException {
        super.setUp();
        ShardingSphereConnection connection = getConnection();
        actual = spy(new PreparedStatementExecutor(connection.getDataSourceMap(), connection.getMetaDataContexts(), new JDBCExecutor(getExecutorEngine(), false)));
    }
    
    @Test
    public void assertNoStatement() throws SQLException {
        assertFalse(actual.execute(Collections.emptyList(), mock(SQLStatement.class), null));
        assertThat(actual.executeUpdate(Collections.emptyList(), createSQLStatementContext(), null), is(0));
        assertThat(actual.executeQuery(Collections.emptyList()).size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnName(1)).thenReturn("column");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("column");
        when(resultSetMetaData.getTableName(1)).thenReturn("table_x");
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        assertThat(actual.executeQuery(getExecutionGroups(Collections.singletonList(preparedStatement), true)).iterator().next().getValue(1, String.class), is("value"));
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
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
        when(preparedStatement1.executeQuery()).thenReturn(resultSet1);
        when(preparedStatement2.executeQuery()).thenReturn(resultSet2);
        List<QueryResultSet> result = actual.executeQuery(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), true));
        assertThat(String.valueOf(result.get(0).getValue(1, int.class)), is("1"));
        assertThat(String.valueOf(result.get(1).getValue(1, int.class)), is("2"));
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement.executeQuery()).thenThrow(ex);
        assertThat(actual.executeQuery(getExecutionGroups(Collections.singletonList(preparedStatement), true)), is(Collections.singletonList((QueryResultSet) null)));
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement1.executeQuery()).thenThrow(ex);
        when(preparedStatement2.executeQuery()).thenThrow(ex);
        List<QueryResultSet> actualQueryResultSets = actual.executeQuery(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), true));
        assertThat(actualQueryResultSets, is(Arrays.asList((QueryResultSet) null, null)));
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeUpdate()).thenReturn(10);
        assertThat(actual.executeUpdate(getExecutionGroups(Collections.singletonList(preparedStatement), false), createSQLStatementContext(), null), is(10));
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.executeUpdate()).thenReturn(10);
        when(preparedStatement2.executeUpdate()).thenReturn(20);
        assertThat(actual.executeUpdate(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), false), createSQLStatementContext(), null), is(30));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement.executeUpdate()).thenThrow(ex);
        assertThat(actual.executeUpdate(getExecutionGroups(Collections.singletonList(preparedStatement), false), createSQLStatementContext(), null), is(0));
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException ex
                = new SQLException("");
        when(preparedStatement1.executeUpdate()).thenThrow(ex);
        when(preparedStatement2.executeUpdate()).thenThrow(ex);
        assertThat(actual.executeUpdate(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), false), createSQLStatementContext(), null), is(0));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.execute()).thenReturn(false);
        assertFalse(actual.execute(getExecutionGroups(Collections.singletonList(preparedStatement), false), mock(SQLStatement.class), null));
        verify(preparedStatement).execute();
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.execute()).thenReturn(false);
        when(preparedStatement2.execute()).thenReturn(false);
        assertFalse(actual.execute(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), false), mock(SQLStatement.class), null));
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement.execute()).thenThrow(ex);
        assertFalse(actual.execute(getExecutionGroups(Collections.singletonList(preparedStatement), false), mock(SQLStatement.class), null));
        verify(preparedStatement).execute();
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement1.execute()).thenThrow(ex);
        when(preparedStatement2.execute()).thenThrow(ex);
        assertFalse(actual.execute(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), false), mock(SQLStatement.class), null));
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementWithDQL() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.execute()).thenReturn(true);
        assertTrue(actual.execute(getExecutionGroups(Collections.singletonList(preparedStatement), true), mock(SQLStatement.class), null));
        verify(preparedStatement).execute();
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatements() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.execute()).thenReturn(true);
        when(preparedStatement2.execute()).thenReturn(true);
        assertTrue(actual.execute(getExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2), true), mock(SQLStatement.class), null));
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
    }
    
    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("jdbc:h2:mem:primary_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private Collection<ExecutionGroup<JDBCExecutionUnit>> getExecutionGroups(final List<PreparedStatement> preparedStatements, final boolean isQuery) {
        Collection<ExecutionGroup<JDBCExecutionUnit>> result = new LinkedList<>();
        List<JDBCExecutionUnit> jdbcExecutionUnits = new LinkedList<>();
        result.add(new ExecutionGroup<>(jdbcExecutionUnits));
        for (PreparedStatement each : preparedStatements) {
            jdbcExecutionUnits.add(new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(isQuery ? DQL_SQL : DML_SQL, Collections.singletonList(1))), ConnectionMode.MEMORY_STRICTLY, each));
        }
        return result;
    }
}
