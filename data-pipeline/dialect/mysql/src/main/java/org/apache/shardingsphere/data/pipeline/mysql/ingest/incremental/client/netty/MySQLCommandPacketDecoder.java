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
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.InternalResultSet;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MySQL command packet decoder.
 */
@HighFrequencyInvocation
public final class MySQLCommandPacketDecoder extends ByteToMessageDecoder {
    
    private final AtomicReference<States> currentState = new AtomicReference<>(States.RESPONSE_PACKET);
    
    private final AtomicReference<InternalResultSet> internalResultSet = new AtomicReference<>();
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(in, ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        decodeCommandPacket(payload, out);
    }
    
    private void decodeCommandPacket(final MySQLPacketPayload payload, final List<Object> out) {
        if (States.FIELD_PACKET == currentState.get()) {
            decodeFieldPacket(payload);
            return;
        }
        if (States.ROW_DATA_PACKET == currentState.get()) {
            decodeRowDataPacket(payload, out);
            return;
        }
        decodeResponsePacket(payload, out);
    }
    
    private void decodeFieldPacket(final MySQLPacketPayload payload) {
        if (MySQLEofPacket.HEADER == (payload.getByteBuf().getByte(0) & 0xff)) {
            new MySQLEofPacket(payload);
            currentState.set(States.ROW_DATA_PACKET);
        } else {
            internalResultSet.get().getFieldDescriptors().add(new MySQLColumnDefinition41Packet(payload));
        }
    }
    
    private void decodeRowDataPacket(final MySQLPacketPayload payload, final List<Object> out) {
        if (MySQLEofPacket.HEADER == (payload.getByteBuf().getByte(0) & 0xff)) {
            new MySQLEofPacket(payload);
            out.add(internalResultSet.get());
            currentState.set(States.RESPONSE_PACKET);
            internalResultSet.set(null);
        } else {
            internalResultSet.get().getFieldValues().add(new MySQLTextResultSetRowPacket(payload, internalResultSet.get().getHeader().getColumnCount()));
        }
    }
    
    private void decodeResponsePacket(final MySQLPacketPayload payload, final List<Object> out) {
        switch (payload.getByteBuf().getByte(0) & 0xff) {
            case MySQLErrPacket.HEADER:
                out.add(new MySQLErrPacket(payload));
                break;
            case MySQLOKPacket.HEADER:
                out.add(new MySQLOKPacket(payload));
                break;
            default:
                MySQLFieldCountPacket fieldCountPacket = new MySQLFieldCountPacket(payload);
                currentState.set(States.FIELD_PACKET);
                internalResultSet.set(new InternalResultSet(fieldCountPacket));
                break;
        }
    }
    
    private enum States {
        
        RESPONSE_PACKET, FIELD_PACKET, ROW_DATA_PACKET
    }
}
