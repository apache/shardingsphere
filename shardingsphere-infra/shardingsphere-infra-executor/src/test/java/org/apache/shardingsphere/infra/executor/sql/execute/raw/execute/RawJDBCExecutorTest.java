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

package org.apache.shardingsphere.infra.executor.sql.execute.raw.execute;

import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.query.ExecuteQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.update.ExecuteUpdateResult;
import org.apache.shardingsphere.infra.executor.sql.execute.driver.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.query.QueryResult;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RawJDBCExecutorTest {
    
    @Test
    public void assertExecuteForResultEmpty() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        RawJDBCExecutor executor = new RawJDBCExecutor(executorEngine, true);
        assertFalse(executor.execute(null, null));
    }
    
    @Test
    public void assertExecuteForExecuteQueryResult() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Collections.singletonList(new ExecuteQueryResult(null, null)));
        RawJDBCExecutor executor = new RawJDBCExecutor(executorEngine, true);
        assertTrue(executor.execute(null, null));
    }
    
    @Test
    public void assertExecuteQueryForExecuteQueryResult() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        ExecuteQueryResult executeQueryResult = mock(ExecuteQueryResult.class);
        QueryResult queryResult = mock(QueryResult.class);
        when(executeQueryResult.getQueryResult()).thenReturn(queryResult);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Collections.singletonList(executeQueryResult));
        RawJDBCExecutor executor = new RawJDBCExecutor(executorEngine, true);
        List<QueryResult> actual = executor.executeQuery(null, null);
        assertThat(actual, is(Collections.singletonList(queryResult)));
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        ExecuteUpdateResult executeUpdateResult1 = new ExecuteUpdateResult(1, 2);
        ExecuteUpdateResult executeUpdateResult2 = new ExecuteUpdateResult(3, 4);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Arrays.asList(executeUpdateResult1, executeUpdateResult2));
        RawJDBCExecutor executor = new RawJDBCExecutor(executorEngine, true);
        assertThat(executor.executeUpdate(null, null), is(4));
    }
    
    @Test
    public void assertExecuteNotThrownSQLException() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
        RawJDBCExecutor rawJDBCExecutor = new RawJDBCExecutor(executorEngine, false);
        ExecutorExceptionHandler.setExceptionThrown(false);
        assertFalse(rawJDBCExecutor.execute(Collections.emptyList(), null));
    }
    
    @Test
    public void assertExecuteSQLException() {
        try {
            ExecutorEngine executorEngine = mock(ExecutorEngine.class);
            when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
            RawJDBCExecutor rawJDBCExecutor = new RawJDBCExecutor(executorEngine, false);
            rawJDBCExecutor.execute(Collections.emptyList(), null);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("TestSQLException"));
        }
    }
}
