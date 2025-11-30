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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthMoreDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.util.List;

/**
 * MySQL negotiate package decoder.
 */
public final class MySQLNegotiatePackageDecoder extends ByteToMessageDecoder {
    
    private volatile boolean handshakeReceived;
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(in, ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        if (handshakeReceived) {
            MySQLPacket responsePacket = decodeResponsePacket(payload);
            if (responsePacket instanceof MySQLOKPacket) {
                ctx.channel().pipeline().remove(this);
            }
            out.add(responsePacket);
        } else {
            out.add(decodeHandshakePacket(payload));
            handshakeReceived = true;
        }
    }
    
    private MySQLHandshakePacket decodeHandshakePacket(final MySQLPacketPayload payload) {
        return new MySQLHandshakePacket(payload);
    }
    
    private MySQLPacket decodeResponsePacket(final MySQLPacketPayload payload) {
        int header = payload.getByteBuf().getByte(0) & 0xff;
        switch (header) {
            case MySQLErrPacket.HEADER:
                return new MySQLErrPacket(payload);
            case MySQLOKPacket.HEADER:
                return new MySQLOKPacket(payload);
            case MySQLAuthSwitchRequestPacket.HEADER:
                return new MySQLAuthSwitchRequestPacket(payload);
            case MySQLAuthMoreDataPacket.HEADER:
                return new MySQLAuthMoreDataPacket(payload);
            default:
                throw new UnsupportedSQLOperationException(String.format("Unsupported negotiate response header: %X", header));
        }
    }
}
