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
import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.infra.connection.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

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
        TransactionTypeHolder.set(TransactionType.LOCAL);
        connection = new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, mockContextManager(), mock(JDBCContext.class));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new ShardingSphereRuleMetaData(Arrays.asList(mock(TransactionRule.class, RETURNS_DEEP_STUBS), mock(TrafficRule.class))));
        return result;
    }
    
    @AfterEach
    void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
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
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(physicalConnection).setAutoCommit(true);
    }
    
    @Test
    void assertSetAutoCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(true)).thenReturn(DistributedTransactionOperationType.COMMIT);
        mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(connectionTransaction).commit();
    }
    
    @Test
    void assertCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(physicalConnection).setAutoCommit(false);
        connection.commit();
        assertFalse(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(physicalConnection).commit();
    }
    
    @Test
    void assertCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        ConnectionManager connectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertTrue(connectionManager.getConnectionContext().getTransactionContext().isInTransaction());
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.commit();
        assertFalse(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionManager).commit();
    }
    
    @Test
    void assertRollbackWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        connection.rollback();
        verify(physicalConnection).rollback();
    }
    
    @Test
    void assertRollbackWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        final ConnectionManager connectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.rollback();
        assertFalse(connection.getConnectionManager().getConnectionContext().getTransactionContext().isInTransaction());
        verify(connectionManager).rollback();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ConnectionManager mockConnectionManager(final ConnectionTransaction connectionTransaction) {
        ConnectionManager result = mock(ConnectionManager.class);
        when(result.getConnectionTransaction()).thenReturn(connectionTransaction);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext());
        Plugins.getMemberAccessor().set(connection.getClass().getDeclaredField("connectionManager"), connection, result);
        return result;
    }
    
    @Test
    void assertIsValidWhenEmptyConnection() throws SQLException {
        assertTrue(connection.isValid(0));
    }
    
    @Test
    void assertIsInvalid() throws SQLException {
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        assertFalse(connection.isValid(0));
    }
    
    @Test
    void assertSetReadOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
        Connection physicalConnection = connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY).get(0);
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
        Connection physicalConnection = connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(physicalConnection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @Test
    void assertCreateArrayOf() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        assertNull(connection.createArrayOf("int", null));
        verify(physicalConnection).createArrayOf("int", null);
    }
    
    @Test
    void assertClose() throws SQLException {
        connection.close();
        assertTrue(connection.isClosed());
    }
}
