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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchSendMessageCommandExecutorTest {
    
    private static final int CONNECTION_ID = 3;
    
    private static final int STATEMENT_ID = 33;
    
    @Mock
    private FirebirdBatchSendMessageCommandPacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchRegistry batchRegistry;
    
    @Mock
    private FirebirdBatchStatement batchStatement;
    
    @Test
    void assertExecute() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(8);
        when(batchStatement.getBufferSize()).thenReturn(16L);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            Collection<DatabasePacket> actual = new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
            verify(batchStatement).addSize(8);
        }
    }
    
    @Test
    void assertExecuteWhenBatchStatementMissing() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(null);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute());
        }
    }
    
    @Test
    void assertExecuteWhenBatchTooBig() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getDataLength()).thenReturn(9);
        when(batchStatement.getAccumulatedSize()).thenReturn(8L);
        when(batchStatement.getBufferSize()).thenReturn(16L);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchSendMessageCommandExecutor(packet, connectionSession).execute());
            verify(batchStatement, never()).addSize(9);
        }
    }
}
