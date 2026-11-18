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

package org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.engine.transaction.DriverTransactionalExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverJDBCPushDownExecuteExecutorTest {
    
    private final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
    
    private final ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
    
    private final QueryContext queryContext = mock(QueryContext.class);
    
    private final Statement statement = mock(Statement.class);
    
    private final ResultSet resultSet = mock(ResultSet.class);
    
    private final JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
    
    @SuppressWarnings("unchecked")
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = mock(DriverExecutionPrepareEngine.class);
    
    private final ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
    
    private final DriverJDBCPushDownExecuteExecutor executor = new DriverJDBCPushDownExecuteExecutor(connection, mock(ShardingSphereMetaData.class), jdbcExecutor);
    
    @Test
    void assertGetResultSetWithSelectStorageType() throws SQLException {
        mockPreparation(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.singletonList(newJdbcExecutionUnit("foo_mysql", statement)));
        when(statement.getResultSet()).thenReturn(resultSet);
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class);
                MockedConstruction<MergeEngine> ignoredMerge = mockConstruction(MergeEngine.class);
                MockedConstruction<ShardingSphereResultSet> ignoredResultSet = mockConstruction(ShardingSphereResultSet.class);
                MockedConstruction<JDBCStreamQueryResult> queryResults = mockConstruction(JDBCStreamQueryResult.class,
                        (mock, context) -> assertThat(context.arguments(), is(Arrays.asList(resultSet, database.getResourceMetaData().getStorageUnits().get("foo_mysql").getStorageType()))))) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertTrue(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
            assertThat(queryResults.constructed().size(), is(1));
        }
    }
    
    private void mockPreparation(final SQLStatementContext sqlStatementContext, final List<JDBCExecutionUnit> executionUnits) throws SQLException {
        doReturn(sqlStatementContext).when(executionContext).getSqlStatementContext();
        doReturn(sqlStatementContext).when(queryContext).getSqlStatementContext();
        Collection<ExecutionUnit> routedUnits = new ArrayList<>(executionUnits.size());
        for (JDBCExecutionUnit each : executionUnits) {
            routedUnits.add(each.getExecutionUnit());
        }
        when(executionContext.getExecutionUnits()).thenReturn(routedUnits);
        when(prepareEngine.prepare(any(), any(), any(), any())).thenReturn(new ExecutionGroupContext<>(Collections.singleton(new ExecutionGroup<>(executionUnits)),
                mock(ExecutionGroupReportContext.class)));
    }
    
    private JDBCExecutionUnit newJdbcExecutionUnit(final String dataSourceName, final Statement storageResource) {
        JDBCExecutionUnit result = mock(JDBCExecutionUnit.class, RETURNS_DEEP_STUBS);
        when(result.getExecutionUnit().getDataSourceName()).thenReturn(dataSourceName);
        when(result.getStorageResource()).thenReturn(storageResource);
        return result;
    }
    
    private MockedConstruction<DriverTransactionalExecutor> mockTransactionalExecutor() {
        return mockConstruction(DriverTransactionalExecutor.class,
                (mock, context) -> when(mock.execute(any(), any(), any())).thenAnswer(invocation -> invocation.<ImplicitTransactionCallback<Boolean>>getArgument(2).execute()));
    }
    
    @Test
    void assertGetResultSetWithDALStatements() throws SQLException {
        Statement secondStatement = mock(Statement.class);
        Statement noResultStatement = mock(Statement.class);
        ResultSet secondResultSet = mock(ResultSet.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        doReturn(mock(DALStatement.class)).when(sqlStatementContext).getSqlStatement();
        mockPreparation(sqlStatementContext, Arrays.asList(newJdbcExecutionUnit("foo_mysql", statement), newJdbcExecutionUnit("foo_mysql", secondStatement),
                newJdbcExecutionUnit("foo_mysql", noResultStatement)));
        when(statement.getResultSet()).thenReturn(resultSet);
        when(secondStatement.getResultSet()).thenReturn(secondResultSet);
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class);
                MockedConstruction<MergeEngine> ignoredMerge = mockConstruction(MergeEngine.class);
                MockedConstruction<ShardingSphereResultSet> ignoredResultSet = mockConstruction(ShardingSphereResultSet.class);
                MockedConstruction<JDBCStreamQueryResult> queryResults = mockConstruction(JDBCStreamQueryResult.class,
                        (mock, context) -> assertThat(context.arguments(), is(Arrays.asList(1 == context.getCount() ? secondResultSet : resultSet,
                                database.getResourceMetaData().getStorageUnits().get("foo_mysql").getStorageType()))))) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertTrue(executor.getResultSet(database, queryContext, statement, Arrays.asList(noResultStatement, secondStatement, statement)).isPresent());
            assertThat(queryResults.constructed().size(), is(2));
            verify(noResultStatement).getResultSet();
        }
    }
    
    @Test
    void assertGetResultSetAfterSelectThenDal() throws SQLException {
        mockPreparation(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.singletonList(newJdbcExecutionUnit("foo_mysql", statement)));
        when(statement.getResultSet()).thenReturn(resultSet);
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class);
                MockedConstruction<MergeEngine> ignoredMerge = mockConstruction(MergeEngine.class);
                MockedConstruction<ShardingSphereResultSet> ignoredResultSet = mockConstruction(ShardingSphereResultSet.class);
                MockedConstruction<JDBCStreamQueryResult> queryResults = mockConstruction(JDBCStreamQueryResult.class, (mock, context) -> {
                    String dataSourceName = 1 == context.getCount() ? "foo_mysql" : "foo_postgresql";
                    assertThat(context.arguments().get(0), sameInstance(resultSet));
                    assertThat(context.arguments().get(1), sameInstance(database.getResourceMetaData().getStorageUnits().get(dataSourceName).getStorageType()));
                })) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertTrue(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
            SQLStatementContext dalStatementContext = mock(SQLStatementContext.class);
            doReturn(mock(DALStatement.class)).when(dalStatementContext).getSqlStatement();
            mockPreparation(dalStatementContext, Collections.singletonList(newJdbcExecutionUnit("foo_postgresql", statement)));
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertTrue(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
            assertThat(queryResults.constructed().size(), is(2));
        }
    }
    
    @Test
    void assertGetResultSetWithPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        JDBCExecutionUnit executionUnit = newJdbcExecutionUnit("foo_mysql", preparedStatement);
        when(executionUnit.getExecutionUnit().getSqlUnit().getParameters()).thenReturn(Collections.singletonList(1));
        mockPreparation(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.singletonList(executionUnit));
        when(prepareEngine.getType()).thenReturn(JDBCDriverType.PREPARED_STATEMENT);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        StatementAddCallback addCallback = mock(StatementAddCallback.class);
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class);
                MockedConstruction<MergeEngine> ignoredMerge = mockConstruction(MergeEngine.class);
                MockedConstruction<ShardingSphereResultSet> ignoredResultSet = mockConstruction(ShardingSphereResultSet.class);
                MockedConstruction<JDBCStreamQueryResult> queryResults = mockConstruction(JDBCStreamQueryResult.class,
                        (mock, context) -> assertThat(context.arguments(), is(Arrays.asList(resultSet, database.getResourceMetaData().getStorageUnits().get("foo_mysql").getStorageType()))))) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, addCallback, () -> {
            });
            assertTrue(executor.getResultSet(database, queryContext, statement, Collections.singletonList(preparedStatement)).isPresent());
            assertThat(queryResults.constructed().size(), is(1));
            verify(addCallback).add(Collections.singletonList(preparedStatement), Collections.singletonList(Collections.singletonList(1)));
        }
    }
    
    @Test
    void assertGetResultSetWithEmptyExecutionUnits() throws SQLException {
        mockPreparation(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList());
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class)) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertFalse(executor.getResultSet(database, queryContext, statement, Collections.emptyList()).isPresent());
        }
    }
    
    @Test
    void assertGetResultSetWithoutQueryResults() throws SQLException {
        mockPreparation(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.singletonList(newJdbcExecutionUnit("foo_mysql", statement)));
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class)) {
            executor.execute(database, executionContext, prepareEngine, (sql, resource) -> true, (resources, params) -> {
            }, () -> {
            });
            assertFalse(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
        }
    }
    
    @Test
    void assertGetResultSetWithUpdate() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        doReturn(mock(DMLStatement.class)).when(sqlStatementContext).getSqlStatement();
        mockPreparation(sqlStatementContext, Collections.singletonList(newJdbcExecutionUnit("foo_mysql", statement)));
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class)) {
            assertFalse(executor.execute(database, executionContext, prepareEngine, (sql, resource) -> false, (resources, params) -> {
            }, () -> {
            }));
            assertFalse(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
            verify(statement, never()).getResultSet();
        }
    }
    
    @Test
    void assertGetResultSetWithDDL() throws SQLException {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        doReturn(mock(DDLStatement.class)).when(sqlStatementContext).getSqlStatement();
        mockPreparation(sqlStatementContext, Collections.singletonList(newJdbcExecutionUnit("foo_mysql", statement)));
        when(connection.getAutoCommit()).thenReturn(true);
        try (
                MockedConstruction<DriverTransactionalExecutor> ignoredTransaction = mockTransactionalExecutor();
                MockedConstruction<ProcessEngine> ignoredProcess = mockConstruction(ProcessEngine.class)) {
            assertFalse(executor.execute(database, executionContext, prepareEngine, (sql, resource) -> false, (resources, params) -> {
            }, () -> {
            }));
            assertFalse(executor.getResultSet(database, queryContext, statement, Collections.singletonList(statement)).isPresent());
            verify(statement, never()).getResultSet();
        }
    }
}
