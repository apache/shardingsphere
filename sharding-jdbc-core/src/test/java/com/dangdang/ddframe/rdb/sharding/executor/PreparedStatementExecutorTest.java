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
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.PreparedStatementExecutorWrapper;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.router.SQLExecutionUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public final class PreparedStatementExecutorTest {
    
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
        executorEngine.shutdown();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertNoStatement() throws SQLException {
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.EMPTY_LIST);
        assertThat(actual.execute(), is(false));
        assertThat(actual.executeUpdate(), is(0));
        assertThat(actual.executeQuery().size(), is(0));
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDQL(preparedStatement, "ds_0");
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertThat(actual.executeQuery(), is(Collections.singletonList(resultSet)));
        verify(preparedStatement).executeQuery();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDQL(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDQL(preparedStatement2, "ds_1");
        ResultSet resultSet1 = mock(ResultSet.class);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(preparedStatement1.executeQuery()).thenReturn(resultSet1);
        when(preparedStatement2.executeQuery()).thenReturn(resultSet2);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, hasItem(resultSet1));
        assertThat(actualResultSets, hasItem(resultSet2));
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteQueryForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDQL(preparedStatement, "ds_0");
        SQLException exp = new SQLException();
        when(preparedStatement.executeQuery()).thenThrow(exp);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertThat(actual.executeQuery(), is(Collections.singletonList((ResultSet) null)));
        verify(preparedStatement).executeQuery();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteQueryForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDQL(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDQL(preparedStatement2, "ds_1");
        SQLException exp = new SQLException();
        when(preparedStatement1.executeQuery()).thenThrow(exp);
        when(preparedStatement2.executeQuery()).thenThrow(exp);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        List<ResultSet> actualResultSets = actual.executeQuery();
        assertThat(actualResultSets, is(Arrays.asList((ResultSet) null, null)));
        verify(preparedStatement1).executeQuery();
        verify(preparedStatement2).executeQuery();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDML(preparedStatement, "ds_0");
        when(preparedStatement.executeUpdate()).thenReturn(10);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertThat(actual.executeUpdate(), is(10));
        verify(preparedStatement).executeUpdate();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDML(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDML(preparedStatement2, "ds_1");
        when(preparedStatement1.executeUpdate()).thenReturn(10);
        when(preparedStatement2.executeUpdate()).thenReturn(20);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        assertThat(actual.executeUpdate(), is(30));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteUpdateForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDML(preparedStatement, "ds_0");
        SQLException exp = new SQLException();
        when(preparedStatement.executeUpdate()).thenThrow(exp);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertThat(actual.executeUpdate(), is(0));
        verify(preparedStatement).executeUpdate();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteUpdateForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDML(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDML(preparedStatement2, "ds_1");
        SQLException exp = new SQLException();
        when(preparedStatement1.executeUpdate()).thenThrow(exp);
        when(preparedStatement2.executeUpdate()).thenThrow(exp);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        assertThat(actual.executeUpdate(), is(0));
        verify(preparedStatement1).executeUpdate();
        verify(preparedStatement2).executeUpdate();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDML(preparedStatement, "ds_0");
        when(preparedStatement.execute()).thenReturn(false);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertFalse(actual.execute());
        verify(preparedStatement).execute();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsSuccessWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDML(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDML(preparedStatement2, "ds_1");
        when(preparedStatement1.execute()).thenReturn(false);
        when(preparedStatement2.execute()).thenReturn(false);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        assertFalse(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDML(preparedStatement, "ds_0");
        SQLException exp = new SQLException();
        when(preparedStatement.execute()).thenThrow(exp);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertFalse(actual.execute());
        verify(preparedStatement).execute();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatementsFailureWithDML() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDML(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDML(preparedStatement2, "ds_1");
        SQLException exp = new SQLException();
        when(preparedStatement1.execute()).thenThrow(exp);
        when(preparedStatement2.execute()).thenThrow(exp);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        assertFalse(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifyDataSource("ds_1");
        verify(eventCaller, times(4)).verifySQL("DELETE FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        verify(eventCaller, times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteForSinglePreparedStatementWithDQL() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper = createPreparedStatementExecutorWrapperForDQL(preparedStatement, "ds_0");
        when(preparedStatement.execute()).thenReturn(true);
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Collections.singleton(wrapper));
        assertTrue(actual.execute());
        verify(preparedStatement).execute();
        verify(eventCaller, times(2)).verifyDataSource("ds_0");
        verify(eventCaller, times(2)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(2)).verifyParameters(Collections.emptyList());
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteForMultiplePreparedStatements() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper1 = createPreparedStatementExecutorWrapperForDQL(preparedStatement1, "ds_0");
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        PreparedStatementExecutorWrapper wrapper2 = createPreparedStatementExecutorWrapperForDQL(preparedStatement2, "ds_0");
        when(preparedStatement1.execute()).thenReturn(true);
        when(preparedStatement2.execute()).thenReturn(true);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        PreparedStatementExecutor actual = new PreparedStatementExecutor(executorEngine, Arrays.asList(wrapper1, wrapper2));
        assertTrue(actual.execute());
        verify(preparedStatement1).execute();
        verify(preparedStatement2).execute();
        verify(preparedStatement1).getConnection();
        verify(preparedStatement2).getConnection();
        verify(eventCaller, times(4)).verifyDataSource("ds_0");
        verify(eventCaller, times(4)).verifySQL("SELECT * FROM dual");
        verify(eventCaller, times(4)).verifyParameters(Collections.emptyList());
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.BEFORE_EXECUTE);
        verify(eventCaller, times(2)).verifyEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        verify(eventCaller, times(0)).verifyException(null);
    }
    
    private PreparedStatementExecutorWrapper createPreparedStatementExecutorWrapperForDQL(final PreparedStatement preparedStatement, final String dataSource) {
        return createPreparedStatementExecutorWrapper(preparedStatement, dataSource, "SELECT * FROM dual");
    }
    
    private PreparedStatementExecutorWrapper createPreparedStatementExecutorWrapperForDML(final PreparedStatement preparedStatement, final String dataSource) {
        return createPreparedStatementExecutorWrapper(preparedStatement, dataSource, "DELETE FROM dual");
    }
    
    private PreparedStatementExecutorWrapper createPreparedStatementExecutorWrapper(final PreparedStatement preparedStatement, final String dataSource, final String sql) {
        try {
            return new PreparedStatementExecutorWrapper(preparedStatement, Collections.emptyList(), new SQLExecutionUnit(dataSource, (SQLBuilder) new SQLBuilder().append(sql)));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
