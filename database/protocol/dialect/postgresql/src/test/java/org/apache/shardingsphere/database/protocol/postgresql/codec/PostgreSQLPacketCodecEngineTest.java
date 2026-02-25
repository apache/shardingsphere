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

package org.apache.shardingsphere.database.protocol.postgresql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLPacketCodecEngineTest {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = (1234 << 16) + 5679;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setUp() {
        when(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.alloc().compositeBuffer(anyInt())).thenAnswer(invocation -> Unpooled.compositeBuffer(invocation.getArgument(0)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validHeaderCases")
    void assertIsValidHeader(final String name, final int readableBytes, final boolean expectedValid) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        assertThat(codecEngine.isValidHeader(readableBytes), is(expectedValid));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validHeaderWhenStartupCompletedCases")
    void assertIsValidHeaderWhenStartupCompleted(final String name, final int readableBytes, final boolean expectedValid) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        setStartupPhase(codecEngine, false);
        assertThat(codecEngine.isValidHeader(readableBytes), is(expectedValid));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeStartupPhaseCases")
    void assertDecodeStartupPhase(final String name, final ByteBuf packet, final int expectedOutSize, final boolean expectedStartupPhase) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, packet, out);
        assertThat(out.size(), is(expectedOutSize));
        assertThat(getStartupPhase(codecEngine), is(expectedStartupPhase));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeWithoutPendingMessagesCases")
    void assertDecodeWithoutPendingMessages(final String name, final char commandType) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        setStartupPhase(codecEngine, false);
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, createCommandPacket(commandType, 4), out);
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(5));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeWithPreparedStateCases")
    void assertDecodeWithPreparedState(final String name, final ByteBuf packet, final int initialPendingMessages, final int expectedOutSize,
                                       final int expectedPendingMessages, final boolean expectedComposite, final int expectedResultReadableBytes) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        setStartupPhase(codecEngine, false);
        for (int i = 0; i < initialPendingMessages; i++) {
            getPendingMessages(codecEngine).add(createCommandPacket(PostgreSQLCommandPacketType.PARSE_COMMAND.getValue(), 4));
        }
        List<Object> out = new LinkedList<>();
        codecEngine.decode(context, packet, out);
        assertThat(out.size(), is(expectedOutSize));
        assertThat(getPendingMessages(codecEngine).size(), is(expectedPendingMessages));
        if (0 < expectedOutSize) {
            assertThat(out.get(0) instanceof CompositeByteBuf, is(expectedComposite));
            assertThat(((ByteBuf) out.get(0)).readableBytes(), is(expectedResultReadableBytes));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("encodeCases")
    void assertEncode(final String name, final boolean identifierPacket, final boolean writeException, final boolean expectedHeader, final char expectedIdentifier) {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        DatabasePacket message = createPacket(identifierPacket);
        if (writeException) {
            doThrow(new RuntimeException("Error")).when(message).write(any(PostgreSQLPacketPayload.class));
        }
        ByteBuf out = Unpooled.buffer();
        codecEngine.encode(context, message, out);
        verify(message).write(any(PostgreSQLPacketPayload.class));
        assertThat(out.readableBytes() > 0, is(expectedHeader));
        if (expectedHeader) {
            assertThat((char) out.getByte(0), is(expectedIdentifier));
            assertThat(out.getInt(1), is(out.readableBytes() - 1));
        }
    }
    
    @Test
    void assertCreatePacketPayload() {
        PostgreSQLPacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
        ByteBuf message = Unpooled.buffer();
        assertThat(codecEngine.createPacketPayload(message, StandardCharsets.UTF_8).getByteBuf(), is(message));
    }
    
    private DatabasePacket createPacket(final boolean identifierPacket) {
        if (identifierPacket) {
            PostgreSQLIdentifierPacket result = mock(PostgreSQLIdentifierPacket.class);
            when(result.getIdentifier()).thenReturn(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST);
            return result;
        }
        return mock(DatabasePacket.class);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setStartupPhase(final PostgreSQLPacketCodecEngine codecEngine, final boolean startupPhase) {
        Plugins.getMemberAccessor().set(PostgreSQLPacketCodecEngine.class.getDeclaredField("startupPhase"), codecEngine, startupPhase);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private boolean getStartupPhase(final PostgreSQLPacketCodecEngine codecEngine) {
        return (boolean) Plugins.getMemberAccessor().get(PostgreSQLPacketCodecEngine.class.getDeclaredField("startupPhase"), codecEngine);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private List<ByteBuf> getPendingMessages(final PostgreSQLPacketCodecEngine codecEngine) {
        return (List<ByteBuf>) Plugins.getMemberAccessor().get(PostgreSQLPacketCodecEngine.class.getDeclaredField("pendingMessages"), codecEngine);
    }
    
    private static ByteBuf createStartupPacket(final int length, final int code) {
        ByteBuf result = Unpooled.buffer(SSL_REQUEST_PAYLOAD_LENGTH);
        result.writeInt(length);
        result.writeInt(code);
        return result;
    }
    
    private static ByteBuf createStartupPacketWithAdditionalByte(final int length, final int code) {
        ByteBuf result = createStartupPacket(length, code);
        result.writeByte(0);
        return result;
    }
    
    private static ByteBuf createCommandPacket(final char commandType, final int payloadLength) {
        ByteBuf result = Unpooled.buffer(1 + Integer.BYTES);
        result.writeByte(commandType);
        result.writeInt(payloadLength);
        return result;
    }
    
    private static Stream<Arguments> validHeaderCases() {
        return Stream.of(
                Arguments.of("startup phase: less than minimum header", 3, false),
                Arguments.of("startup phase: equal minimum header", 4, true),
                Arguments.of("startup phase: greater than minimum header", 8, true));
    }
    
    private static Stream<Arguments> validHeaderWhenStartupCompletedCases() {
        return Stream.of(
                Arguments.of("non-startup phase: less than minimum header", 4, false),
                Arguments.of("non-startup phase: equal minimum header", 5, true),
                Arguments.of("non-startup phase: greater than minimum header", 9, true));
    }
    
    private static Stream<Arguments> decodeStartupPhaseCases() {
        return Stream.of(
                Arguments.of("decode ssl request packet", createStartupPacket(SSL_REQUEST_PAYLOAD_LENGTH, SSL_REQUEST_CODE), 1, true),
                Arguments.of("decode startup packet and enter command phase", createStartupPacket(SSL_REQUEST_PAYLOAD_LENGTH, 1), 1, false),
                Arguments.of("decode startup packet with mismatched declared length", createStartupPacket(SSL_REQUEST_PAYLOAD_LENGTH - 1, 1), 0, true),
                Arguments.of("decode startup packet with additional payload byte", createStartupPacketWithAdditionalByte(SSL_REQUEST_PAYLOAD_LENGTH + 1, 1), 1, false));
    }
    
    private static Stream<Arguments> decodeWithoutPendingMessagesCases() {
        return Stream.of(
                Arguments.of("decode simple query command", PostgreSQLCommandPacketType.SIMPLE_QUERY.getValue()),
                Arguments.of("decode sync command", PostgreSQLCommandPacketType.SYNC_COMMAND.getValue()),
                Arguments.of("decode flush command", PostgreSQLCommandPacketType.FLUSH_COMMAND.getValue()));
    }
    
    private static Stream<Arguments> decodeWithPreparedStateCases() {
        return Stream.of(
                Arguments.of("decode with invalid header", Unpooled.wrappedBuffer(new byte[4]), 0, 0, 0, false, 0),
                Arguments.of("decode with incomplete payload", createCommandPacket(PostgreSQLCommandPacketType.SIMPLE_QUERY.getValue(), 8), 0, 0, 0, false, 0),
                Arguments.of("decode with aggregation command", createCommandPacket(PostgreSQLCommandPacketType.PARSE_COMMAND.getValue(), 4), 0, 0, 1, false, 0),
                Arguments.of("decode with pending messages", createCommandPacket(PostgreSQLCommandPacketType.SIMPLE_QUERY.getValue(), 4), 1, 1, 0, true, 10));
    }
    
    private static Stream<Arguments> encodeCases() {
        return Stream.of(
                Arguments.of("encode non identifier packet", false, false, false, '\0'),
                Arguments.of("encode identifier packet", true, false, true, PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST.getValue()),
                Arguments.of("encode packet with write exception", false, true, true, PostgreSQLMessagePacketType.ERROR_RESPONSE.getValue()));
    }
}
