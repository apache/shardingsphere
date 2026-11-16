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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStreamBlob;
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
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler;
import org.apache.shardingsphere.proxy.frontend.netty.ProxyFlowControlHandler;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.CircuitBreakProxyState;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    
    private static final int BATCH_STATEMENT_HANDLE = 42;
    
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
                splitContinuation ? Unpooled.EMPTY_BUFFER : buildBatchMessage(false, 100), splitContinuation ? Unpooled.EMPTY_BUFFER : buildBatchSync());
        channel.writeInbound(in);
        commands.executeAll(channel);
        if (splitContinuation) {
            assertResponses(channel, ISCConstants.isc_batch_open);
            ByteBuf tail = continuation.readRetainedSlice(continuation.readableBytes());
            continuation.release();
            channel.writeInbound(Unpooled.wrappedUnmodifiableBuffer(tail, buildBatchMessage(false, 100), buildBatchSync()));
            commands.executeAll(channel);
            assertResponses(channel, 0, 0, 0);
        } else {
            assertResponses(channel, ISCConstants.isc_batch_open, 0, 0, 0);
        }
        FirebirdBatchStatement actualBatch = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        assertThat(actualBatch, sameInstance(expectedBatch));
        assertThat(actualBatch.getParameterValues(), is(Collections.singletonList(Collections.singletonList(100))));
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
        if (!commands.circuitBreak) {
            when(connectionSession.getConnectionId()).thenReturn(BATCH_CONNECTION_ID);
            when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
            ServerPreparedStatementRegistry preparedStatements = new ServerPreparedStatementRegistry();
            preparedStatements.addPreparedStatement(BATCH_STATEMENT_HANDLE, mock(FirebirdServerPreparedStatement.class));
            when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(preparedStatements);
            when(ProxyContext.getInstance()).thenReturn(proxyContext);
            when(proxyContext.getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        }
        FirebirdBatchRegistry.getInstance().registerConnection(BATCH_CONNECTION_ID);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(BATCH_CONNECTION_ID, FirebirdProtocolVersion.PROTOCOL_VERSION16);
        EmbeddedChannel result = new EmbeddedChannel(new PacketCodec(engine.getCodecEngine()), new ProxyFlowControlHandler());
        result.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        result.attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).set(FirebirdProtocolVersion.PROTOCOL_VERSION16);
        result.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        result.pipeline().addLast(FrontendChannelInboundHandler.class.getSimpleName(), commands);
        engine.initChannel(result);
        return result;
    }
    
    private FirebirdBatchStatement registerBatch() {
        FirebirdBatchStatement result = new FirebirdBatchStatement(BATCH_STATEMENT_HANDLE,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 16777216L, false, false, true);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE, result);
        return result;
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
        ByteBuf blr = Unpooled.buffer().writeByte(blrType < 0 ? 99 : BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2)
                .writeByte(blrType < 0 ? BlrConstants.blr_long : blrType).writeByte(0).writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        int blrLength = blr.readableBytes();
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(blrLength).writeBytes(blr)
                .writeZero((4 - blrLength % 4) % 4).writeInt(BlrConstants.blr_int64 == blrType ? 10 : 6);
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
                Arguments.of("different format", BlrConstants.blr_int64, false),
                Arguments.of("malformed format", -1, false),
                Arguments.of("fragmented continuation", BlrConstants.blr_int64, true));
    }
    
    @Test
    void assertInitChannelCreatesBatchAfterQueuedRelease() {
        BatchCommandHandler commands = new BatchCommandHandler();
        EmbeddedChannel channel = createBatchChannel(commands);
        FirebirdBatchStatement previousBatch = registerBatch();
        channel.writeInbound(buildBlobStream(32, Unpooled.buffer().writeLong(1L).writeInt(20).writeInt(0).writeZero(16)));
        commands.executeAll(channel);
        assertResponses(channel, 0);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBatchRelease(), buildBatchCreate(BlrConstants.blr_int64, false),
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
        commands.circuitBreak = true;
        EmbeddedChannel channel = createBatchChannel(commands);
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHandleExceptionParameters")
    void assertHandleException(final String testName, final boolean exceptionOccur, final Exception exception) {
        ConnectionContext connectionContext = mockConnectionSession(true, exceptionOccur);
        engine.handleException(connectionSession, exception);
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
    
    private static Stream<Arguments> provideHandleExceptionParameters() {
        return Stream.of(Arguments.of("mark when in transaction", false, new Exception("error")), Arguments.of("keep marked when already occurred", true, new Exception("error")));
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
                Arguments.of("skip when batch blob continuation bpb exception", true, new BatchBlobContinuationBpbException(4L)),
                Arguments.of("skip when batch bpb too big exception", true, new BatchBpbTooBigException(8L, 4L)),
                Arguments.of("skip when batch segment exceeds blob exception", true, new BatchSegmentExceedsBlobException(50, 10)),
                Arguments.of("skip when batch segment too big exception", true, new BatchSegmentTooBigException(50, 10)),
                Arguments.of("skip when batch small data exception", true, new BatchSmallDataException("BLOB")),
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
