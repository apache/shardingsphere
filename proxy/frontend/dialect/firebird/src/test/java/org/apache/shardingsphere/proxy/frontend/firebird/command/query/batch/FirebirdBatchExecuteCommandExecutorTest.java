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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse.DetailedError;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
    void assertExecuteWithoutRecordCounts() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new int[]{5, 5}))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            Collection<DatabasePacket> actual = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            FirebirdBatchCompletionStateResponse actualResponse = (FirebirdBatchCompletionStateResponse) actual.iterator().next();
            assertThat(actualResponse, isA(FirebirdBatchCompletionStateResponse.class));
            assertThat(getRecordsCount(actualResponse), is(2L));
            assertArrayEquals(new int[0], getUpdateCounts(actualResponse));
            verify(batchRegistry).getBatchStatement(CONNECTION_ID, STATEMENT_ID);
            verify(connectionSession.getServerPreparedStatementRegistry()).getPreparedStatement(STATEMENT_ID);
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithRecordCounts() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new int[]{5, 5}))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(2L));
            assertArrayEquals(new int[]{5, 5}, getUpdateCounts(actual));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithBatchUpdateFailure() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        BatchUpdateException failure = new BatchUpdateException("violation of PRIMARY or UNIQUE KEY constraint", "23000", 335544665, new int[0]);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenThrow(failure))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(2L));
            assertArrayEquals(new int[0], getUpdateCounts(actual));
            List<DetailedError> detailedErrors = getDetailedErrors(actual);
            assertThat(detailedErrors.size(), is(1));
            assertThat(detailedErrors.get(0).getElement(), is(0));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithBatchUpdateFailureUsesSuccessCountAsElement() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2"), Arrays.asList(3, "foo_3")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        BatchUpdateException failure = new BatchUpdateException("violation", "23000", 335544665, new int[]{1});
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenThrow(failure))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertArrayEquals(new int[]{1}, getUpdateCounts(actual));
            List<DetailedError> detailedErrors = getDetailedErrors(actual);
            assertThat(detailedErrors.size(), is(1));
            assertThat(detailedErrors.get(0).getElement(), is(1));
        }
    }
    
    @Test
    void assertExecuteWithBatchUpdateFailureFindsFirstExecuteFailedElement() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2"), Arrays.asList(3, "foo_3")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        BatchUpdateException failure = new BatchUpdateException("violation", "23000", 335544665, new int[]{1, Statement.EXECUTE_FAILED, 1});
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenThrow(failure))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            List<DetailedError> detailedErrors = getDetailedErrors(actual);
            assertThat(detailedErrors.size(), is(1));
            assertThat(detailedErrors.get(0).getElement(), is(1));
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
            assertThrows(InvalidBatchHandleException.class, executor::execute);
            verify(batchRegistry).getBatchStatement(CONNECTION_ID, STATEMENT_ID);
            verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
        }
    }
    
    private long getRecordsCount(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (Long) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("recordsCount"), response);
    }
    
    private int[] getUpdateCounts(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (int[]) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("updateCounts"), response);
    }
    
    @SuppressWarnings("unchecked")
    private List<DetailedError> getDetailedErrors(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (List<DetailedError>) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("detailedErrors"), response);
    }
}
