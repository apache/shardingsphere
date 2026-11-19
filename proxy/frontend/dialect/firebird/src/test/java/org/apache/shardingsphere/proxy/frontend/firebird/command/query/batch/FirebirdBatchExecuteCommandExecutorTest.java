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
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobBufferFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ConversionErrorException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.MalformedStringException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegisterBlobCommandPacket;
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
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.BlrConstants;
import org.jspecify.annotations.Nullable;
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
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.Charset;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test of {@link FirebirdBatchExecuteCommandExecutor}.
 *
 * <p>{@link FirebirdBatchedStatementsExecutor} is mocked with a direct {@code mockConstruction} because every test stubs the result of the batch in the initializer of the construction mock,
 * which {@code AutoMockExtension} does not support.</p>
 */
@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({FirebirdBlobWriteCache.class, FirebirdBlobBinaryProtocolValue.class})
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
    private FirebirdBatchStatement batchStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FirebirdServerPreparedStatement preparedStatement;
    
    @Mock
    private FirebirdBlobWriteCache blobWriteCache;
    
    private int transactionId;
    
    @BeforeEach
    void setUp() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{5, 5})))) {
            Collection<DatabasePacket> actual = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actual.size(), is(1));
            FirebirdBatchCompletionStateResponse actualResponse = (FirebirdBatchCompletionStateResponse) actual.iterator().next();
            assertThat(actualResponse, isA(FirebirdBatchCompletionStateResponse.class));
            assertThat(getRecordsCount(actualResponse), is(2L));
            assertThat(getUpdateCounts(actualResponse), is(new int[0]));
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{5, 5})))) {
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
        new FirebirdBatchCreateCommandExecutor(createPacket, connectionSession).execute();
        clearInvocations(connectionSession.getServerPreparedStatementRegistry());
        FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(getRecordsCount(actual), is(0L));
        assertThat(getUpdateCounts(actual), is(new int[0]));
        verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        SQLException failureCause = new SQLException("violation of PRIMARY or UNIQUE KEY constraint", "23000", 335544665);
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(
                2, new int[]{1, FirebirdBatchCompletion.EXECUTE_FAILED}, Collections.singletonList(new FirebirdBatchCompletion.Failure(1, failureCause)));
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        SQLException failureCause = new SQLException("violation", "23000", 335544665);
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(
                1, new int[]{FirebirdBatchCompletion.EXECUTE_FAILED}, Collections.singletonList(new FirebirdBatchCompletion.Failure(0, failureCause)));
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
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
    void assertExecuteWithBlobParameters(final String name, final long blobId, final byte[] expectedBytes) throws SQLException {
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        List<List<Object>> params = Arrays.asList(Arrays.asList(13L, blobId), Arrays.asList(14L, blobId));
        when(batchStatement.getParameterValues()).thenReturn(params);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(false);
        when(batchStatement.getColumnDescriptors()).thenReturn(Arrays.asList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.INT64, 8, 0, 0), columnDescriptor));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        if (blobId > 0L) {
            when(blobWriteCache.useBlobData(CONNECTION_ID, blobId, transactionId)).thenReturn(Optional.of(expectedBytes));
        }
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{1, 1}));
        })) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
            if (blobId < 0L) {
                when(FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, blobId)).thenReturn(expectedBytes);
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
        return Stream.of(Arguments.of("uploaded_blob", 13L, new byte[]{1, 2, 3}), Arguments.of("empty_upload", 13L, new byte[0]), Arguments.of("result_blob", -1L, new byte[]{4, 5, 6}));
    }
    
    private FirebirdBatchColumnDescriptor createBlobColumnDescriptor(final boolean batchBlobId) {
        ByteBuf blobBlr = batchBlobId ? Unpooled.buffer().writeByte(BlrConstants.blr_blob2).writeZero(4) : Unpooled.buffer().writeByte(BlrConstants.blr_quad).writeByte(0);
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2).writeBytes(blobBlr)
                .writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields().get(0);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("zeroBlobIdArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithZeroBlobId(final String name, final boolean batchBlobId, final Map<Long, Long> registeredBlobIds, final long blobId) throws SQLException {
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(null)));
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertNull(actualParams.get(0).get(0));
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithInvalidUploadedBlob() throws SQLException, ReflectiveOperationException {
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(13L)));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
            FirebirdBatchCompletionStateResponse actual = (FirebirdBatchCompletionStateResponse) new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(getRecordsCount(actual), is(1L));
            assertThat(getDetailedErrors(actual).get(0).getElement(), is(0));
            assertTrue(mockedExecutor.constructed().isEmpty());
            verify(batchStatement).reset();
        }
    }
    
    @Test
    void assertExecuteWithUnknownResultBlob() throws SQLException, ReflectiveOperationException {
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList(-1L)));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        if (existingBlobId > 0L) {
            Map<Long, byte[]> uploads = new HashMap<>(3);
            uploads.put(1L, new byte[]{99});
            uploads.put(existingBlobId, expectedBytes);
            when(blobWriteCache.useBlobData(anyInt(), anyLong(), anyInt())).thenAnswer(invocation -> Optional.ofNullable(uploads.get(invocation.<Long>getArgument(1))));
        }
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{1, 1}));
        })) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
            if (existingBlobId < 0L) {
                when(FirebirdBlobBinaryProtocolValue.getBlobContent(CONNECTION_ID, existingBlobId)).thenReturn(expectedBytes);
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1})))) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        Collection<Integer> actualExecutedRows = new ArrayList<>(existingBlobIds.length);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
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
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isMultiError()).thenReturn(true);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(Arrays.asList(Collections.singletonList(0L), Collections.singletonList(0L), Collections.singletonList(9L), Collections.singletonList(0L)));
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.BLOB, 8, 0, 0)));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1})))) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.isRecordCounts()).thenReturn(true);
        when(batchStatement.getParameterValues()).thenReturn(params);
        when(batchStatement.getBlobIds()).thenReturn(blobIds);
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        when(batchStatement.getColumnDescriptors()).thenReturn(Collections.singletonList(columnDescriptor));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        SQLException failure = new SQLException("duplicate key");
        FirebirdBatchCompletion completion = new FirebirdBatchCompletion(1, new int[]{-1}, Collections.singleton(new FirebirdBatchCompletion.Failure(0, failure)));
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenReturn(completion))) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, firstBatch);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID + 1, secondBatch);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID + 1)).thenReturn(preparedStatement);
        when(blobWriteCache.useBlobData(CONNECTION_ID, 9L, transactionId)).thenReturn(Optional.of(new byte[]{9}));
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}), 20L);
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
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
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(0L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}), 20L);
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
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
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}), 20L);
        batchStatement.setDefaultBpb(new byte[]{1, 2, 1, 2, 3, 1, 1});
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
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
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(Unpooled.buffer().writeLong(1L).writeInt(3).writeInt(0).writeBytes(new byte[]{1, 2, 3}), 20L);
        batchStatement.setDefaultBpb(new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 127, 5, 1, 4, 3, 1, 1});
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[]{1, 2, 3}));
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithContinuedStreamBlobs() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        ByteBuf blobStream = Unpooled.buffer().writeLong(1L).writeInt(2).writeInt(0).writeBytes(new byte[]{1, 2});
        blobStream.writeLong(0L).writeInt(1).writeInt(0).writeByte(3);
        blobStream.writeLong(2L).writeInt(1).writeInt(0).writeByte(4);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(blobStream, 60L);
        batchStatement.addParameterValues(Collections.singletonList(1L));
        batchStatement.addParameterValues(Collections.singletonList(2L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(2);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(2, new int[]{1, 1}));
        })) {
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(2));
            assertThat((byte[]) actualParams.get(0).get(0), is(new byte[]{1, 2, 3}));
            assertThat((byte[]) actualParams.get(1).get(0), is(new byte[]{4}));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transliteratedStreamBlobArguments")
    @SuppressWarnings("unchecked")
    void assertExecuteWithTransliteratedStreamBlob(final String name, final ByteBuf blobStream, final long length, final byte[] expected) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(blobStream, length);
        batchStatement.setDefaultBpb(new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 53, 3, 1, 1});
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute();
            assertThat(actualParams.size(), is(1));
            assertThat((byte[]) actualParams.get(0).get(0), is(expected));
        }
    }
    
    private static Stream<Arguments> transliteratedStreamBlobArguments() {
        byte[] bpb = {1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 53, 3, 1, 0};
        return Stream.of(
                Arguments.of("default_bpb", Unpooled.buffer().writeLong(1L).writeInt(2).writeInt(0).writeBytes("é".getBytes(StandardCharsets.UTF_8)),
                        20L, "é".getBytes(Charset.forName("windows-1252"))),
                Arguments.of("continued_character",
                        Unpooled.buffer().writeLong(1L).writeInt(23).writeInt(bpb.length).writeBytes(bpb).writeInt(2).writeByte('a').writeByte(0xC3).writeInt(1).writeByte(0xA9), 40L,
                        new byte[]{'a', (byte) 0xE9}),
                Arguments.of("empty_blob", Unpooled.buffer().writeLong(1L).writeInt(0).writeInt(0), 16L, new byte[0]));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTransliteratedStreamBlobArguments")
    void assertExecuteWithInvalidTransliteratedStreamBlob(final String name, final ByteBuf blobStream, final long length) {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchStatement batchStatement = createStreamBlobBatchStatement(blobStream, length);
        batchStatement.addParameterValues(Collections.singletonList(1L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            assertThrows(TransliterationFailedException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertTrue(mockedExecutor.constructed().isEmpty());
            assertTrue(batchStatement.getBlobBuffer().isEmpty());
            assertTrue(batchStatement.getParameterValues().isEmpty());
        }
    }
    
    private static Stream<Arguments> invalidTransliteratedStreamBlobArguments() {
        byte[] segmentedBpb = {1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 52, 3, 1, 0};
        byte[] streamBpb = {1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 52, 3, 1, 1};
        return Stream.of(
                Arguments.of("split_first_character",
                        Unpooled.buffer().writeLong(1L).writeInt(23).writeInt(segmentedBpb.length).writeBytes(segmentedBpb).writeInt(1).writeByte(0xD0).writeInt(1).writeByte(0xB2), 40L),
                Arguments.of("invalid_second_segment",
                        Unpooled.buffer().writeLong(1L).writeInt(23).writeInt(segmentedBpb.length).writeBytes(segmentedBpb).writeInt(1).writeByte('a').writeInt(1).writeByte(0xFF), 40L),
                Arguments.of("native_put_boundary", Unpooled.buffer().writeLong(1L).writeInt(32785).writeInt(streamBpb.length).writeBytes(streamBpb).writeZero(32768).writeByte(0xFF), 32804L));
    }
    
    private FirebirdBatchStatement createStreamBlobBatchStatement(final ByteBuf blobStream, final long length) {
        FirebirdBatchColumnDescriptor columnDescriptor = createBlobColumnDescriptor(true);
        FirebirdBatchStatement result = new FirebirdBatchStatement(STATEMENT_ID, Collections.singletonList(columnDescriptor), Math.max(1024L, length), false, false, true);
        result.appendBlobStream(blobStream, length);
        return result;
    }
    
    @Test
    void assertStreamBlobIdRemainsUniqueAfterUnknownIdFailure() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(4);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        ByteBuf data = Unpooled.buffer().writeLong(1L).writeInt(0).writeInt(0);
        FirebirdBatchStatement batch = createStreamBlobBatchStatement(data, 16L);
        data.release();
        batch.addParameterValues(Collections.singletonList(2L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batch);
        assertThrows(UnknownBatchBlobIdException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
        FirebirdBatchRegisterBlobCommandPacket registerPacket = mock(FirebirdBatchRegisterBlobCommandPacket.class);
        when(registerPacket.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(registerPacket.getBatchBlobId()).thenReturn(1L);
        RepeatedBatchBlobIdException actual = assertThrows(RepeatedBatchBlobIdException.class, () -> new FirebirdBatchRegisterBlobCommandExecutor(registerPacket, connectionSession).execute());
        assertThat(actual.getBatchBlobId(), is(1L));
        assertTrue(batch.getBlobIds().isEmpty());
        assertTrue(batch.getStreamBlobContents().containsKey(1L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReadBlobArguments")
    void assertExecuteRejectsReadBlobConversion(final String name, final int sourceSubType, final int targetSubType, final int sourceCharsetId, final int connectionCharsetId, final byte[] content,
                                                final int targetType, final Class<? extends SQLDialectException> expectedErrorType) {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get()).thenReturn(connectionCharsetId);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2)
                .writeByte(BlrConstants.blr_blob2).writeShortLE(sourceSubType).writeShortLE(sourceCharsetId).writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end)
                .writeByte(BlrConstants.blr_eoc);
        FirebirdBatchStatement batch = new FirebirdBatchStatement(STATEMENT_ID, FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields(), 1024L, true, false, true);
        blr.release();
        ByteBuf data = Unpooled.buffer().writeLong(1L).writeInt(content.length).writeInt(0).writeBytes(content);
        byte[] nextContent = Types.BOOLEAN == targetType ? "TRUE".getBytes(StandardCharsets.US_ASCII) : new byte[]{'a'};
        data.writeLong(2L).writeInt(nextContent.length).writeInt(0).writeBytes(nextContent);
        batch.appendBlobStream(data, (16L + content.length + 3 & ~3L) + (16L + nextContent.length + 3 & ~3L));
        data.release();
        batch.addParameterValues(Collections.singletonList(1L));
        batch.addParameterValues(Collections.singletonList(2L));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batch);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", targetType, false, false, true, true, false, true);
        when(preparedStatement.getParameterColumns()).thenReturn(Collections.singletonList(
                new FirebirdReturnColumnPacket(Collections.emptyList(), 1, table, column, null, null, null, null, Types.BLOB == targetType, targetSubType)));
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual.getConversionError(), isA(expectedErrorType));
            assertTrue(mockedExecutor.constructed().isEmpty());
        }
    }
    
    private static Stream<Arguments> invalidReadBlobArguments() {
        return Stream.of(
                Arguments.of("native_charset_without_java_decoder", 1, 1, 19, 4, new byte[]{(byte) 0xC3, (byte) 0xA9}, Types.BLOB, UnsupportedBlobFilterException.class),
                Arguments.of("malformed_suffix", 1, 1, 4, 52, new byte[]{'a', (byte) 0xFF}, Types.BLOB, TransliterationFailedException.class),
                Arguments.of("incomplete_suffix", 1, 1, 4, 52, new byte[]{'a', (byte) 0xD0}, Types.BLOB, TransliterationFailedException.class),
                Arguments.of("malformed_none_charset", 1, 1, 0, 4, new byte[]{(byte) 0xFF}, Types.BLOB, MalformedStringException.class),
                Arguments.of("malformed_binary_charset", 1, 1, 1, 4, new byte[]{(byte) 0xFF}, Types.BLOB, MalformedStringException.class),
                Arguments.of("incomplete_none_charset", 1, 1, 0, 4, new byte[]{'a', (byte) 0xD0}, Types.BLOB, MalformedStringException.class),
                Arguments.of("gb2312_bad_tail", 1, 1, 0, 57, new byte[]{(byte) 0xA1, (byte) 0x80}, Types.BLOB, MalformedStringException.class),
                Arguments.of("invalid_boolean_word", 1, 0, 4, 4, "garbage".getBytes(StandardCharsets.US_ASCII), Types.BOOLEAN, ConversionErrorException.class),
                Arguments.of("invalid_boolean_number", 1, 0, 4, 4, new byte[]{'1'}, Types.BOOLEAN, ConversionErrorException.class),
                Arguments.of("empty_boolean", 1, 0, 4, 4, new byte[0], Types.BOOLEAN, ConversionErrorException.class),
                Arguments.of("internal_subtype_to_text", 2, 1, 4, 4, new byte[]{'a', 'b'}, Types.BLOB, UnsupportedBlobFilterException.class),
                Arguments.of("text_to_other_subtype", 1, 2, 4, 4, new byte[]{'a', 'b'}, Types.BLOB, BlobFilterNotFoundException.class));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteWithConvertedBlobParameters() throws SQLException {
        mockBlobParameterBatch(Collections.singletonList(Arrays.asList(10L, 11L)), Arrays.asList(createParameterColumn(Types.INTEGER, null), createParameterColumn(Types.VARCHAR, 20)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 10L, transactionId)).thenReturn(Optional.of("231".getBytes(StandardCharsets.US_ASCII)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 11L, transactionId)).thenReturn(Optional.of("short".getBytes(StandardCharsets.US_ASCII)));
        List<List<Object>> actualParams = new ArrayList<>(1);
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class, (mock, context) -> {
            actualParams.addAll((List<List<Object>>) context.arguments().get(2));
            when(mock.executeBatch()).thenReturn(new FirebirdBatchCompletion(1, new int[]{1}));
        })) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> mockedExecutor = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
            ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual.getConversionError(), isA(InvalidSegstrIdException.class));
            assertTrue(mockedExecutor.constructed().isEmpty());
        }
    }
    
    @Test
    void assertExecuteWithLastParameterConversionFailure() {
        mockBlobParameterBatch(Collections.singletonList(Arrays.asList(10L, 11L)), Arrays.asList(createParameterColumn(Types.INTEGER, null), createParameterColumn(Types.VARCHAR, 4)));
        when(blobWriteCache.useBlobData(CONNECTION_ID, 11L, transactionId)).thenReturn(Optional.of("too-long".getBytes(StandardCharsets.US_ASCII)));
        try (MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class)) {
            when(FirebirdBlobWriteCache.getInstance()).thenReturn(blobWriteCache);
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
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(preparedStatement.getParameterColumns()).thenReturn(parameterColumns);
    }
    
    private FirebirdReturnColumnPacket createParameterColumn(final int dataType, @Nullable final Integer length) {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return new FirebirdReturnColumnPacket(Collections.emptyList(), 1, table, new ShardingSphereColumn("foo_col", dataType, false, false, true, true, false, true), null, null, null, length, false,
                null);
    }
    
    @Test
    void assertExecuteWhenBatchExecutionFailed() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        when(batchStatement.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(batchStatement.getParameterValues()).thenReturn(Collections.singletonList(Collections.singletonList("foo_value")));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        SQLException expected = new SQLException("batch execution failed");
        try (
                MockedConstruction<FirebirdBatchedStatementsExecutor> ignored = mockConstruction(FirebirdBatchedStatementsExecutor.class,
                        (mock, context) -> when(mock.executeBatch()).thenThrow(expected))) {
            SQLException actual = assertThrows(SQLException.class, () -> new FirebirdBatchExecuteCommandExecutor(packet, connectionSession).execute());
            assertThat(actual, is(expected));
            verify(batchStatement, never()).reset();
        }
    }
    
    @Test
    void assertExecuteWithNoBatchStatement() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        FirebirdBatchExecuteCommandExecutor executor = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession);
        assertThrows(InvalidBatchHandleException.class, executor::execute);
        verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
    }
    
    @Test
    void assertExecuteWithClosedTransactionHandle() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionHandle()).thenReturn(transactionId);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdBatchExecuteCommandExecutor executor = new FirebirdBatchExecuteCommandExecutor(packet, connectionSession);
        assertThrows(InvalidTransactionHandleException.class, executor::execute);
        verify(connectionSession.getServerPreparedStatementRegistry(), never()).getPreparedStatement(anyInt());
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
    
    @SuppressWarnings("unchecked")
    private List<DetailedError> getDetailedErrors(final FirebirdBatchCompletionStateResponse response) throws ReflectiveOperationException {
        return (List<DetailedError>) Plugins.getMemberAccessor().get(FirebirdBatchCompletionStateResponse.class.getDeclaredField("detailedErrors"), response);
    }
}
