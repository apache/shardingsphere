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

package org.apache.shardingsphere.db.protocol.firebird.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdPacketCodecEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setup() {
        when(context.channel().attr(AttributeKey.<Charset>valueOf(Charset.class.getName())).get()).thenReturn(StandardCharsets.UTF_8);
        when(context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).get()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(context.alloc().compositeBuffer(anyInt())).thenAnswer(invocation -> new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, invocation.getArgument(0)));
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
        ByteBuf byteBuf = Unpooled.buffer(8);
        byteBuf.writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue());
        byteBuf.writeInt(1);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
        assertThat(((ByteBuf) out.get(0)).readableBytes(), is(8));
    }
    
    @Test
    void assertDecodeMultiplePackets() {
        ByteBuf byteBuf = Unpooled.buffer(16);
        byteBuf.writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue());
        byteBuf.writeInt(1);
        byteBuf.writeInt(FirebirdCommandPacketType.ALLOCATE_STATEMENT.getValue());
        byteBuf.writeInt(2);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(2));
    }
    
    @Test
    void assertDecodeWithPartialPacket() {
        ByteBuf byteBuf = Unpooled.buffer(16);
        byteBuf.writeInt(FirebirdCommandPacketType.TRANSACTION.getValue());
        byteBuf.writeInt(1);
        List<Object> out = new LinkedList<>();
        new FirebirdPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    void assertEncode() {
        ByteBuf out = mock(ByteBuf.class);
        DatabasePacket packet = mock(DatabasePacket.class);
        new FirebirdPacketCodecEngine().encode(context, packet, out);
        verify(packet).write(any(FirebirdPacketPayload.class));
    }
    
    @Test
    void assertEncodeOccursException() {
        ByteBuf out = mock(ByteBuf.class);
        DatabasePacket packet = mock(DatabasePacket.class);
        doThrow(RuntimeException.class).when(packet).write(any(FirebirdPacketPayload.class));
        new FirebirdPacketCodecEngine().encode(context, packet, out);
        verify(out).resetWriterIndex();
    }
    
    @Test
    void assertCreatePacketPayload() {
        ByteBuf byteBuf = Unpooled.buffer();
        assertThat(new FirebirdPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
}
