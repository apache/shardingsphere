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
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionContext connectionContext;
    
    @Mock
    private Connection connection;
    
    @Mock
    private Connection anotherConnection;
    
    private LocalTransactionManager localTransactionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
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
        verify(connectionContext.getTransactionContext()).isExceptionOccur();
        verify(connection).commit();
    }
    
    @Test
    void assertCommitWithExceptionOccur() throws SQLException {
        when(connectionContext.getTransactionContext().isExceptionOccur()).thenReturn(true);
        localTransactionManager.commit();
        verify(connection).rollback();
    }
    
    @Test
    void assertCommitWithSQLExceptionChain() throws SQLException {
        when(connectionContext.getTransactionContext().isExceptionOccur()).thenReturn(false);
        Multimap<String, Connection> cachedConnections = HashMultimap.create();
        cachedConnections.put("ds1", connection);
        cachedConnections.put("ds2", anotherConnection);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(cachedConnections);
        SQLException first = new SQLException("first");
        SQLException second = new SQLException("second");
        doThrow(first).when(connection).commit();
        doThrow(second).when(anotherConnection).commit();
        SQLException actual = assertThrows(SQLException.class, () -> localTransactionManager.commit());
        assertThat(new HashSet<>(Arrays.asList(actual.getMessage(), actual.getNextException().getMessage())), is(new HashSet<>(Arrays.asList("first", "second"))));
    }
    
    @Test
    void assertRollback() throws SQLException {
        localTransactionManager.rollback();
        verify(transactionStatus).isInTransaction();
        verify(connection).rollback();
    }
    
    @Test
    void assertRollbackWithoutInTransaction() throws SQLException {
        when(transactionStatus.isInTransaction()).thenReturn(false);
        localTransactionManager.rollback();
        verify(connection, never()).rollback();
    }
    
    @Test
    void assertRollbackWithSQLExceptionChain() throws SQLException {
        Multimap<String, Connection> cachedConnections = HashMultimap.create();
        cachedConnections.put("ds1", connection);
        cachedConnections.put("ds2", anotherConnection);
        when(databaseConnectionManager.getCachedConnections()).thenReturn(cachedConnections);
        SQLException first = new SQLException("first");
        SQLException second = new SQLException("second");
        doThrow(first).when(connection).rollback();
        doThrow(second).when(anotherConnection).rollback();
        SQLException actual = assertThrows(SQLException.class, () -> localTransactionManager.rollback());
        assertThat(new HashSet<>(Arrays.asList(actual.getMessage(), actual.getNextException().getMessage())), is(new HashSet<>(Arrays.asList("first", "second"))));
    }
}
