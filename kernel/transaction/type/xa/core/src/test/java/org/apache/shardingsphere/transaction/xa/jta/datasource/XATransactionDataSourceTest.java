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

package org.apache.shardingsphere.transaction.xa.jta.datasource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionOptionReplayCallback;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.postgresql.core.BaseConnection;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XATransactionDataSourceTest {
    
    @Mock
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private Transaction transaction;
    
    @BeforeEach
    void setUp() throws SystemException {
        when(xaTransactionManagerProvider.getTransactionManager()).thenReturn(transactionManager);
        when(transactionManager.getTransaction()).thenReturn(transaction);
    }
    
    @Test
    void assertGetAtomikosConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        try (Connection ignored = transactionDataSource.getConnection(mock())) {
            verify(xaTransactionManagerProvider, never()).getTransactionManager();
        }
    }
    
    @Test
    void assertCloseAtomikosConnectionWhenTransactionOptionReplayFailed() throws SQLException {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds12");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds12", dataSource, xaTransactionManagerProvider);
        AtomicReference<Connection> actualConnection = new AtomicReference<>();
        SQLException expectedException = new SQLException("replay transaction option failed");
        SQLException actualException = assertThrows(SQLException.class, () -> transactionDataSource.getConnection(actual -> {
            actualConnection.set(actual);
            throw expectedException;
        }));
        assertThat(actualException, sameInstance(expectedException));
        assertTrue(actualConnection.get().isClosed());
    }
    
    @Test
    void assertGetHikariConnection() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        TransactionOptionReplayCallback transactionOptionReplayCallback = mock(TransactionOptionReplayCallback.class);
        try (Connection actual = transactionDataSource.getConnection(transactionOptionReplayCallback)) {
            InOrder ordered = inOrder(transactionOptionReplayCallback, transaction);
            ordered.verify(transactionOptionReplayCallback).replay(actual);
            ordered.verify(transaction).enlistResource(any(SingleXAResource.class));
            verify(transaction).registerSynchronization(any(Synchronization.class));
        }
        try (Connection ignored = transactionDataSource.getConnection(mock())) {
            verify(transaction, times(2)).enlistResource(any(SingleXAResource.class));
            verify(transaction, times(2)).registerSynchronization(any(Synchronization.class));
        }
    }
    
    @Test
    void assertGetPostgreSQLHikariConnection() throws SQLException, RollbackException, SystemException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DataSource dataSource = spy(DataSourceUtils.build(HikariDataSource.class, databaseType, "ds1"));
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true, true, false);
        when(connection.unwrap(BaseConnection.class)).thenReturn(mock(BaseConnection.class));
        doReturn(connection).when(dataSource).getConnection();
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(databaseType, "ds1", dataSource, xaTransactionManagerProvider);
        TransactionOptionReplayCallback transactionOptionReplayCallback = mock(TransactionOptionReplayCallback.class);
        try (Connection actualConnection = transactionDataSource.getConnection(transactionOptionReplayCallback)) {
            assertThat(actualConnection, sameInstance(connection));
            InOrder ordered = inOrder(connection, transactionOptionReplayCallback, transaction);
            ordered.verify(transactionOptionReplayCallback).replay(connection);
            ordered.verify(connection).setAutoCommit(false);
            ordered.verify(transaction).enlistResource(any(SingleXAResource.class));
            ArgumentCaptor<Synchronization> synchronizationCaptor = ArgumentCaptor.forClass(Synchronization.class);
            ordered.verify(transaction).registerSynchronization(synchronizationCaptor.capture());
            synchronizationCaptor.getValue().afterCompletion(Status.STATUS_COMMITTED);
            ordered.verify(connection).setAutoCommit(true);
        }
    }
    
    @Test
    void assertGetPostgreSQLHikariConnectionWithAutoCommitDisabled() throws SQLException, RollbackException, SystemException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DataSource dataSource = spy(DataSourceUtils.build(HikariDataSource.class, databaseType, "ds1"));
        Connection connection = mock(Connection.class);
        when(connection.unwrap(BaseConnection.class)).thenReturn(mock(BaseConnection.class));
        doReturn(connection).when(dataSource).getConnection();
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(databaseType, "ds1", dataSource, xaTransactionManagerProvider);
        try (Connection ignored = transactionDataSource.getConnection(mock())) {
            ArgumentCaptor<Synchronization> synchronizationCaptor = ArgumentCaptor.forClass(Synchronization.class);
            verify(transaction).registerSynchronization(synchronizationCaptor.capture());
            synchronizationCaptor.getValue().afterCompletion(Status.STATUS_COMMITTED);
            verify(connection, never()).setAutoCommit(anyBoolean());
        }
    }
    
    @Test
    void assertCloseConnectionWhenTransactionOptionReplayFailed() throws SQLException, RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        AtomicReference<Connection> actualConnection = new AtomicReference<>();
        SQLException expectedException = new SQLException("replay transaction option failed");
        SQLException actualException = assertThrows(SQLException.class, () -> transactionDataSource.getConnection(actual -> {
            actualConnection.set(actual);
            throw expectedException;
        }));
        assertThat(actualException, sameInstance(expectedException));
        assertTrue(actualConnection.get().isClosed());
        verify(transaction, never()).enlistResource(any());
    }
    
    @Test
    void assertCloseAtomikosDataSourceBean() {
        DataSource dataSource = DataSourceUtils.build(AtomikosDataSourceBean.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds11");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds11", dataSource, xaTransactionManagerProvider);
        transactionDataSource.close();
        verify(xaTransactionManagerProvider, never()).removeRecoveryResource(anyString(), any(XADataSource.class));
    }
    
    @Test
    void assertCloseHikariDataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        transactionDataSource.close();
        verify(xaTransactionManagerProvider).removeRecoveryResource(anyString(), any(XADataSource.class));
    }
    
    @Test
    void assertCloseConnectionWhenEnlistResourceFailedWithSystemException() throws SystemException, RollbackException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        when(transaction.enlistResource(any())).thenThrow(new SystemException("enlist resource failed"));
        assertThrows(SystemException.class, () -> transactionDataSource.getConnection(mock()));
        verify(transaction).enlistResource(any(SingleXAResource.class));
    }
    
    @Test
    void assertCloseConnectionWhenEnlistResourceFailedWithRollbackException() throws RollbackException, SystemException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        when(transaction.enlistResource(any())).thenThrow(new RollbackException("enlist resource failed"));
        assertThrows(RollbackException.class, () -> transactionDataSource.getConnection(mock()));
        verify(transaction).enlistResource(any(SingleXAResource.class));
    }
    
    @Test
    void assertCloseConnectionWhenEnlistResourceFailedWithRuntimeException() throws SystemException, RollbackException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(TypedSPILoader.getService(DatabaseType.class, "H2"), "ds1", dataSource, xaTransactionManagerProvider);
        when(transaction.enlistResource(any())).thenThrow(new RuntimeException("enlist resource failed"));
        assertThrows(RuntimeException.class, () -> transactionDataSource.getConnection(mock()));
        verify(transaction).enlistResource(any(SingleXAResource.class));
    }
}
