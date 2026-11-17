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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FirebirdTransactionIdGeneratorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertNextTransactionId() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        assertThat(FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession), is(1));
        assertThat(FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession), is(2));
    }
    
    @Test
    void assertGetTransactionId() {
        assertThat(FirebirdTransactionIdGenerator.getInstance().getTransactionId(CONNECTION_ID), is(0));
    }
    
    @Test
    void assertGetTransactionIdWithoutRegisteredConnection() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionId(CONNECTION_ID));
    }
    
    @Test
    void assertIsTransactionActiveWithClosedTransaction() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertIsTransactionActiveWithoutRegisteredConnection() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
    }
    
    @Test
    void assertHasActiveTransaction() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        assertTrue(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
    }
    
    @Test
    void assertStartTransactionWithIndependentSessions() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().getCloseLock()).thenReturn(new Object());
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(connectionSession, true, true, TransactionIsolationLevel.SERIALIZABLE);
        int actualTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(connectionSession, true, false, TransactionIsolationLevel.READ_COMMITTED);
        ConnectionSession firstSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, firstTransactionId);
        ConnectionSession actualSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, actualTransactionId);
        assertThat(actualSession, not(sameInstance(firstSession)));
        assertThat(actualSession.getDatabaseConnectionManager(), not(sameInstance(firstSession.getDatabaseConnectionManager())));
        assertTrue(firstSession.isReadOnly());
        assertFalse(actualSession.isReadOnly());
        assertThat(firstSession.getIsolationLevel().get(), is(TransactionIsolationLevel.SERIALIZABLE));
        assertThat(actualSession.getIsolationLevel().get(), is(TransactionIsolationLevel.READ_COMMITTED));
    }
    
    @Test
    void assertGetTransactionSessionWithUnknownHandle() {
        assertThrows(InvalidTransactionHandleException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, 99));
    }
    
    @Test
    void assertCloseTransaction() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        verify(connectionSession.getDatabaseConnectionManager()).closeAllResources();
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertCloseTransactionWithCleanupFailureKeepsHandle() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.singletonList(new SQLException("foo_failure")));
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        ConnectionSession siblingSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(siblingSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(siblingSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.emptyList());
        int siblingTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(siblingSession);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        try {
            assertThrows(BackendConnectionException.class, () -> FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId));
            assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, siblingTransactionId));
            assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(3));
        } finally {
            FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertUnregisterConnectionWithCleanupFailures() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        SQLException expected = new SQLException("foo_failure");
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.singletonList(expected));
        ConnectionSession otherSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(otherSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(otherSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.emptyList());
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(otherSession);
        BackendConnectionException actual = assertThrows(BackendConnectionException.class, () -> FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID));
        assertThat(actual.getExceptions(), contains(expected));
        verify(otherSession.getDatabaseConnectionManager()).closeAllResources();
    }
    
    @Test
    void assertStartTransactionWithBeginFailure() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().getCloseLock()).thenReturn(new Object());
        try (
                MockedConstruction<ProxyBackendTransactionManager> ignored = mockConstruction(ProxyBackendTransactionManager.class,
                        (mock, context) -> doThrow(new IllegalStateException("foo_failure")).when(mock).begin())) {
            assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().startTransaction(connectionSession, false, false, TransactionIsolationLevel.READ_COMMITTED));
            assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        }
    }
    
    @Test
    void assertCloseLastTransactionReleasesAttachment() {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        assertTrue(attachmentSession.getTransactionStatus().isInTransaction());
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
        assertFalse(attachmentSession.getConnectionContext().getTransactionContext().isTransactionStarted());
    }
    
    private void mockLocalTransactionRule() {
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        TransactionRule transactionRule = mock(TransactionRule.class);
        doReturn(transactionRule).when(ruleMetaData).getSingleRule(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
    }
    
    private ConnectionSession createAttachmentSession() {
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        ConnectionSession result = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, "Firebird"), null);
        result.setConnectionId(CONNECTION_ID);
        result.setGrantee(mock(Grantee.class));
        result.setCurrentDatabaseName("foo_db");
        return result;
    }
    
    @Test
    void assertStartTransactionWithUnusedUnavailableStorageUnit() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_online", mock(StorageUnit.class));
        storageUnits.put("foo_offline", mock(StorageUnit.class));
        Connection pooledConnection = mock(Connection.class);
        FBConnection firebirdConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(firebirdConnection);
        when(firebirdConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(firebirdConnection.getFbDatabase().startTransaction(any())).thenAnswer(invocation -> mock(FbTransaction.class));
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), anyString(), eq(1), eq(ConnectionMode.MEMORY_STRICTLY), any())).thenAnswer(invocation -> {
            if ("foo_offline".equals(invocation.getArgument(1))) {
                throw new SQLException("foo_unavailable", "08001");
            }
            return Collections.singletonList(pooledConnection);
        });
        ConnectionSession attachmentSession = createAttachmentSession();
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, true, TransactionIsolationLevel.REPEATABLE_READ);
        int secondTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
        verify(ProxyContext.getInstance().getBackendDataSource(), never()).getConnections(anyString(), anyString(), eq(1), any(), any());
        ConnectionSession firstSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, firstTransactionId);
        ConnectionSession secondSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, secondTransactionId);
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            assertTrue(firstSession.getConnectionContext().getUsedDataSourceNames().isEmpty());
            assertTrue(secondSession.getConnectionContext().getUsedDataSourceNames().isEmpty());
            Connection writer = secondSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_online", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            verify(firebirdConnection.getFbDatabase(), times(2)).startTransaction(any());
            assertTrue(firstSession.getConnectionContext().getUsedDataSourceNames().isEmpty());
            assertThat(secondSession.getConnectionContext().getUsedDataSourceNames(), contains("foo_online"));
            writer.createStatement().close();
            Connection snapshot = firstSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_online", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            assertTrue(snapshot.isReadOnly());
            snapshot.createStatement().close();
            new ProxyBackendTransactionManager(firstSession.getDatabaseConnectionManager()).commit();
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, firstTransactionId);
            new ProxyBackendTransactionManager(secondSession.getDatabaseConnectionManager()).rollback();
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, secondTransactionId);
        }
        verify(ProxyContext.getInstance().getBackendDataSource()).getConnections(eq("foo_db"), eq("foo_online"), eq(1), eq(ConnectionMode.MEMORY_STRICTLY), any());
        verify(ProxyContext.getInstance().getBackendDataSource(), never()).getConnections(eq("foo_db"), eq("foo_offline"), eq(1), any(), any());
        verify(firebirdConnection.getFbDatabase(), times(2)).startTransaction(any());
        verify(pooledConnection, never()).close();
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        verify(pooledConnection).close();
    }
    
    @Test
    void assertRoutedConnectionOffsetsAndPendingSnapshots() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("foo_unused", mock(StorageUnit.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        List<Connection> pooledConnections = Arrays.asList(mock(Connection.class), mock(Connection.class), mock(Connection.class));
        List<FBConnection> nativeConnections = new ArrayList<>(3);
        List<FbTransaction> snapshotTransactions = new ArrayList<>(3);
        List<FbTransaction> writerTransactions = new ArrayList<>(3);
        for (Connection each : pooledConnections) {
            FBConnection nativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
            when(each.unwrap(FBConnection.class)).thenReturn(nativeConnection);
            when(nativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
            FbTransaction snapshotTransaction = mock(FbTransaction.class);
            FbTransaction writerTransaction = mock(FbTransaction.class);
            when(nativeConnection.getFbDatabase().startTransaction(any())).thenReturn(snapshotTransaction, writerTransaction);
            snapshotTransactions.add(snapshotTransaction);
            writerTransactions.add(writerTransaction);
            nativeConnections.add(nativeConnection);
        }
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), eq("foo_ds"), eq(2), eq(ConnectionMode.MEMORY_STRICTLY), any()))
                .thenReturn(pooledConnections.subList(0, 2));
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), eq("foo_ds"), eq(1), eq(ConnectionMode.MEMORY_STRICTLY), any()))
                .thenReturn(Collections.singletonList(pooledConnections.get(2)));
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int snapshotId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            int writerId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
            ConnectionSession snapshotSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, snapshotId);
            ConnectionSession writerSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, writerId);
            Connection initial = writerSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 1, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            Connection additional = writerSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 2, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            assertThat(writerSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 1, 1, ConnectionMode.MEMORY_STRICTLY).get(0), sameInstance(initial));
            assertThat(writerSession.getDatabaseConnectionManager().getConnectionSize(), is(3));
            assertTrue(snapshotSession.getConnectionContext().getUsedDataSourceNames().isEmpty());
            List<Connection> attachments = attachmentSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 3, ConnectionMode.MEMORY_STRICTLY);
            for (int i = 0; i < attachments.size(); i++) {
                assertThat(attachments.get(i).unwrap(FBConnection.class), sameInstance(nativeConnections.get(i)));
                verify(nativeConnections.get(i).getFbDatabase(), times(2)).startTransaction(any());
            }
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, snapshotId);
            for (int i = 0; i < 3; i++) {
                verify(snapshotTransactions.get(i)).rollback();
                verify(writerTransactions.get(i), never()).rollback();
            }
            additional.createStatement().close();
            new ProxyBackendTransactionManager(writerSession.getDatabaseConnectionManager()).commit();
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, writerId);
        }
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        for (Connection each : pooledConnections) {
            verify(each).close();
        }
    }
    
    @Test
    void assertRoutedConnectionsWithPartialStartFailure() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        Connection firstPooledConnection = mock(Connection.class);
        Connection secondPooledConnection = mock(Connection.class);
        FBConnection firstNativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        FBConnection secondNativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(firstPooledConnection.unwrap(FBConnection.class)).thenReturn(firstNativeConnection);
        when(secondPooledConnection.unwrap(FBConnection.class)).thenReturn(secondNativeConnection);
        when(firstNativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(secondNativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        FbTransaction siblingTransaction = mock(FbTransaction.class);
        FbTransaction writerTransaction = mock(FbTransaction.class);
        when(firstNativeConnection.getFbDatabase().startTransaction(any())).thenReturn(writerTransaction, siblingTransaction);
        SQLException expected = new SQLException("foo_unavailable", "08001");
        when(secondNativeConnection.getFbDatabase().startTransaction(any())).thenThrow(expected);
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), eq("foo_ds"), eq(2), eq(ConnectionMode.MEMORY_STRICTLY), any()))
                .thenReturn(Arrays.asList(firstPooledConnection, secondPooledConnection));
        ConnectionSession attachmentSession = createAttachmentSession();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("foo_unused", mock(StorageUnit.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int siblingId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
            int writerId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            ConnectionSession writerSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, writerId);
            SQLException actual = assertThrows(SQLException.class, () -> writerSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 2, ConnectionMode.MEMORY_STRICTLY));
            assertThat(actual, sameInstance(expected));
            assertThat(writerSession.getDatabaseConnectionManager().getConnectionSize(), is(0));
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, writerId);
            verify(writerTransaction).rollback();
            verify(siblingTransaction, never()).rollback();
            ConnectionSession siblingSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, siblingId);
            siblingSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0).createStatement().close();
            verify(firstNativeConnection.getFbDatabase(), times(2)).startTransaction(any());
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, siblingId);
            verify(siblingTransaction).rollback();
        }
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        verify(firstPooledConnection).close();
        verify(secondPooledConnection).close();
    }
    
    @Test
    void assertStartSnapshotOnExistingAttachment() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("foo_unused", mock(StorageUnit.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        Connection pooledConnection = mock(Connection.class);
        FBConnection nativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(nativeConnection);
        when(nativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(nativeConnection.getFbDatabase().startTransaction(any())).thenReturn(mock(FbTransaction.class));
        attachmentSession.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", pooledConnection);
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.SERIALIZABLE);
            verify(nativeConnection.getFbDatabase()).startTransaction(any());
            ConnectionSession transactionSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId);
            assertTrue(transactionSession.getConnectionContext().getUsedDataSourceNames().isEmpty());
            Connection connection = transactionSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            try (Statement actual = connection.createStatement()) {
                assertThat(actual.getConnection(), sameInstance(connection));
            }
            verify(nativeConnection.getFbDatabase()).startTransaction(any());
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        }
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        verify(pooledConnection).close();
    }
    
    @Test
    void assertClosePendingSnapshotWithRollbackFailure() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        Connection pooledConnection = mock(Connection.class);
        FBConnection nativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(nativeConnection);
        when(nativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        FbTransaction snapshotTransaction = mock(FbTransaction.class);
        FbTransaction siblingTransaction = mock(FbTransaction.class);
        when(nativeConnection.getFbDatabase().startTransaction(any())).thenReturn(snapshotTransaction, siblingTransaction);
        doThrow(new SQLException("foo_rollback_failure")).doNothing().when(snapshotTransaction).rollback();
        ConnectionSession attachmentSession = createAttachmentSession();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("foo_unused", mock(StorageUnit.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        attachmentSession.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", pooledConnection);
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int snapshotId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            int siblingId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
            BackendConnectionException actual = assertThrows(BackendConnectionException.class, () -> FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, snapshotId));
            assertThat(actual.getExceptions().size(), is(1));
            assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, siblingId));
            ConnectionSession siblingSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, siblingId);
            siblingSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0).createStatement().close();
            new ProxyBackendTransactionManager(siblingSession.getDatabaseConnectionManager()).rollback();
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, siblingId);
            verify(pooledConnection, never()).close();
            attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
            verify(snapshotTransaction, times(2)).rollback();
        }
        verify(pooledConnection).close();
    }
    
    @Test
    void assertRoutedSnapshotsExcludeInactiveSessions() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, true, false, TransactionIsolationLevel.REPEATABLE_READ);
        int suspendedId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        ConnectionSession suspendedSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, suspendedId);
        new ProxyBackendTransactionManager(suspendedSession.getDatabaseConnectionManager()).commit();
        ConnectionSession nonLocalSession = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, "Firebird"), null);
        nonLocalSession.setConnectionId(CONNECTION_ID);
        nonLocalSession.setGrantee(mock(Grantee.class));
        nonLocalSession.setAutoCommit(false);
        nonLocalSession.setIsolationLevel(TransactionIsolationLevel.REPEATABLE_READ);
        nonLocalSession.getTransactionStatus().setInTransaction(true);
        nonLocalSession.getConnectionContext().getTransactionContext().beginTransaction(TransactionType.XA.name(), null);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(nonLocalSession);
        Connection pooledConnection = mock(Connection.class);
        FBConnection nativeConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(nativeConnection);
        when(nativeConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(nativeConnection.getFbDatabase().startTransaction(any())).thenReturn(mock(FbTransaction.class));
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), eq("foo_ds"), eq(1), eq(ConnectionMode.MEMORY_STRICTLY), any()))
                .thenReturn(Collections.singletonList(pooledConnection));
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int activeId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            ConnectionSession activeSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, activeId);
            activeSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0).createStatement().close();
            verify(nativeConnection.getFbDatabase()).startTransaction(any());
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, activeId);
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        verify(pooledConnection).close();
    }
    
    @Test
    void assertStartTransactionWithSingleStorageUnit() throws SQLException, BackendConnectionException {
        mockLocalTransactionRule();
        Connection pooledConnection = mock(Connection.class);
        FBConnection firebirdConnection = mock(FBConnection.class, RETURNS_DEEP_STUBS);
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(firebirdConnection);
        when(firebirdConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(firebirdConnection.getFbDatabase().startTransaction(any())).thenReturn(mock(FbTransaction.class));
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(eq("foo_db"), eq("foo_ds"), eq(1), eq(ConnectionMode.MEMORY_STRICTLY), any()))
                .thenReturn(Collections.singletonList(pooledConnection));
        ConnectionSession attachmentSession = createAttachmentSession();
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        try (MockedConstruction<FBConnection> ignored = mockConstruction(FBConnection.class, (mock, context) -> when(mock.createStatement()).thenReturn(mock(Statement.class)))) {
            int actualTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            verify(firebirdConnection.getFbDatabase()).startTransaction(any());
            Connection transactionConnection = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, actualTransactionId)
                    .getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            try (Statement actualStatement = transactionConnection.createStatement()) {
                assertThat(actualStatement.getConnection(), sameInstance(transactionConnection));
            }
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, actualTransactionId);
        }
        attachmentSession.getDatabaseConnectionManager().closeExecutionResources();
        verify(pooledConnection).close();
    }
    
    @Test
    void assertFirstUseAfterAttachmentClosed() throws SQLException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("foo_other_ds", mock(StorageUnit.class));
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        ConnectionSession transactionSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId);
        attachmentSession.getDatabaseConnectionManager().closeAllResources();
        SQLException actualException = assertThrows(SQLException.class,
                () -> transactionSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.MEMORY_STRICTLY));
        assertThat(actualException.getSQLState(), is("08003"));
        verify(ProxyContext.getInstance().getBackendDataSource(), never()).getConnections(anyString(), anyString(), eq(1), any(), any());
    }
    
    @Test
    void assertCloseTransactionRetainsSiblingAttachment() {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        int secondTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, firstTransactionId);
        assertTrue(attachmentSession.getTransactionStatus().isInTransaction());
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, secondTransactionId));
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, secondTransactionId);
        assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    void assertCloseLastTransactionWithCleanupFailure() throws SQLException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        Connection backendConnection = mock(Connection.class);
        SQLException expected = new SQLException("foo_failure");
        doThrow(expected).when(backendConnection).close();
        FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId).getDatabaseConnectionManager().getCachedConnections().put("foo_ds", backendConnection);
        BackendConnectionException actual = assertThrows(BackendConnectionException.class, () -> FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId));
        assertThat(actual.getExceptions(), contains(expected));
        assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
        assertFalse(attachmentSession.getConnectionContext().getTransactionContext().isTransactionStarted());
    }
    
    @Test
    void assertUnregisterReleasesAttachment() {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
        assertFalse(attachmentSession.getConnectionContext().getTransactionContext().isTransactionStarted());
    }
    
    @Test
    void assertStartTransactionAfterDisconnect() {
        ConnectionSession attachmentSession = createAttachmentSession();
        attachmentSession.getDatabaseConnectionManager().closeAllResources();
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        SQLException actual = assertThrows(SQLException.class,
                () -> FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ));
        assertThat(actual.getSQLState(), is("08003"));
        assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
    }
    
    @Test
    void assertDisconnectWaitsForTransactionStart() throws Exception {
        ConnectionSession attachmentSession = createAttachmentSession();
        CountDownLatch beginning = new CountDownLatch(1);
        CountDownLatch closing = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> disconnect = executor.submit(() -> {
                assertTrue(beginning.await(5, TimeUnit.SECONDS));
                closing.countDown();
                attachmentSession.getDatabaseConnectionManager().closeAllResources();
                FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
                return null;
            });
            try (MockedConstruction<ProxyBackendTransactionManager> ignored = mockConstruction(ProxyBackendTransactionManager.class, (mock, constructionContext) -> {
                ConnectionSession childSession = ((ProxyDatabaseConnectionManager) constructionContext.arguments().get(0)).getConnectionSession();
                doAnswer(invocation -> {
                    childSession.getTransactionStatus().setInTransaction(true);
                    childSession.getConnectionContext().getTransactionContext().beginTransaction(TransactionType.LOCAL.name(), null);
                    beginning.countDown();
                    assertTrue(closing.await(5, TimeUnit.SECONDS));
                    assertThrows(TimeoutException.class, () -> disconnect.get(100, TimeUnit.MILLISECONDS));
                    return null;
                }).when(mock).begin();
            })) {
                FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
            }
            disconnect.get(5, TimeUnit.SECONDS);
            assertFalse(attachmentSession.getTransactionStatus().isInTransaction());
            assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionId(CONNECTION_ID));
        } finally {
            beginning.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
    
    @Test
    void assertGetExecutionSessionWithoutAttachment() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.emptyList());
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        assertThat(FirebirdTransactionIdGenerator.getInstance().getTransactionSessionForExecution(CONNECTION_ID, transactionId), sameInstance(connectionSession));
    }
    
    @Test
    void assertGetExecutionSessionWithActiveTransaction() throws SQLException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        ConnectionSession transactionSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId);
        Connection backendConnection = mock(Connection.class);
        transactionSession.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", backendConnection);
        ConnectionSession actual = FirebirdTransactionIdGenerator.getInstance().getTransactionSessionForExecution(CONNECTION_ID, transactionId);
        assertThat(actual, sameInstance(transactionSession));
        assertTrue(actual.getTransactionStatus().isInTransaction());
        verify(backendConnection, never()).close();
    }
    
    @Test
    void assertGetExecutionSessionAfterImplicitCommit() throws SQLException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        int siblingTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
        ConnectionSession transactionSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId);
        Connection backendConnection = mock(Connection.class);
        transactionSession.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", backendConnection);
        new ProxyBackendTransactionManager(transactionSession.getDatabaseConnectionManager()).commit();
        assertFalse(transactionSession.getTransactionStatus().isInTransaction());
        ConnectionSession actual = FirebirdTransactionIdGenerator.getInstance().getTransactionSessionForExecution(CONNECTION_ID, transactionId);
        assertThat(actual, sameInstance(transactionSession));
        assertTrue(actual.getTransactionStatus().isInTransaction());
        assertFalse(actual.isReadOnly());
        assertTrue(FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, siblingTransactionId).getTransactionStatus().isInTransaction());
        assertThat(actual.getIsolationLevel().get(), is(TransactionIsolationLevel.REPEATABLE_READ));
        verify(backendConnection).close();
    }
    
    @Test
    void assertGetExecutionSessionWithResumeFailure() throws SQLException {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        int siblingTransactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.READ_COMMITTED);
        ConnectionSession transactionSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, transactionId);
        new ProxyBackendTransactionManager(transactionSession.getDatabaseConnectionManager()).commit();
        try (
                MockedConstruction<ProxyBackendTransactionManager> ignored = mockConstruction(ProxyBackendTransactionManager.class,
                        (mock, context) -> doThrow(new IllegalStateException("foo_failure")).when(mock).begin())) {
            assertThrows(IllegalStateException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionSessionForExecution(CONNECTION_ID, transactionId));
        }
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, siblingTransactionId));
        assertTrue(attachmentSession.getTransactionStatus().isInTransaction());
        FirebirdBlobHandleGenerator.getInstance().registerConnection(CONNECTION_ID);
        try {
            assertThat(FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(CONNECTION_ID), is(3));
        } finally {
            FirebirdBlobHandleGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertGetExecutionSessionAfterAttachmentClosed() {
        mockLocalTransactionRule();
        ConnectionSession attachmentSession = createAttachmentSession();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(attachmentSession, false, false, TransactionIsolationLevel.REPEATABLE_READ);
        attachmentSession.getDatabaseConnectionManager().closeAllResources();
        SQLException actual = assertThrows(SQLException.class, () -> FirebirdTransactionIdGenerator.getInstance().getTransactionSessionForExecution(CONNECTION_ID, transactionId));
        assertThat(actual.getSQLState(), is("08003"));
    }
}
