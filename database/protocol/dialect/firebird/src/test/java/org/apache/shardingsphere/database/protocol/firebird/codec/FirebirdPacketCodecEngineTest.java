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
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdPacketCodecEngineTest {
    
    private final FirebirdPacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setUp() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(context.alloc().compositeBuffer(anyInt())).thenAnswer(invocation -> Unpooled.compositeBuffer(invocation.getArgument(0)));
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
        final ByteBuf pendingMessage = Unpooled.wrappedBuffer(new byte[]{1, 2});
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
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.INFO_REQUEST, 8);
        List<Object> out = new LinkedList<>();
        assertThrows(IllegalStateException.class, () -> codecEngine.decode(context, in, out));
    }
    
    @Test
    void assertDecodeWithProcessPacketsExceptionAndPendingMessages() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenThrow(IllegalStateException.class);
        final ByteBuf pendingMessage = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
        getPendingMessages().add(pendingMessage);
        ByteBuf in = createCommandPacket(FirebirdCommandPacketType.INFO_REQUEST, 8);
        List<Object> out = new LinkedList<>();
        assertThrows(IllegalStateException.class, () -> codecEngine.decode(context, in, out));
    }
    
    @Test
    void assertEncode() {
        DatabasePacket packet = mock(DatabasePacket.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        codecEngine.encode(context, packet, byteBuf);
        verify(packet).write(any(FirebirdPacketPayload.class));
    }
    
    @Test
    void assertEncodeWithException() {
        DatabasePacket packet = mock(DatabasePacket.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        doThrow(RuntimeException.class).when(packet).write(any(FirebirdPacketPayload.class));
        codecEngine.encode(context, packet, byteBuf);
        verify(byteBuf).resetWriterIndex();
    }
    
    @Test
    void assertCreatePacketPayload() {
        ByteBuf byteBuf = mock(ByteBuf.class);
        assertThat(codecEngine.createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
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
        result.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any())).thenReturn(expectedLength);
        return result;
    }
    
    private MockedStatic<FirebirdCommandPacketFactory> mockExpectedLengthException() {
        MockedStatic<FirebirdCommandPacketFactory> result = mockStatic(FirebirdCommandPacketFactory.class);
        result.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any())).thenThrow(IndexOutOfBoundsException.class);
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
