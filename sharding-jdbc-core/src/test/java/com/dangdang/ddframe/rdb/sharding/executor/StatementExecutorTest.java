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

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.EventCaller;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.ExecutorTestUtil;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.TestDMLExecutionEventListener;
import com.dangdang.ddframe.rdb.sharding.executor.fixture.TestDQLExecutionEventListener;
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.StatementExecutorWrapper;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.router.SQLExecutionUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
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

public final class StatementExecutorTest {

    private static final String SELECT_FROM_DUAL = "SELECT * FROM dual";
    
    private static final String DELETE_FROM_DUAL = "DELETE FROM dual";

    private ExecutorEngine executorEngine;
    
    @Mock
    private EventCaller eventCaller;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ExecutorExceptionHandler.setExceptionThrown(false);
        executorEngine = new ExecutorEngine(ExecutorTestUtil.createShardingProperties());
        DMLExecutionEventBus.register(new TestDMLExecutionEventListener(eventCaller));
        DQLExecutionEventBus.register(new TestDQLExecutionEventListener(eventCaller));
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        ExecutorTestUtil.clear();
        DMLExecutionEventBus.clearListener();
        DQLExecutionEventBus.clearListener();
    }
    
    @Test
    public void assertNoStatement() throws SQLException {
        StatementExecutor actual = new StatementExecutor(executorEngine);
        assertThat(actual.execute(), is(false));
        assertThat(actual.executeUpdate(), is(0));
        assertThat(actual.executeQuery().size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementSuccess() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeQuery(), is(Collections.singletonList(resultSet)));
        verify(statement).executeQuery(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_1");
        ResultSet resultSet1 = mock(ResultSet.class);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(statement1.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet1);
        when(statement2.executeQuery(SELECT_FROM_DUAL)).thenReturn(resultSet2);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, hasItem(resultSet1));
        assertThat(actualResultSets, hasItem(resultSet2));
        verify(statement1).executeQuery(SELECT_FROM_DUAL);
        verify(statement2).executeQuery(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForSingleStatementFailure() throws SQLException {
        Statement statement = mock(Statement.class);
        SQLException exp = new SQLException();
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        when(statement.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeQuery(), is(Collections.singletonList((ResultSet) null)));
        verify(statement).executeQuery(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteQueryForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_1");
        SQLException exp = new SQLException();
        when(statement1.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        when(statement2.executeQuery(SELECT_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, is(Arrays.asList((ResultSet) null, null)));
        verify(statement1).executeQuery(SELECT_FROM_DUAL);
        verify(statement2).executeQuery(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementSuccess() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL)).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsSuccess() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        when(statement1.executeUpdate(DELETE_FROM_DUAL)).thenReturn(10);
        when(statement2.executeUpdate(DELETE_FROM_DUAL)).thenReturn(20);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        assertThat(actual.executeUpdate(), is(30));
        verify(statement1).executeUpdate(DELETE_FROM_DUAL);
        verify(statement2).executeUpdate(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForSingleStatementFailure() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        SQLException exp = new SQLException();
        when(statement.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(), is(0));
        verify(statement).executeUpdate(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForMultipleStatementsFailure() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        SQLException exp = new SQLException();
        when(statement1.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement2.executeUpdate(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        assertThat(actual.executeUpdate(), is(0));
        verify(statement1).executeUpdate(DELETE_FROM_DUAL);
        verify(statement2).executeUpdate(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateWithAutoGeneratedKeys() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS)).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(Statement.NO_GENERATED_KEYS), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateWithColumnIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, new int[] {1})).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(new int[] {1}), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, new int[] {1});
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateWithColumnNames() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.executeUpdate(DELETE_FROM_DUAL, new String[] {"col"})).thenReturn(10);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertThat(actual.executeUpdate(new String[] {"col"}), is(10));
        verify(statement).executeUpdate(DELETE_FROM_DUAL, new String[] {"col"});
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSingleStatementSuccessWithDML() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL)).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertFalse(actual.execute());
        verify(statement).execute(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsSuccessWithDML() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        when(statement1.execute(DELETE_FROM_DUAL)).thenReturn(false);
        when(statement2.execute(DELETE_FROM_DUAL)).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        assertFalse(actual.execute());
        verify(statement1).execute(DELETE_FROM_DUAL);
        verify(statement2).execute(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSingleStatementFailureWithDML() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        SQLException exp = new SQLException();
        when(statement.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertFalse(actual.execute());
        verify(statement).execute(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForMultipleStatementsFailureWithDML() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDML(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDML(statement2, "ds_1");
        SQLException exp = new SQLException();
        when(statement1.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        when(statement2.execute(DELETE_FROM_DUAL)).thenThrow(exp);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        assertFalse(actual.execute());
        verify(statement1).execute(DELETE_FROM_DUAL);
        verify(statement2).execute(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSingleStatementWithDQL() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDQL(statement, "ds_0");
        when(statement.execute(SELECT_FROM_DUAL)).thenReturn(true);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertTrue(actual.execute());
        verify(statement).execute(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultipleStatements() throws SQLException {
        Statement statement1 = mock(Statement.class);
        StatementExecutorWrapper wrapper1 = createStatementExecutorWrapperForDQL(statement1, "ds_0");
        Statement statement2 = mock(Statement.class);
        StatementExecutorWrapper wrapper2 = createStatementExecutorWrapperForDQL(statement2, "ds_0");
        when(statement1.execute(SELECT_FROM_DUAL)).thenReturn(true);
        when(statement2.execute(SELECT_FROM_DUAL)).thenReturn(true);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper1);
        actual.addStatement(wrapper2);
        assertTrue(actual.execute());
        verify(statement1).execute(SELECT_FROM_DUAL);
        verify(statement2).execute(SELECT_FROM_DUAL);
        verify(eventCaller, times(4)).verifyDataSource("ds_0");
        verify(eventCaller, times(4)).verifySQL(SELECT_FROM_DUAL);
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithAutoGeneratedKeys() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS)).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertFalse(actual.execute(Statement.NO_GENERATED_KEYS));
        verify(statement).execute(DELETE_FROM_DUAL, Statement.NO_GENERATED_KEYS);
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithColumnIndexes() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, new int[] {1})).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertFalse(actual.execute(new int[] {1}));
        verify(statement).execute(DELETE_FROM_DUAL, new int[] {1});
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteWithColumnNames() throws SQLException {
        Statement statement = mock(Statement.class);
        StatementExecutorWrapper wrapper = createStatementExecutorWrapperForDML(statement, "ds_0");
        when(statement.execute(DELETE_FROM_DUAL, new String[] {"col"})).thenReturn(false);
        StatementExecutor actual = new StatementExecutor(executorEngine);
        actual.addStatement(wrapper);
        assertFalse(actual.execute(new String[] {"col"}));
        verify(statement).execute(DELETE_FROM_DUAL, new String[] {"col"});
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL(DELETE_FROM_DUAL);
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    private StatementExecutorWrapper createStatementExecutorWrapperForDQL(final Statement statement, final String dataSource) {
        try {
            return new StatementExecutorWrapper(statement, new SQLExecutionUnit(dataSource, (SQLBuilder) new SQLBuilder().append(SELECT_FROM_DUAL)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private StatementExecutorWrapper createStatementExecutorWrapperForDML(final Statement statement, final String dataSource) {
        try {
            return new StatementExecutorWrapper(statement, new SQLExecutionUnit(dataSource, (SQLBuilder) new SQLBuilder().append(DELETE_FROM_DUAL)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
