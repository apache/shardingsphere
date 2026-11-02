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

package org.apache.shardingsphere.database.protocol.firebird.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdPacketCodecEngineTest {
    
    private static final int BATCH_CONNECTION_ID = 1;
    
    private static final int BATCH_STATEMENT_HANDLE = 42;
    
    private static final int UNKNOWN_COMMAND_TYPE = 99999;
    
    private final FirebirdPacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setUp() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).get()).thenReturn(1);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdBatchRegistry.getInstance().unregisterConnection(BATCH_CONNECTION_ID);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validHeaderCases")
    void assertIsValidHeader(final String name, final int readableBytes, final boolean expectedValid) {
        assertThat(codecEngine.isValidHeader(readableBytes), is(expectedValid));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("fixedLengthCommandCases")
    void assertDecodeFixedLengthCommand(final String name, final FirebirdCommandPacketType commandType, final int packetLength, final int packetCount) {
        ByteBuf in = createCommandPackets(commandType, packetLength, packetCount);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(packetCount));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(packetLength));
    }
    
    @Test
    void assertDecodeWithIncompleteHeader() {
        ByteBuf in = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(in.readerIndex(), is(0));
    }
    
    @Test
    void assertDecodeWithVoidCommandType() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.VOID, 4);
        List<Object> out = new LinkedList<>();
        assertThat(in.readerIndex(), is(0));
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
    }
    
    @Test
    void assertDecodeCoalescedSeekBlobAndFollowingPacket() {
        ByteBuf seekBlob = Unpooled.buffer().writeInt(FirebirdCommandPacketType.SEEK_BLOB.getValue()).writeInt(5).writeInt(0).writeInt(100);
        ByteBuf commit = Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(1);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(seekBlob, commit);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(2));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(16));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.SEEK_BLOB.getValue()));
        assertThat(((ByteBuf) out.get(1)).readableBytes(), is(8));
        assertThat(((ByteBuf) out.get(1)).getInt(0), is(FirebirdCommandPacketType.COMMIT.getValue()));
    }
    
    @Test
    void assertDecodeWithExpectedLength() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.PREPARE_STATEMENT, 8);
        List<Object> out = new LinkedList<>();
        assertThat(in.writerIndex(), is(8));
        try (MockedStatic<FirebirdCommandPacketFactory> ignored = mockExpectedLength(8)) {
            codecEngine.decode(context, in, out);
        }
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(8));
    }
    
    @Test
    void assertDecodeWithNonPositiveExpectedLength() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.PREPARE_STATEMENT, 8);
        List<Object> out = new LinkedList<>();
        assertTrue(in.maxCapacity() >= 8);
        try (MockedStatic<FirebirdCommandPacketFactory> ignored = mockExpectedLength(0)) {
            codecEngine.decode(context, in, out);
        }
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(8));
    }
    
    @Test
    void assertDecodeWithExpectedLengthOverflow() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.PREPARE_STATEMENT, 8);
        List<Object> out = new LinkedList<>();
        assertThat(in.refCnt(), is(1));
        try (MockedStatic<FirebirdCommandPacketFactory> ignored = mockExpectedLength(12)) {
            codecEngine.decode(context, in, out);
        }
        assertTrue(out.isEmpty());
        assertThat(in.readerIndex(), is(0));
    }
    
    @Test
    void assertDecodeWithExpectedLengthException() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.PREPARE_STATEMENT, 8);
        List<Object> out = new LinkedList<>();
        assertThat(in.readableBytes(), is(8));
        try (MockedStatic<FirebirdCommandPacketFactory> ignored = mockExpectedLengthException()) {
            codecEngine.decode(context, in, out);
        }
        assertTrue(out.isEmpty());
        assertThat(in.readerIndex(), is(0));
    }
    
    @Test
    void assertDecodeWithProcessPacketsExceptionAndNoPendingMessages() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenThrow(IllegalStateException.class);
        ChannelFuture errorResponseFuture = mock(ChannelFuture.class);
        when(context.channel().writeAndFlush(any())).thenReturn(errorResponseFuture);
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.INFO_REQUEST, 8);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(in.readableBytes(), is(0));
        assertThat(captureErrorResponse().getErrorCode(), is(335544382));
        verify(errorResponseFuture).addListener(ChannelFutureListener.CLOSE);
    }
    
    @Test
    void assertDecodeWithProcessPacketsExceptionAfterOutput() {
        ChannelFuture errorResponseFuture = mock(ChannelFuture.class);
        when(context.channel().writeAndFlush(any())).thenReturn(errorResponseFuture);
        ByteBuf in = Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(1).writeInt(UNKNOWN_COMMAND_TYPE);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(in.readableBytes(), is(0));
        assertThat(captureErrorResponse().getErrorCode(), is(335544382));
    }
    
    private FirebirdGenericResponsePacket captureErrorResponse() {
        ArgumentCaptor<DatabasePacket> captor = ArgumentCaptor.forClass(DatabasePacket.class);
        verify(context.channel()).writeAndFlush(captor.capture());
        return (FirebirdGenericResponsePacket) captor.getValue();
    }
    
    @Test
    void assertEncode() {
        DatabasePacket packet = mock(DatabasePacket.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        codecEngine.encode(context, packet, byteBuf);
        verify(packet).write(any(FirebirdPacketPayload.class));
    }
    
    @Test
    void assertEncodeWritesErrorResponseOnException() {
        DatabasePacket packet = mock(DatabasePacket.class);
        ByteBuf byteBuf = Unpooled.buffer();
        doThrow(RuntimeException.class).when(packet).write(any(FirebirdPacketPayload.class));
        codecEngine.encode(context, packet, byteBuf);
        assertThat(byteBuf.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        verify(context, never()).close();
    }
    
    @Test
    void assertDecodeUnknownCommandWritesEncodedErrorAndClosesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.writeInbound(Unpooled.buffer().writeInt(UNKNOWN_COMMAND_TYPE).writeInt(0));
        channel.runPendingTasks();
        assertNull(channel.readInbound());
        ByteBuf encodedError = channel.readOutbound();
        assertThat(encodedError.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertFalse(channel.isOpen());
        encodedError.release();
    }
    
    @Test
    void assertDecodeValidPacketFollowedByInvalidDiscardsValidPacket() {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        ByteBuf in = Unpooled.buffer().writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue()).writeInt(0).writeInt(UNKNOWN_COMMAND_TYPE).writeInt(0);
        channel.writeInbound(in);
        channel.runPendingTasks();
        assertNull(channel.readInbound());
        ByteBuf encodedError = channel.readOutbound();
        assertThat(encodedError.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertFalse(channel.isOpen());
        encodedError.release();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("shortTailReadCases")
    void assertDecodePacketCompletedByShortTailRead(final String name, final ByteBuf[] reads, final int packetCount) {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        for (ByteBuf each : reads) {
            channel.writeInbound(each);
        }
        for (int i = 0; i < packetCount; i++) {
            ByteBuf actual = channel.readInbound();
            assertThat(actual.readableBytes(), is(8));
            assertThat(actual.getInt(0), is(FirebirdCommandPacketType.COMMIT.getValue()));
            actual.release();
        }
        assertNull(channel.readInbound());
        assertTrue(channel.isOpen());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handshakeShortTailReadCases")
    void assertDecodeHandshakePacketCompletedByShortTailRead(final String name, final ByteBuf handshakePacket, final int tailLength) {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        int headLength = handshakePacket.readableBytes() - tailLength;
        channel.writeInbound(handshakePacket.copy(0, headLength));
        assertNull(channel.readInbound());
        channel.writeInbound(handshakePacket.copy(headLength, tailLength));
        ByteBuf actual = channel.readInbound();
        assertThat(actual, is(handshakePacket));
        actual.release();
        assertNull(channel.readInbound());
        assertTrue(channel.isOpen());
    }
    
    @Test
    void assertDecodeIncompleteConnectReleasedOnClose() {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        ByteBuf in = PooledByteBufAllocator.DEFAULT.buffer().writeInt(FirebirdCommandPacketType.CONNECT.getValue()).writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(3).writeInt(1);
        channel.writeInbound(in);
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
        assertThat(in.refCnt(), is(0));
    }
    
    @Test
    void assertDecodeIncompleteAttachReleasedOnClose() {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        ByteBuf in = PooledByteBufAllocator.DEFAULT.buffer().writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(0);
        channel.writeInbound(in);
        assertTrue(channel.isOpen());
        channel.finishAndReleaseAll();
        assertThat(in.refCnt(), is(0));
    }
    
    @Test
    void assertDecodeHighlyFragmentedConnect() {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketCodec(new FirebirdPacketCodecEngine()));
        channel.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(StandardCharsets.UTF_8);
        channel.attr(FirebirdConstant.CURRENT_CONNECTION).set(BATCH_CONNECTION_ID);
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.CONNECT.getValue()).writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(3).writeInt(1)
                .writeInt(4000).writeZero(4000).writeInt(1).writeInt(3).writeBytes(new byte[]{9, 1, 'x'}).writeZero(1)
                .writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION13.getCode()).writeInt(1).writeInt(0).writeInt(5).writeInt(2);
        for (int i = 0; i < packet.readableBytes(); i++) {
            channel.writeInbound(Unpooled.buffer(1).writeByte(packet.getByte(i)));
        }
        ByteBuf actual = channel.readInbound();
        assertThat(actual, is(packet));
        assertTrue(actual.release());
        packet.release();
        channel.finishAndReleaseAll();
    }
    
    @Test
    void assertCreatePacketPayload() {
        ByteBuf byteBuf = mock(ByteBuf.class);
        assertThat(codecEngine.createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
    
    @Test
    void assertDecodeBatchMessageWithMultipleRowsInSingleFrame() {
        setUpBatchContext();
        ByteBuf in = buildBatchMessage(BATCH_STATEMENT_HANDLE, 100, 200);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(1));
        FirebirdBatchMessageCommandPacket actualPacket = createBatchSendMessagePacket((ByteBuf) out.iterator().next());
        assertThat(actualPacket.getStatementHandle(), is(BATCH_STATEMENT_HANDLE));
        assertThat(actualPacket.getMessageCount(), is(2L));
    }
    
    @Test
    void assertDecodeBatchMessageSplitInsideRow() {
        setUpBatchContext();
        ByteBuf batchMessage = buildBatchMessage(BATCH_STATEMENT_HANDLE, 100, 200);
        ByteBuf firstFrame = batchMessage.readRetainedSlice(24);
        ByteBuf secondFrame = batchMessage.readRetainedSlice(batchMessage.readableBytes());
        List<Object> firstOut = new LinkedList<>();
        codecEngine.decode(context, firstFrame, firstOut);
        assertTrue(firstOut.isEmpty());
        assertThat(firstFrame.readerIndex(), is(0));
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        assertTrue(batchStatement.getParameterValues().isEmpty());
        List<Object> secondOut = new LinkedList<>();
        codecEngine.decode(context, Unpooled.wrappedBuffer(firstFrame, secondFrame), secondOut);
        assertThat(secondOut.size(), is(1));
        assertThat(createBatchSendMessagePacket((ByteBuf) secondOut.iterator().next()).getMessageCount(), is(2L));
        assertTrue(batchStatement.getParameterValues().isEmpty());
    }
    
    @Test
    void assertDecodeBatchMessageWithoutRegisteredBatchStatementSendsErrorResponse() {
        FirebirdBatchRegistry.getInstance().registerConnection(BATCH_CONNECTION_ID);
        ByteBuf in = buildBatchMessage(999, 100);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(captureErrorResponse().getErrorCode(), is(335545159));
    }
    
    @Test
    void assertDecodeBatchMessageOverBufferSizeIsStillFramedWithoutCodecError() {
        FirebirdBatchRegistry.getInstance().registerConnection(BATCH_CONNECTION_ID);
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(BATCH_STATEMENT_HANDLE,
                Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 8L);
        batchStatement.addSize(1L);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE, batchStatement);
        ByteBuf in = buildBatchMessage(BATCH_STATEMENT_HANDLE, 100);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(1));
        assertThat(createBatchSendMessagePacket((ByteBuf) out.iterator().next()).getStatementHandle(), is(BATCH_STATEMENT_HANDLE));
        verify(context.channel(), never()).writeAndFlush(any());
    }
    
    @Test
    void assertDecodeBatchMessageWithIncompleteHeaderDefersOutput() {
        setUpBatchContext();
        ByteBuf in = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(BATCH_STATEMENT_HANDLE);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(in.readerIndex(), is(0));
    }
    
    @Test
    void assertDecodeBatchMessageAfterResetReadsFreshHeader() {
        setUpBatchContext();
        List<Object> firstOut = new LinkedList<>();
        codecEngine.decode(context, buildBatchMessage(BATCH_STATEMENT_HANDLE, 100), firstOut);
        assertThat(createBatchSendMessagePacket((ByteBuf) firstOut.iterator().next()).getStatementHandle(), is(BATCH_STATEMENT_HANDLE));
        List<Object> secondOut = new LinkedList<>();
        codecEngine.decode(context, buildBatchMessage(BATCH_STATEMENT_HANDLE, 300), secondOut);
        assertThat(createBatchSendMessagePacket((ByteBuf) secondOut.iterator().next()).getStatementHandle(), is(BATCH_STATEMENT_HANDLE));
    }
    
    @Test
    void assertDecodeCoalescedBatchCreateAndMixedMessage() {
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildMixedBatchCreate(), buildMixedBatchMessage());
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(2));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
        ByteBuf batchMessage = (ByteBuf) out.get(1);
        assertThat(batchMessage.readableBytes(), is(32));
        assertThat(createBatchSendMessagePacket(batchMessage).getMessageCount(), is(1L));
    }
    
    @Test
    void assertDecodeCoalescedBatchCreateAndSplitMessage() {
        ByteBuf batchMessage = buildBatchMessage(BATCH_STATEMENT_HANDLE, 100);
        ByteBuf firstFrame = Unpooled.wrappedUnmodifiableBuffer(buildBatchCreate(), batchMessage.readRetainedSlice(18));
        List<Object> firstOut = new LinkedList<>();
        codecEngine.decode(context, firstFrame, firstOut);
        assertThat(firstOut.size(), is(1));
        assertThat(firstFrame.readableBytes(), is(18));
        List<Object> secondOut = new LinkedList<>();
        codecEngine.decode(context, Unpooled.wrappedBuffer(firstFrame, batchMessage.readRetainedSlice(batchMessage.readableBytes())), secondOut);
        assertThat(secondOut.size(), is(1));
        assertThat(createBatchSendMessagePacket((ByteBuf) secondOut.get(0)).getMessageCount(), is(1L));
    }
    
    @Test
    void assertDecodeStandaloneBatchCreateWithBlobBlrDefersToCommandPath() {
        ByteBuf in = buildBlobBatchCreate();
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        verify(context, never()).close();
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
    }
    
    @Test
    void assertDecodeCoalescedBatchCreateWithBlobAndMessageDefersToCommandPath() {
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildBlobBatchCreate(), buildBlobBatchMessage());
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        verify(context, never()).close();
        assertThat(out.size(), is(2));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
        assertThat(((ByteBuf) out.get(1)).getInt(0), is(FirebirdCommandPacketType.BATCH_MSG.getValue()));
    }
    
    private ByteBuf buildBlobBatchCreate() {
        ByteBuf blr = Unpooled.buffer()
                .writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0)
                .writeShortLE(2).writeByte(BlrConstants.blr_blob2).writeZero(4).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return buildBatchCreate(blr, 8);
    }
    
    private ByteBuf buildBlobBatchMessage() {
        return Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(1).writeByte(0).writeZero(3).writeLong(123L);
    }
    
    @Test
    void assertDecodeStandaloneBatchCreateWithInvalidBlrDefersToCommandPath() {
        ByteBuf in = buildInvalidBatchCreate();
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        verify(context, never()).close();
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
    }
    
    @Test
    void assertDecodeCoalescedBatchCreateWithMalformedBlrWritesErrorResponseAndClosesChannel() {
        ChannelFuture errorResponseFuture = mock(ChannelFuture.class);
        when(context.channel().writeAndFlush(any())).thenReturn(errorResponseFuture);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(buildInvalidBatchCreate(), buildBatchMessage(BATCH_STATEMENT_HANDLE, 100));
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(in.readableBytes(), is(0));
        assertThat(captureErrorResponse().getErrorCode(), is(335544382));
        verify(errorResponseFuture).addListener(ChannelFutureListener.CLOSE);
    }
    
    private ByteBuf buildInvalidBatchCreate() {
        ByteBuf blr = Unpooled.buffer()
                .writeByte(99).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0)
                .writeShortLE(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return buildBatchCreate(blr, 0);
    }
    
    private void setUpBatchContext() {
        FirebirdBatchRegistry.getInstance().registerConnection(BATCH_CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE,
                new FirebirdBatchStatement(BATCH_STATEMENT_HANDLE,
                        Collections.singletonList(new FirebirdBatchColumnDescriptor(FirebirdBinaryColumnType.LONG, Integer.BYTES, 0, 0)), 256L * 1024 * 1024));
    }
    
    private FirebirdBatchMessageCommandPacket createBatchSendMessagePacket(final ByteBuf byteBuf) {
        return new FirebirdBatchMessageCommandPacket(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
    }
    
    private ByteBuf buildBatchMessage(final int statementHandle, final int... values) {
        ByteBuf result = Unpooled.buffer();
        result.writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue());
        result.writeInt(statementHandle);
        result.writeInt(values.length);
        for (int value : values) {
            result.writeByte(0);
            result.writeZero(3);
            result.writeInt(value);
        }
        return result;
    }
    
    private ByteBuf buildBatchCreate() {
        ByteBuf blr = Unpooled.buffer()
                .writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0)
                .writeShortLE(2).writeByte(BlrConstants.blr_long).writeByte(0).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return buildBatchCreate(blr, 6);
    }
    
    private ByteBuf buildBatchCreate(final ByteBuf blr, final int messageLength) {
        int blrLength = blr.readableBytes();
        return Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(BATCH_STATEMENT_HANDLE)
                .writeInt(blrLength).writeBytes(blr).writeZero((4 - blrLength % 4) % 4).writeInt(messageLength).writeInt(0);
    }
    
    private ByteBuf buildMixedBatchCreate() {
        ByteBuf blr = Unpooled.buffer()
                .writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(6)
                .writeByte(BlrConstants.blr_text).writeShortLE(3).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_varying2).writeShortLE(4).writeShortLE(5).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_long).writeByte(0).writeByte(BlrConstants.blr_short).writeByte(0)
                .writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return buildBatchCreate(blr, 22);
    }
    
    private ByteBuf buildMixedBatchMessage() {
        ByteBuf result = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_MSG.getValue()).writeInt(BATCH_STATEMENT_HANDLE).writeInt(1).writeByte(0).writeZero(3);
        result.writeCharSequence("abc", StandardCharsets.UTF_8);
        result.writeByte(0).writeInt(3);
        result.writeCharSequence("def", StandardCharsets.UTF_8);
        result.writeByte(0).writeInt(42);
        return result;
    }
    
    private ByteBuf createCommandPackets(final FirebirdCommandPacketType commandType, final int packetLength, final int packetCount) {
        ByteBuf result = Unpooled.buffer(packetLength * packetCount);
        for (int i = 0; i < packetCount; i++) {
            result.writeBytes(createCommandPacket(commandType, packetLength));
        }
        return result;
    }
    
    private ByteBuf createCommandPacket(final FirebirdCommandPacketType commandType, final int packetLength) {
        ByteBuf result = Unpooled.buffer(packetLength);
        result.writeInt(commandType.getValue());
        result.writeZero(packetLength - Integer.BYTES);
        return result;
    }
    
    private MockedStatic<FirebirdCommandPacketFactory> mockExpectedLength(final int expectedLength) {
        MockedStatic<FirebirdCommandPacketFactory> result = mockStatic(FirebirdCommandPacketFactory.class);
        result.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any(), anyInt())).thenReturn(expectedLength);
        return result;
    }
    
    private MockedStatic<FirebirdCommandPacketFactory> mockExpectedLengthException() {
        MockedStatic<FirebirdCommandPacketFactory> result = mockStatic(FirebirdCommandPacketFactory.class);
        result.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any(), anyInt())).thenThrow(IndexOutOfBoundsException.class);
        return result;
    }
    
    private static Stream<Arguments> validHeaderCases() {
        return Stream.of(
                Arguments.of("readable bytes greater than header", 5, true),
                Arguments.of("readable bytes equal to header", 4, true),
                Arguments.of("readable bytes less than header", 3, false));
    }
    
    private static Stream<Arguments> shortTailReadCases() {
        ByteBuf commit = Unpooled.buffer(8).writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(1);
        return Stream.of(
                Arguments.of("body completed by short read", new ByteBuf[]{commit.copy(0, 6), commit.copy(6, 2)}, 1),
                Arguments.of("body completed by one byte", new ByteBuf[]{commit.copy(0, 7), commit.copy(7, 1)}, 1),
                Arguments.of("header completed by short reads", new ByteBuf[]{Unpooled.wrappedBuffer(commit.copy(), commit.copy(0, 2)), commit.copy(2, 3), commit.copy(5, 3)}, 2));
    }
    
    private static Stream<Arguments> handshakeShortTailReadCases() {
        ByteBuf connectPacket = Unpooled.buffer(60).writeInt(FirebirdCommandPacketType.CONNECT.getValue()).writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(3)
                .writeInt(FirebirdArchType.ARCH_GENERIC.getCode()).writeInt(6).writeBytes("foo_db".getBytes(StandardCharsets.US_ASCII)).writeZero(2).writeInt(1)
                .writeInt(3).writeBytes(new byte[]{9, 1, 'x'}).writeZero(1)
                .writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION13.getCode()).writeInt(FirebirdArchType.ARCH_GENERIC.getCode()).writeInt(0).writeInt(5).writeInt(2);
        ByteBuf attachPacket = Unpooled.buffer(28).writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(0).writeInt(6)
                .writeBytes("foo_db".getBytes(StandardCharsets.US_ASCII)).writeZero(2).writeInt(1).writeByte(1).writeZero(3);
        return Stream.of(
                Arguments.of("connect completed by one byte", connectPacket, 1),
                Arguments.of("connect completed by two bytes", connectPacket, 2),
                Arguments.of("connect completed by three bytes", connectPacket, 3),
                Arguments.of("attach completed by one byte", attachPacket, 1),
                Arguments.of("attach completed by two bytes", attachPacket, 2),
                Arguments.of("attach completed by three bytes", attachPacket, 3));
    }
    
    private static Stream<Arguments> fixedLengthCommandCases() {
        return Stream.of(
                Arguments.of("decode allocate statement", FirebirdCommandPacketType.ALLOCATE_STATEMENT, 8, 1),
                Arguments.of("decode free statement", FirebirdCommandPacketType.FREE_STATEMENT, 12, 1),
                Arguments.of("decode consecutive free statements", FirebirdCommandPacketType.FREE_STATEMENT, 12, 2));
    }
}
