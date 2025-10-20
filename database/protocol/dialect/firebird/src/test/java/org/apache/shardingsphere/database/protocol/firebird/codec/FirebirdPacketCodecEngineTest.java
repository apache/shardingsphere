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
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private DatabasePacket packet;
    
    @BeforeEach
    void setup() {
        when(context.channel().attr(AttributeKey.<Charset>valueOf(Charset.class.getName())).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(context.alloc().compositeBuffer(anyInt())).thenAnswer(inv -> mock(CompositeByteBuf.class, org.mockito.Mockito.RETURNS_SELF));
    }
    
    @Test
    void assertIsValidHeader() {
        assertTrue(new FirebirdPacketCodecEngine().isValidHeader(50));
    }
    
    @Test
    void assertIsInvalidHeader() {
        assertFalse(new FirebirdPacketCodecEngine().isValidHeader(3));
    }
    
    @Test
    void assertDecodeSingleAllocateStatement() {
        ByteBuf in = mock(ByteBuf.class);
        ByteBuf slice = mock(ByteBuf.class);
        when(in.readableBytes()).thenReturn(8, 0);
        when(in.readerIndex()).thenReturn(0);
        when(in.getInt(0)).thenReturn(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue());
        when(in.readRetainedSlice(8)).thenReturn(slice);
        when(slice.readableBytes()).thenReturn(8);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, in, out);
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(8));
    }
    
    @Test
    void assertDecodeMultiplePackets() {
        ByteBuf in = mock(ByteBuf.class);
        ByteBuf slice1 = mock(ByteBuf.class);
        ByteBuf slice2 = mock(ByteBuf.class);
        when(in.readableBytes()).thenReturn(16, 8, 8, 0);
        when(in.readerIndex()).thenReturn(0);
        when(in.getInt(0)).thenReturn(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue());
        when(in.readRetainedSlice(8)).thenReturn(slice1, slice2);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, in, out);
        assertThat(out.size(), is(2));
    }
    
    @Test
    void assertDecodeWithPartialPacket() {
        ByteBuf in = mock(ByteBuf.class);
        ByteBuf slice = mock(ByteBuf.class);
        ByteBuf payloadSlice = mock(ByteBuf.class);
        when(in.readableBytes()).thenReturn(8, 8, 8);
        when(in.readerIndex()).thenReturn(0);
        when(in.getInt(0)).thenReturn(FirebirdCommandPacketType.INFO_REQUEST.getValue());
        when(in.writerIndex()).thenReturn(8);
        when(in.capacity()).thenReturn(16);
        when(in.isReadable()).thenReturn(true, true, false);
        when(in.retainedSlice(anyInt(), anyInt())).thenReturn(payloadSlice);
        when(payloadSlice.release()).thenReturn(true);
        when(in.readRetainedSlice(8)).thenReturn(slice);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, in, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    void assertDecodeWithTruncatedPacketAcrossBuffers() {
        ByteBuf firstPart = mock(ByteBuf.class);
        ByteBuf firstSlice = mock(ByteBuf.class);
        ByteBuf secondPart = mock(ByteBuf.class);
        ByteBuf secondSlice = mock(ByteBuf.class);
        CompositeByteBuf firstComposite = mock(CompositeByteBuf.class, org.mockito.Mockito.RETURNS_SELF);
        CompositeByteBuf secondComposite = mock(CompositeByteBuf.class, org.mockito.Mockito.RETURNS_SELF);
        when(context.alloc().compositeBuffer(anyInt())).thenReturn(firstComposite, secondComposite);
        ByteBuf firstPayloadSlice = mock(ByteBuf.class);
        ByteBuf compositePayloadSlice = mock(ByteBuf.class);
        ByteBuf compositeOutputSlice = mock(ByteBuf.class);
        when(firstPart.readableBytes()).thenReturn(8, 8, 8, 8);
        when(firstPart.readerIndex()).thenReturn(0);
        when(firstPart.getInt(0)).thenReturn(FirebirdCommandPacketType.PREPARE_STATEMENT.getValue());
        when(firstPart.writerIndex()).thenReturn(8);
        when(firstPart.capacity()).thenReturn(8);
        when(firstPart.isReadable()).thenReturn(true, true, false);
        when(firstPart.retainedSlice(anyInt(), anyInt())).thenReturn(firstPayloadSlice);
        when(firstPayloadSlice.release()).thenReturn(true);
        when(firstPart.readRetainedSlice(8)).thenReturn(firstSlice);
        when(secondPart.readableBytes()).thenReturn(4, 0);
        when(secondPart.isReadable()).thenReturn(true, false);
        when(secondPart.writerIndex()).thenReturn(4);
        when(secondPart.capacity()).thenReturn(4);
        when(secondPart.readRetainedSlice(4)).thenReturn(secondSlice);
        when(firstComposite.isReadable()).thenReturn(true, false);
        when(firstComposite.readableBytes()).thenReturn(12, 12, 12);
        when(firstComposite.readerIndex()).thenReturn(0);
        when(firstComposite.getInt(0)).thenReturn(FirebirdCommandPacketType.PREPARE_STATEMENT.getValue());
        when(firstComposite.retainedSlice(anyInt(), anyInt())).thenReturn(compositePayloadSlice);
        when(compositePayloadSlice.release()).thenReturn(true);
        when(firstComposite.readRetainedSlice(12)).thenReturn(compositeOutputSlice);
        when(compositeOutputSlice.readableBytes()).thenReturn(12);
        when(firstComposite.release()).thenReturn(true);
        List<Object> out = new LinkedList<>();
        FirebirdPacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
        try (MockedStatic<FirebirdCommandPacketFactory> mocked = mockStatic(FirebirdCommandPacketFactory.class)) {
            mocked.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any())).thenReturn(12, 12);
            codecEngine.decode(context, firstPart, out);
            assertTrue(out.isEmpty());
            codecEngine.decode(context, secondPart, out);
            assertThat(out.size(), is(1));
            assertThat(((ByteBuf) out.get(0)).readableBytes(), is(12));
        }
    }
    
    @Test
    void assertDecodeWithFullBufferAndValidLength() {
        ByteBuf in = mock(ByteBuf.class);
        ByteBuf slice = mock(ByteBuf.class);
        CompositeByteBuf composite = mock(CompositeByteBuf.class, org.mockito.Mockito.RETURNS_SELF);
        when(context.alloc().compositeBuffer(anyInt())).thenReturn(composite);
        ByteBuf payloadSlice = mock(ByteBuf.class);
        when(in.readableBytes()).thenReturn(8, 8, 8, 8);
        when(in.readerIndex()).thenReturn(0);
        when(in.getInt(0)).thenReturn(FirebirdCommandPacketType.PREPARE_STATEMENT.getValue());
        when(in.writerIndex()).thenReturn(8);
        when(in.capacity()).thenReturn(8);
        when(in.isReadable()).thenReturn(true, true, false);
        when(in.retainedSlice(anyInt(), anyInt())).thenReturn(payloadSlice);
        when(payloadSlice.release()).thenReturn(true);
        when(in.readRetainedSlice(8)).thenReturn(slice);
        when(composite.isReadable()).thenReturn(true, false);
        when(composite.readableBytes()).thenReturn(8, 8, 8);
        when(composite.readerIndex()).thenReturn(0);
        when(composite.getInt(0)).thenReturn(FirebirdCommandPacketType.PREPARE_STATEMENT.getValue());
        when(composite.retainedSlice(anyInt(), anyInt())).thenReturn(payloadSlice);
        when(slice.readableBytes()).thenReturn(8);
        when(composite.readRetainedSlice(8)).thenReturn(slice);
        when(composite.release()).thenReturn(true);
        List<Object> out = new LinkedList<>();
        try (MockedStatic<FirebirdCommandPacketFactory> mocked = mockStatic(FirebirdCommandPacketFactory.class)) {
            mocked.when(() -> FirebirdCommandPacketFactory.getExpectedLength(any(), any(), any())).thenReturn(8);
            new FirebirdPacketCodecEngine().decode(context, in, out);
            assertThat(out.size(), is(1));
            assertThat(((ByteBuf) out.get(0)).readableBytes(), is(8));
        }
    }
    
    @Test
    void assertEncode() {
        new FirebirdPacketCodecEngine().encode(context, packet, byteBuf);
        verify(packet).write(any(FirebirdPacketPayload.class));
    }
    
    @Test
    void assertEncodeOccursException() {
        doThrow(RuntimeException.class).when(packet).write(any(FirebirdPacketPayload.class));
        new FirebirdPacketCodecEngine().encode(context, packet, byteBuf);
        verify(byteBuf).resetWriterIndex();
    }
    
    @Test
    void assertCreatePacketPayload() {
        assertThat(new FirebirdPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
}
