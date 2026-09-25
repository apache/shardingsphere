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

package org.apache.shardingsphere.driver.executor.engine.transaction;

import org.apache.shardingsphere.driver.jdbc.core.connection.DriverDatabaseConnectionManager;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class DriverTransactionalExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereConnection connection;
    
    @Mock
    private DriverDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private TransactionRule transactionRule;
    
    @Mock
    private ConnectionTransaction connectionTransaction;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(connection.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(databaseConnectionManager.getConnectionTransaction()).thenReturn(connectionTransaction);
        lenient().when(connectionTransaction.isLocalTransaction()).thenReturn(true);
        lenient().when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(transactionRule)));
    }
    
    @Test
    void assertExecuteWhenFailureToleranceDisabled() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
    }
    
    @Test
    void assertExecuteForDistributedTransaction() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
    }
    
    @Test
    void assertExecuteForSelectStatement() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(SelectStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
    }
    
    @Test
    void assertExecuteForNonDMLStatement() throws SQLException {
        when(transactionRule.isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean())).thenReturn(true);
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(SQLStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
        verify(connection).commit();
    }
    
    @Test
    void assertExecuteForCallStatement() throws SQLException {
        when(transactionRule.isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean())).thenReturn(true);
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(CallStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
        verify(connection).commit();
    }
    
    @Test
    void assertExecuteWithPrimaryOnly() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), Collections.singleton(createExecutionUnit("primary_ds")));
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
    }
    
    @Test
    void assertExecuteWithReplicaOnly() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), Collections.singleton(createExecutionUnit("replica_ds")));
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean());
    }
    
    @Test
    void assertExecuteInExplicitLocalTransaction() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);
        DriverTransactionalExecutor executor = createExecutor();
        Collection<ExecutionUnit> executionUnits = createDualWriteExecutionUnits();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), executionUnits);
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        InOrder actual = inOrder(databaseConnectionManager, callback);
        actual.verify(callback).execute();
        verify(databaseConnectionManager, never()).begin();
        verify(databaseConnectionManager, never()).commit();
        verify(databaseConnectionManager, never()).rollback();
    }
    
    @Test
    void assertExecuteInExplicitLocalTransactionWhenSQLExceptionThrown() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mock(ImplicitTransactionCallback.class);
        SQLException expected = new SQLException("foo_callback_failure");
        doThrow(expected).when(callback).execute();
        SQLException actual = assertThrows(SQLException.class, () -> executor.execute(database, executionContext, callback));
        assertThat(actual, is(expected));
        verify(databaseConnectionManager, never()).rollback();
        verify(databaseConnectionManager, never()).commit();
    }
    
    @Test
    void assertExecuteInExplicitLocalTransactionWhenRuntimeExceptionThrown() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mock(ImplicitTransactionCallback.class);
        RuntimeException expected = new IllegalStateException("foo_callback_failure");
        doThrow(expected).when(callback).execute();
        RuntimeException actual = assertThrows(RuntimeException.class, () -> executor.execute(database, executionContext, callback));
        assertThat(actual, is(expected));
        verify(databaseConnectionManager, never()).rollback();
        verify(databaseConnectionManager, never()).commit();
    }
    
    @Test
    void assertExecuteWithExistingImplicitDistributedTransaction() throws SQLException {
        when(transactionRule.isImplicitCommitTransaction(any(), anyBoolean(), any(), anyBoolean())).thenReturn(true);
        DriverTransactionalExecutor executor = createExecutor();
        ExecutionContext executionContext = createExecutionContext(mock(UpdateStatement.class), createDualWriteExecutionUnits());
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, executionContext, callback), is("foo_result"));
        InOrder actual = inOrder(databaseConnectionManager, callback, connection);
        actual.verify(databaseConnectionManager).begin();
        actual.verify(callback).execute();
        actual.verify(connection).commit();
    }
    
    @Test
    void assertExecuteWithSQLStatementAndMultiExecutionUnitsDoesNotEnableFailureTolerance() throws SQLException {
        DriverTransactionalExecutor executor = createExecutor();
        SQLStatement sqlStatement = mock(UpdateStatement.class);
        ImplicitTransactionCallback<String> callback = mockCallback();
        assertThat(executor.execute(database, sqlStatement, true, callback), is("foo_result"));
        verify(transactionRule).isImplicitCommitTransaction(sqlStatement, true, connectionTransaction, true);
    }
    
    private DriverTransactionalExecutor createExecutor() {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(props);
        return new DriverTransactionalExecutor(connection);
    }
    
    private ExecutionContext createExecutionContext(final SQLStatement sqlStatement, final Collection<ExecutionUnit> executionUnits) {
        ExecutionContext result = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        when(result.getExecutionUnits()).thenReturn(executionUnits);
        return result;
    }
    
    private Collection<ExecutionUnit> createDualWriteExecutionUnits() {
        return Arrays.asList(createExecutionUnit("primary_ds"), createExecutionUnit("replica_ds"));
    }
    
    private ExecutionUnit createExecutionUnit(final String dataSourceName) {
        return new ExecutionUnit(dataSourceName, new SQLUnit("UPDATE foo SET value = 1", Collections.emptyList()));
    }
    
    @SuppressWarnings("unchecked")
    private ImplicitTransactionCallback<String> mockCallback() throws SQLException {
        ImplicitTransactionCallback<String> callback = mock(ImplicitTransactionCallback.class);
        when(callback.execute()).thenReturn("foo_result");
        return callback;
    }
}
