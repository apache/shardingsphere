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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
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
        when(context.alloc().compositeBuffer(anyInt())).thenAnswer(invocation -> Unpooled.compositeBuffer(invocation.getArgument(0)));
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
    void assertIncompletePacketStateAcrossFragmentedTransaction() {
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.TRANSACTION.getValue()).writeInt(0).writeInt(4).writeInt(1);
        ByteBuf first = packet.readRetainedSlice(8);
        ByteBuf second = packet.readRetainedSlice(packet.readableBytes());
        List<Object> out = new LinkedList<>();
        assertFalse(codecEngine.hasIncompletePacket());
        codecEngine.decode(context, first, out);
        assertTrue(codecEngine.hasIncompletePacket());
        codecEngine.decode(context, second, out);
        assertFalse(codecEngine.hasIncompletePacket());
        assertThat(out.size(), is(1));
        ((ByteBuf) out.get(0)).release();
        first.release();
        second.release();
        packet.release();
    }
    
    @ParameterizedTest
    @EnumSource(value = FirebirdCommandPacketType.class, names = {"CONNECT", "ATTACH", "CONT_AUTH"})
    void assertDecodeFragmentedAuthenticationPacket(final FirebirdCommandPacketType type) {
        byte[] packet = createAuthenticationPacket(type);
        for (int split = Integer.BYTES; split < packet.length; split++) {
            FirebirdPacketCodecEngine engine = new FirebirdPacketCodecEngine();
            ByteBuf first = Unpooled.copiedBuffer(packet, 0, split);
            ByteBuf second = Unpooled.copiedBuffer(packet, split, packet.length - split);
            List<Object> out = new LinkedList<>();
            try {
                engine.decode(context, first, out);
                assertTrue(out.isEmpty(), "Packet was decoded at split " + split);
                assertTrue(engine.hasIncompletePacket());
                engine.decode(context, second, out);
                assertThat(out.size(), is(1));
                ByteBuf decoded = (ByteBuf) out.get(0);
                byte[] actual = new byte[decoded.readableBytes()];
                decoded.getBytes(decoded.readerIndex(), actual);
                assertThat(actual, is(packet));
                assertFalse(engine.hasIncompletePacket());
            } finally {
                out.forEach(each -> ((ByteBuf) each).release());
                first.release();
                second.release();
            }
        }
    }
    
    @ParameterizedTest
    @EnumSource(value = FirebirdCommandPacketType.class, names = {"CONNECT", "ATTACH", "CONT_AUTH"})
    void assertDecodeCoalescedAuthenticationPacket(final FirebirdCommandPacketType type) {
        byte[] packet = createAuthenticationPacket(type);
        ByteBuf in = Unpooled.buffer().writeBytes(packet).writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(1);
        List<Object> out = new LinkedList<>();
        try {
            codecEngine.decode(context, in, out);
            assertThat(out.size(), is(2));
            assertThat(((ByteBuf) out.get(0)).readableBytes(), is(packet.length));
            assertThat(((ByteBuf) out.get(1)).readableBytes(), is(8));
            assertThat(((ByteBuf) out.get(1)).getInt(((ByteBuf) out.get(1)).readerIndex()), is(FirebirdCommandPacketType.COMMIT.getValue()));
        } finally {
            out.forEach(each -> ((ByteBuf) each).release());
            in.release();
        }
    }
    
    private byte[] createAuthenticationPacket(final FirebirdCommandPacketType type) {
        ByteBuf packet = Unpooled.buffer().writeInt(type.getValue());
        switch (type) {
            case CONNECT:
                packet.writeInt(FirebirdCommandPacketType.ATTACH.getValue()).writeInt(3).writeInt(1);
                writeAuthenticationBuffer(packet, "db".getBytes(StandardCharsets.US_ASCII));
                packet.writeInt(2);
                writeAuthenticationBuffer(packet, new byte[]{1, 2, 3});
                packet.writeZero(40);
                break;
            case ATTACH:
                packet.writeInt(0);
                writeAuthenticationBuffer(packet, "db".getBytes(StandardCharsets.US_ASCII));
                writeAuthenticationBuffer(packet, new byte[]{1, 2, 3, 4, 5});
                break;
            case CONT_AUTH:
                writeAuthenticationBuffer(packet, new byte[]{1, 2, 3});
                writeAuthenticationBuffer(packet, "Srp256".getBytes(StandardCharsets.US_ASCII));
                writeAuthenticationBuffer(packet, "Srp,Srp256".getBytes(StandardCharsets.US_ASCII));
                writeAuthenticationBuffer(packet, new byte[0]);
                break;
            default:
                throw new IllegalArgumentException(type.name());
        }
        byte[] result = new byte[packet.readableBytes()];
        packet.readBytes(result);
        packet.release();
        return result;
    }
    
    private void writeAuthenticationBuffer(final ByteBuf packet, final byte[] data) {
        packet.writeInt(data.length).writeBytes(data).writeZero((4 - data.length % 4) % 4);
    }
    
    @Test
    void assertUnreadInputStateAfterAllocateStatement() {
        ByteBuf input = Unpooled.buffer().writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue()).writeInt(0).writeByte(0);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, input, out);
        assertTrue(codecEngine.hasIncompletePacket());
        input.writeZero(3);
        codecEngine.decode(context, input, out);
        assertFalse(codecEngine.hasIncompletePacket());
        assertThat(out.size(), is(1));
        ((ByteBuf) out.get(0)).release();
        input.release();
    }
    
    @Test
    void assertDecodeWithIncompleteHeader() {
        ByteBuf pendingMessage = Unpooled.wrappedBuffer(new byte[]{1, 2});
        getPendingMessages().add(pendingMessage);
        ByteBuf in = Unpooled.wrappedBuffer(new byte[]{3});
        List<Object> out = new LinkedList<>();
        assertThat(in.capacity(), is(1));
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertThat(getPendingMessages().size(), is(1));
        assertThat(getPendingMessages().get(0).readableBytes(), is(3));
    }
    
    @Test
    void assertDecodeWithVoidCommandType() {
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.VOID, 4);
        List<Object> out = new LinkedList<>();
        assertThat(in.readerIndex(), is(0));
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertTrue(getPendingMessages().isEmpty());
        assertFalse(codecEngine.hasIncompletePacket());
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
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
    }
    
    @Test
    void assertDecodeCoalescedExecuteImmediateAndCommit() {
        byte[] sql = "SET TRANSACTION".getBytes(StandardCharsets.US_ASCII);
        ByteBuf in = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue()).writeInt(0).writeInt(0).writeInt(3)
                .writeInt(sql.length).writeBytes(sql).writeZero((4 - sql.length % 4) % 4).writeInt(0).writeInt(0)
                .writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(1);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(2));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(44));
        assertThat(((ByteBuf) out.get(1)).readableBytes(), is(8));
        assertThat(((ByteBuf) out.get(1)).getInt(0), is(FirebirdCommandPacketType.COMMIT.getValue()));
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
        ((ByteBuf) out.get(0)).release();
        ((ByteBuf) out.get(1)).release();
        in.release();
    }
    
    @Test
    void assertDecodeFragmentedExecuteImmediate() {
        byte[] sql = "SET TRANSACTION".getBytes(StandardCharsets.US_ASCII);
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue()).writeInt(0).writeInt(0).writeInt(3)
                .writeInt(sql.length).writeBytes(sql).writeZero((4 - sql.length % 4) % 4).writeInt(0).writeInt(0);
        ByteBuf first = packet.readRetainedSlice(24);
        ByteBuf second = packet.readRetainedSlice(packet.readableBytes());
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, first, out);
        assertTrue(out.isEmpty());
        codecEngine.decode(context, second, out);
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(44));
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
        ((ByteBuf) out.get(0)).release();
        first.release();
        second.release();
        packet.release();
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
        assertNull(getPendingPacketType());
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
        assertThat(getPendingPacketType(), is(FirebirdCommandPacketType.PREPARE_STATEMENT));
        assertThat(getPendingMessages().size(), is(1));
        assertThat(getPendingMessages().get(0).readableBytes(), is(8));
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
        assertThat(getPendingPacketType(), is(FirebirdCommandPacketType.PREPARE_STATEMENT));
        assertThat(getPendingMessages().size(), is(1));
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
    void assertDecodeWithProcessPacketsExceptionAndPendingMessages() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenThrow(IllegalStateException.class);
        ByteBuf pendingMessage = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
        getPendingMessages().add(pendingMessage);
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.INFO_REQUEST, 8);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertTrue(out.isEmpty());
        assertTrue(getPendingMessages().isEmpty());
        assertThat(pendingMessage.refCnt(), is(0));
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
        ByteBuf in = Unpooled.buffer().writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue()).writeInt(0).writeInt(UNKNOWN_COMMAND_TYPE).writeInt(0);
        channel.writeInbound(in);
        channel.runPendingTasks();
        assertNull(channel.readInbound());
        ByteBuf encodedError = channel.readOutbound();
        assertThat(encodedError.getInt(0), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertFalse(channel.isOpen());
        encodedError.release();
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
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
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
        assertThat(getPendingPacketType(), is(FirebirdCommandPacketType.BATCH_MSG));
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(BATCH_CONNECTION_ID, BATCH_STATEMENT_HANDLE);
        assertTrue(batchStatement.getParameterValues().isEmpty());
        List<Object> secondOut = new LinkedList<>();
        codecEngine.decode(context, secondFrame, secondOut);
        assertThat(secondOut.size(), is(1));
        assertThat(createBatchSendMessagePacket((ByteBuf) secondOut.iterator().next()).getMessageCount(), is(2L));
        assertTrue(batchStatement.getParameterValues().isEmpty());
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
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
        assertThat(getPendingPacketType(), is(FirebirdCommandPacketType.BATCH_MSG));
        assertThat(getPendingMessages().size(), is(1));
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
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
    }
    
    @Test
    void assertDecodeCoalescedBatchCreateAndSplitMessage() {
        ByteBuf batchMessage = buildBatchMessage(BATCH_STATEMENT_HANDLE, 100);
        ByteBuf firstFrame = Unpooled.wrappedUnmodifiableBuffer(buildBatchCreate(), batchMessage.readRetainedSlice(18));
        List<Object> firstOut = new LinkedList<>();
        codecEngine.decode(context, firstFrame, firstOut);
        assertThat(firstOut.size(), is(1));
        assertThat(getPendingPacketType(), is(FirebirdCommandPacketType.BATCH_MSG));
        List<Object> secondOut = new LinkedList<>();
        codecEngine.decode(context, batchMessage.readRetainedSlice(batchMessage.readableBytes()), secondOut);
        assertThat(secondOut.size(), is(1));
        assertThat(createBatchSendMessagePacket((ByteBuf) secondOut.get(0)).getMessageCount(), is(1L));
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
    }
    
    @Test
    void assertDecodeStandaloneBatchCreateWithBlobBlrDefersToCommandPath() {
        ByteBuf in = buildBlobBatchCreate();
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        verify(context, never()).close();
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
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
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
    }
    
    @Test
    void assertDecodeCoalescedBatchWithSignedStatementHandle() {
        ByteBuf create = buildBatchCreate();
        create.setInt(4, 32768);
        ByteBuf in = Unpooled.wrappedUnmodifiableBuffer(create, buildBatchMessage(-32768, 100));
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, in, out);
        assertThat(out.size(), is(2));
        assertThat(((ByteBuf) out.get(0)).getInt(0), is(FirebirdCommandPacketType.BATCH_CREATE.getValue()));
        assertThat(((ByteBuf) out.get(1)).getInt(0), is(FirebirdCommandPacketType.BATCH_MSG.getValue()));
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
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
        assertNull(getPendingPacketType());
        assertTrue(getPendingMessages().isEmpty());
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
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private List<ByteBuf> getPendingMessages() {
        return (List<ByteBuf>) Plugins.getMemberAccessor().get(FirebirdPacketCodecEngine.class.getDeclaredField("pendingMessages"), codecEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private FirebirdCommandPacketType getPendingPacketType() {
        return (FirebirdCommandPacketType) Plugins.getMemberAccessor().get(FirebirdPacketCodecEngine.class.getDeclaredField("pendingPacketType"), codecEngine);
    }
    
    private static Stream<Arguments> validHeaderCases() {
        return Stream.of(
                Arguments.of("readable bytes greater than header", 5, true),
                Arguments.of("readable bytes equal to header", 4, true),
                Arguments.of("readable bytes less than header", 3, false));
    }
    
    private static Stream<Arguments> fixedLengthCommandCases() {
        return Stream.of(
                Arguments.of("decode allocate statement", FirebirdCommandPacketType.ALLOCATE_STATEMENT, 8, 1),
                Arguments.of("decode free statement", FirebirdCommandPacketType.FREE_STATEMENT, 12, 1),
                Arguments.of("decode consecutive free statements", FirebirdCommandPacketType.FREE_STATEMENT, 12, 2));
    }
}
