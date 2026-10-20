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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
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
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, connectionTransaction);
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
    void assertRollbackSavepointWithDroppedDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
            mockDroppedDatabase(contextManager, expected);
            assertThat(assertThrows(UnknownDatabaseException.class, () -> connection.rollback(mock(Savepoint.class))), sameInstance(expected));
        }
    }
    
    @Test
    void assertSetNamedSavepointWithDroppedDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
            mockDroppedDatabase(contextManager, expected);
            assertThat(assertThrows(UnknownDatabaseException.class, () -> connection.setSavepoint("foo_savepoint")), sameInstance(expected));
        }
    }
    
    @Test
    void assertSetSavepointWithDroppedDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
            mockDroppedDatabase(contextManager, expected);
            assertThat(assertThrows(UnknownDatabaseException.class, connection::setSavepoint), sameInstance(expected));
        }
    }
    
    @Test
    void assertReleaseSavepointWithDroppedDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
            mockDroppedDatabase(contextManager, expected);
            assertThat(assertThrows(UnknownDatabaseException.class, () -> connection.releaseSavepoint(mock(Savepoint.class))), sameInstance(expected));
        }
    }
    
    private void mockDroppedDatabase(final ContextManager contextManager, final UnknownDatabaseException ex) {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
        when(contextManager.getDatabase("foo_db")).thenThrow(ex);
    }
    
    @Test
    void assertSetSavepointOutsidePostgreSQLTransaction() throws SQLException {
        ContextManager contextManager = mockContextManagerWithDatabaseType("PostgreSQL");
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema()).thenReturn(Optional.of("public"));
        try (
                ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager);
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class,
                        (registry, context) -> when(registry.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData))) {
            SQLFeatureNotSupportedException actual = assertThrows(SQLFeatureNotSupportedException.class, connection::setSavepoint);
            assertThat(actual.getMessage(), is("Savepoint can only be used in transaction blocks"));
        }
    }
    
    @Test
    void assertSetNamedSavepointOutsideMySQLTransaction() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManagerWithDatabaseType("MySQL"))) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, mock(ConnectionTransaction.class));
            Savepoint expected = mock(Savepoint.class);
            when(databaseConnectionManager.setSavepoint("foo_savepoint")).thenReturn(expected);
            assertThat(connection.setSavepoint("foo_savepoint"), sameInstance(expected));
        }
    }
    
    @Test
    void assertSetSavepointOutsideMySQLTransaction() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManagerWithDatabaseType("MySQL"))) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, mock(ConnectionTransaction.class));
            Savepoint expected = mock(Savepoint.class);
            when(databaseConnectionManager.setSavepoint()).thenReturn(expected);
            assertThat(connection.setSavepoint(), sameInstance(expected));
        }
    }
    
    @Test
    void assertRollbackSavepointWithSQLException() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManagerWithDatabaseType("MySQL"))) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, mock(ConnectionTransaction.class));
            Savepoint savepoint = mock(Savepoint.class);
            SQLException expected = new SQLException("foo_savepoint failure");
            doThrow(expected).when(databaseConnectionManager).rollback(savepoint);
            assertThat(assertThrows(SQLException.class, () -> connection.rollback(savepoint)), sameInstance(expected));
        }
    }
    
    @Test
    void assertReleaseSavepointOutsideMySQLTransaction() throws SQLException {
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", mockContextManagerWithDatabaseType("MySQL"))) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connection, mock(ConnectionTransaction.class));
            Savepoint savepoint = mock(Savepoint.class);
            connection.releaseSavepoint(savepoint);
            verify(databaseConnectionManager, never()).releaseSavepoint(savepoint);
        }
    }
    
    private ContextManager mockContextManagerWithDatabaseType(final String databaseType) {
        ContextManager result = mockContextManager();
        when(result.getDatabase("foo_db").getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, databaseType));
        return result;
    }
    
    @Test
    void assertRollbackSavepointInTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManagerInTransaction(connection);
            Savepoint savepoint = mock(Savepoint.class);
            connection.rollback(savepoint);
            verify(databaseConnectionManager).rollback(savepoint);
            verify(contextManager, never()).getDatabase("foo_db");
        }
    }
    
    @Test
    void assertSetNamedSavepointInTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManagerInTransaction(connection);
            Savepoint expected = mock(Savepoint.class);
            when(databaseConnectionManager.setSavepoint("foo_savepoint")).thenReturn(expected);
            assertThat(connection.setSavepoint("foo_savepoint"), sameInstance(expected));
            verify(contextManager, never()).getDatabase("foo_db");
        }
    }
    
    @Test
    void assertSetSavepointInTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManagerInTransaction(connection);
            Savepoint expected = mock(Savepoint.class);
            when(databaseConnectionManager.setSavepoint()).thenReturn(expected);
            assertThat(connection.setSavepoint(), sameInstance(expected));
            verify(contextManager, never()).getDatabase("foo_db");
        }
    }
    
    @Test
    void assertReleaseSavepointInTransaction() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManagerInTransaction(connection);
            Savepoint savepoint = mock(Savepoint.class);
            connection.releaseSavepoint(savepoint);
            verify(databaseConnectionManager).releaseSavepoint(savepoint);
            verify(contextManager, never()).getDatabase("foo_db");
        }
    }
    
    private DriverDatabaseConnectionManager mockConnectionManagerInTransaction(final ShardingSphereConnection connection) {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.isHoldTransaction(true)).thenReturn(true);
        return mockConnectionManager(connection, connectionTransaction);
    }
    
    @Test
    void assertSetSavepointOnClosedConnection() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (ShardingSphereConnection connection = new ShardingSphereConnection("foo_db", contextManager)) {
            connection.close();
            SQLException actual = assertThrows(SQLException.class, connection::setSavepoint);
            assertThat(actual.getMessage(), is("Connection has been closed."));
            verify(contextManager, never()).getDatabase("foo_db");
        }
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
