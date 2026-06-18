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

package org.apache.shardingsphere.database.protocol.mysql.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handle MySQL sequence ID before sending to downstream.
 */
public final class MySQLSequenceIdInboundHandler extends ChannelInboundHandlerAdapter {
    
    public MySQLSequenceIdInboundHandler(final Channel channel) {
        channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).set(new AtomicInteger());
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        short sequenceId = byteBuf.readUnsignedByte();
        context.channel().attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY).get().set(sequenceId + 1);
        context.fireChannelRead(byteBuf.readSlice(byteBuf.readableBytes()));
    }
}
