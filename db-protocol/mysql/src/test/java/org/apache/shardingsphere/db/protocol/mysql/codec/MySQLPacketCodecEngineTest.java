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

package org.apache.shardingsphere.db.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLPacketCodecEngineTest {
    
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
        assertTrue(new MySQLPacketCodecEngine().isValidHeader(50));
    }
    
    @Test
    public void assertIsInvalidHeader() {
        assertFalse(new MySQLPacketCodecEngine().isValidHeader(3));
    }
    
    @Test
    public void assertDecode() {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(50);
        when(byteBuf.readableBytes()).thenReturn(51);
        when(byteBuf.readRetainedSlice(51)).thenReturn(byteBuf);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    public void assertDecodeWithEmptyPacket() {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(1);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(0);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodecEngine().decode(context, byteBuf, out);
        assertThat(out.size(), is(1));
    }
    
    @Test
    public void assertDecodeWithStickyPacket() {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(50);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodecEngine().decode(context, byteBuf, out);
        assertTrue(out.isEmpty());
    }
    
    @Test
    public void assertDecodePacketMoreThan16MB() {
        MySQLPacketCodecEngine engine = new MySQLPacketCodecEngine();
        when(context.alloc().compositeBuffer(2)).thenReturn(new CompositeByteBuf(UnpooledByteBufAllocator.DEFAULT, false, 2));
        List<Object> actual = new ArrayList<>(1);
        for (ByteBuf each : preparePacketMoreThan16MB()) {
            engine.decode(context, each, actual);
        }
        assertThat(actual.size(), is(1));
        assertThat(((ByteBuf) actual.get(0)).readableBytes(), is(1 << 24));
    }
    
    private List<ByteBuf> preparePacketMoreThan16MB() {
        byte[] firstPacketData = new byte[4 + (1 << 24) - 1];
        firstPacketData[0] = firstPacketData[1] = firstPacketData[2] = (byte) 0xff;
        firstPacketData[3] = (byte) 0;
        byte[] secondPacketData = new byte[]{0x00, 0x00, 0x00, 0x01};
        return Arrays.asList(Unpooled.wrappedBuffer(firstPacketData), Unpooled.wrappedBuffer(secondPacketData));
    }
    
    @Test
    public void assertEncode() {
        when(byteBuf.writeInt(anyInt())).thenReturn(byteBuf);
        when(byteBuf.markWriterIndex()).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(8);
        MySQLPacket actualMessage = mock(MySQLPacket.class);
        when(actualMessage.getSequenceId()).thenReturn(1);
        new MySQLPacketCodecEngine().encode(context, actualMessage, byteBuf);
        verify(byteBuf).writeInt(0);
        verify(byteBuf).markWriterIndex();
        verify(actualMessage).write(any(MySQLPacketPayload.class));
        verify(byteBuf).setMediumLE(0, 4);
        verify(byteBuf).setByte(3, 1);
    }
    
    @Test
    public void assertEncodeOccursException() {
        when(byteBuf.writeInt(anyInt())).thenReturn(byteBuf);
        when(byteBuf.markWriterIndex()).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(12);
        RuntimeException ex = mock(RuntimeException.class);
        MySQLPacket actualMessage = mock(MySQLPacket.class);
        doThrow(ex).when(actualMessage).write(any(MySQLPacketPayload.class));
        when(actualMessage.getSequenceId()).thenReturn(2);
        new MySQLPacketCodecEngine().encode(context, actualMessage, byteBuf);
        verify(byteBuf).writeInt(0);
        verify(byteBuf).markWriterIndex();
        verify(byteBuf).resetWriterIndex();
        verify(byteBuf).setMediumLE(0, 8);
        verify(byteBuf).setByte(3, 2);
    }
    
    @Test
    public void assertCreatePacketPayload() {
        assertThat(new MySQLPacketCodecEngine().createPacketPayload(byteBuf, StandardCharsets.UTF_8).getByteBuf(), is(byteBuf));
    }
}
