/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.executor;

import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.StatementExecutorWrapper;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StatementExecutorTest extends AbstractBaseExecutorTest {

    private static final String SELECT_FROM_DUAL = "SELECT * FROM dual";
    
    private static final String DELETE_FROM_DUAL = "DELETE FROM dual";
    
    @Test
    public void assertNoStatement() throws SQLException {
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        assertFalse(actual.execute());
        assertThat(actual.executeUpdate(), is(0));
        assertThat(actual.executeQuery().size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementSuccess() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeQuery(), is(Collections.singletonList(resultSet)));
        verify(statement).executeQuery(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        ResultSet resultSet1 = mock(ResultSet.class);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(statement1.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet1);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet2);
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_1");
        actual.addStatement(wrapper2);
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, hasItem(resultSet1));
        assertThat(actualResultSets, hasItem(resultSet2));
        verify(statement1).executeQuery(SELECT_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).executeQuery(SELECT_FROM_DUAL);
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementFailure() throws SQLException {
        Statement statement = mock(Statement.class);
        SQLException exp = new SQLException();
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        when(statement.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeQuery(), is(Collections.singletonList((ResultSet) null)));
        verify(statement).executeQuery(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        SQLException exp = new SQLException();
        when(statement1.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        when(statement2.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_1");
        actual.addStatement(wrapper2);
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, is(Arrays.asList((ResultSet) null, null)));
        verify(statement1).executeQuery(SELECT_FROM_DUAL);
        verify(statement2).executeQuery(SELECT_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementSuccess() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL)).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        when(statement1.executeUpdate(DELETE_FROM_DUAL)).thenReturn(10);
        when(statement2.executeUpdate(DELETE_FROM_DUAL)).thenReturn(20);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        actual.addStatement(wrapper2);
        assertThat(actual.executeUpdate(), is(30));
        verify(statement1).executeUpdate(DELETE_FROM_DUAL);
        verify(statement2).executeUpdate(DELETE_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementFailure() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        SQLException exp = new SQLException();
        when(statement.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(), is(0));
        verify(statement).executeUpdate(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        SQLException exp = new SQLException();
        when(statement1.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement2.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        actual.addStatement(wrapper2);
        assertThat(actual.executeUpdate(), is(0));
        verify(statement1).executeUpdate(DELETE_FROM_DUAL);
        verify(statement2).executeUpdate(DELETE_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateWithAutoGeneratedKeys() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS)).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(Statement.NO_GENERATED_KEYS), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateWithColumnIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, new int[] {1})).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(new int[] {1}), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, new int[] {1});
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateWithColumnNames() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, new String[] {"col"})).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(new String[] {"col"}), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, new String[] {"col"});
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSingleStatementSuccessWithDML() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL)).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertFalse(actual.execute());
        verify(statement).execute(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsSuccessWithDML() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        when(statement1.execute(DELETE_FROM_DUAL)).thenReturn(false);
        when(statement2.execute(DELETE_FROM_DUAL)).thenReturn(false);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        actual.addStatement(wrapper2);
        assertFalse(actual.execute());
        verify(statement1).execute(DELETE_FROM_DUAL);
        verify(statement2).execute(DELETE_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSingleStatementFailureWithDML() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        SQLException exp = new SQLException();
        when(statement.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertFalse(actual.execute());
        verify(statement).execute(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller()).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsFailureWithDML() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        SQLException exp = new SQLException();
        when(statement1.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement2.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        actual.addStatement(wrapper2);
        assertFalse(actual.execute());
        verify(statement1).execute(DELETE_FROM_DUAL);
        verify(statement2).execute(DELETE_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSingleStatementWithDQL() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        when(statement.execute(SELECT_FROM_DUAL)).thenReturn(true);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertTrue(actual.execute());
        verify(statement).execute(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultipleStatements() throws SQLException {
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        when(statement1.execute(SELECT_FROM_DUAL)).thenReturn(true);
        when(statement2.execute(SELECT_FROM_DUAL)).thenReturn(true);
        when(statement1.getConnection()).thenReturn(mock(Connection.class));
        when(statement2.getConnection()).thenReturn(mock(Connection.class));
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        actual.addStatement(wrapper1);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_0");
        actual.addStatement(wrapper2);
        assertTrue(actual.execute());
        verify(statement1).execute(SELECT_FROM_DUAL);
        verify(statement2).execute(SELECT_FROM_DUAL);
        verify(statement1).getConnection();
        verify(statement2).getConnection();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.emptyList());
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithAutoGeneratedKeys() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS)).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertFalse(actual.execute(Statement.NO_GENERATED_KEYS));
        verify(statement).execute(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS);
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithColumnIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, new int[] {1})).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertFalse(actual.execute(new int[] {1}));
        verify(statement).execute(DELETE_FROM_DUAL, new int[] {1});
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithColumnNames() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, new String[] {"col"})).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(getExecutorEngine());
        actual.addStatement(wrapper);
        assertFalse(actual.execute(new String[] {"col"}));
        verify(statement).execute(DELETE_FROM_DUAL, new String[] {"col"});
        verify(getEventCaller(), times(2)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.emptyList());
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(getEventCaller()).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    private StatementExecutorWrapper createStatementExecutorWrapperForDQL(final Statement statement, final String dataSource) {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals(SELECT_FROM_DUAL);
        return new StatementExecutorWrapper(statement, new SQLExecutionUnit(dataSource, sqlBuilder.toSQL(Collections.<String, String>emptyMap())));
    }
    
    private StatementExecutorWrapper createStatementExecutorWrapperForDML(final Statement statement, final String dataSource) {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals(DELETE_FROM_DUAL);
        return new StatementExecutorWrapper(statement, new SQLExecutionUnit(dataSource, sqlBuilder.toSQL(Collections.<String, String>emptyMap())));
    }
}
