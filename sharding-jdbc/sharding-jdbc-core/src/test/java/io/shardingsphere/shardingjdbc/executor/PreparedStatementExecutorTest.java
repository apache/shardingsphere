/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.executor;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.event.ShardingEventType;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class PreparedStatementExecutorTest extends AbstractBaseExecutorTest {
    
    private static final String DQL_SQL = "SELECT * FROM table_x";
    
    private static final String DML_SQL = "DELETE FROM table_x";
    
    private PreparedStatementExecutor actual;
    
    @Override
    public void setUp() throws SQLException {
        super.setUp();
        actual = new PreparedStatementExecutor(1, 1, 1, false, getConnection());
    }
    
    @Test
    public void assertNoStatement() throws SQLException {
        assertFalse(actual.execute());
        assertThat(actual.executeUpdate(), is(0));
        assertThat(actual.executeQuery().size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(1)).thenReturn(1);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DQL);
        assertThat((int) actual.executeQuery().iterator().next().getValue(1, int.class), is(resultSet.getInt(1)));
        verify(preparedStatement).executeQuery();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("jdbc:h2:mem:ds_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        return statement;
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        ResultSet resultSet1 = mock(ResultSet.class);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(resultSet1.getInt(1)).thenReturn(1);
        when(resultSet2.getInt(1)).thenReturn(2);
        when(preparedStatement1.executeQuery()).thenReturn(resultSet1);
        when(preparedStatement2.executeQuery()).thenReturn(resultSet2);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DQL);
        List<QueryResult> result = actual.executeQuery();
        List<ResultSet> resultSets = Arrays.asList(resultSet1, resultSet2);
        for (int i = 0; i < result.size(); i++) {
            assertThat((int) result.get(i).getValue(1, int.class), is(resultSets.get(i).getInt(1)));
        }
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement.executeQuery()).thenThrow(exp);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DQL);
        assertThat(actual.executeQuery(), is(Collections.singletonList((QueryResult) null)));
        verify(preparedStatement).executeQuery();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement1.executeQuery()).thenThrow(exp);
        when(preparedStatement2.executeQuery()).thenThrow(exp);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DQL);
        List<QueryResult> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, is(Arrays.asList((QueryResult) null, null)));
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeUpdate()).thenReturn(10);
        setSQLType(SQLType.DML);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DML);
        assertThat(actual.executeUpdate(), is(10));
        verify(preparedStatement).executeUpdate();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.executeUpdate()).thenReturn(10);
        when(preparedStatement2.executeUpdate()).thenReturn(20);
        setSQLType(SQLType.DML);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DML);
        assertThat(actual.executeUpdate(), is(30));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement.executeUpdate()).thenThrow(exp);
        setSQLType(SQLType.DML);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DML);
        assertThat(actual.executeUpdate(), is(0));
        verify(preparedStatement).executeUpdate();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement1.executeUpdate()).thenThrow(exp);
        when(preparedStatement2.executeUpdate()).thenThrow(exp);
        setSQLType(SQLType.DML);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DML);
        assertThat(actual.executeUpdate(), is(0));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.execute()).thenReturn(false);
        setSQLType(SQLType.DML);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DML);
        assertFalse(actual.execute());
        verify(preparedStatement).execute();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.execute()).thenReturn(false);
        when(preparedStatement2.execute()).thenReturn(false);
        setSQLType(SQLType.DML);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DML);
        assertFalse(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement.execute()).thenThrow(exp);
        setSQLType(SQLType.DML);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DML);
        assertFalse(actual.execute());
        verify(preparedStatement).execute();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException exp = new SQLException();
        when(preparedStatement1.execute()).thenThrow(exp);
        when(preparedStatement2.execute()).thenThrow(exp);
        setSQLType(SQLType.DML);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DML);
        assertFalse(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DML_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementWithDQL() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.execute()).thenReturn(true);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Collections.singletonList(preparedStatement), SQLType.DQL);
        assertTrue(actual.execute());
        verify(preparedStatement).execute();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatements() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.execute()).thenReturn(true);
        when(preparedStatement2.execute()).thenReturn(true);
        setSQLType(SQLType.DQL);
        setExecuteGroups(Arrays.asList(preparedStatement1, preparedStatement2), SQLType.DQL);
        assertTrue(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(DQL_SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.singletonList((Object) 1));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @SneakyThrows
    private void setSQLType(final SQLType sqlType) {
        Field field = PreparedStatementExecutor.class.getSuperclass().getDeclaredField("sqlType");
        field.setAccessible(true);
        field.set(actual, sqlType);
    }
    
    @SneakyThrows
    private void setExecuteGroups(final List<PreparedStatement> preparedStatements, final SQLType sqlType) {
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> executeGroups = new LinkedList<>();
        List<StatementExecuteUnit> preparedStatementExecuteUnits = new LinkedList<>();
        executeGroups.add(new ShardingExecuteGroup<>(preparedStatementExecuteUnits));
        for (PreparedStatement each : preparedStatements) {
            List<List<Object>> parameterSets = new LinkedList<>();
            String sql = SQLType.DQL.equals(sqlType) ? DQL_SQL : DML_SQL;
            parameterSets.add(Collections.singletonList((Object) 1));
            preparedStatementExecuteUnits.add(new StatementExecuteUnit(new RouteUnit("ds_0", new SQLUnit(sql, parameterSets)), each, ConnectionMode.MEMORY_STRICTLY));
        }
        Field field = PreparedStatementExecutor.class.getSuperclass().getDeclaredField("executeGroups");
        field.setAccessible(true);
        field.set(actual, executeGroups);
    }
}
