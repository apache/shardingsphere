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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereConnectionTest {
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        TransactionTypeHolder.set(TransactionType.LOCAL);
        connection = new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, mockContextManager(), mock(JDBCContext.class));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class, RETURNS_DEEP_STUBS));
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(mock(TrafficRule.class));
        return result;
    }
    
    @After
    public void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    public void assertIsHoldTransaction() throws SQLException {
        connection.setAutoCommit(false);
        assertTrue(connection.isHoldTransaction());
    }
    
    @Test
    public void assertIsNotHoldTransaction() throws SQLException {
        connection.setAutoCommit(true);
        assertFalse(connection.isHoldTransaction());
    }
    
    @Test
    public void assertSetAutoCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(physicalConnection).setAutoCommit(true);
    }
    
    @Test
    public void assertSetAutoCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(true)).thenReturn(DistributedTransactionOperationType.COMMIT);
        mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(connectionTransaction).commit();
    }
    
    @Test
    public void assertCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(physicalConnection).setAutoCommit(false);
        connection.commit();
        assertFalse(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(physicalConnection).commit();
    }
    
    @Test
    public void assertCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        final ConnectionManager connectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.commit();
        assertFalse(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(connectionManager).commit();
    }
    
    @Test
    public void assertRollbackWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        connection.rollback();
        verify(physicalConnection).rollback();
    }
    
    @Test
    public void assertRollbackWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        final ConnectionManager connectionManager = mockConnectionManager(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(connectionTransaction).begin();
        connection.rollback();
        assertFalse(connection.getConnectionContext().getTransactionConnectionContext().isInTransaction());
        verify(connectionManager).rollback();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ConnectionManager mockConnectionManager(final ConnectionTransaction connectionTransaction) {
        Field field = connection.getClass().getDeclaredField("connectionManager");
        field.setAccessible(true);
        ConnectionManager result = mock(ConnectionManager.class);
        when(result.getConnectionTransaction()).thenReturn(connectionTransaction);
        field.set(connection, result);
        return result;
    }
    
    @Test
    public void assertIsValidWhenEmptyConnection() throws SQLException {
        assertTrue(connection.isValid(0));
    }
    
    @Test
    public void assertIsInvalid() throws SQLException {
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        assertFalse(connection.isValid(0));
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
        Connection physicalConnection = connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        connection.setReadOnly(true);
        assertTrue(connection.isReadOnly());
        verify(physicalConnection).setReadOnly(true);
    }
    
    @Test
    public void assertGetTransactionIsolationWithoutCachedConnections() throws SQLException {
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        Connection physicalConnection = connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(physicalConnection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @Test
    public void assertCreateArrayOf() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        assertNull(connection.createArrayOf("int", null));
        verify(physicalConnection).createArrayOf("int", null);
    }
    
    @Test
    public void assertClose() throws SQLException {
        connection.close();
        assertTrue(connection.isClosed());
    }
}
