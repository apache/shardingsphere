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

package io.shardingsphere.transaction.saga.servicecomb.transport;

import com.google.common.collect.Lists;
import io.shardingsphere.transaction.saga.constant.ExecutionResult;
import io.shardingsphere.transaction.saga.SagaSubTransaction;
import io.shardingsphere.transaction.saga.SagaTransaction;
import org.apache.servicecomb.saga.core.TransportFailedException;
import org.apache.servicecomb.saga.format.JsonSuccessfulSagaResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShardingSQLTransportTest {
    
    @Mock
    private PreparedStatement statement;
    
    @Mock
    private SagaTransaction sagaTransaction;
    
    private String dataSourceName = "ds";
    
    private String sql = "SELECT * FROM ds.table WHERE id = ? AND column = ?";
    
    @Before
    public void setUp() throws SQLException {
        getDatasourceMap();
    }
    
    @Test
    public void assertWithSuccessResult() {
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        List<List<String>> params = getParams();
        recordMockResult(params, ExecutionResult.SUCCESS);
        shardingSQLTransport.with(dataSourceName, sql, params);
        verify(sagaTransaction, never()).getDataSourceMap();
    }
    
    @Test
    public void assertWithFailureResult() throws SQLException {
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        List<List<String>> params = getParams();
        recordMockResult(params, ExecutionResult.FAILURE);
        shardingSQLTransport.with(dataSourceName, sql, params);
        verify(sagaTransaction).getDataSourceMap();
        verify(statement, times(2)).addBatch();
        verify(statement).executeBatch();
    }
    
    @Test
    public void assertWithNoResultForMultiParamsSuccess() throws SQLException {
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        List<List<String>> params = getParams();
        assertThat(shardingSQLTransport.with(dataSourceName, sql, params), instanceOf(JsonSuccessfulSagaResponse.class));
        verify(statement, times(2)).addBatch();
        verify(statement).executeBatch();
    }
    
    @Test(expected = TransportFailedException.class)
    public void assertWithNoResultForMultiParamsFailure() throws SQLException {
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        when(statement.executeBatch()).thenThrow(new SQLException("test execute failed"));
        List<List<String>> params = getParams();
        assertThat(shardingSQLTransport.with(dataSourceName, sql, params), instanceOf(JsonSuccessfulSagaResponse.class));
        verify(statement, times(2)).addBatch();
    }
    
    @Test
    public void assertWithNoResultForEmptyParams() throws SQLException {
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        List<List<String>> params = Lists.newArrayList();
        assertThat(shardingSQLTransport.with(dataSourceName, sql, params), instanceOf(JsonSuccessfulSagaResponse.class));
        verify(statement).executeUpdate();
    }
    
    @Test(expected = TransportFailedException.class)
    public void assertGetConnectionFailure() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        DataSource dataSource = mock(DataSource.class);
        dataSourceMap.put(dataSourceName, dataSource);
        when(sagaTransaction.getDataSourceMap()).thenReturn(dataSourceMap);
        when(dataSource.getConnection()).thenThrow(new SQLException("test get connection fail"));
        ShardingSQLTransport shardingSQLTransport = new ShardingSQLTransport(sagaTransaction);
        List<List<String>> params = Lists.newArrayList();
        shardingSQLTransport.with(dataSourceName, sql, params);
    }
    
    private void getDatasourceMap() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        dataSourceMap.put(dataSourceName, dataSource);
        when(sagaTransaction.getDataSourceMap()).thenReturn(dataSourceMap);
    }
    
    private void recordMockResult(final List<List<String>> params, final ExecutionResult executionResult) {
        Map<SagaSubTransaction, ExecutionResult> resultMap = new ConcurrentHashMap<>();
        resultMap.put(new SagaSubTransaction(dataSourceName, sql, copyList(params)), executionResult);
        when(sagaTransaction.getExecutionResultMap()).thenReturn(resultMap);
    }
    
    private List<List<Object>> copyList(final List<List<String>> origin) {
        List<List<Object>> result = Lists.newArrayList();
        for (List<String> each : origin) {
            result.add(Lists.<Object>newArrayList(each));
        }
        return result;
    }
    
    private List<List<String>> getParams() {
        List<List<String>> result = Lists.newArrayList();
        List<String> param = Lists.newArrayList();
        param.add("1");
        param.add("x");
        result.add(param);
        param = Lists.newArrayList();
        param.add("2");
        param.add("y");
        result.add(param);
        return result;
    }
}