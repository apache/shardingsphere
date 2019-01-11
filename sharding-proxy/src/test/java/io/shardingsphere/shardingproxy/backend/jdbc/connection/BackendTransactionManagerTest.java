/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.transaction.api.TransactionType;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackendTransactionManagerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    private BackendTransactionManager backendTransactionManager = new BackendTransactionManager(backendConnection);
    
    @Test
    public void assertLocalTransactionCommit() throws SQLException {
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 2);
        backendTransactionManager.begin();
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
        assertThat(backendConnection.getMethodInvocations().size(), is(1));
        assertThat(backendConnection.getMethodInvocations().iterator().next().getArguments(), is(new Object[]{false}));
        assertTrue(backendConnection.getCachedConnections().isEmpty());
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 2);
        backendTransactionManager.commit();
        Iterator<Connection> iterator = backendConnection.getCachedConnections().values().iterator();
        verify(iterator.next()).commit();
        verify(iterator.next()).commit();
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertLocalTransactionCommitWithException() throws SQLException {
        backendTransactionManager.begin();
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 2);
        MockConnectionUtil.mockThrowException(backendConnection.getCachedConnections().values());
        try {
            backendTransactionManager.commit();
        } catch (final SQLException ex) {
            assertThat(ex.getNextException().getNextException(), instanceOf(SQLException.class));
        }
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertLocalTransactionRollback() throws SQLException {
        backendTransactionManager.begin();
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 2);
        backendTransactionManager.rollback();
        Iterator<Connection> iterator = backendConnection.getCachedConnections().values().iterator();
        verify(iterator.next()).rollback();
        verify(iterator.next()).rollback();
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertLocalTransactionRollbackWithException() throws SQLException {
        backendTransactionManager.begin();
        MockConnectionUtil.setCachedConnections(backendConnection, "ds1", 2);
        MockConnectionUtil.mockThrowException(backendConnection.getCachedConnections().values());
        try {
            backendTransactionManager.rollback();
        } catch (final SQLException ex) {
            assertThat(ex.getNextException().getNextException(), instanceOf(SQLException.class));
        }
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertXATransactionCommit() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        backendConnection.setTransactionType(TransactionType.XA);
        backendTransactionManager.begin();
        assertTrue(backendConnection.getMethodInvocations().isEmpty());
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
        backendTransactionManager.commit();
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
        backendTransactionManager.begin();
    }
    
    @Test
    public void assertXATransactionRollback() throws SQLException {
        backendConnection.setCurrentSchema("schema");
        backendConnection.setTransactionType(TransactionType.XA);
        backendTransactionManager.begin();
        assertTrue(backendConnection.getMethodInvocations().isEmpty());
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
        backendTransactionManager.rollback();
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertBASETransactionCommit() throws SQLException {
        LogicSchema logicSchema = mock(LogicSchema.class);
        when(logicSchema.getBackendDataSource()).thenReturn(mock(JDBCBackendDataSource.class));
        GlobalRegistry.getInstance().getLogicSchemas().put("schema", logicSchema);
        backendConnection.setCurrentSchema("schema");
        backendConnection.setTransactionType(TransactionType.BASE);
        backendTransactionManager.doInTransaction(TransactionOperationType.BEGIN);
        assertTrue(backendConnection.getMethodInvocations().isEmpty());
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
        backendTransactionManager.doInTransaction(TransactionOperationType.COMMIT);
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
    
    @Test
    public void assertBASETransactionRollback() throws SQLException {
        LogicSchema logicSchema = mock(LogicSchema.class);
        when(logicSchema.getBackendDataSource()).thenReturn(mock(JDBCBackendDataSource.class));
        GlobalRegistry.getInstance().getLogicSchemas().put("schema", logicSchema);
        backendConnection.setCurrentSchema("schema");
        backendConnection.setTransactionType(TransactionType.BASE);
        backendTransactionManager.doInTransaction(TransactionOperationType.BEGIN);
        assertTrue(backendConnection.getMethodInvocations().isEmpty());
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TRANSACTION));
        backendTransactionManager.doInTransaction(TransactionOperationType.ROLLBACK);
        assertThat(backendConnection.getStateHandler().getStatus(), is(ConnectionStatus.TERMINATED));
    }
}
