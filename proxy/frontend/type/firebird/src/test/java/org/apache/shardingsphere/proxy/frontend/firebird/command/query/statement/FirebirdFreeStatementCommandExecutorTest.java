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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdFreeStatementCommandExecutorTest {
    
    @Mock
    private FirebirdFreeStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ServerPreparedStatementRegistry registry;
    
    @Test
    void assertExecuteWithDrop() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.DROP);
        when(packet.getStatementId()).thenReturn(1);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), instanceOf(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(1);
    }
    
    @Test
    void assertExecuteWithUnprepare() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.UNPREPARE);
        when(packet.getStatementId()).thenReturn(1);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), instanceOf(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(1);
    }
    
    @Test
    void assertExecuteWithClose() throws SQLException {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.CLOSE);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        verify(connectionSession.getConnectionContext()).clearCursorContext();
    }
    
    @Test
    void assertExecuteWithUnknownOption() {
        when(packet.getOption()).thenReturn(999);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        assertThrows(FirebirdProtocolException.class, executor::execute);
    }
}
