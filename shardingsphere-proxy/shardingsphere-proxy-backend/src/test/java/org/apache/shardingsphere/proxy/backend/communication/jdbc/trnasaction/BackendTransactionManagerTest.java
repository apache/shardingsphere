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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.trnasaction;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.LocalTransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
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
public final class BackendTransactionManagerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Mock
    private LocalTransactionManager localTransactionManager;
    
    @Mock
    private ShardingTransactionManager shardingTransactionManager;
    
    private BackendTransactionManager backendTransactionManager;
    
    @Before
    public void setUp() {
        setTransactionContexts();
        when(backendConnection.getSchemaName()).thenReturn("schema");
        when(backendConnection.getTransactionStatus()).thenReturn(transactionStatus);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setTransactionContexts() {
        Field transactionContexts = ProxyContext.getInstance().getClass().getDeclaredField("transactionContexts");
        transactionContexts.setAccessible(true);
        transactionContexts.set(ProxyContext.getInstance(), getTransactionContexts());
    }
    
    private TransactionContexts getTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        ShardingTransactionManagerEngine transactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        when(result.getEngines().get("schema")).thenReturn(transactionManagerEngine);
        when(transactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(shardingTransactionManager);
        return result;
    }
    
    @Test
    public void assertBeginForLocalTransaction() {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.begin();
        verify(transactionStatus).setInTransaction(true);
        verify(backendConnection).releaseConnections(false);
        verify(localTransactionManager).begin();
    }
    
    @Test
    public void assertBeginForDistributedTransaction() {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.begin();
        verify(transactionStatus, times(0)).setInTransaction(true);
        verify(backendConnection, times(0)).releaseConnections(false);
        verify(shardingTransactionManager).begin();
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
        verify(shardingTransactionManager).commit();
    }
    
    @Test
    public void assertCommitWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.commit();
        verify(transactionStatus, times(0)).setInTransaction(false);
        verify(localTransactionManager, times(0)).commit();
        verify(shardingTransactionManager, times(0)).commit();
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
        verify(shardingTransactionManager).rollback();
    }
    
    @Test
    public void assertRollbackWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.rollback();
        verify(transactionStatus, times(0)).setInTransaction(false);
        verify(localTransactionManager, times(0)).rollback();
        verify(shardingTransactionManager, times(0)).rollback();
    }
    
    private void newBackendTransactionManager(final TransactionType transactionType, final boolean inTransaction) {
        when(backendConnection.getTransactionType()).thenReturn(transactionType);
        when(transactionStatus.isInTransaction()).thenReturn(inTransaction);
        backendTransactionManager = new BackendTransactionManager(backendConnection);
        setLocalTransactionManager();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setLocalTransactionManager() {
        Field field = BackendTransactionManager.class.getDeclaredField("localTransactionManager");
        field.setAccessible(true);
        field.set(backendTransactionManager, localTransactionManager);
    }
}
