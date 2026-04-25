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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchExecuteCommandExecutorTest {
    
    private static final int CONNECTION_ID = 3;
    
    private static final int STATEMENT_ID = 33;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchExecuteCommandPacket packet;
    
    @Mock
    private FirebirdBatchRegistry batchRegistry;
    
    @Mock
    private FirebirdBatchStatement batchStatement;
    
    @Mock
    private FirebirdServerPreparedStatement preparedStatement;
    
    @Test
    void assertExecute() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Arrays.asList(1, "foo_1")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBatchSendMessageCommandPacket> mockedPacket = mockStatic(FirebirdBatchSendMessageCommandPacket.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new int[]{1, 0, -2, 3}))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            Collection<DatabasePacket> actual = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            assertThat(actual.iterator().next(), isA(FirebirdBatchCompletionStateResponse.class));
            verify(batchRegistry).getBatchStatement(CONNECTION_ID, STATEMENT_ID);
            verify(connectionSession.getServerPreparedStatementRegistry()).getPreparedStatement(STATEMENT_ID);
            verify(batchStatement).clearParameterValues();
            mockedPacket.verify(() -> FirebirdBatchSendMessageCommandPacket.resetBatchMessageHeader(CONNECTION_ID));
        }
    }
    
    @Test
    void assertExecuteWithNoBatchStatement() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(null);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchExecuteCommandExecutor executor = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession);
            if (FirebirdBatchExecuteCommandExecutor.class.desiredAssertionStatus()) {
                org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class, executor::execute);
            } else {
                org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, executor::execute);
            }
            verify(batchRegistry).getBatchStatement(CONNECTION_ID, STATEMENT_ID);
            verify(connectionSession.getServerPreparedStatementRegistry(), org.mockito.Mockito.never()).getPreparedStatement(anyInt());
        }
    }
}
