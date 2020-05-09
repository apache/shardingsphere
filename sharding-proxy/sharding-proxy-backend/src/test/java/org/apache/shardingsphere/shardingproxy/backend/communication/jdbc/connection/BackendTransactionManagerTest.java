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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BackendTransactionManagerTest {
    
    @Mock
    private LogicSchema logicSchema;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private ConnectionStateHandler stateHandler;
    
    @Mock
    private LocalTransactionManager localTransactionManager;
    
    @Mock
    private ShardingTransactionManager shardingTransactionManager;
    
    private BackendTransactionManager backendTransactionManager;
    
    @Before
    public void setUp() {
        ShardingTransactionManagerEngine shardingTransactionManagerEngine = mock(ShardingTransactionManagerEngine.class);
        when(shardingTransactionManagerEngine.getTransactionManager(TransactionType.XA)).thenReturn(shardingTransactionManager);
        JDBCBackendDataSource backendDataSource = mock(JDBCBackendDataSource.class);
        when(backendDataSource.getShardingTransactionManagerEngine()).thenReturn(shardingTransactionManagerEngine);
        when(logicSchema.getBackendDataSource()).thenReturn(backendDataSource);
        when(backendConnection.getLogicSchema()).thenReturn(logicSchema);
        when(backendConnection.getStateHandler()).thenReturn(stateHandler);
    }
    
    @Test
    public void assertBeginForLocalTransaction() {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.begin();
        verify(stateHandler).setStatus(ConnectionStatus.TRANSACTION);
        verify(backendConnection).releaseConnections(false);
        verify(localTransactionManager).begin();
    }
    
    @Test
    public void assertBeginForDistributedTransaction() {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.begin();
        verify(stateHandler, times(0)).setStatus(ConnectionStatus.TRANSACTION);
        verify(backendConnection, times(0)).releaseConnections(false);
        verify(shardingTransactionManager).begin();
    }
    
    @Test
    public void assertCommitForLocalTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, true);
        backendTransactionManager.commit();
        verify(stateHandler).setStatus(ConnectionStatus.TERMINATED);
        verify(localTransactionManager).commit();
    }
    
    @Test
    public void assertCommitForDistributedTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.commit();
        verify(stateHandler).setStatus(ConnectionStatus.TERMINATED);
        verify(shardingTransactionManager).commit();
    }
    
    @Test
    public void assertCommitWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.commit();
        verify(stateHandler, times(0)).setStatus(ConnectionStatus.TERMINATED);
        verify(localTransactionManager, times(0)).commit();
        verify(shardingTransactionManager, times(0)).commit();
    }
    
    @Test
    public void assertRollbackForLocalTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, true);
        backendTransactionManager.rollback();
        verify(stateHandler).setStatus(ConnectionStatus.TERMINATED);
        verify(localTransactionManager).rollback();
    }
    
    @Test
    public void assertRollbackForDistributedTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.XA, true);
        backendTransactionManager.rollback();
        verify(stateHandler).setStatus(ConnectionStatus.TERMINATED);
        verify(shardingTransactionManager).rollback();
    }
    
    @Test
    public void assertRollbackWithoutTransaction() throws SQLException {
        newBackendTransactionManager(TransactionType.LOCAL, false);
        backendTransactionManager.rollback();
        verify(stateHandler, times(0)).setStatus(ConnectionStatus.TERMINATED);
        verify(localTransactionManager, times(0)).rollback();
        verify(shardingTransactionManager, times(0)).rollback();
    }
    
    private void newBackendTransactionManager(final TransactionType transactionType, final boolean inTransaction) {
        when(backendConnection.getTransactionType()).thenReturn(transactionType);
        when(stateHandler.isInTransaction()).thenReturn(inTransaction);
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
