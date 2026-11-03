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

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
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
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        when(packet.isAutoCommit()).thenReturn(true);
        when(packet.isReadOnly()).thenReturn(true);
        when(packet.getIsolationLevel()).thenReturn(TransactionIsolationLevel.SERIALIZABLE);
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        DatabasePacket first = actual.iterator().next();
        assertThat(first, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) first).getHandle(), is(1));
        verify(connectionSession).setAutoCommit(true);
        verify(connectionSession).setReadOnly(true);
        verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
    }
    
    @Test
    void assertExecuteWithActiveTransactionCreatesIndependentSession() {
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        executor.execute();
        FirebirdGenericResponsePacket actualSecond = (FirebirdGenericResponsePacket) executor.execute().iterator().next();
        assertThat(actualSecond.getHandle(), is(2));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, 2), not(sameInstance(connectionSession)));
    }
    
    @Test
    void assertExecuteReusesClosedTransactionHandle() {
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, firstTransactionId);
        when(packet.isAutoCommit()).thenReturn(true);
        when(packet.isReadOnly()).thenReturn(false);
        when(packet.getIsolationLevel()).thenReturn(TransactionIsolationLevel.SERIALIZABLE);
        FirebirdStartTransactionCommandExecutor executor = new FirebirdStartTransactionCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(firstTransactionId));
    }
}
