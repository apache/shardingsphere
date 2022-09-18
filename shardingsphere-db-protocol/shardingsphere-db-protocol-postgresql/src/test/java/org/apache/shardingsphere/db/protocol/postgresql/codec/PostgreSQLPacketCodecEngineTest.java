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

package org.apache.shardingsphere.db.protocol.postgresql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLPacketCodecEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Before
    public void setup() {
        when(context.channel().attr(AttributeKey.<Charset>valueOf(Charset.class.getName())).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    public void assertIsValidHeader() {
        assertTrue(new PostgreSQLPacketCodecEngine().isValidHeader(50));
    }
    
    @Test
    public void assertIsInvalidHeader() {
        assertTrue(new PostgreSQLPacketCodecEngine().isValidHeader(4));
    }
    
    @Test
    public void assertDecode() {
        when(byteBuf.readableBytes()).thenReturn(51, 47, 0);
        List<Object> out = new LinkedList<>();
        new PostgreSQLPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    public void assertDecodeWithStickyPacket() {
        List<Object> out = new LinkedList<>();
        new PostgreSQLPacketCodecEngine().decode(context, byteBuf, out);
        assertTrue(out.isEmpty());
    }
    
    @Test
    public void assertEncodePostgreSQLPacket() {
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        new PostgreSQLPacketCodecEngine().encode(context, packet, byteBuf);
        verify(packet).write(any(PostgreSQLPacketPayload.class));
    }
    
    @Test
    public void assertEncodePostgreSQLIdentifierPacket() {
        PostgreSQLIdentifierPacket packet = mock(PostgreSQLIdentifierPacket.class);
        when(packet.getIdentifier()).thenReturn(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST);
        when(byteBuf.readableBytes()).thenReturn(9);
        new PostgreSQLPacketCodecEngine().encode(context, packet, byteBuf);
        verify(byteBuf).writeByte(PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST.getValue());
        verify(byteBuf).writeInt(0);
        verify(packet).write(any(PostgreSQLPacketPayload.class));
        verify(byteBuf).setInt(1, 8);
    }
    
    @Test
    public void assertEncodeOccursException() {
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        RuntimeException ex = mock(RuntimeException.class);
        when(ex.getMessage()).thenReturn("Error");
        doThrow(ex).when(packet).write(any(PostgreSQLPacketPayload.class));
        when(byteBuf.readableBytes()).thenReturn(9);
        new PostgreSQLPacketCodecEngine().encode(context, packet, byteBuf);
        verify(byteBuf).resetWriterIndex();
        verify(byteBuf).writeByte(PostgreSQLMessagePacketType.ERROR_RESPONSE.getValue());
        verify(byteBuf).setInt(1, 8);
    }
    
    @Test
    public void assertCreatePacketPayload() {
        assertThat(new PostgreSQLPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
}
