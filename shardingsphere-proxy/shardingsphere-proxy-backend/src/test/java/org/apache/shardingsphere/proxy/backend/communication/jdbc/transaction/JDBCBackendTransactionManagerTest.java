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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction;

import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCBackendTransactionManagerTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Mock
    private LocalTransactionManager localTransactionManager;
    
    @Mock
    private ShardingSphereTransactionManager shardingSphereTransactionManager;
    
    private JDBCBackendTransactionManager backendTransactionManager;
    
    @Before
    public void setUp() {
        setTransactionContexts();
        when(connectionSession.getSchemaName()).thenReturn("schema");
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setTransactionContexts() {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TransactionContexts transactionContexts = mockTransactionContexts();
        when(contextManager.getTransactionContexts()).thenReturn(transactionContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private TransactionContexts mockTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereTransactionManagerEngine transactionManagerEngine = mock(ShardingSphereTransactionManagerEngine.class);
        when(result.getEngines().get("schema")).thenReturn(transactionManagerEngine);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(shardingSphereTransactionManager);
        return result;
    }
    
    @Test
    public void assertBeginForLocalTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.begin();
        verify(transactionStatus).setInTransaction(true);
        verify(backendConnection).closeDatabaseCommunicationEngines(true);
        verify(backendConnection).closeConnections(false);
        verify(localTransactionManager).begin();
    }
    
    @Test
    public void assertBeginForDistributedTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.begin();
        verify(transactionStatus, times(0)).setInTransaction(true);
        verify(backendConnection, times(0)).closeConnections(false);
        verify(shardingSphereTransactionManager).begin();
    }
    
    @Test
    public void assertCommitForLocalTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, true);
        backendTransactionManager.commit();
        verify(transactionStatus).setInTransaction(false);
        verify(localTransactionManager).commit();
    }
    
    @Test
    public void assertCommitForDistributedTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.commit();
        verify(transactionStatus).setInTransaction(false);
        verify(shardingSphereTransactionManager).commit(false);
    }
    
    @Test
    public void assertCommitWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.commit();
        verify(transactionStatus, times(0)).setInTransaction(false);
        verify(localTransactionManager, times(0)).commit();
        verify(shardingSphereTransactionManager, times(0)).commit(false);
    }
    
    @Test
    public void assertRollbackForLocalTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, true);
        backendTransactionManager.rollback();
        verify(transactionStatus).setInTransaction(false);
        verify(localTransactionManager).rollback();
    }
    
    @Test
    public void assertRollbackForDistributedTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.rollback();
        verify(transactionStatus).setInTransaction(false);
        verify(shardingSphereTransactionManager).rollback();
    }
    
    @Test
    public void assertRollbackWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.rollback();
        verify(transactionStatus, times(0)).setInTransaction(false);
        verify(localTransactionManager, times(0)).rollback();
        verify(shardingSphereTransactionManager, times(0)).rollback();
    }
    
    private void newBackendTransactionManager(final TransactionType transactionType, final boolean inTransaction) {
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(transactionType);
        when(transactionStatus.isInTransaction()).thenReturn(inTransaction);
        backendTransactionManager = new JDBCBackendTransactionManager(backendConnection);
        setLocalTransactionManager();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLocalTransactionManager() {
        Field field = JDBCBackendTransactionManager.class.getDeclaredField("localTransactionManager");
        field.setAccessible(true);
        field.set(backendTransactionManager, localTransactionManager);
    }
}
