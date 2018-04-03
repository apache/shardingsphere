/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.opentracing;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.executor.BaseStatementUnit;
import io.shardingjdbc.core.executor.ExecuteCallback;
import io.shardingjdbc.core.executor.ExecutorEngine;
import io.shardingjdbc.core.executor.type.statement.StatementUnit;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public final class ExecuteEventListenerTest {
    
    private static final MockTracer TRACER = new MockTracer(new ThreadLocalActiveSpanSource(),
            MockTracer.Propagator.TEXT_MAP);
    
    private final ExecutorEngine executorEngine = new ExecutorEngine(5);
    
    @BeforeClass
    public static void init() {
        ShardingJDBCTracer.init(TRACER);
    }
    
    @Before
    public void before() {
        TRACER.reset();
    }
    
    @Test
    public void assertSingleStatement() throws Exception {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        executorEngine.executeStatement(SQLType.DML, Collections.singleton(new StatementUnit(new SQLExecutionUnit("ds_0", "insert into ..."), statement)), new ExecuteCallback<Integer>() {
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return 0;
            }
        });
        assertThat(TRACER.finishedSpans().size(), is(2));
    }
    
    @Test
    public void assertMultiStatement() throws Exception {
        List<StatementUnit> statementUnitList = new ArrayList<>(2);
        Statement stm1 = mock(Statement.class);
        when(stm1.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new StatementUnit(new SQLExecutionUnit("ds_0", "insert into ..."), stm1));
        Statement stm2 = mock(Statement.class);
        when(stm2.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new StatementUnit(new SQLExecutionUnit("ds_0", "insert into ..."), stm2));
        executorEngine.executeStatement(SQLType.DML, statementUnitList, new ExecuteCallback<Integer>() {
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                return 0;
            }
        });
        assertThat(TRACER.finishedSpans().size(), is(3));
    }
    
    @Test(expected = SQLException.class)
    public void assertSQLException() throws Exception {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        executorEngine.executeStatement(SQLType.DQL, Collections.singleton(new StatementUnit(new SQLExecutionUnit("ds_0", "select ..."), statement)), new ExecuteCallback<Integer>() {
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                throw new SQLException();
            }
        });
    }
}
