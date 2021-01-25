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

package org.apache.shardingsphere.driver.executor.batch;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.driver.executor.AbstractBaseExecutorTest;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class BatchPreparedStatementExecutorTest extends AbstractBaseExecutorTest {
    
    private static final String SQL = "DELETE FROM table_x WHERE id=?";
    
    private BatchPreparedStatementExecutor actual;
    
    @Mock
    private SQLStatementContext<?> sqlStatementContext;
    
    @Override
    public void setUp() throws SQLException {
        super.setUp();
        actual = spy(new BatchPreparedStatementExecutor(getConnection().getMetaDataContexts(), new JDBCExecutor(getExecutorEngine(), false)));
        when(sqlStatementContext.getTablesContext()).thenReturn(mock(TablesContext.class));
    }
    
    @Test
    public void assertNoPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeBatch()).thenReturn(new int[] {0, 0});
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThat(actual.executeBatch(sqlStatementContext), is(new int[] {0, 0}));
    }
    
    @Test
    public void assertExecuteBatchForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        when(preparedStatement.executeBatch()).thenReturn(new int[] {10, 20});
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThat(actual.executeBatch(sqlStatementContext), is(new int[] {10, 20}));
        verify(preparedStatement).executeBatch();
    }
    
    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("jdbc:h2:mem:primary_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    @Test
    public void assertExecuteBatchForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        when(preparedStatement1.executeBatch()).thenReturn(new int[] {10, 20});
        when(preparedStatement2.executeBatch()).thenReturn(new int[] {20, 40});
        setExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2));
        assertThat(actual.executeBatch(sqlStatementContext), is(new int[] {30, 60}));
        verify(preparedStatement1).executeBatch();
        verify(preparedStatement2).executeBatch();
    }
    
    @Test
    public void assertExecuteBatchForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement.executeBatch()).thenThrow(ex);
        setExecutionGroups(Collections.singletonList(preparedStatement));
        assertThat(actual.executeBatch(sqlStatementContext), is(new int[] {0, 0}));
        verify(preparedStatement).executeBatch();
    }
    
    @Test
    public void assertExecuteBatchForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = getPreparedStatement();
        PreparedStatement preparedStatement2 = getPreparedStatement();
        SQLException ex = new SQLException("");
        when(preparedStatement1.executeBatch()).thenThrow(ex);
        when(preparedStatement2.executeBatch()).thenThrow(ex);
        setExecutionGroups(Arrays.asList(preparedStatement1, preparedStatement2));
        assertThat(actual.executeBatch(sqlStatementContext), is(new int[] {0, 0}));
        verify(preparedStatement1).executeBatch();
        verify(preparedStatement2).executeBatch();
    }
    
    private void setExecutionGroups(final List<PreparedStatement> preparedStatements) {
        Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = new LinkedList<>();
        List<JDBCExecutionUnit> executionUnits = new LinkedList<>();
        executionGroups.add(new ExecutionGroup<>(executionUnits));
        Collection<BatchExecutionUnit> batchExecutionUnits = new LinkedList<>();
        for (PreparedStatement each : preparedStatements) {
            BatchExecutionUnit batchExecutionUnit = new BatchExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(SQL, Collections.singletonList(1))));
            batchExecutionUnit.mapAddBatchCount(0);
            batchExecutionUnit.mapAddBatchCount(1);
            batchExecutionUnits.add(batchExecutionUnit);
            executionUnits.add(new JDBCExecutionUnit(new ExecutionUnit("ds_0", new SQLUnit(SQL, Collections.singletonList(1))),
                    ConnectionMode.MEMORY_STRICTLY, each));
        }
        setFields(executionGroups, batchExecutionUnits);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFields(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final Collection<BatchExecutionUnit> batchExecutionUnits) {
        Field field = BatchPreparedStatementExecutor.class.getDeclaredField("executionGroups");
        field.setAccessible(true);
        field.set(actual, executionGroups);
        field = BatchPreparedStatementExecutor.class.getDeclaredField("batchExecutionUnits");
        field.setAccessible(true);
        field.set(actual, batchExecutionUnits);
        field = BatchPreparedStatementExecutor.class.getDeclaredField("batchCount");
        field.setAccessible(true);
        field.set(actual, 2);
    }
}
