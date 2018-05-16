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

package io.shardingsphere.proxy.transport.mysql.codec;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.quit.ComQuitPacket;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MySQLPacketCodecTest {
    private MySQLPacketCodec mySQLPacketCodec;

    private ChannelHandlerContext channelHandlerContext;

    private ByteBuf byteBuf;

    @Before
    public void init() {
        mySQLPacketCodec = new MySQLPacketCodec();
        channelHandlerContext = mock(ChannelHandlerContext.class);
        byteBuf = mock(ByteBuf.class);
    }

    @Test
    public void assertMySQLPacketDoDecode() {
        final List<Object> out = Lists.newArrayList();
        when(byteBuf.markReaderIndex()).thenReturn(byteBuf);
        when(byteBuf.markReaderIndex().readMedium()).thenReturn(50);
        when(byteBuf.readRetainedSlice(anyInt())).thenReturn(byteBuf);
        mySQLPacketCodec.doDecode(channelHandlerContext, byteBuf, out, 54);
        assertThat(out.size(), is(1));
    }

    @Test
    public void assertMySQLPacketDoEncode() {
        final MySQLPacket message = new ComQuitPacket(10);
        when(channelHandlerContext.alloc()).thenReturn(mock(ByteBufAllocator.class));
        when(channelHandlerContext.alloc().buffer()).thenReturn(byteBuf);
        when(byteBuf.writeMediumLE(anyInt())).thenReturn(byteBuf);
        when(byteBuf.writeByte(anyInt())).thenReturn(byteBuf);
        when(byteBuf.writeBytes(ArgumentMatchers.<ByteBuf>any())).thenReturn(byteBuf);
        mySQLPacketCodec.doEncode(channelHandlerContext, message, byteBuf);
    }
}
