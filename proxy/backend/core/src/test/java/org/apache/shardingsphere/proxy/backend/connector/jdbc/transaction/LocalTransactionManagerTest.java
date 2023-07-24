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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalTransactionManagerTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Mock
    private Connection connection;
    
    private LocalTransactionManager localTransactionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(setCachedConnections());
        when(transactionStatus.isInTransaction()).thenReturn(true);
        localTransactionManager = new LocalTransactionManager(databaseConnectionManager);
    }
    
    private Multimap<String, Connection> setCachedConnections() {
        Multimap<String, Connection> result = HashMultimap.create();
        List<Connection> connections = new ArrayList<>(1);
        connections.add(connection);
        result.putAll("ds1", connections);
        return result;
    }
    
    @Test
    void assertBegin() {
        localTransactionManager.begin();
        verify(databaseConnectionManager).getConnectionPostProcessors();
    }
    
    @Test
    void assertCommit() throws SQLException {
        localTransactionManager.commit();
        verify(transactionStatus).isExceptionOccur();
        verify(connection).commit();
    }
    
    @Test
    void assertRollback() throws SQLException {
        localTransactionManager.rollback();
        verify(transactionStatus).isInTransaction();
        verify(connection).rollback();
    }
}
