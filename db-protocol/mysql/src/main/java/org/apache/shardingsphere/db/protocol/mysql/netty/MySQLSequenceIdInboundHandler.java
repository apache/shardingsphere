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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;

/**
 * Handle MySQL sequence ID before sending to downstream.
 */
public final class MySQLSequenceIdInboundHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        short sequenceId = byteBuf.readUnsignedByte();
        context.channel().attr(MySQLConstants.MYSQL_SEQUENCE_ID).get().set(sequenceId + 1);
        context.fireChannelRead(byteBuf.readSlice(byteBuf.readableBytes()));
    }
}
