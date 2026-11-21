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

package org.apache.shardingsphere.proxy.frontend.firebird;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchDefaultBpbChangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchWithoutBlobsException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchBlobPolicyException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchStatementTypeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.RepeatedBatchBlobIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStreamBlob;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler;
import org.apache.shardingsphere.proxy.frontend.netty.ProxyFlowControlHandler;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.CircuitBreakProxyState;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.BlrConstants;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, FirebirdConnectionResourceManager.class})
class FirebirdFrontendEngineTest {
    
    private static final int BATCH_CONNECTION_ID = 19342;
    
    private static final int BATCH_STATEMENT_HANDLE = 1;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyContext proxyContext;
    
    @Mock
    private FirebirdConnectionResourceManager connectionResourceManager;
    
    @AfterEach
    void tearDownBatchConnection() {
        FirebirdBatchRegistry.getInstance().unregisterConnection(BATCH_CONNECTION_ID);
        FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(BATCH_CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(BATCH_CONNECTION_ID);
    }
    
    @Test
    void assertGetAuthenticationEngine() {
        assertThat(engine.getAuthenticationEngine(), isA(FirebirdAuthenticationEngine.class));
    }
    
    @Test
    void assertGetCommandExecuteEngine() {
        assertThat(engine.getCommandExecuteEngine(), isA(FirebirdCommandExecuteEngine.class));
    }
    
    @Test
    void assertGetCodecEngine() {
        assertThat(engine.getCodecEngine(), isA(FirebirdPacketCodecEngine.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("duplicateCreateCases")
    void assertInitChannelPreservesDuplicateContinuation(final String name, final int duplicateBlrType, final boolean splitContinuation) {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        FirebirdBatchStatement expectedBatch = registerBatch();
        channel.writeInbound(buildBlobStream(32, Unpooled.buffer().writeLong(1L).writeInt(20).writeInt(0).writeZero(16)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        ByteBuf continuation = buildBlobStream(4, Unpooled.buffer().writeInt(123));
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchCreate(duplicateBlrType, false), splitContinuation ? continuation.readRetainedSlice(13) : continuation,
                splitContinuation ? Unpooled.EMPTY_BUFFER : buildBatchMessage(true, 100), splitContinuation ? Unpooled.EMPTY_BUFFER : buildBatchSync());
        channel.writeInbound(in);
        commands.executeAll(channel);
        if (splitContinuation) {
            assertResponses(channel, ISCConstants.isc_batch_open);
            ByteBuf tail = continuation.readRetainedSlice(continuation.readableBytes());
            continuation.release();
            channel.writeInbound(Unpooled.wrappedUnmodifiableBuffer(tail, buildBatchMessage(true, 100), buildBatchSync()));
            commands.executeAll(channel);
            assertResponses(channel, 0, 0, 0);
        } else {
            assertResponses(channel, ISCConstants.isc_batch_open, 0, 0, 0);
        }
        FirebirdBatchStatement actualBatch = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        assertThat(actualBatch, sameInstance(expectedBatch));
        assertThat(actualBatch.getParameterValues(), is(Collections.singletonList(Collections.singletonList(100L))));
        assertThat(actualBatch.getBlobStreamSize(), is(36L));
        FirebirdBatchStreamBlob actualBlob = actualBatch.readStreamBlobs().iterator().next();
        byte[] expectedData = new byte[20];
        expectedData[19] = 123;
        assertThat(actualBlob.getData().toByteArray(), is(expectedData));
        assertTrue(channel.isOpen());
        assertThat(in.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    private EmbeddedChannel createBatchChannel(final BatchCommandHandler commands) {
        when(connectionSession.getConnectionId()).thenReturn(BATCH_CONNECTION_ID);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        ServerPreparedStatementRegistry preparedStatements = new ServerPreparedStatementRegistry();
        preparedStatements.addPreparedStatement(BATCH_STATEMENT_HANDLE, mock(FirebirdServerPreparedStatement.class, Answers.RETURNS_DEEP_STUBS));
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(preparedStatements);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        when(proxyContext.getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        FirebirdBatchRegistry.getInstance().registerConnection(BATCH_CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().registerConnection(BATCH_CONNECTION_ID);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(BATCH_CONNECTION_ID, FirebirdProtocolVersion.PROTOCOL_VERSION16);
        EmbeddedChannel result = new EmbeddedChannel(new PacketCodec(engine.getCodecEngine()), new ProxyFlowControlHandler());
        result.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        result.attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).set(FirebirdProtocolVersion.PROTOCOL_VERSION16);
        result.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        result.pipeline().addLast(FrontendChannelInboundHandler.class.getSimpleName(), commands);
        engine.initChannel(result);
        result.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue()).writeInt(0));
        commands.executeAll(result);
        assertResponses(result, 0);
        commands.commandTypes.clear();
        return result;
    }
    
    private FirebirdBatchStatement registerBatch() {
        ByteBuf blr = buildBatchBlr(BlrConstants.blr_blob2);
        FirebirdBatchStatement result = new FirebirdBatchStatement(BATCH_STATEMENT_HANDLE, FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields(), 16777216L, false, false, true);
        blr.release();
        FirebirdBatchRegistry.getInstance().registerBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE, result);
        return result;
    }
    
    private ByteBuf buildBatchBlr(final int blrType) {
        ByteBuf result = Unpooled.buffer().writeByte(blrType < 0 ? 99 : BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2)
                .writeByte(blrType < 0 ? BlrConstants.blr_long : blrType);
        if (BlrConstants.blr_blob2 == blrType) {
            result.writeZero(4);
        } else {
            result.writeByte(0);
        }
        return result.writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
    }
    
    private ByteBuf buildBlobStream(final int length, final ByteBuf data) {
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(length).writeBytes(data);
        data.release();
        return result;
    }
    
    private void assertResponses(final EmbeddedChannel channel, final int... expectedErrors) {
        for (int each : expectedErrors) {
            ByteBuf actual = channel.readOutbound();
            assertNotNull(actual);
            assertThat(actual.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
            assertThat(actual.getInt(20), is(0 == each ? 0 : 1));
            if (0 != each) {
                assertThat(actual.getInt(24), is(each));
            }
            actual.release();
        }
        assertNull(channel.readOutbound());
    }
    
    private ByteBuf buildBatchCreate(final int blrType, final boolean invalidParams) {
        ByteBuf blr = buildBatchBlr(blrType);
        int blrLength = blr.readableBytes();
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(blrLength).writeBytes(blr)
                .writeZero((4 - blrLength % 4) % 4).writeInt(BlrConstants.blr_int64 == blrType || BlrConstants.blr_blob2 == blrType ? 10 : 6);
        if (invalidParams) {
            result.writeInt(1).writeByte(2).writeZero(3);
        } else {
            result.writeInt(10).writeBytes(new byte[]{1, 4, 4, 0, 0, 0, 3, 0, 0, 0}).writeZero(2);
        }
        blr.release();
        return result;
    }
    
    private ByteBuf buildBatchMessage(final boolean wide, final int value) {
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(1).writeByte(0).writeZero(3);
        if (wide) {
            result.writeLong(value);
        } else {
            result.writeInt(value);
        }
        return result;
    }
    
    private ByteBuf buildBatchSync() {
        return Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_SYNC.getValue());
    }
    
    private static Stream<Arguments> duplicateCreateCases() {
        return Stream.of(
                Arguments.of("different format", BlrConstants.blr_long, false),
                Arguments.of("malformed format", -1, false),
                Arguments.of("fragmented continuation", BlrConstants.blr_long, true));
    }
    
    @Test
    void assertInitChannelCreatesBatchAfterQueuedRelease() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        FirebirdBatchStatement previousBatch = registerBatch();
        channel.writeInbound(buildBlobStream(32, Unpooled.buffer().writeLong(1L).writeInt(20).writeInt(0).writeZero(16)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchRelease(), buildBatchCreate(BlrConstants.blr_blob2, false),
                buildBlobStream(16, Unpooled.buffer().writeLong(2L).writeInt(0).writeInt(0)), buildBatchMessage(true, 321), buildBatchSync());
        channel.writeInbound(in);
        assertThat(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE), sameInstance(previousBatch));
        commands.executeAll(channel);
        FirebirdBatchStatement actualBatch = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        assertNotNull(actualBatch);
        assertThat(actualBatch, not(sameInstance(previousBatch)));
        assertThat(actualBatch.getParameterValues(), is(Collections.singletonList(Collections.singletonList(321L))));
        assertThat(actualBatch.getBlobStreamSize(), is(16L));
        assertThat(actualBatch.readStreamBlobs().iterator().next().getBatchBlobId(), is(2L));
        assertResponses(channel, 0, 0, 0, 0, 0);
        channel.writeInbound(buildBatchRelease());
        commands.executeAll(channel);
        assertResponses(channel, 0);
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE));
        assertThat(in.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    private ByteBuf buildBatchRelease() {
        return Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_RLS.getValue()).writeInt(BATCH_STATEMENT_HANDLE);
    }
    
    @Test
    void assertInitChannelReleasesBlobStateOnDrop() throws ReflectiveOperationException {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        channel.writeInbound(buildBlobStream(65552, Unpooled.buffer().writeLong(1L).writeInt(100000).writeInt(100000).writeByte(1).writeZero(65535)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        Map<?, ?> actualStreams = getCodecBlobStreams();
        assertTrue(actualStreams.containsKey(BATCH_STATEMENT_HANDLE));
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.FREE_STATEMENT.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(FirebirdFreeStatementPacket.DROP));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        assertTrue(actualStreams.isEmpty());
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE));
        channel.finishAndReleaseAll();
    }
    
    private Map<?, ?> getCodecBlobStreams() throws ReflectiveOperationException {
        return (Map<?, ?>) Plugins.getMemberAccessor().get(FirebirdPacketCodecEngine.class.getDeclaredField("batchBlobStreams"), engine.getCodecEngine());
    }
    
    @Test
    void assertInitChannelKeepsBlobStateAfterRejectedFree() throws ReflectiveOperationException {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        channel.writeInbound(buildBlobStream(32, Unpooled.buffer().writeLong(1L).writeInt(20).writeInt(0).writeZero(16)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        Map<?, ?> actualStreams = getCodecBlobStreams();
        Object expectedStream = actualStreams.get(BATCH_STATEMENT_HANDLE);
        assertNotNull(expectedStream);
        FirebirdBatchStatement expectedBatch = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.FREE_STATEMENT.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(99));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_random);
        assertThat(actualStreams.get(BATCH_STATEMENT_HANDLE), sameInstance(expectedStream));
        assertThat(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE), sameInstance(expectedBatch));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("retainedStatementOperations")
    void assertInitChannelPreservesFramingAfterRelease(final String name, final int freeOption) {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        channel.writeInbound(buildBlobStream(32, Unpooled.buffer().writeLong(1L).writeInt(100).writeInt(0).writeZero(16)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        ByteBuf operation = 0 == freeOption
                ? buildBatchRelease()
                : Unpooled.buffer().writeInt(FirebirdCommandPacketType.FREE_STATEMENT.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(freeOption);
        channel.writeInbound(operation);
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(buildBlobStream(4, Unpooled.buffer().writeInt(123)));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_bad_batch_handle);
        channel.writeInbound(Unpooled.wrappedUnmodifiableBuffer(buildBlobStream(4, Unpooled.buffer().writeInt(123)), buildBatchSync()));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_bad_batch_handle, 0);
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }
    
    private static Stream<Arguments> retainedStatementOperations() {
        return Stream.of(
                Arguments.of("release batch", 0),
                Arguments.of("close statement", FirebirdFreeStatementPacket.CLOSE),
                Arguments.of("unprepare statement", FirebirdFreeStatementPacket.UNPREPARE));
    }
    
    @Test
    void assertInitChannelRetiresRejectedBlobHandles() throws ReflectiveOperationException {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        Map<?, ?> actualStreams = getCodecBlobStreams();
        ByteBuf[] requests = {
                buildBlobStream(0, Unpooled.buffer()).setInt(4, BATCH_STATEMENT_HANDLE + 1),
                Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_SET_BPB.getValue()).writeInt(BATCH_STATEMENT_HANDLE + 2).writeInt(4).writeBytes(new byte[]{1, 3, 1, 1}),
                buildBlobStream(65552, Unpooled.buffer().writeLong(1L).writeInt(100000).writeInt(100000).writeByte(1).writeZero(65535)).setInt(4, BATCH_STATEMENT_HANDLE + 3)};
        for (ByteBuf each : requests) {
            channel.writeInbound(each);
            commands.executeAll(channel);
            assertResponses(channel, ISCConstants.isc_bad_batch_handle);
            assertThat(actualStreams.size(), is(1));
            assertTrue(actualStreams.containsKey(BATCH_STATEMENT_HANDLE));
        }
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertInitChannelFramesRejectedNewCreate() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchCreate(BlrConstants.blr_int64, true), buildBatchMessage(true, 321), buildBatchSync());
        channel.writeInbound(in);
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_batch_param_version, ISCConstants.isc_bad_batch_handle, 0);
        assertThat(commands.commandTypes, is(Arrays.asList(FirebirdCommandPacketType.BATCH_CREATE, FirebirdCommandPacketType.BATCH_MSG, FirebirdCommandPacketType.BATCH_SYNC)));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE));
        assertTrue(channel.isOpen());
        assertThat(in.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertInitChannelFramesRejectedCreateAfterRelease() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchRelease(), buildBatchCreate(BlrConstants.blr_int64, true), buildBatchMessage(true, 321), buildBatchSync());
        channel.writeInbound(in);
        commands.executeAll(channel);
        assertResponses(channel, 0, ISCConstants.isc_batch_param_version, ISCConstants.isc_bad_batch_handle, 0);
        assertThat(commands.commandTypes, is(Arrays.asList(
                FirebirdCommandPacketType.BATCH_RLS,
                FirebirdCommandPacketType.BATCH_CREATE,
                FirebirdCommandPacketType.BATCH_MSG,
                FirebirdCommandPacketType.BATCH_SYNC)));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE));
        assertTrue(channel.isOpen());
        assertThat(in.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("apiRejectedCreateCases")
    void assertInitChannelResetsFramingAfterApiRejectedCreate(final String name, final boolean selectStatement, final boolean emptyBlr, final int invalidIntegerTag, final int expectedError) {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        if (selectStatement) {
            FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(BATCH_STATEMENT_HANDLE);
            when(preparedStatement.getSqlStatementContext().getSqlStatement()).thenReturn(mock(SelectStatement.class));
        }
        registerBatch();
        channel.writeInbound(buildBlobStream(20, Unpooled.buffer().writeLong(1L).writeInt(8).writeInt(0).writeInt(123)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        commands.commandTypes.clear();
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchRelease(), buildRejectedBatchCreate(emptyBlr, false, false, invalidIntegerTag),
                buildBlobStream(16, Unpooled.buffer().writeLong(2L).writeInt(0).writeInt(0)), buildBatchSync());
        channel.writeInbound(in);
        commands.executeAll(channel);
        assertResponses(channel, 0, expectedError, ISCConstants.isc_bad_batch_handle, 0);
        assertThat(commands.commandTypes, is(Arrays.asList(FirebirdCommandPacketType.BATCH_RLS, FirebirdCommandPacketType.BATCH_CREATE,
                FirebirdCommandPacketType.BATCH_BLOB_STREAM, FirebirdCommandPacketType.BATCH_SYNC)));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE));
        assertTrue(channel.isOpen());
        assertThat(in.refCnt(), is(0));
        channel.finishAndReleaseAll();
    }
    
    private static Stream<Arguments> apiRejectedCreateCases() {
        return Stream.of(
                Arguments.of("missing_parameters", false, true, 0, ISCConstants.isc_batch_param),
                Arguments.of("select_statement", true, false, 0, ISCConstants.isc_sqlerr),
                Arguments.of("select_without_parameters", true, true, 0, ISCConstants.isc_sqlerr),
                Arguments.of("invalid_multierror_integer", false, false, 1, ISCConstants.isc_random),
                Arguments.of("invalid_buffer_size_integer", false, false, 3, ISCConstants.isc_random));
    }
    
    private ByteBuf buildRejectedBatchCreate(final boolean emptyBlr, final boolean invalidMessageLength, final boolean invalidParams, final int invalidIntegerTag) {
        if (0 != invalidIntegerTag) {
            return buildBatchCreateWithInvalidInteger(invalidIntegerTag);
        }
        return emptyBlr ? buildEmptyBatchCreate(invalidParams, invalidMessageLength) : buildBatchCreate(BlrConstants.blr_blob2, invalidParams);
    }
    
    private ByteBuf buildBatchCreateWithInvalidInteger(final int tag) {
        ByteBuf result = buildBatchCreate(BlrConstants.blr_blob2, false);
        int parametersOffset = 16 + (result.getInt(8) + 3 & ~3);
        result.writerIndex(parametersOffset);
        return result.writeInt(11).writeByte(1).writeByte(tag).writeIntLE(5).writeZero(6);
    }
    
    private ByteBuf buildEmptyBatchCreate(final boolean invalidParams, final boolean invalidMessageLength) {
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(0)
                .writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(blr.readableBytes()).writeBytes(blr)
                .writeInt(invalidMessageLength ? 4 : 0).writeInt(invalidParams ? 1 : 0);
        if (invalidParams) {
            result.writeByte(2).writeZero(3);
        }
        blr.release();
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("earlyRejectedCreateCases")
    void assertInitChannelRetainsFramingAfterEarlyRejectedCreate(final String name, final boolean emptyBlr, final boolean invalidMessageLength, final int invalidIntegerTag, final int expectedError) {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        channel.writeInbound(buildBlobStream(20, Unpooled.buffer().writeLong(1L).writeInt(12).writeInt(0).writeInt(123)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(buildBatchRelease());
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(buildRejectedBatchCreate(emptyBlr, invalidMessageLength, !invalidMessageLength, invalidIntegerTag));
        commands.executeAll(channel);
        assertResponses(channel, expectedError);
        commands.commandTypes.clear();
        channel.writeInbound(Unpooled.wrappedUnmodifiableBuffer(buildBlobStream(4, Unpooled.buffer().writeInt(123)), buildBatchSync()));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_bad_batch_handle, 0);
        assertThat(commands.commandTypes, is(Arrays.asList(FirebirdCommandPacketType.BATCH_BLOB_STREAM, FirebirdCommandPacketType.BATCH_SYNC)));
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedCreateDefaultBpbCases")
    void assertInitChannelUpdatesDefaultBpbAtNativeCreatePhase(final String name, final int invalidIntegerTag, final boolean expectedSegmented) {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        registerBatch();
        channel.writeInbound(Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_SET_BPB.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(4)
                .writeBytes(new byte[]{1, 3, 1, 0}));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(buildBlobStream(20, Unpooled.buffer().writeLong(1L).writeInt(12).writeInt(0).writeInt(10).writeZero(2)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(buildBatchRelease());
        commands.executeAll(channel);
        assertResponses(channel, 0);
        channel.writeInbound(0 == invalidIntegerTag ? buildBatchCreate(BlrConstants.blr_blob2, true) : buildBatchCreateWithInvalidInteger(invalidIntegerTag));
        commands.executeAll(channel);
        assertResponses(channel, 0 == invalidIntegerTag ? ISCConstants.isc_batch_param_version : ISCConstants.isc_random);
        channel.writeInbound(buildBlobStream(8, Unpooled.buffer().writeZero(8)));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_bad_batch_handle);
        ByteBuf data = Unpooled.buffer().writeLong(2L).writeInt(4).writeInt(0);
        if (expectedSegmented) {
            data.writeInt(2).writeZero(2);
        } else {
            data.writeInt(123);
        }
        channel.writeInbound(Unpooled.wrappedUnmodifiableBuffer(buildBlobStream(20, data), buildBatchSync()));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_bad_batch_handle, 0);
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }
    
    private static Stream<Arguments> rejectedCreateDefaultBpbCases() {
        return Stream.of(
                Arguments.of("parameter_version_preserves_default", 0, true),
                Arguments.of("record_counts_preserves_default", 2, true),
                Arguments.of("blob_policy_clears_default", 4, false));
    }
    
    private static Stream<Arguments> earlyRejectedCreateCases() {
        return Stream.of(
                Arguments.of("parameter_version", false, false, 0, ISCConstants.isc_batch_param_version),
                Arguments.of("empty_metadata_parameter_version", true, false, 0, ISCConstants.isc_batch_param_version),
                Arguments.of("empty_metadata_message_length", true, true, 0, ISCConstants.isc_sqlerr),
                Arguments.of("invalid_record_counts_integer", false, false, 2, ISCConstants.isc_random),
                Arguments.of("invalid_blob_policy_integer", false, false, 4, ISCConstants.isc_random));
    }
    
    @Test
    void assertInitChannelHandlesStandaloneMalformedCreate() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        channel.writeInbound(buildBatchCreate(-1, false));
        commands.executeAll(channel);
        assertResponses(channel, ISCConstants.isc_random);
        channel.writeInbound(buildBatchSync());
        commands.executeAll(channel);
        assertResponses(channel, 0);
        assertThat(commands.commandTypes, is(Arrays.asList(FirebirdCommandPacketType.BATCH_CREATE, FirebirdCommandPacketType.BATCH_SYNC)));
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertInitChannelResumesCircuitBreakResponse() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        commands.circuitBreak = true;
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchCreate(BlrConstants.blr_long, false), buildBatchSync());
        channel.writeInbound(in);
        channel.runPendingTasks();
        assertThat(commands.commandTypes, is(Arrays.asList(FirebirdCommandPacketType.BATCH_CREATE, FirebirdCommandPacketType.BATCH_SYNC)));
        assertTrue(commands.tasks.isEmpty());
        assertThat(commands.maximumReadDepth, is(1));
        ByteBuf actualCreateResponse = channel.readOutbound();
        ByteBuf actualSyncResponse = channel.readOutbound();
        assertNotNull(actualCreateResponse);
        assertNotNull(actualSyncResponse);
        assertThat(actualCreateResponse.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertThat(actualSyncResponse.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertThat(actualSyncResponse.getInt(24), is(actualCreateResponse.getInt(24)));
        assertNull(channel.readOutbound());
        assertTrue(channel.isOpen());
        assertThat(in.refCnt(), is(0));
        actualCreateResponse.release();
        actualSyncResponse.release();
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertRelease() {
        int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(FirebirdConnectionResourceManager.getInstance()).thenReturn(connectionResourceManager);
        engine.release(connectionSession);
        verify(connectionResourceManager).unregisterConnection(connectionId);
    }
    
    @Test
    void assertHandleException() {
        ConnectionContext connectionContext = mockConnectionSession(true, false);
        engine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    private ConnectionContext mockConnectionSession(final boolean inTransaction, final boolean exceptionOccur) {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(inTransaction);
        ConnectionContext result = new ConnectionContext(Collections::emptyList);
        result.getTransactionContext().setExceptionOccur(exceptionOccur);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        if (inTransaction) {
            when(connectionSession.getConnectionContext()).thenReturn(result);
        }
        return result;
    }
    
    @Test
    void assertHandleExceptionKeepsPreviousFailure() {
        ConnectionContext connectionContext = mockConnectionSession(true, true);
        engine.handleException(connectionSession, new Exception("error"));
        assertTrue(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideIgnoredExceptionParameters")
    void assertHandleIgnoredException(final String testName, final boolean inTransaction, final Exception exception) {
        ConnectionContext connectionContext = mockConnectionSession(inTransaction, false);
        engine.handleException(connectionSession, exception);
        assertFalse(connectionContext.getTransactionContext().isExceptionOccur());
    }
    
    private static Stream<Arguments> provideIgnoredExceptionParameters() {
        return Stream.of(
                Arguments.of("skip when in transaction exception", true, new InTransactionException()),
                Arguments.of("skip when invalid batch statement type exception", true, new InvalidBatchStatementTypeException()),
                Arguments.of("skip when invalid batch handle exception", true, new InvalidBatchHandleException(1)),
                Arguments.of("skip when batch blob continuation bpb exception", true, new BatchBlobContinuationBpbException(4L)),
                Arguments.of("skip when batch bpb too big exception", true, new BatchBpbTooBigException(8L, 4L)),
                Arguments.of("skip when batch default bpb change exception", true, new BatchDefaultBpbChangeException(1)),
                Arguments.of("skip when batch segment exceeds blob exception", true, new BatchSegmentExceedsBlobException(50, 10)),
                Arguments.of("skip when batch segment too big exception", true, new BatchSegmentTooBigException(50, 10)),
                Arguments.of("skip when batch small data exception", true, new BatchSmallDataException("BLOB")),
                Arguments.of("skip when batch too big exception", true, new BatchTooBigException(1, 16L, 16L, 16L)),
                Arguments.of("skip when batch without blobs exception", true, new BatchWithoutBlobsException(1)),
                Arguments.of("skip when invalid batch blob policy exception", true, new InvalidBatchBlobPolicyException("addBlobStream")),
                Arguments.of("skip when repeated batch blob id exception", true, new RepeatedBatchBlobIdException(1L)),
                Arguments.of("skip when blob filter not found exception", true, new BlobFilterNotFoundException(0, 2)),
                Arguments.of("skip when invalid bpb version exception", true, new InvalidBpbVersionException(2, 1)),
                Arguments.of("skip when parameter conversion exception", true, new ParameterConversionException(new StringTruncationException(20, 32))),
                Arguments.of("skip when transliteration failed exception", true, new TransliterationFailedException()),
                Arguments.of("skip when unknown batch blob id exception", true, new UnknownBatchBlobIdException(1L)),
                Arguments.of("skip when not in transaction", false, new Exception("error")));
    }
    
    private final class BatchCommandHandler extends ChannelInboundHandlerAdapter {
        
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        
        private final Collection<FirebirdCommandPacketType> commandTypes = new ArrayList<>();
        
        private boolean circuitBreak;
        
        private int readDepth;
        
        private int maximumReadDepth;
        
        @Override
        public void channelRead(final ChannelHandlerContext context, final Object message) {
            readDepth++;
            maximumReadDepth = Math.max(maximumReadDepth, readDepth);
            try {
                ByteBuf packet = (ByteBuf) message;
                commandTypes.add(FirebirdCommandPacketType.valueOf(packet.getInt(packet.readerIndex())));
                if (circuitBreak) {
                    new CircuitBreakProxyState().execute(context, message, engine, connectionSession);
                    packet.release();
                } else {
                    context.channel().config().setAutoRead(false);
                    tasks.add(new CommandExecutorTask(engine, connectionSession, context, message));
                }
            } finally {
                readDepth--;
            }
        }
        
        private void executeAll(final EmbeddedChannel channel) {
            while (!tasks.isEmpty()) {
                tasks.remove().run();
                channel.runPendingTasks();
            }
        }
    }
}
