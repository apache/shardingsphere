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

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.TransactionHolder;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereConnectionTest {
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        connection = new ShardingSphereConnection(DefaultSchema.LOGIC_NAME, mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceMap(DefaultSchema.LOGIC_NAME)).thenReturn(Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
        when(result.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.empty());
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
    public void assertGetRandomPhysicalDataSourceNameFromContextManager() {
        String actual = connection.getRandomPhysicalDataSourceName();
        assertThat(actual, is("ds"));
    }
    
    @Test
    public void assertGetRandomPhysicalDataSourceNameFromCache() throws SQLException {
        connection.getConnection("ds");
        String actual = connection.getRandomPhysicalDataSourceName();
        assertThat(actual, is("ds"));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertThat(connection.getConnection("ds"), is(connection.getConnection("ds")));
    }
    
    @Test
    public void assertGetConnectionsWhenAllInCache() throws SQLException {
        Connection expected = connection.getConnection("ds");
        List<Connection> actual = connection.getConnections("ds", 1, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));
    }
    
    @Test
    public void assertGetConnectionsWhenEmptyCache() throws SQLException {
        List<Connection> actual = connection.getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGetConnectionsWhenPartInCacheWithMemoryStrictlyMode() throws SQLException {
        connection.getConnection("ds");
        List<Connection> actual = connection.getConnections("ds", 3, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    public void assertGetConnectionsWhenPartInCacheWithConnectionStrictlyMode() throws SQLException {
        connection.getConnection("ds");
        List<Connection> actual = connection.getConnections("ds", 3, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    public void assertGetConnectionsWhenConnectionCreateFailed() throws SQLException {
        when(connection.getContextManager().getDataSourceMap(DefaultSchema.LOGIC_NAME).get("ds").getConnection()).thenThrow(new SQLException());
        try {
            connection.getConnections("ds", 3, ConnectionMode.CONNECTION_STRICTLY);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("Can not get 3 connections one time, partition succeed connection(0) have released!"));
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
        when(connection.getContextManager().getDataSourceMap(DefaultSchema.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnection("ds");
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(physicalConnection).setAutoCommit(true);
    }
    
    @Test
    public void assertSetAutoCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(true)).thenReturn(DistributedTransactionOperationType.COMMIT);
        setConnectionTransaction(connectionTransaction);
        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        verify(connectionTransaction).commit();
    }
    
    @Test
    public void assertCommitWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultSchema.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnection("ds");
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(TransactionHolder.isTransaction());
        verify(physicalConnection).setAutoCommit(false);
        connection.commit();
        assertFalse(TransactionHolder.isTransaction());
        verify(physicalConnection).commit();
    }
    
    @Test
    public void assertCommitWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        setConnectionTransaction(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(TransactionHolder.isTransaction());
        verify(connectionTransaction).begin();
        connection.commit();
        assertFalse(TransactionHolder.isTransaction());
        verify(connectionTransaction).commit();
    }
    
    @Test
    public void assertRollbackWithLocalTransaction() throws SQLException {
        Connection physicalConnection = mock(Connection.class);
        when(connection.getContextManager().getDataSourceMap(DefaultSchema.LOGIC_NAME).get("ds").getConnection()).thenReturn(physicalConnection);
        connection.getConnection("ds");
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        connection.rollback();
        verify(physicalConnection).rollback();
    }
    
    @Test
    public void assertRollbackWithDistributedTransaction() throws SQLException {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.getDistributedTransactionOperationType(false)).thenReturn(DistributedTransactionOperationType.BEGIN);
        setConnectionTransaction(connectionTransaction);
        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertTrue(TransactionHolder.isTransaction());
        verify(connectionTransaction).begin();
        connection.rollback();
        assertFalse(TransactionHolder.isTransaction());
        verify(connectionTransaction).rollback();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionTransaction(final ConnectionTransaction connectionTransaction) {
        Field field = connection.getClass().getDeclaredField("connectionTransaction");
        field.setAccessible(true);
        field.set(connection, connectionTransaction);
    }
    
    @Test
    public void assertIsValidWhenEmptyConnection() throws SQLException {
        assertTrue(connection.isValid(0));
    }
    
    @Test
    public void assertIsInvalid() throws SQLException {
        connection.getConnection("ds");
        assertFalse(connection.isValid(0));
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
        Connection physicalConnection = connection.getConnection("ds");
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
        Connection physicalConnection = connection.getConnection("ds");
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(physicalConnection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Multimap<String, Connection> getCachedConnections(final ShardingSphereConnection shardingSphereConnection) {
        Field field = ShardingSphereConnection.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        return (Multimap<String, Connection>) field.get(shardingSphereConnection);
    }
    
    @Test
    public void assertClose() throws SQLException {
        connection.close();
        assertTrue(connection.isClosed());
        assertTrue(getCachedConnections(connection).isEmpty());
    }
    
    @Test
    public void assertCloseShouldNotClearTransactionType() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        connection.close();
        assertTrue(connection.isClosed());
        assertTrue(getCachedConnections(connection).isEmpty());
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
    }
}
