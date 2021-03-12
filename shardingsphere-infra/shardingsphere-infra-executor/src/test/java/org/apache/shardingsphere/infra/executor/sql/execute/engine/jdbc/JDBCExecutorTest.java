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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.jdbc;

import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCExecutorTest {
    
    @Test
    public void assertExecute() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        ExecutionGroup<JDBCExecutionUnit> group = new ExecutionGroup<>(Collections.singletonList(mock(JDBCExecutionUnit.class)));
        ExecutionGroupContext context = new ExecutionGroupContext(Collections.singletonList(group));
        when(executorEngine.execute(any(), any(), any(), anyBoolean())).thenReturn(Collections.singletonList("test"));
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, false);
        List<?> actual1 = jdbcExecutor.execute(context, null);
        assertThat(actual1, is(Collections.singletonList("test")));
        List<?> actual2 = jdbcExecutor.execute(context, null, null);
        assertThat(actual2, is(Collections.singletonList("test")));
    }
    
    @Test
    public void assertExecuteSQLException() {
        try {
            ExecutorEngine executorEngine = mock(ExecutorEngine.class);
            when(executorEngine.execute(new ExecutionGroupContext<>(anyCollection()), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
            JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, false);
            jdbcExecutor.execute(new ExecutionGroupContext<>(Collections.emptyList()), null);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("TestSQLException"));
        }
    }
    
    @Test
    public void assertExecuteNotThrownSQLException() throws SQLException {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        when(executorEngine.execute(new ExecutionGroupContext<>(anyCollection()), any(), any(), anyBoolean())).thenThrow(new SQLException("TestSQLException"));
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, false);
        SQLExecutorExceptionHandler.setExceptionThrown(false);
        List<?> actual = jdbcExecutor.execute(new ExecutionGroupContext<>(Collections.emptyList()), null);
        assertThat(actual, is(Collections.emptyList()));
    }
}
