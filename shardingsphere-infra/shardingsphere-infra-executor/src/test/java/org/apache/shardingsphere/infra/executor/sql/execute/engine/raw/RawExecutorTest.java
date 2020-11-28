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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.raw;

import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
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

public final class RawExecutorTest {
    
    @Test
    public void assertExecuteForResultEmpty() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        RawExecutor executor = new RawExecutor(executorEngine, true);
        assertFalse(executor.execute(null, null));
    }
    
    @Test
    public void assertExecuteForQueryResult() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Collections.singletonList(mock(QueryResult.class)));
        RawExecutor executor = new RawExecutor(executorEngine, true);
        assertTrue(executor.execute(null, null));
    }
    
    @Test
    public void assertExecuteQueryForQueryResult() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        QueryResult queryResult = mock(QueryResult.class);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Collections.singletonList(queryResult));
        RawExecutor executor = new RawExecutor(executorEngine, true);
        List<QueryResult> actual = executor.executeQuery(null, null);
        assertThat(actual, is(Collections.singletonList(queryResult)));
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        UpdateResult updateResult1 = new UpdateResult(1, 2);
        UpdateResult updateResult2 = new UpdateResult(3, 4);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Arrays.asList(updateResult1, updateResult2));
        RawExecutor executor = new RawExecutor(executorEngine, true);
        assertThat(executor.executeUpdate(null, null), is(4));
    }
    
    @Test
    public void assertExecuteNotThrownSQLException() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
        RawExecutor rawExecutor = new RawExecutor(executorEngine, false);
        SQLExecutorExceptionHandler.setExceptionThrown(false);
        assertFalse(rawExecutor.execute(Collections.emptyList(), null));
    }
    
    @Test
    public void assertExecuteSQLException() {
        try {
            ExecutorEngine executorEngine = mock(ExecutorEngine.class);
            when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
            RawExecutor rawExecutor = new RawExecutor(executorEngine, false);
            rawExecutor.execute(Collections.emptyList(), null);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("TestSQLException"));
        }
    }
}
