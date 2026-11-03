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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionInitializer;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionPostProcessor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FirebirdStartTransactionCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdStartTransactionPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private final List<ConnectionInitializer> connectionInitializers = new ArrayList<>();
    
    private final List<ConnectionPostProcessor> connectionReleaseProcessors = new ArrayList<>();
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getRawTpb()).thenReturn(new byte[0]);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() throws SQLException {
        when(packet.isReadOnly()).thenReturn(true);
        when(packet.getIsolationLevel()).thenReturn(TransactionIsolationLevel.SERIALIZABLE);
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        DatabasePacket first = actual.iterator().next();
        assertThat(first, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) first).getHandle(), is(1));
        verify(connectionSession).setAutoCommit(false);
        verify(connectionSession).setReadOnly(true);
        verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
        assertTrue(connectionInitializers.isEmpty());
        assertTrue(connectionReleaseProcessors.isEmpty());
    }
    
    @Test
    void assertExecuteRegistersRawTransactionOptions() throws SQLException {
        when(packet.getRawTpb()).thenReturn(new byte[]{3, 8});
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(databaseConnectionManager.getConnectionInitializers()).thenReturn(connectionInitializers);
        when(databaseConnectionManager.getConnectionReleaseProcessors()).thenReturn(connectionReleaseProcessors);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")
                .getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", null));
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(databaseConnectionManager).handleAutoCommit();
        verify(databaseConnectionManager).getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(connectionInitializers.size(), is(1));
        assertThat(connectionReleaseProcessors.size(), is(1));
    }
    
    @Test
    void assertExecuteWithActiveTransactionCreatesIndependentSession() throws SQLException {
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        executor.execute();
        FirebirdGenericResponsePacket actualSecond = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertThat(actualSecond.getHandle(), is(2));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, 2), not(sameInstance(connectionSession)));
    }
    
    @Test
    void assertExecuteReusesClosedTransactionHandle() throws SQLException {
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, firstTransactionId);
        when(packet.isReadOnly()).thenReturn(false);
        when(packet.getIsolationLevel()).thenReturn(TransactionIsolationLevel.SERIALIZABLE);
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(firstTransactionId));
    }
}
