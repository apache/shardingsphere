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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.shardingproxy.transport.common.codec.PacketCodec;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.PostgreSQLPacketPayload;

import java.util.List;

/**
 * PostgreSQL packet codec.
 *
 * @author zhangyonglun
 */
public final class PostgreSQLPacketCodec extends PacketCodec<PostgreSQLPacket> {
    
    @Override
    protected boolean isValidHeader(final int readableBytes) {
        return readableBytes > PostgreSQLPacket.MESSAGE_TYPE_LENGTH;
    }
    
    @Override
    protected void doDecode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int readableBytes) {
        in.readRetainedSlice(PostgreSQLPacket.MESSAGE_TYPE_LENGTH);
        int payloadLength = in.markReaderIndex().readInt();
        int realPacketLength = payloadLength + PostgreSQLPacket.MESSAGE_TYPE_LENGTH;
        if (readableBytes < realPacketLength) {
            in.resetReaderIndex();
            return;
        }
        out.add(in.readRetainedSlice(payloadLength - PostgreSQLPacket.PAYLOAD_LENGTH));
    }
    
    @Override
    protected void doEncode(final ChannelHandlerContext context, final PostgreSQLPacket message, final ByteBuf out) {
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(context.alloc().buffer())) {
            message.write(payload);
            out.writeMediumLE(payload.getByteBuf().readableBytes());
            out.writeByte(message.getSequenceId());
            out.writeBytes(payload.getByteBuf());
        }
    }
}
