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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free;

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdFreeStatementCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdFreeStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ServerPreparedStatementRegistry registry;
    
    @Mock
    private ProxyDatabaseConnectionManager connectionManager;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @BeforeEach
    void setUp() {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(connectionManager);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithDrop() {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.DROP);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(1);
    }
    
    @Test
    void assertExecuteWithUnprepare() {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.UNPREPARE);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(1);
    }
    
    @Test
    void assertExecuteWithClose() {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.CLOSE);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession.getConnectionContext()).clearCursorContext();
        verify(connectionManager).unmarkResourceInUse(proxyBackendHandler);
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID), nullValue());
    }
    
    @Test
    void assertExecuteWithUnknownOption() {
        when(packet.getOption()).thenReturn(999);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        assertThrows(FirebirdProtocolException.class, executor::execute);
    }
}
