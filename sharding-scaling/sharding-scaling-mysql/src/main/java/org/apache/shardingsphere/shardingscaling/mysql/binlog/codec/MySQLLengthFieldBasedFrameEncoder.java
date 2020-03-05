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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.HeaderPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL length field based frame encoder.
 */
@Slf4j
public final class MySQLLengthFieldBasedFrameEncoder extends MessageToByteEncoder {
    
    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) {
        ByteBuf byteBuf = ((AbstractPacket) msg).toByteBuf();
        HeaderPacket h = new HeaderPacket();
        h.setPacketBodyLength(byteBuf.readableBytes());
        h.setPacketSequenceNumber(((AbstractPacket) msg).getSequenceNumber());
        out.writeBytes(h.toByteBuf());
        out.writeBytes(byteBuf);
    }
}
