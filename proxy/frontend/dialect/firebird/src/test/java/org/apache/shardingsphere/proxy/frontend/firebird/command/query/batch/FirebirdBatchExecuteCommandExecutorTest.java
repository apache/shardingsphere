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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse.DetailedError;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private FirebirdBatchCreateCommandPacket createPacket;
    
    @Mock
    private FirebirdBatchRegistry batchRegistry;
    
    @Mock
    private FirebirdBatchStatement batchStatement;
    
    @Mock
    private FirebirdServerPreparedStatement preparedStatement;
    
    @Mock
    private FirebirdBlobWriteCache blobWriteCache;
    
    private int transactionId;
    
    @BeforeEach
    void setUp() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithoutRecordCounts() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{5, 5})))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            Collection<DatabasePacket> actual = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            FirebirdBatchCompletionStateResponse actualResponse = (FirebirdBatchCompletionStateResponse) actual.iterator().next();
            assertThat(actualResponse, isA(FirebirdBatchCompletionStateResponse.class));
            assertThat(getRecordsCount(actualResponse), is(2L));
            assertThat(getUpdateCounts(actualResponse), is(new int[0]));
            verify(batchRegistry).getBatchStatement(CONNECTION_ID, STATEMENT_ID);
            verify(connectionSession.getServerPreparedStatementRegistry()).getPreparedStatement(STATEMENT_ID);
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithRecordCounts() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{5, 5})))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(2L));
            assertThat(getUpdateCounts(actual), is(new int[]{5, 5}));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteEmptyBatch() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(createPacket.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(createPacket.getBatchBlr()).thenReturn(createBatchBlr());
        when(createPacket.getBatchMessageLength()).thenReturn(6L);
        when(createPacket.getBatchParametersBuffer()).thenReturn(Unpooled.EMPTY_BUFFER);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        try {
            new FirebirdBatchCreateCommandExecutor(createPacket, connectionSession).execute();
            clearInvocations(connectionSession.getServerPreparedStatementRegistry());
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(0L));
            assertThat(getUpdateCounts(actual), is(new int[0]));
            verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecuteWithFailedMessage() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2"), Arrays.asList(3, "foo_3")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        SQLException failureCause = new SQLException("violation of PRIMARY or UNIQUE KEY constraint", "23000", 335544665);
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(
                2, new int[]{1, FirebirdBatchCompletion.EXECUTE_FAILED}, Collections.singletonList(new FirebirdBatchCompletion.Failure(1, failureCause)));
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(2L));
            assertThat(getUpdateCounts(actual), is(new int[]{1, FirebirdBatchCompletion.EXECUTE_FAILED}));
            List<DetailedError> detailedErrors = getDetailedErrors(actual);
            assertThat(detailedErrors.size(), is(1));
            assertThat(detailedErrors.get(0).getElement(), is(1));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithFailedMessageWithoutRecordCounts() throws ReflectiveOperationException, SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Arrays.asList(1, "foo_1"), Arrays.asList(2, "foo_2")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        SQLException failureCause = new SQLException("violation", "23000", 335544665);
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(
                1, new int[]{FirebirdBatchCompletion.EXECUTE_FAILED}, Collections.singletonList(new FirebirdBatchCompletion.Failure(0, failureCause)));
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getUpdateCounts(actual), is(new int[0]));
            List<DetailedError> detailedErrors = getDetailedErrors(actual);
            assertThat(detailedErrors.size(), is(1));
            assertThat(detailedErrors.get(0).getElement(), is(0));
            verify(batchStatement).reset();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobParameterArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithBlobParameters(final String name, final long blobId, final byte[] expectedBytes, final boolean batchBlobId) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        List<List<Object>> params = Arrays.asList(Arrays.asList(13L, blobId), Arrays.asList(14L, blobId));
        when(batchStatement.getParameterValues()).thenReturn(params);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(batchBlobId);
        when(batchStatement.getColumnDescriptors()).thenReturn(Arrays.asList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.INT64, 8, 0, 0), columnDescriptor));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        if (blobId > 0L) {
            when(blobWriteCache.useBlobData(CONNECTION_ID, blobId, transactionId)).thenReturn(Optional.of(expectedBytes));
        }
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedStatic<FirebirdBlobBinaryProtocolValue> mockedBlobValues = mockStatic(FirebirdBlobBinaryProtocolValue.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{1, 1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            if (blobId < 0L) {
                mockedBlobValues.when(() -> FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId)).thenReturn(expectedBytes);
            }
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(2));
            assertThat(actualParams.get(0).get(0), is(13L));
            assertThat(actualParams.get(1).get(0), is(14L));
            for (List<Object> each : actualParams) {
                assertThat((byte[]) each.get(1), is(expectedBytes));
            }
            assertThat(params, is(Arrays.asList(Arrays.asList(13L, blobId), Arrays.asList(14L, blobId))));
            verify(batchStatement).reset();
            if (blobId > 0L) {
                verify(blobWriteCache, times(2)).useBlobData(CONNECTION_ID, blobId, transactionId);
                verify(blobWriteCache, never()).removeWrite(anyInt(), anyLong());
            } else {
                verifyNoInteractions(blobWriteCache);
            }
        }
    }
    
    private static Stream<Arguments> blobParameterArguments() {
        return Stream.of(
                Arguments.of("uploaded_blob", 13L, new byte[]{1, 2, 3}, false),
                Arguments.of("empty_upload", 13L, new byte[0], false),
                Arguments.of("result_blob", -1L, new byte[]{4, 5, 6}, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("zeroBlobIdArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithZeroBlobId(final String name, final boolean batchBlobId, final Map<Long, Long> registeredBlobIds, final long blobId) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(blobId)));
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(batchBlobId);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        if (!registeredBlobIds.isEmpty()) {
            when(batchStatement.getBlobIds()).thenReturn(new HashMap<>(registeredBlobIds));
        }
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[0]));
            verifyNoInteractions(blobWriteCache);
        }
    }
    
    private static Stream<Arguments> zeroBlobIdArguments() {
        return Stream.of(
                Arguments.of("zero_blob_id", false, Collections.emptyMap(), 0L),
                Arguments.of("zero_batch_blob_id", true, Collections.emptyMap(), 0L),
                Arguments.of("registered_zero_blob_id", true, Collections.singletonMap(1L, 0L), 1L));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithNullBlobParameter() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(null)));
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertNull(actualParams.get(0).get(0));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithInvalidUploadedBlob() throws SQLException, ReflectiveOperationException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(13L)));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getDetailedErrors(actual).get(0).getElement(), is(0));
            assertTrue(mockedExecutor.constructed().isEmpty());
            verify(batchStatement).reset();
            verify(blobWriteCache, never()).removeWrite(anyInt(), anyLong());
        }
    }
    
    @Test
    void assertExecuteWithUnknownResultBlob() throws SQLException, ReflectiveOperationException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(-1L)));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobBinaryProtocolValue> ignored = mockStatic(FirebirdBlobBinaryProtocolValue.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getDetailedErrors(actual).get(0).getElement(), is(0));
            assertTrue(mockedExecutor.constructed().isEmpty());
            verify(batchStatement).reset();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("registeredBlobArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithRegisteredBlobs(final String name, final long batchBlobId, final long existingBlobId, final byte[] expectedBytes) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        List<List<Object>> params = Arrays.asList(Collections.singletonList(batchBlobId), Collections.singletonList(batchBlobId + 1L));
        when(batchStatement.getParameterValues()).thenReturn(params);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        Map<Long, Long> blobIds = new HashMap<>(3);
        blobIds.put(batchBlobId, existingBlobId);
        blobIds.put(batchBlobId + 1L, existingBlobId);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        if (existingBlobId > 0L) {
            Map<Long, byte[]> uploads = new HashMap<>(3);
            uploads.put(1L, new byte[]{99});
            uploads.put(existingBlobId, expectedBytes);
            when(blobWriteCache.useBlobData(anyInt(), anyLong(), anyInt())).thenAnswer(invocation -> Optional.ofNullable(uploads.get(invocation.<Long>getArgument(1))));
        }
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedStatic<FirebirdBlobBinaryProtocolValue> mockedBlobValues = mockStatic(FirebirdBlobBinaryProtocolValue.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{1, 1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            if (existingBlobId < 0L) {
                mockedBlobValues.when(() -> FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, existingBlobId)).thenReturn(expectedBytes);
            }
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(2));
            for (List<Object> each : actualParams) {
                assertThat((byte[]) each.get(0), is(expectedBytes));
            }
            assertThat(params, is(Arrays.asList(Collections.singletonList(batchBlobId), Collections.singletonList(batchBlobId + 1L))));
            verify(batchStatement).reset();
            if (existingBlobId > 0L) {
                verify(blobWriteCache, times(2)).useBlobData(CONNECTION_ID, existingBlobId, transactionId);
                verify(blobWriteCache, never()).removeWrite(anyInt(), anyLong());
            } else {
                verifyNoInteractions(blobWriteCache);
            }
        }
    }
    
    private static Stream<Arguments> registeredBlobArguments() {
        return Stream.of(
                Arguments.of("different_ids", 1L, 3L, new byte[]{3, 4}),
                Arguments.of("colliding_ids", 1L, 2L, new byte[]{2, 4}),
                Arguments.of("result_blob", 1L, -1L, new byte[]{4, 5}),
                Arguments.of("negative_batch_id", -2L, 3L, new byte[]{3, 4}));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unregisteredBlobArguments")
    void assertExecuteWithUnregisteredBlob(final String name, final List<List<Object>> params, final Map<Long, Long> registeredBlobIds, final int expectedExecutions,
                                           final Map<Long, Long> expectedBlobIds, final long expectedBatchBlobId) {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(params);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<FirebirdBatchColumnDescriptor> columnDescriptors = new ArrayList<>(params.get(0).size());
        for (int i = 0; i < params.get(0).size(); i++) {
            columnDescriptors.add(createBlobColumnDescriptor(true));
        }
        when(batchStatement.getColumnDescriptors()).thenReturn(columnDescriptors);
        Map<Long, Long> blobIds = new HashMap<>(registeredBlobIds);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1})))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            UnknownBatchBlobIdException actual = assertThrows(UnknownBatchBlobIdException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual.getBatchBlobId(), is(expectedBatchBlobId));
            assertThat(mockedExecutor.constructed().size(), is(expectedExecutions));
            assertThat(blobIds, is(expectedBlobIds));
            verifyNoInteractions(blobWriteCache);
            verify(batchStatement, never()).reset();
        }
    }
    
    private static Stream<Arguments> unregisteredBlobArguments() {
        return Stream.of(
                Arguments.of("missing_registration", Collections.singletonList(Collections.singletonList(1L)), Collections.emptyMap(), 0, Collections.emptyMap(), 1L),
                Arguments.of("other_registration", Collections.singletonList(Collections.singletonList(1L)), Collections.singletonMap(2L, 1L), 0, Collections.singletonMap(2L, 1L), 1L),
                Arguments.of("consumed_registration", Arrays.asList(Collections.singletonList(1L), Collections.singletonList(1L)), Collections.singletonMap(1L, 0L), 1, Collections.emptyMap(), 1L),
                Arguments.of("invalid_upload_before_unregistered_column", Collections.singletonList(Arrays.asList(1L, 0x100000002L)), Collections.singletonMap(1L, 99L), 0, Collections.emptyMap(),
                        0x100000002L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobFailureArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithBlobFailures(final String name, final boolean multiError, final boolean backendFailure, final int[] existingBlobIds,
                                       final int[] expectedCounts, final int[] expectedErrors, final Collection<Integer> expectedExecutedRows) throws SQLException, ReflectiveOperationException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isMultiError()).thenReturn(multiError);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        List<List<Object>> params = new ArrayList<>(existingBlobIds.length);
        Map<Long, Long> blobIds = new HashMap<>(4);
        for (int i = 0; i < existingBlobIds.length; i++) {
            params.add(Arrays.asList(i, 10L + i));
            blobIds.put(10L + i, (long) existingBlobIds[i]);
        }
        when(batchStatement.getParameterValues()).thenReturn(params);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Arrays.asList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, 4, 0, 0), columnDescriptor));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        Collection<Integer> actualExecutedRows = new ArrayList<>(existingBlobIds.length);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    List<List<Object>> boundParams = (List<List<Object>>) context.arguments().get(2);
                    for (List<Object> each : boundParams) {
                        actualExecutedRows.add((Integer) each.get(0));
                    }
                    int[] updateCounts = new int[boundParams.size()];
                    Arrays.fill(updateCounts, 1);
                    if (backendFailure && 0 == (Integer) boundParams.get(0).get(0)) {
                        updateCounts[0] = FirebirdBatchCompletion.EXECUTE_FAILED;
                        when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(boundParams.size(), updateCounts,
                                Collections.singleton(new FirebirdBatchCompletion.Failure(0, new SQLException("duplicate key")))));
                    } else {
                        when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(boundParams.size(), updateCounts));
                    }
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is((long) expectedCounts.length));
            assertThat(getUpdateCounts(actual), is(expectedCounts));
            List<DetailedError> actualErrors = getDetailedErrors(actual);
            assertThat(actualErrors.size(), is(expectedErrors.length));
            for (int i = 0; i < expectedErrors.length; i++) {
                assertThat(actualErrors.get(i).getElement(), is(expectedErrors[i]));
            }
            assertThat(actualExecutedRows, is(expectedExecutedRows));
            verify(batchStatement).reset();
            verify(blobWriteCache, never()).removeWrite(anyInt(), anyLong());
        }
    }
    
    private static Stream<Arguments> blobFailureArguments() {
        return Stream.of(
                Arguments.of("continue_after_first", true, false, new int[]{9, 0, 0}, new int[]{-1, 1, 1}, new int[]{0}, Arrays.asList(1, 2)),
                Arguments.of("continue_after_middle", true, false, new int[]{0, 9, 0}, new int[]{1, -1, 1}, new int[]{1}, Arrays.asList(0, 2)),
                Arguments.of("failure_at_end", true, false, new int[]{0, 0, 9}, new int[]{1, 1, -1}, new int[]{2}, Arrays.asList(0, 1)),
                Arguments.of("stop_at_middle", false, false, new int[]{0, 9, 0}, new int[]{1, -1}, new int[]{1}, Collections.singletonList(0)),
                Arguments.of("stop_at_first", false, false, new int[]{9, 0, 0}, new int[]{-1}, new int[]{0}, Collections.emptyList()),
                Arguments.of("all_invalid", true, false, new int[]{9, 9}, new int[]{-1, -1}, new int[]{0, 1}, Collections.emptyList()),
                Arguments.of("backend_and_blob_failures", true, true, new int[]{0, 9, 0}, new int[]{-1, -1, 1}, new int[]{0, 1}, Arrays.asList(0, 2)));
    }
    
    @Test
    void assertExecuteWhenBackendStopsBeforeInvalidBlob() throws SQLException, ReflectiveOperationException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isMultiError()).thenReturn(true);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Collections.singletonList(0L), Collections.singletonList(0L), Collections.singletonList(9L), Collections.singletonList(0L)));
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1})))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getUpdateCounts(actual), is(new int[]{1}));
            assertTrue(getDetailedErrors(actual).isEmpty());
            assertThat(mockedExecutor.constructed().size(), is(1));
            verify(batchStatement).reset();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("laterBlobFailureArguments")
    void assertExecuteWithEarlierBackendFailure(final String name, final List<List<Object>> params, final Map<Long, Long> blobIds) throws SQLException, ReflectiveOperationException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(params);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        SQLException failure = new SQLException("duplicate key");
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(1, new int[]{-1}, Collections.singleton(new FirebirdBatchCompletion.Failure(0, failure)));
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getUpdateCounts(actual), is(new int[]{-1}));
            assertThat(getDetailedErrors(actual).size(), is(1));
            assertThat(getDetailedErrors(actual).get(0).getElement(), is(0));
            assertThat(mockedExecutor.constructed().size(), is(1));
            verify(batchStatement).reset();
        }
    }
    
    private static Stream<Arguments> laterBlobFailureArguments() {
        return Stream.of(
                Arguments.of("missing_upload", Arrays.asList(Collections.singletonList(0L), Collections.singletonList(13L)), Collections.singletonMap(13L, 99L)),
                Arguments.of("missing_registration", Arrays.asList(Collections.singletonList(0L), Collections.singletonList(13L)), Collections.emptyMap()),
                Arguments.of("consumed_registration", Arrays.asList(Collections.singletonList(13L), Collections.singletonList(13L)), Collections.singletonMap(13L, 0L)));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithSharedUpload() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchExecuteCommandPacket otherPacket = mock(FirebirdBatchExecuteCommandPacket.class);
        when(otherPacket.getStatementHandle()).thenReturn(STATEMENT_ID + 1);
        when(otherPacket.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        FirebirdBatchStatement firstBatch = new FirebirdBatchStatement(STATEMENT_ID, Collections.singletonList(columnDescriptor), 1024L);
        firstBatch.getBlobIds().put(1L, 9L);
        firstBatch.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchStatement secondBatch = new FirebirdBatchStatement(STATEMENT_ID + 1, Collections.singletonList(columnDescriptor), 1024L);
        secondBatch.getBlobIds().put(2L, 9L);
        secondBatch.addParameterValues(Collections.singletonList(2L));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(firstBatch);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID + 1)).thenReturn(secondBatch);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID + 1)).thenReturn(preparedStatement);
        when(blobWriteCache.useBlobData(CONNECTION_ID, 9L, transactionId)).thenReturn(Optional.of(new byte[]{9}));
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            new FirebirdBatchExecuteCommandExecutor(otherPacket, connectionSession).execute();
            assertThat(actualParams.size(), is(2));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[]{9}));
            assertThat((byte[]) actualParams.get(1).get(0), is(new byte[]{9}));
            verify(blobWriteCache, never()).removeWrite(anyInt(), anyLong());
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithStreamBlobs() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}));
        batchStatement.addParameterValues(Collections.singletonList(1L));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[]{1, 2, 3}));
            assertTrue(batchStatement.getStreamBlobContents().isEmpty());
            assertTrue(batchStatement.getParameterValues().isEmpty());
        }
    }
    
    @Test
    void assertExecuteWithInvalidStreamBlob() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(0L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}));
        batchStatement.addParameterValues(Collections.singletonList(1L));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(BatchBlobBufferFormatException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertTrue(mockedExecutor.constructed().isEmpty());
            assertTrue(batchStatement.getBlobBuffer().isEmpty());
            assertTrue(batchStatement.getParameterValues().isEmpty());
        }
    }
    
    @Test
    void assertExecuteWithMissingFilterStreamBlob() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}));
        batchStatement.setDefaultBpb(new byte[]{1, 2, 1, 2, 3, 1, 1});
        batchStatement.addParameterValues(Collections.singletonList(1L));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            assertThrows(BlobFilterNotFoundException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertTrue(mockedExecutor.constructed().isEmpty());
            assertTrue(batchStatement.getBlobBuffer().isEmpty());
            assertThat(batchStatement.getBlobStreamSize(), is(0L));
            assertTrue(batchStatement.getParameterValues().isEmpty());
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithConnectionCharsetStreamBlob() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}));
        batchStatement.setDefaultBpb(new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 127, 5, 1, 4, 3, 1, 1});
        batchStatement.addParameterValues(Collections.singletonList(1L));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[]{1, 2, 3}));
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithConvertedBlobParameters() throws SQLException {
        mockBlobParameterBatch(Collections.singletonList(Arrays.asList(10L, 11L)), Arrays.asList(createParameterColumn(Types.INTEGER, null), createParameterColumn(Types.VARCHAR, 20)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 10L, transactionId)).thenReturn(Optional.of("231".getBytes(StandardCharsets.US_ASCII)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 11L, transactionId)).thenReturn(Optional.of("short".getBytes(StandardCharsets.US_ASCII)));
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams, is(Collections.singletonList(Arrays.<Object>asList(231, "short"))));
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithParameterConversionFailure() {
        mockBlobParameterBatch(Arrays.asList(Collections.singletonList(10L), Collections.singletonList(11L), Collections.singletonList(12L)),
                Collections.singletonList(createParameterColumn(Types.VARCHAR, 4)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 10L, transactionId)).thenReturn(Optional.of("ok".getBytes(StandardCharsets.US_ASCII)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 11L, transactionId)).thenReturn(Optional.of("too-long".getBytes(StandardCharsets.US_ASCII)));
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
                    actualParams.addAll((List<List<Object>>) context.arguments().get(2));
                    when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
                })) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            StringTruncationException actualError = (StringTruncationException) actual.getConversionError();
            assertThat(actualError.getExpectedLength(), is(4));
            assertThat(actualError.getActualLength(), is(8));
            assertThat(mockedExecutor.constructed().size(), is(1));
            assertThat(actualParams, is(Collections.singletonList(Collections.<Object>singletonList("ok"))));
            verify(blobWriteCache, never()).useBlobData(CONNECTION_ID, 12L, transactionId);
            verify(batchStatement, never()).reset();
        }
    }
    
    @Test
    void assertExecuteWithInvalidBlobForConvertedParameter() {
        mockBlobParameterBatch(Collections.singletonList(Collections.singletonList(10L)), Collections.singletonList(createParameterColumn(Types.VARCHAR, 20)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 10L, transactionId)).thenReturn(Optional.empty());
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual.getConversionError(), isA(InvalidSegstrIdException.class));
            assertTrue(mockedExecutor.constructed().isEmpty());
        }
    }
    
    @Test
    void assertExecuteWithLastParameterConversionFailure() {
        mockBlobParameterBatch(Collections.singletonList(Arrays.asList(10L, 11L)), Arrays.asList(createParameterColumn(Types.INTEGER, null), createParameterColumn(Types.VARCHAR, 4)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 11L, transactionId)).thenReturn(Optional.of("too-long".getBytes(StandardCharsets.US_ASCII)));
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedStatic<FirebirdBlobWriteCache> mockedBlobCache = mockStatic(FirebirdBlobWriteCache.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            mockedBlobCache.when(FirebirdBlobWriteCache::getInstance).thenReturn(blobWriteCache);
            ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual.getConversionError(), isA(StringTruncationException.class));
            verify(blobWriteCache, never()).useBlobData(CONNECTION_ID, 10L, transactionId);
        }
    }
    
    private void mockBlobParameterBatch(final List<List<Object>> params, final List<FirebirdReturnColumnPacket> parameterColumns) {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(params);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.nCopies(params.get(0).size(), createBlobColumnDescriptor(false)));
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(preparedStatement.getParameterColumns()).thenReturn(parameterColumns);
    }
    
    private FirebirdReturnColumnPacket createParameterColumn(final int dataType, final Integer length) {
        ShardingSphereTable table = new ShardingSphereTable("T", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return new FirebirdReturnColumnPacket(Collections.emptyList(), 1, table, new ShardingSphereColumn("C", dataType, false, false, true, true, false, true), null, null, null, length, false, null);
    }
    
    private FirebirdBatchStatement createStreamBlobBatchStatement(final ByteBuf blobStream) {
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        FirebirdBatchStatement result = new FirebirdBatchStatement(STATEMENT_ID, Collections.singletonList(columnDescriptor), 1024L, false, false, true);
        result.appendBlobStream(blobStream, 20L);
        return result;
    }
    
    @Test
    void assertExecuteWhenBatchExecutionFailed() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList("foo_value")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        SQLException expected = new SQLException("batch execution failed");
        try (
                MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class);
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenThrow(expected))) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            SQLException actual = assertThrows(SQLException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual, is(expected));
            verify(batchStatement, never()).reset();
        }
    }
    
    @Test
    void assertExecuteWithNoBatchStatement() {
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
    
    @Test
    void assertExecuteWithClosedTransactionHandle() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID)).thenReturn(batchStatement);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        try (MockedStatic<FirebirdBatchRegistry> mockedRegistry = mockStatic(FirebirdBatchRegistry.class)) {
            mockedRegistry.when(FirebirdBatchRegistry::getInstance).thenReturn(batchRegistry);
            FirebirdBatchExecuteCommandExecutor executor = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession);
            assertThrows(InvalidTransactionHandleException.class, executor::execute);
            verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
        }
    }
    
    private long getRecordsCount(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (Long) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("recordsCount"), response);
    }
    
    private int[] getUpdateCounts(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (int[]) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("updateCounts"), response);
    }
    
    private ByteBuf createBatchBlr() {
        return Unpooled.wrappedBuffer(new byte[]{
                (byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0,
                2, 0, (byte) BlrConstants.blr_long, 0, (byte) BlrConstants.blr_short, 0, (byte) BlrConstants.blr_end, (byte) BlrConstants.blr_eoc});
    }
    
    private FirebirdBatchColumnDescriptor createBlobColumnDescriptor(final boolean batchBlobId) {
        ByteBuf blobBlr = batchBlobId ? Unpooled.buffer().writeByte(BlrConstants.blr_blob2).writeZero(4) : Unpooled.buffer().writeByte(BlrConstants.blr_quad).writeByte(0);
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2).writeBytes(blobBlr)
                .writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields().get(0);
    }
    
    @SuppressWarnings("unchecked")
    private List<DetailedError> getDetailedErrors(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (List<DetailedError>) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("detailedErrors"), response);
    }
}
