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

package org.apache.shardingsphere.driver.jdbc.core.connection;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereConnectionTest {
    
    @Test
    void assertSetAutoCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager(physicalConnection))) {
            connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
        }
        verify(physicalConnection).setAutoCommit(true);
    }
    
    @Test
    void assertSetAutoCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(true)).thenReturn(Optional.of(DistributedTransactionOperationType.COMMIT));
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            DriverDatabaseConnectionManager connectionManager = mockConnectionManager(connection, connectionTransaction);
            connection.setAutoCommit(true);
            assertTrue(connection.getAutoCommit());
            verify(connectionManager).commit();
        }
    }
    
    @Test
    void assertCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager(physicalConnection))) {
            connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
            connection.setAutoCommit(false);
            assertFalse(connection.getAutoCommit());
            assertTrue(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
            verify(physicalConnection).setAutoCommit(false);
            connection.commit();
            assertFalse(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
            verify(physicalConnection).commit();
        }
    }
    
    @Test
    void assertCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(Optional.of(DistributedTransactionOperationType.BEGIN));
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, connectionTransaction);
            connection.setAutoCommit(false);
            assertFalse(connection.getAutoCommit());
            verify(databaseConnectionManager).begin();
            connection.commit();
            verify(databaseConnectionManager).commit();
        }
    }
    
    @Test
    void assertRollbackWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager(physicalConnection))) {
            connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
            connection.setAutoCommit(false);
            assertFalse(connection.getAutoCommit());
            connection.rollback();
        }
        verify(physicalConnection).rollback();
    }
    
    @Test
    void assertRollbackWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(Optional.of(DistributedTransactionOperationType.BEGIN));
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            final DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, connectionTransaction);
            connection.setAutoCommit(false);
            assertFalse(connection.getAutoCommit());
            verify(databaseConnectionManager).begin();
            connection.rollback();
            verify(databaseConnectionManager).rollback();
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DriverDatabaseConnectionManager mockConnectionManager(final ShardingSphereConnection connection, final ConnectionTransaction connectionTransaction) {
        DriverDatabaseConnectionManager result = mock(DriverDatabaseConnectionManager.class);
        when(result.getConnectionTransaction()).thenReturn(connectionTransaction);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptySet));
        Plugins.getMemberAccessor().set(ShardingSphereConnection.class.getDeclaredField("databaseConnectionManager"), connection, result);
        return result;
    }
    
    @Test
    void assertIsValidWhenEmptyConnection() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            assertTrue(connection.isValid(0));
        }
    }
    
    @Test
    void assertIsInvalid() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
            assertFalse(connection.isValid(0));
        }
    }
    
    @Test
    void assertSetReadOnly() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            assertFalse(connection.isReadOnly());
            Connection physicalConnection = connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            connection.setReadOnly(true);
            assertTrue(connection.isReadOnly());
            verify(physicalConnection).setReadOnly(true);
        }
    }
    
    @Test
    void assertGetTransactionIsolationWithoutCachedConnections() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
        }
        
    }
    
    @Test
    void assertSetTransactionIsolation() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            Connection physicalConnection = connection.getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            verify(physicalConnection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }
    }
    
    @Test
    void assertClose() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManager())) {
            connection.close();
            assertTrue(connection.isClosed());
        }
    }
    
    private ContextManager mockContextManager() {
        return mockContextManager(new MockedDataSource());
    }
    
    private ContextManager mockContextManager(final Connection connection) {
        return mockContextManager(new MockedDataSource(connection));
    }
    
    private ContextManager mockContextManager(final DataSource dataSource) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        when(result.getStorageUnits("foo_db")).thenReturn(Collections.singletonMap("ds", storageUnit));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mockTransactionRule())));
        return result;
    }
    
    private TransactionRule mockTransactionRule() {
        return new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), "", new Properties()), Collections.emptyList());
    }
}
