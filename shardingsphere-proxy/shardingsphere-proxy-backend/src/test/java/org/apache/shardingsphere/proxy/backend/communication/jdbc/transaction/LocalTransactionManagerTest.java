
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
import org.apache.shardingsphere.infra.transaction.TransactionHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LocalTransactionManagerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private LocalTransactionManager localTransactionManager;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Before
    public void setUp() {
        when(backendConnection.getTransactionStatus()).thenReturn(transactionStatus);
    }
    
    @Test
    public void assertBegin() {
        if (!backendConnection.getTransactionStatus().isInTransaction()) {
            backendConnection.getTransactionStatus().setInTransaction(true);
            TransactionHolder.setInTransaction();
            backendConnection.closeConnections(false);
        }
        localTransactionManager.begin();
        verify(localTransactionManager).begin();
        verify(transactionStatus).setInTransaction(true);
        verify(backendConnection).closeConnections(false);
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertCommit() {
        if (backendConnection.getTransactionStatus().isInTransaction()) {
            try {
                localTransactionManager.commit();
                verify(localTransactionManager).commit();
            } finally {
                backendConnection.getTransactionStatus().setInTransaction(false);
                TransactionHolder.clear();
            }
        }
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertRollback() {
        if (backendConnection.getTransactionStatus().isInTransaction()) {
            try {
                localTransactionManager.rollback();
                verify(localTransactionManager).rollback();
            } finally {
                backendConnection.getTransactionStatus().setInTransaction(false);
                TransactionHolder.clear();
            }
        }
    }
}
