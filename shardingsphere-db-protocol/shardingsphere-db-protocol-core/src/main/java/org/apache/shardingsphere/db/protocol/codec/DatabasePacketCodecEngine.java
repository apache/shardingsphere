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

package org.apache.shardingsphere.db.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;

import java.util.List;

/**
 * Database packet codec engine.
 * 
 * @param <T> type of database packet
 */
public interface DatabasePacketCodecEngine<T extends DatabasePacket<?>> {
    
    /**
     * Judge is valid header or not.
     * 
     * @param readableBytes readable bytes
     * @return is valid header or not
     */
    boolean isValidHeader(int readableBytes);
    
    /**
     * Decode.
     * 
     * @param context channel handler context
     * @param in input
     * @param out output
     * @param readableBytes readable bytes
     */
    void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out, int readableBytes);
    
    /**
     * Encode.
     * 
     * @param context channel handler context
     * @param message message of database packet
     * @param out output
     */
    void encode(ChannelHandlerContext context, T message, ByteBuf out);
    
    /**
     * Create packet payload.
     *
     * @param message message
     * @return packet payload
     */
    PacketPayload createPacketPayload(ByteBuf message);
}
