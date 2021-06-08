
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LocalTransactionManagerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private TransactionStatus transactionStatus;
    
    @Mock
    private Connection connection;
    
    private LocalTransactionManager localTransactionManager;
    
    @Before
    public void setUp() throws SQLException {
        when(backendConnection.getTransactionStatus()).thenReturn(transactionStatus);
        when(backendConnection.getCachedConnections()).thenReturn(setCachedConnections());
        when(transactionStatus.isInTransaction()).thenReturn(true);
        localTransactionManager = new LocalTransactionManager(backendConnection);
    }
    
    private Multimap<String, Connection> setCachedConnections() {
        Multimap<String, Connection> result = HashMultimap.create();
        List<Connection> connections = new ArrayList<>(1);
        connections.add(connection);
        result.putAll("ds1", connections);
        return result;
    }
    
    @Test
    public void assertBegin() {
        localTransactionManager.begin();
        verify(backendConnection).getConnectionPostProcessors();
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertCommit() {
        localTransactionManager.commit();
        verify(transactionStatus).isInTransaction();
        verify(connection).commit();
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertRollback() {
        localTransactionManager.rollback();
        verify(transactionStatus).isInTransaction();
        verify(connection).rollback();
    }
}
