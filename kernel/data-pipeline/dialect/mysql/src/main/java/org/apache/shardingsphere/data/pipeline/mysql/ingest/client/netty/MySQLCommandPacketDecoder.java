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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.InternalResultSet;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.List;

/**
 * MySQL Command Packet decoder.
 */
public final class MySQLCommandPacketDecoder extends ByteToMessageDecoder {
    
    private enum States {
        ResponsePacket, FieldPacket, RowDataPacket
    }
    
    private volatile States currentState = States.ResponsePacket;
    
    private volatile InternalResultSet internalResultSet;
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(in, ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        decodeCommandPacket(payload, out);
    }
    
    private void decodeCommandPacket(final MySQLPacketPayload payload, final List<Object> out) {
        if (States.FieldPacket == currentState) {
            decodeFieldPacket(payload);
            return;
        }
        if (States.RowDataPacket == currentState) {
            decodeRowDataPacket(payload, out);
            return;
        }
        decodeResponsePacket(payload, out);
    }
    
    private void decodeFieldPacket(final MySQLPacketPayload payload) {
        if (MySQLEofPacket.HEADER != (payload.getByteBuf().getByte(1) & 0xff)) {
            internalResultSet.getFieldDescriptors().add(new MySQLColumnDefinition41Packet(payload));
        } else {
            new MySQLEofPacket(payload);
            currentState = States.RowDataPacket;
        }
    }
    
    private void decodeRowDataPacket(final MySQLPacketPayload payload, final List<Object> out) {
        if (MySQLEofPacket.HEADER != (payload.getByteBuf().getByte(1) & 0xff)) {
            internalResultSet.getFieldValues().add(new MySQLTextResultSetRowPacket(payload, internalResultSet.getHeader().getColumnCount()));
        } else {
            new MySQLEofPacket(payload);
            out.add(internalResultSet);
            currentState = States.ResponsePacket;
            internalResultSet = null;
        }
    }
    
    private void decodeResponsePacket(final MySQLPacketPayload payload, final List<Object> out) {
        switch (payload.getByteBuf().getByte(1) & 0xff) {
            case MySQLErrPacket.HEADER:
                out.add(new MySQLErrPacket(payload));
                break;
            case MySQLOKPacket.HEADER:
                out.add(new MySQLOKPacket(payload));
                break;
            default:
                MySQLFieldCountPacket fieldCountPacket = new MySQLFieldCountPacket(payload);
                currentState = States.FieldPacket;
                internalResultSet = new InternalResultSet(fieldCountPacket);
        }
    }
}
