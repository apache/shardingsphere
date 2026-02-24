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

package org.apache.shardingsphere.database.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
class MySQLPacketCodecEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @BeforeEach
    void setup() {
        when(context.channel().attr(AttributeKey.<Charset>valueOf(Charset.class.getName())).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get()).thenReturn(new AtomicInteger());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isValidHeaderArguments")
    void assertIsValidHeader(final String name, final int readableBytes, final boolean expected) {
        assertThat(new MySQLPacketCodecEngine().isValidHeader(readableBytes), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeArguments")
    void assertDecode(final String name, final int payloadLength, final int readableBytes, final int expectedOutSize) {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(payloadLength);
        when(byteBuf.readableBytes()).thenReturn(readableBytes);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(expectedOutSize));
    }
    
    @Test
    void assertDecodePacketMoreThan16MB() {
        MySQLPacketCodecEngine engine = new MySQLPacketCodecEngine();
        when(context.alloc().compositeBuffer(3)).thenReturn(new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, 3));
        List<Object> actual = new ArrayList<>(1);
        for (ByteBuf each : preparePacketMoreThan16MB()) {
            engine.decode(context, each, actual);
        }
        assertThat(actual.size(), is(1));
        assertThat(((ByteBuf) actual.get(0)).readableBytes(), is(1 << 24));
    }
    
    @Test
    void assertDecodePacketMoreThan32MB() {
        MySQLPacketCodecEngine engine = new MySQLPacketCodecEngine();
        List<ByteBuf> packets = preparePacketMoreThan32MB();
        List<Object> actual = new ArrayList<>(1);
        engine.decode(context, packets.get(0), actual);
        engine.decode(context, packets.get(1), actual);
        when(context.alloc().compositeBuffer(4)).thenReturn(new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, 4));
        engine.decode(context, packets.get(2), actual);
        assertThat(actual.size(), is(1));
        assertThat(((ByteBuf) actual.get(0)).readableBytes(), is(1 << 25));
    }
    
    private List<ByteBuf> preparePacketMoreThan16MB() {
        byte[] firstPacketData = new byte[4 + (1 << 24) - 1];
        firstPacketData[0] = firstPacketData[1] = firstPacketData[2] = (byte) 0xff;
        firstPacketData[3] = (byte) 0;
        byte[] secondPacketData = new byte[]{0x00, 0x00, 0x00, 0x01};
        return Arrays.asList(Unpooled.wrappedBuffer(firstPacketData), Unpooled.wrappedBuffer(secondPacketData));
    }
    
    private List<ByteBuf> preparePacketMoreThan32MB() {
        byte[] firstPacketData = new byte[4 + (1 << 24) - 1];
        firstPacketData[0] = firstPacketData[1] = firstPacketData[2] = (byte) 0xff;
        byte[] secondPacketData = new byte[4 + (1 << 24) - 1];
        secondPacketData[0] = secondPacketData[1] = secondPacketData[2] = (byte) 0xff;
        byte[] thirdPacketData = new byte[]{0x01, 0x00, 0x00, 0x02, 0x01};
        return Arrays.asList(Unpooled.wrappedBuffer(firstPacketData), Unpooled.wrappedBuffer(secondPacketData), Unpooled.wrappedBuffer(thirdPacketData));
    }
    
    @Test
    void assertEncode() {
        when(byteBuf.writeInt(anyInt())).thenReturn(byteBuf);
        when(byteBuf.markWriterIndex()).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(8);
        MySQLPacket actualMessage = mock(MySQLPacket.class);
        context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get().set(1);
        new MySQLPacketCodecEngine().encode(context, actualMessage, byteBuf);
        verify(byteBuf).writeInt(0);
        verify(byteBuf).markWriterIndex();
        verify(actualMessage).write(any(MySQLPacketPayload.class));
        verify(byteBuf).setMediumLE(0, 4);
        verify(byteBuf).setByte(3, 1);
    }
    
    @Test
    void assertEncodePacketMoreThan16MB() {
        CompositeByteBuf expected = new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, 6);
        when(context.alloc().compositeBuffer(6)).thenReturn(expected);
        when(context.alloc().ioBuffer(4, 4)).thenReturn(Unpooled.buffer(4, 4), Unpooled.buffer(4, 4), Unpooled.buffer(4, 4));
        MySQLPacketCodecEngine engine = new MySQLPacketCodecEngine();
        engine.encode(context, mock(MySQLPacket.class), prepareByteBufAlmost32MB());
        verify(context).write(expected);
        assertThat(expected.numComponents(), is(5));
        assertThat(expected.component(0).getUnsignedByte(3), is((short) 0));
        assertThat(expected.component(1).readableBytes(), is(0xFFFFFF));
        assertThat(expected.component(2).getUnsignedByte(3), is((short) 1));
        assertThat(expected.component(3).readableBytes(), is(0xFFFFFF));
        assertThat(expected.component(4).getUnsignedByte(3), is((short) 2));
    }
    
    private ByteBuf prepareByteBufAlmost32MB() {
        int max = 0xFFFFFF << 1;
        ByteBuf result = Unpooled.buffer(max + 4);
        result.writerIndex(max);
        return result.retain();
    }
    
    @Test
    void assertEncodeOccursException() {
        when(byteBuf.writeInt(anyInt())).thenReturn(byteBuf);
        when(byteBuf.markWriterIndex()).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(12);
        RuntimeException ex = mock(RuntimeException.class);
        MySQLPacket actualMessage = mock(MySQLPacket.class);
        doThrow(ex).when(actualMessage).write(any(MySQLPacketPayload.class));
        context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get().set(2);
        new MySQLPacketCodecEngine().encode(context, actualMessage, byteBuf);
        verify(byteBuf).writeInt(0);
        verify(byteBuf).markWriterIndex();
        verify(byteBuf).resetWriterIndex();
        verify(byteBuf).setMediumLE(0, 8);
        verify(byteBuf).setByte(3, 2);
    }
    
    @Test
    void assertCreatePacketPayload() {
        assertThat(new MySQLPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
    
    private static Stream<Arguments> isValidHeaderArguments() {
        return Stream.of(
                Arguments.of("exact header size", 4, true),
                Arguments.of("larger than header size", 50, true),
                Arguments.of("smaller than header size", 3, false));
    }
    
    private static Stream<Arguments> decodeArguments() {
        return Stream.of(
                Arguments.of("normal packet", 50, 51, 1),
                Arguments.of("empty payload packet", 0, 1, 1),
                Arguments.of("sticky packet", 50, 0, 0));
    }
}
