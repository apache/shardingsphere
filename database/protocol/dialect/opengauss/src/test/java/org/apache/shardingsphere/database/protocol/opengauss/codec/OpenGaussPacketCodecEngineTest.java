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

package org.apache.shardingsphere.database.protocol.opengauss.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenGaussPacketCodecEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @BeforeEach
    void setup() {
        when(context.channel().attr(AttributeKey.<Charset>valueOf(Charset.class.getName())).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertIsValidHeader() {
        assertTrue(new OpenGaussPacketCodecEngine().isValidHeader(50));
    }
    
    @Test
    void assertIsInvalidHeader() {
        assertTrue(new OpenGaussPacketCodecEngine().isValidHeader(4));
    }
    
    @Test
    void assertDecode() {
        when(byteBuf.readableBytes()).thenReturn(51, 47, 0);
        List<Object> out = new LinkedList<>();
        new OpenGaussPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    void assertDecodeWithStickyPacket() {
        List<Object> out = new LinkedList<>();
        new OpenGaussPacketCodecEngine().decode(context, byteBuf, out);
        assertTrue(out.isEmpty());
    }
    
    @Test
    void assertEncodePostgreSQLPacket() {
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        new OpenGaussPacketCodecEngine().encode(context, packet, byteBuf);
        verify(packet).write(any(PostgreSQLPacketPayload.class));
    }
    
    @Test
    void assertEncodePostgreSQLIdentifierPacket() {
        PostgreSQLIdentifierPacket packet = mock(PostgreSQLIdentifierPacket.class);
        when(packet.getIdentifier()).thenReturn(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST);
        when(byteBuf.readableBytes()).thenReturn(9);
        new OpenGaussPacketCodecEngine().encode(context, packet, byteBuf);
        verify(byteBuf).writeByte(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST.getValue());
        verify(byteBuf).writeInt(0);
        verify(packet).write(any(PostgreSQLPacketPayload.class));
        verify(byteBuf).setInt(1, 8);
    }
    
    @Test
    void assertEncodeOccursException() {
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        RuntimeException ex = mock(RuntimeException.class);
        when(ex.getMessage()).thenReturn("Error");
        doThrow(ex).when(packet).write(any(PostgreSQLPacketPayload.class));
        when(byteBuf.readableBytes()).thenReturn(9);
        new OpenGaussPacketCodecEngine().encode(context, packet, byteBuf);
        verify(byteBuf).resetWriterIndex();
        verify(byteBuf).writeByte(PostgreSQLMessagePacketType.ERROR_RESPONSE.getValue());
        verify(byteBuf).setInt(1, 8);
    }
    
    @Test
    void assertCreatePacketPayload() {
        assertThat(new OpenGaussPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
}
