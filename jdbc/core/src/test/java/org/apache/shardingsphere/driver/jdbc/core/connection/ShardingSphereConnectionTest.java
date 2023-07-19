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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereConnectionTest {
    
    private ShardingSphereConnection connection;
    
    @BeforeEach
    void setUp() {
        connection = new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new ShardingSphereRuleMetaData(Arrays.asList(mockTransactionRule(), mock(TrafficRule.class))));
        return result;
    }
    
    private TransactionRule mockTransactionRule() {
        return new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), "", new Properties()), Collections.emptyMap());
    }
    
    @AfterEach
    void clear() {
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    void assertIsHoldTransaction() throws SQLException {
        connection.setAutoCommit(false);
        assertTrue(connection.isHoldTransaction());
    }
    
    @Test
    void assertIsNotHoldTransaction() throws SQLException {
        connection.setAutoCommit(true);
        assertFalse(connection.isHoldTransaction());
    }
    
    @Test
    void assertSetAutoCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(physicalConnection).setAutoCommit(true);
    }
    
    @Test
    void assertSetAutoCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(true)).thenReturn(DistributedTransactionOperationType.COMMIT);
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(connectionTransaction).commit();
    }
    
    @Test
    void assertCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(physicalConnection).setAutoCommit(false);
        connection.commit();
        assertFalse(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(physicalConnection).commit();
    }
    
    @Test
    void assertCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertTrue(databaseConnectionManager.getConnectionContext().getTransactionContext().isInTransaction());
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.commit();
        assertFalse(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(databaseConnectionManager).commit();
    }
    
    @Test
    void assertRollbackWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        connection.rollback();
        verify(physicalConnection).rollback();
    }
    
    @Test
    void assertRollbackWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        when(connectionTransaction.getTransactionType()).thenReturn(TransactionType.XA);
        final DriverDatabaseConnectionManager databaseConnectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.rollback();
        assertFalse(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(databaseConnectionManager).rollback();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DriverDatabaseConnectionManager mockConnectionManager(final ConnectionTransaction connectionTransaction) {
        DriverDatabaseConnectionManager result = mock(DriverDatabaseConnectionManager.class);
        when(result.getConnectionTransaction()).thenReturn(connectionTransaction);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext());
        Plugins.getMemberAccessor().set(connection.getClass().getDeclaredField("databaseConnectionManager"), connection, result);
        return result;
    }
    
    @Test
    void assertIsValidWhenEmptyConnection() throws SQLException {
        assertTrue(connection.isValid(0));
    }
    
    @Test
    void assertIsInvalid() throws SQLException {
        connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertFalse(connection.isValid(0));
    }
    
    @Test
    void assertSetReadOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
        Connection physicalConnection = connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        connection.setReadOnly(true);
        assertTrue(connection.isReadOnly());
        verify(physicalConnection).setReadOnly(true);
    }
    
    @Test
    void assertGetTransactionIsolationWithoutCachedConnections() throws SQLException {
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
    }
    
    @Test
    void assertSetTransactionIsolation() throws SQLException {
        Connection physicalConnection = connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(physicalConnection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @Test
    void assertCreateArrayOf() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getDatabaseConnectionManager().getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertNull(connection.createArrayOf("int", null));
        verify(physicalConnection).createArrayOf("int", null);
    }
    
    @Test
    void assertPrepareCall() throws SQLException {
        CallableStatement expected = mock(CallableStatement.class);
        Connection physicalConnection = mock(Connection.class);
        when(physicalConnection.prepareCall("")).thenReturn(expected);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        CallableStatement actual = connection.prepareCall("");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertClose() throws SQLException {
        connection.close();
        assertTrue(connection.isClosed());
    }
}
