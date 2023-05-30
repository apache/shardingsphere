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

package org.apache.shardingsphere.db.protocol.mysql.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.shardingsphere.db.protocol.event.WriteCompleteEvent;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.netty.ProxyFlowControlHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLSequenceIdInboundHandlerTest {
    
    @Test
    void assertChannelReadWithFlowControl() {
        EmbeddedChannel channel = new EmbeddedChannel(new FixtureOutboundHandler(), new ProxyFlowControlHandler(), new MySQLSequenceIdInboundHandler(), new FixtureInboundHandler());
        channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID).set(new AtomicInteger());
        channel.writeInbound(Unpooled.wrappedBuffer(new byte[1]), Unpooled.wrappedBuffer(new byte[1]), Unpooled.wrappedBuffer(new byte[1]));
        assertThat(channel.<ByteBuf>readOutbound().readUnsignedByte(), is((short) 1));
        assertThat(channel.<ByteBuf>readOutbound().readUnsignedByte(), is((short) 1));
        assertThat(channel.<ByteBuf>readOutbound().readUnsignedByte(), is((short) 1));
    }
    
    private static class FixtureOutboundHandler extends ChannelOutboundHandlerAdapter {
        
        @Override
        public void write(final ChannelHandlerContext context, final Object msg, final ChannelPromise promise) {
            byte sequenceId = (byte) context.channel().attr(MySQLConstants.MYSQL_SEQUENCE_ID).get().getAndIncrement();
            context.writeAndFlush(Unpooled.wrappedBuffer(new byte[]{sequenceId}));
        }
    }
    
    private static class FixtureInboundHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(final ChannelHandlerContext context, final Object msg) {
            context.channel().config().setAutoRead(false);
            context.executor().execute(() -> {
                context.writeAndFlush(Unpooled.EMPTY_BUFFER);
                context.channel().pipeline().fireUserEventTriggered(new WriteCompleteEvent());
            });
        }
    }
}
