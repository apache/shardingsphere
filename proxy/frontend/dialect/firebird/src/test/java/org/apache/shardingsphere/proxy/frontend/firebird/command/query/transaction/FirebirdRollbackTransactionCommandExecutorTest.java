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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdRollbackTransactionCommandExecutorTest {
    
    private static final int CONNECTION_ID = 202;
    
    @Mock
    private FirebirdRollbackTransactionPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() throws SQLException {
        when(packet.getTransactionId()).thenReturn(1);
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, 11, 21L, 1, false);
        FirebirdBlobWriteCache.getInstance().closeWrite(CONNECTION_ID, 11);
        FirebirdBlobWriteCache.getInstance().useBlobData(CONNECTION_ID, 21L, 1);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class, (mock, context) -> {
        })) {
            Collection<DatabasePacket> actual = new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            verify(mocked.constructed().get(0)).rollback();
            assertFalse(FirebirdBlobWriteCache.getInstance().getBlobData(CONNECTION_ID, 21L).isPresent());
            assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
        }
    }
    
    @Test
    void assertExecuteWithAutoCommit() throws SQLException {
        when(packet.getTransactionId()).thenReturn(1);
        when(connectionSession.isAutoCommit()).thenReturn(true);
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, 11, 21L, 1, false);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class)) {
            Collection<DatabasePacket> actual = new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            assertTrue(mocked.constructed().isEmpty());
            assertFalse(FirebirdBlobWriteCache.getInstance().getBlobData(CONNECTION_ID, 21L).isPresent());
            assertFalse(FirebirdBlobWriteCache.getInstance().getBlobId(CONNECTION_ID, 11).isPresent());
        }
    }
    
    @Test
    void assertExecuteWhenRollbackFails() {
        when(packet.getTransactionId()).thenReturn(1);
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, 11, 21L, 1, false);
        FirebirdBlobWriteCache.getInstance().appendSegment(CONNECTION_ID, 11, new byte[]{1, 2, 3});
        FirebirdBlobWriteCache.getInstance().closeWrite(CONNECTION_ID, 11);
        FirebirdBlobWriteCache.getInstance().useBlobData(CONNECTION_ID, 21L, 1);
        SQLException expected = new SQLException("foo_rollback_failure");
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class, (mock, context) -> doThrow(expected).when(mock).rollback())) {
            assertThat(assertThrows(SQLException.class, () -> new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute()), sameInstance(expected));
            assertThat(FirebirdBlobWriteCache.getInstance().useBlobData(CONNECTION_ID, 21L, 1).get(), is(new byte[]{1, 2, 3}));
            assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
        }
    }
    
    @Test
    void assertExecuteWithUnknownTransactionHandle() {
        when(packet.getTransactionId()).thenReturn(99);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class)) {
            assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute());
            assertTrue(mocked.constructed().isEmpty());
        }
    }
    
    @Test
    void assertExecuteTwiceWithSameTransactionHandle() throws SQLException {
        when(packet.getTransactionId()).thenReturn(1);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class)) {
            new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute());
        }
    }
    
    @Test
    void assertExecuteWithRolledBackTransactionHandleAfterNewTransaction() throws SQLException {
        when(packet.getTransactionId()).thenReturn(1);
        try (MockedConstruction<ProxyBackendTransactionManager> mocked = mockConstruction(ProxyBackendTransactionManager.class)) {
            new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
            assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute());
            when(packet.getTransactionId()).thenReturn(2);
            Collection<DatabasePacket> actual = new FirebirdRollbackTransactionCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        }
    }
}
