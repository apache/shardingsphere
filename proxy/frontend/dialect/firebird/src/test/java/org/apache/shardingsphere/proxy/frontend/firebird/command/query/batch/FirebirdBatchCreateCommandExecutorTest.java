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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchAlreadyOpenedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchParametersRequiredException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchMessageFormatException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchParameterVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBatchCreateCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 11;
    
    private static final long DEFAULT_BUFFER_SIZE = 16L * 1024 * 1024;
    
    private static final long MAX_BUFFER_SIZE = 256L * 1024 * 1024;
    
    private static final int BATCH_VERSION_1 = 1;
    
    private static final int TAG_MULTIERROR = 1;
    
    private static final int TAG_RECORD_COUNTS = 2;
    
    private static final int TAG_BUFFER_BYTES_SIZE = 3;
    
    private static final int TAG_BLOB_POLICY = 4;
    
    private static final int BLOB_NONE = 0;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchCreateCommandPacket packet;
    
    @Mock
    private FirebirdServerPreparedStatement preparedStatement;
    
    @AfterEach
    void tearDown() {
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() throws SQLException {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBatchBlr());
        when(packet.getBatchMessageLength()).thenReturn(6L);
        when(packet.getBatchParametersBuffer()).thenReturn(Unpooled.EMPTY_BUFFER);
        Collection<DatabasePacket> actual = new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        DatabasePacket actualPacket = actual.iterator().next();
        assertThat(actualPacket, isA(FirebirdGenericResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) actualPacket).getHandle(), is(STATEMENT_ID));
        FirebirdBatchStatement actualBatchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertNotNull(actualBatchStatement);
        assertThat(actualBatchStatement.getStatementHandle(), is(STATEMENT_ID));
        assertFalse(actualBatchStatement.isRecordCounts());
        assertFalse(actualBatchStatement.isMultiError());
        assertThat(actualBatchStatement.getColumnDescriptors().size(), is(1));
        assertThat(actualBatchStatement.getColumnDescriptors().get(0).getType(), is(FirebirdBinaryColumnType.LEGACY_TEXT));
        assertThat(actualBatchStatement.getColumnDescriptors().get(0).getLength(), is(4));
        assertThat(actualBatchStatement.getColumnDescriptors().get(0).getScale(), is(0));
        assertThat(actualBatchStatement.getColumnDescriptors().get(0).getOffset(), is(0));
    }
    
    @Test
    void assertExecuteWithRecordCounts() throws SQLException {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBatchBlr());
        when(packet.getBatchMessageLength()).thenReturn(6L);
        when(packet.getBatchParametersBuffer()).thenReturn(createBatchParametersBuffer(TAG_RECORD_COUNTS, 1));
        new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute();
        assertTrue(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID).isRecordCounts());
    }
    
    @Test
    void assertExecuteWithMultiError() throws SQLException {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBatchBlr());
        when(packet.getBatchMessageLength()).thenReturn(6L);
        when(packet.getBatchParametersBuffer()).thenReturn(createBatchParametersBuffer(TAG_MULTIERROR, 1));
        new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute();
        assertTrue(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID).isMultiError());
    }
    
    @Test
    void assertExecuteWhenBatchAlreadyOpen() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchStatement expectedBatchStatement = new FirebirdBatchStatement(STATEMENT_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, expectedBatchStatement);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        assertThrows(BatchAlreadyOpenedException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
        assertThat(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID), is(expectedBatchStatement));
    }
    
    @Test
    void assertExecuteWhenStatementDoesNotExist() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(null);
        assertThrows(InvalidStatementHandleException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
        verify(packet, never()).getBatchBlr();
    }
    
    @Test
    void assertExecuteWhenBatchBlrHasNoFields() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createEmptyBatchBlr());
        assertThrows(BatchParametersRequiredException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWhenMessageLengthMismatched() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBatchBlr());
        when(packet.getBatchMessageLength()).thenReturn(5L);
        assertThrows(InvalidBatchMessageFormatException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWhenBatchBlrContainsBlob() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBlobBatchBlr());
        FirebirdProtocolException actual = assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
        assertThat(actual.getMessage(), is("BLOB fields are not supported in Firebird batch operations"));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertBlobBatchCreateProducesFirebirdErrorResponseWithoutClosingChannel() {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(STATEMENT_ID)).thenReturn(preparedStatement);
        when(packet.getBatchBlr()).thenReturn(createBlobBatchBlr());
        FirebirdProtocolException cause = assertThrows(FirebirdProtocolException.class, () -> new FirebirdBatchCreateCommandExecutor(packet, connectionSession).execute());
        FirebirdGenericResponsePacket errorPacket = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(cause);
        assertThat(errorPacket.getErrorMessage(), containsString("BLOB fields are not supported in Firebird batch operations"));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertParseBatchParametersWithEmptyBuffer() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.EMPTY_BUFFER);
        assertThat(actual.getVersion(), is(BATCH_VERSION_1));
        assertThat(actual.getBufferSize(), is(DEFAULT_BUFFER_SIZE));
        assertFalse(actual.isRecordCounts());
        assertFalse(actual.isMultiError());
    }
    
    @Test
    void assertParseBatchParametersWithBufferSize() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_BUFFER_BYTES_SIZE, 1024));
        assertThat(actual.getBufferSize(), is(1024L));
    }
    
    @Test
    void assertParseBatchParametersWithMaximumBufferSize() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_BUFFER_BYTES_SIZE, 0));
        assertThat(actual.getBufferSize(), is(MAX_BUFFER_SIZE));
    }
    
    @Test
    void assertParseBatchParametersWithTooLargeBufferSize() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_BUFFER_BYTES_SIZE, -1));
        assertThat(actual.getBufferSize(), is(MAX_BUFFER_SIZE));
    }
    
    @Test
    void assertParseBatchParametersWithRecordCounts() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_RECORD_COUNTS, 1));
        assertTrue(actual.isRecordCounts());
    }
    
    @Test
    void assertParseBatchParametersWithMultiError() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_MULTIERROR, 1));
        assertTrue(actual.isMultiError());
    }
    
    @Test
    void assertParseBatchParametersWithDisabledMultiError() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual = FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_MULTIERROR, 0));
        assertFalse(actual.isMultiError());
    }
    
    @Test
    void assertParseBatchParametersWithBlobPolicy() {
        FirebirdProtocolException actual = assertThrows(FirebirdProtocolException.class,
                () -> FirebirdBatchCreateCommandExecutor.BatchParameters.parse(createBatchParametersBuffer(TAG_BLOB_POLICY, BLOB_NONE)));
        assertThat(actual.getMessage(), is("BLOB policy is not supported in Firebird batch operations"));
    }
    
    @Test
    void assertParseBatchParametersWhenVersionInvalid() {
        assertThrows(InvalidBatchParameterVersionException.class, () -> FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.wrappedBuffer(new byte[]{2})));
    }
    
    @Test
    void assertParseBatchParametersWhenClumpletTruncated() {
        assertThrows(FirebirdProtocolException.class, () -> FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.wrappedBuffer(new byte[]{BATCH_VERSION_1, TAG_BUFFER_BYTES_SIZE})));
    }
    
    @Test
    void assertParseBatchParametersWhenValueTruncated() {
        assertThrows(FirebirdProtocolException.class,
                () -> FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.buffer().writeByte(BATCH_VERSION_1).writeByte(TAG_BUFFER_BYTES_SIZE).writeIntLE(4)));
    }
    
    @Test
    void assertParseBatchParametersWhenIntegerLengthInvalid() {
        assertThrows(FirebirdProtocolException.class,
                () -> FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.buffer().writeByte(BATCH_VERSION_1).writeByte(TAG_BUFFER_BYTES_SIZE).writeIntLE(1).writeByte(1)));
    }
    
    @Test
    void assertParseBatchParametersWithUnknownTag() {
        FirebirdBatchCreateCommandExecutor.BatchParameters actual =
                FirebirdBatchCreateCommandExecutor.BatchParameters.parse(Unpooled.buffer().writeByte(BATCH_VERSION_1).writeByte(99).writeIntLE(1).writeByte(1));
        assertThat(actual.getVersion(), is(BATCH_VERSION_1));
        assertThat(actual.getBufferSize(), is(DEFAULT_BUFFER_SIZE));
        assertFalse(actual.isRecordCounts());
    }
    
    private ByteBuf createBatchBlr() {
        return Unpooled.wrappedBuffer(new byte[]{
                (byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0,
                2, 0, (byte) BlrConstants.blr_text, 4, 0, (byte) BlrConstants.blr_short, 0, (byte) BlrConstants.blr_end, (byte) BlrConstants.blr_eoc});
    }
    
    private ByteBuf createEmptyBatchBlr() {
        return Unpooled.wrappedBuffer(new byte[]{
                (byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0, 0, 0, (byte) BlrConstants.blr_end, (byte) BlrConstants.blr_eoc});
    }
    
    private ByteBuf createBlobBatchBlr() {
        return Unpooled.wrappedBuffer(new byte[]{
                (byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0,
                2, 0, (byte) BlrConstants.blr_blob2, 0, 0, 0, 0, (byte) BlrConstants.blr_short, 0, (byte) BlrConstants.blr_end, (byte) BlrConstants.blr_eoc});
    }
    
    private ByteBuf createBatchParametersBuffer(final int tag, final int value) {
        return Unpooled.buffer().writeByte(BATCH_VERSION_1).writeByte(tag).writeIntLE(4).writeIntLE(value);
    }
}
