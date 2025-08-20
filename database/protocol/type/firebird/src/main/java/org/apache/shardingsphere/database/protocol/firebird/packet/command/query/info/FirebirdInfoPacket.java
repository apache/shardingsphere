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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Information command packet for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdInfoPacket extends FirebirdCommandPacket {
    
    private final int handle;
    
    private final int incarnation;
    
    private final List<FirebirdInfoPacketType> infoItems = new ArrayList<>();
    
    private final int maxLength;
    
    public FirebirdInfoPacket(final FirebirdPacketPayload payload, final IntFunction<FirebirdInfoPacketType> valueOf) {
        payload.skipReserved(4);
        handle = payload.readInt4();
        incarnation = payload.readInt4();
        parseInfo(payload.readBuffer(), valueOf);
        maxLength = payload.readInt4();
    }
    
    private void parseInfo(final ByteBuf buffer, final IntFunction<FirebirdInfoPacketType> valueOf) {
        while (buffer.isReadable()) {
            int code = buffer.readByte();
            FirebirdInfoPacketType type = valueOf.apply(code);
            if (null != type) {
                infoItems.add(type);
            } else {
                infoItems.add(FirebirdCommonInfoPacketType.valueOf(code));
            }
        }
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return Length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 12;
        length += payload.getBufferLength(length);
        return length + 4;
    }
}
