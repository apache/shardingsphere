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

package io.shardingsphere.opentracing.listener.executor;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.opentracing.listener.BaseOpenTracingListenerTest;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecuteEventListenerTest extends BaseOpenTracingListenerTest {
    
    private static final String HOST_URL = "jdbc:mysql://127.0.0.1:3306/ds_0";
    
    private ShardingExecuteEngine executeEngine = new ShardingExecuteEngine(5);
    
    private final SQLExecuteTemplate sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine);
    
    @After
    public void tearDown() {
        executeEngine.close();
    }
    
    @Test
    public void assertSingleStatement() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        when(statement.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(statement.getConnection().getMetaData().getURL()).thenReturn(HOST_URL);
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(DatabaseType.MySQL, SQLType.DML, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) {
                return 0;
            }
        };
        ShardingExecuteGroup<StatementExecuteUnit> shardingExecuteGroup = new ShardingExecuteGroup<>(Collections.singletonList(new StatementExecuteUnit(new RouteUnit("ds_0",
                new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), statement, ConnectionMode.MEMORY_STRICTLY)));
        sqlExecuteTemplate.executeGroup((Collection) Collections.singletonList(shardingExecuteGroup), executeCallback);
        assertThat(getTracer().finishedSpans().size(), is(1));
    }
    
    @Test
    public void assertMultiStatement() throws SQLException {
        final List<StatementExecuteUnit> statementExecuteUnits = new ArrayList<>(2);
        Statement stm1 = mock(Statement.class);
        when(stm1.getConnection()).thenReturn(mock(Connection.class));
        when(stm1.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(stm1.getConnection().getMetaData().getURL()).thenReturn(HOST_URL);
        statementExecuteUnits.add(new StatementExecuteUnit(new RouteUnit("ds_0", 
                new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), stm1, ConnectionMode.MEMORY_STRICTLY));
        Statement stm2 = mock(Statement.class);
        when(stm2.getConnection()).thenReturn(mock(Connection.class));
        when(stm2.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(stm2.getConnection().getMetaData().getURL()).thenReturn(HOST_URL);
        statementExecuteUnits.add(new StatementExecuteUnit(new RouteUnit("ds_0", 
                new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), stm2, ConnectionMode.MEMORY_STRICTLY));
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(DatabaseType.MySQL, SQLType.DML, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) {
                return 0;
            }
        };
        ShardingExecuteGroup<StatementExecuteUnit> shardingExecuteGroup = new ShardingExecuteGroup<>(statementExecuteUnits);
        sqlExecuteTemplate.executeGroup((Collection) Collections.singletonList(shardingExecuteGroup), executeCallback);
        assertThat(getTracer().finishedSpans().size(), is(2));
    }
    
    @Test(expected = SQLException.class)
    public void assertSQLException() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        when(statement.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(statement.getConnection().getMetaData().getURL()).thenReturn(HOST_URL);
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        SQLExecuteCallback<Integer> executeCallback = new SQLExecuteCallback<Integer>(DatabaseType.MySQL, SQLType.DQL, isExceptionThrown, dataMap) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                throw new SQLException();
            }
        };
        ShardingExecuteGroup<StatementExecuteUnit> shardingExecuteGroup = new ShardingExecuteGroup<>(Collections.singletonList(new StatementExecuteUnit(new RouteUnit("ds_0",
                new SQLUnit("select ...", Collections.singletonList(Collections.<Object>singletonList(1)))), statement, ConnectionMode.MEMORY_STRICTLY)));
        sqlExecuteTemplate.executeGroup((Collection) Collections.singletonList(shardingExecuteGroup), executeCallback);
    }
}
