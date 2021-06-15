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
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.error.CommonErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.List;

/**
 * Database packet codec for MySQL.
 */
public final class MySQLPacketCodecEngine implements DatabasePacketCodecEngine<MySQLPacket> {
    
    private static final int PAYLOAD_LENGTH = 3;
    
    private static final int SEQUENCE_LENGTH = 1;
    
    @Override
    public boolean isValidHeader(final int readableBytes) {
        return readableBytes >= PAYLOAD_LENGTH + SEQUENCE_LENGTH;
    }
    
    @Override
    public void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out, final int readableBytes) {
        int payloadLength = in.markReaderIndex().readMediumLE();
        int realPacketLength = payloadLength + PAYLOAD_LENGTH + SEQUENCE_LENGTH;
        if (readableBytes < realPacketLength) {
            in.resetReaderIndex();
            return;
        }
        out.add(in.readRetainedSlice(payloadLength + SEQUENCE_LENGTH));
    }
    
    @Override
    public void encode(final ChannelHandlerContext context, final MySQLPacket message, final ByteBuf out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(context.alloc().buffer());
        try {
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            payload.getByteBuf().resetWriterIndex();
            new MySQLErrPacket(1, CommonErrorCode.UNKNOWN_EXCEPTION, ex.getMessage()).write(payload);
        } finally {
            out.writeMediumLE(payload.getByteBuf().readableBytes());
            out.writeByte(message.getSequenceId());
            out.writeBytes(payload.getByteBuf());
            payload.close();
        }
    }
    
    @Override
    public MySQLPacketPayload createPacketPayload(final ByteBuf message) {
        return new MySQLPacketPayload(message);
    }
}
