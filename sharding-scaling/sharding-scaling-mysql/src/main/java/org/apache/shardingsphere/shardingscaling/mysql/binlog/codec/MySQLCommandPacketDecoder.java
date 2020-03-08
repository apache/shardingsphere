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

import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.EofPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.ErrorPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.FieldPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.InternalResultSet;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.OkPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.ResultSetHeaderPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.RowDataPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * MySQL Command Packet decoder.
 */
@Slf4j
public final class MySQLCommandPacketDecoder extends ByteToMessageDecoder {
    
    private enum States { Initiate, ResponsePacket, FieldPacket, RowDataPacket }
    
    private States currentState = States.Initiate;
    
    private InternalResultSet internalResultSet;
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        // first packet from server is handshake initialization packet
        if (States.Initiate.equals(currentState)) {
            out.add(decodeHandshakeInitializationPacket(in));
            currentState = States.ResponsePacket;
            return;
        }
        if (States.FieldPacket.equals(currentState)) {
            decodeFieldPacket(in);
            return;
        }
        if (States.RowDataPacket.equals(currentState)) {
            decodeRowDataPacket(in, out);
            return;
        }
        decodeResponsePacket(in, out);
    }
    
    private HandshakeInitializationPacket decodeHandshakeInitializationPacket(final ByteBuf in) {
        HandshakeInitializationPacket result = new HandshakeInitializationPacket();
        result.fromByteBuf(in);
        if (PacketConstants.PROTOCOL_VERSION != result.getProtocolVersion()) {
            throw new UnsupportedOperationException();
        }
        if (!AuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.equals(result.getAuthPluginName())) {
            throw new UnsupportedOperationException();
        }
        return result;
    }
    
    private void decodeFieldPacket(final ByteBuf in) {
        if (PacketConstants.EOF_PACKET_MARK != in.getByte(0)) {
            FieldPacket fieldPacket = new FieldPacket();
            fieldPacket.fromByteBuf(in);
            internalResultSet.getFieldDescriptors().add(fieldPacket);
        } else {
            EofPacket eofPacket = new EofPacket();
            eofPacket.fromByteBuf(in);
            currentState = States.RowDataPacket;
        }
    }
    
    private void decodeRowDataPacket(final ByteBuf in, final List<Object> out) {
        if (PacketConstants.EOF_PACKET_MARK != in.getByte(0)) {
            RowDataPacket rowDataPacket = new RowDataPacket();
            rowDataPacket.fromByteBuf(in);
            internalResultSet.getFieldValues().add(rowDataPacket);
        } else {
            EofPacket eofPacket = new EofPacket();
            eofPacket.fromByteBuf(in);
            out.add(internalResultSet);
            currentState = States.ResponsePacket;
            internalResultSet = null;
        }
    }
    
    private void decodeResponsePacket(final ByteBuf in, final List<Object> out) {
        switch (in.getByte(0)) {
            case PacketConstants.ERR_PACKET_MARK:
                ErrorPacket error = new ErrorPacket();
                error.fromByteBuf(in);
                out.add(error);
                break;
            case PacketConstants.OK_PACKET_MARK:
                OkPacket ok = new OkPacket();
                ok.fromByteBuf(in);
                out.add(ok);
                break;
            default:
                ResultSetHeaderPacket resultSetHeaderPacket = new ResultSetHeaderPacket();
                resultSetHeaderPacket.fromByteBuf(in);
                currentState = States.FieldPacket;
                internalResultSet = new InternalResultSet(resultSetHeaderPacket);
        }
    }
}
