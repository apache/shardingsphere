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

package org.apache.shardingsphere.globalclock.executor;

import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.spi.TransactionHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class GlobalClockTransactionHookTest {
    
    private GlobalClockTransactionHook transactionHook;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GlobalClockRule rule;
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private TransactionConnectionContext transactionContext;
    
    @Mock
    private GlobalClockProvider globalClockProvider;
    
    @Mock
    private GlobalClockTransactionExecutor globalClockTransactionExecutor;
    
    @BeforeEach
    void setUp() {
        transactionHook = (GlobalClockTransactionHook) OrderedSPILoader.getServices(TransactionHook.class, Collections.singleton(rule)).get(rule);
    }
    
    @Test
    void assertBeforeBegin() {
        assertDoesNotThrow(() -> transactionHook.beforeBegin(rule, databaseType, transactionContext));
    }
    
    @Test
    void assertAfterBeginWhenGlobalClockProviderAbsent() {
        transactionHook.afterBegin(rule, databaseType, transactionContext);
        verify(transactionContext, times(0)).setBeginMills(anyLong());
    }
    
    @Test
    void assertAfterBeginWhenGlobalClockProviderPresent() {
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(globalClockProvider));
        when(globalClockProvider.getCurrentTimestamp()).thenReturn(10L);
        transactionHook.afterBegin(rule, databaseType, transactionContext);
        verify(transactionContext).setBeginMills(10L);
    }
    
    @Test
    void assertAfterCreateConnectionsWhenDisabledGlobalClockRule() throws SQLException {
        transactionHook.afterCreateConnections(rule, databaseType, Collections.emptyList(), transactionContext);
        verify(globalClockTransactionExecutor, times(0)).sendSnapshotTimestamp(any(), anyLong());
    }
    
    @Test
    void assertAfterCreateConnectionsWhenGlobalClockTransactionExecutorAbsent() throws SQLException {
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.empty());
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        transactionHook.afterCreateConnections(rule, databaseType, Collections.emptyList(), transactionContext);
        verify(globalClockTransactionExecutor, times(0)).sendSnapshotTimestamp(any(), anyLong());
    }
    
    @Test
    void assertAfterCreateConnectionsWhenGlobalClockTransactionExecutorPresent() throws SQLException {
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.of(globalClockTransactionExecutor));
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        transactionHook.afterCreateConnections(rule, databaseType, Collections.emptyList(), transactionContext);
        verify(globalClockTransactionExecutor).sendSnapshotTimestamp(Collections.emptyList(), 0L);
    }
    
    @Test
    void assertBeforeExecuteSQLWhenDisabledGlobalClockRule() throws SQLException {
        transactionHook.beforeExecuteSQL(rule, databaseType, Collections.emptyList(), transactionContext, TransactionIsolationLevel.READ_COMMITTED);
        verify(rule, times(0)).getGlobalClockProvider();
    }
    
    @Test
    void assertBeforeExecuteSQLWhenNotReadCommittedIsolationLevel() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        transactionHook.beforeExecuteSQL(rule, databaseType, Collections.emptyList(), transactionContext, TransactionIsolationLevel.REPEATABLE_READ);
        verify(rule, times(0)).getGlobalClockProvider();
    }
    
    @Test
    void assertBeforeExecuteSQLWhenGlobalClockTransactionExecutorAbsent() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(globalClockProvider));
        transactionHook.beforeExecuteSQL(rule, databaseType, Collections.emptyList(), transactionContext, TransactionIsolationLevel.READ_COMMITTED);
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.empty());
        verify(globalClockTransactionExecutor, times(0)).sendSnapshotTimestamp(any(), anyLong());
    }
    
    @Test
    void assertBeforeExecuteSQLWhenNullTransactionIsolationLevel() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(globalClockProvider));
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.of(globalClockTransactionExecutor));
        when(globalClockProvider.getCurrentTimestamp()).thenReturn(10L);
        transactionHook.beforeExecuteSQL(rule, databaseType, Collections.emptyList(), transactionContext, null);
        verify(globalClockTransactionExecutor).sendSnapshotTimestamp(Collections.emptyList(), 10L);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBeforeCommitWhenDisabledGlobalClockRule() throws SQLException {
        LockContext lockContext = mock(LockContext.class);
        transactionHook.beforeCommit(rule, databaseType, Collections.emptyList(), transactionContext, lockContext);
        verify(lockContext, times(0)).tryLock(any(), anyLong());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertBeforeCommitWhenTryLockFailed() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        LockContext lockContext = mock(LockContext.class);
        transactionHook.beforeCommit(rule, databaseType, Collections.emptyList(), transactionContext, lockContext);
        verify(globalClockTransactionExecutor, times(0)).sendCommitTimestamp(any(), anyLong());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBeforeCommitWhenGlobalClockTransactionExecutorAbsent() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        LockContext lockContext = mock(LockContext.class);
        when(lockContext.tryLock(any(), anyLong())).thenReturn(true);
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.empty());
        transactionHook.beforeCommit(rule, databaseType, Collections.emptyList(), transactionContext, lockContext);
        verify(globalClockTransactionExecutor, times(0)).sendCommitTimestamp(any(), anyLong());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBeforeCommit() throws SQLException {
        when(rule.getConfiguration().isEnabled()).thenReturn(true);
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(globalClockProvider));
        when(globalClockProvider.getCurrentTimestamp()).thenReturn(10L);
        LockContext lockContext = mock(LockContext.class);
        when(lockContext.tryLock(any(), anyLong())).thenReturn(true);
        when(DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType)).thenReturn(Optional.of(globalClockTransactionExecutor));
        transactionHook.beforeCommit(rule, databaseType, Collections.emptyList(), transactionContext, lockContext);
        verify(globalClockTransactionExecutor).sendCommitTimestamp(Collections.emptyList(), 10L);
    }
    
    @Test
    void assertAfterCommitWhenGlobalClockProviderAbsent() {
        transactionHook.afterCommit(rule, databaseType, Collections.emptyList(), transactionContext, mock(LockContext.class));
        verify(globalClockProvider, times(0)).getNextTimestamp();
    }
    
    @Test
    void assertAfterCommitWhenGlobalClockProviderPresent() {
        when(rule.getGlobalClockProvider()).thenReturn(Optional.of(globalClockProvider));
        transactionHook.afterCommit(rule, databaseType, Collections.emptyList(), transactionContext, mock(LockContext.class));
        verify(globalClockProvider).getNextTimestamp();
    }
    
    @Test
    void assertBeforeRollback() {
        assertDoesNotThrow(() -> transactionHook.beforeRollback(rule, databaseType, Collections.emptyList(), transactionContext));
    }
    
    @Test
    void assertAfterRollback() {
        assertDoesNotThrow(() -> transactionHook.afterRollback(rule, databaseType, Collections.emptyList(), transactionContext));
    }
}
