/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingjdbc.proxy.transport.common.codec.PacketCodec;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;

import java.util.List;

/**
 * MySQL packet codec.
 * 
 * @author zhangliang 
 */
public final class MySQLPacketCodec extends PacketCodec<MySQLPacket> {
    
    @Override
    protected boolean isValidHeader(final int readableBytes) {
        return readableBytes > MySQLPacket.PAYLOAD_LENGTH + MySQLPacket.SEQUENCE_LENGTH;
    }
    
    @Override
    protected void doDecode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int readableBytes) {
        int payloadLength = in.markReaderIndex().readMediumLE();
        int realPacketLength = payloadLength + MySQLPacket.PAYLOAD_LENGTH + MySQLPacket.SEQUENCE_LENGTH;
        if (readableBytes < realPacketLength) {
            in.resetReaderIndex();
            return;
        }
        if (readableBytes > realPacketLength) {
            out.add(in.readRetainedSlice(payloadLength + MySQLPacket.SEQUENCE_LENGTH));
            return;
        }
        out.add(in);
    }
    
    @Override
    protected void doEncode(final ChannelHandlerContext context, final MySQLPacket message, final ByteBuf out) {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload(context.alloc().buffer());
        message.write(mysqlPacketPayload);
        out.writeMediumLE(mysqlPacketPayload.getByteBuf().readableBytes());
        out.writeByte(message.getSequenceId());
        out.writeBytes(mysqlPacketPayload.getByteBuf());
    }
}
