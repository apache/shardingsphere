/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLPacketCodecTest {
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertIsValidHeader() {
        assertTrue(new MySQLPacketCodec().isValidHeader(50));
    }
    
    @Test
    public void assertIsInvalidHeader() {
        assertFalse(new MySQLPacketCodec().isValidHeader(3));
    }
    
    @Test
    public void assertDoDecode() {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readMediumLE()).thenReturn(50);
        when(byteBuf.readRetainedSlice(51)).thenReturn(byteBuf);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodec().doDecode(context, byteBuf, out, 54);
        assertThat(out.size(), is(1));
    }
    
    @Test
    public void assertDoDecodeWithStickyPacket() {
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.readMediumLE()).thenReturn(50);
        List<Object> out = new LinkedList<>();
        new MySQLPacketCodec().doDecode(context, byteBuf, out, 40);
        assertTrue(out.isEmpty());
    }
    
    @Test
    public void assertDoEncode() {
        ByteBufAllocator byteBufAllocator = mock(ByteBufAllocator.class);
        when(context.alloc()).thenReturn(byteBufAllocator);
        ByteBuf payloadByteBuf = mock(ByteBuf.class);
        when(byteBufAllocator.buffer()).thenReturn(payloadByteBuf);
        when(payloadByteBuf.readableBytes()).thenReturn(50);
        MySQLPacket actualMessage = mock(MySQLPacket.class);
        when(actualMessage.getSequenceId()).thenReturn(1);
        new MySQLPacketCodec().doEncode(context, actualMessage, byteBuf);
        verify(actualMessage).write(ArgumentMatchers.<MySQLPacketPayload>any());
        verify(byteBuf).writeMediumLE(50);
        verify(byteBuf).writeByte(1);
        verify(byteBuf).writeBytes(payloadByteBuf);
    }
}
