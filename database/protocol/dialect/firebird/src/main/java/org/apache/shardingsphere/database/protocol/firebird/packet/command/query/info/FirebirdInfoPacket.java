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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Information command packet for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdInfoPacket extends FirebirdCommandPacket {
    
    private final int objectHandle;
    
    @Getter
    private final int incarnation;
    
    private final List<FirebirdInfoPacketType> items = new ArrayList<>();
    
    @Getter
    private final int bufferLength;
    
    @HighFrequencyInvocation
    public FirebirdInfoPacket(final FirebirdPacketPayload payload, final FirebirdCommandPacketType commandPacketType) {
        this(payload, getInfoItemResolver(commandPacketType));
    }
    
    @HighFrequencyInvocation
    public FirebirdInfoPacket(final FirebirdPacketPayload payload, final IntFunction<FirebirdInfoPacketType> valueOf) {
        payload.skipReserved(4);
        objectHandle = payload.readInt4();
        incarnation = payload.readInt4();
        ByteBuf buffer = payload.readBuffer();
        while (buffer.isReadable()) {
            items.add(resolveInfoItem(buffer.readByte(), valueOf));
        }
        bufferLength = payload.readInt4();
    }
    
    private static FirebirdInfoPacketType resolveInfoItem(final int code, final IntFunction<FirebirdInfoPacketType> valueOf) {
        FirebirdInfoPacketType type = valueOf.apply(code);
        return null == type ? FirebirdCommonInfoPacketType.valueOf(code) : type;
    }
    
    private static IntFunction<FirebirdInfoPacketType> getInfoItemResolver(final FirebirdCommandPacketType commandPacketType) {
        switch (commandPacketType) {
            case INFO_DATABASE:
                return FirebirdDatabaseInfoPacketType::valueOf;
            case INFO_BLOB:
                return FirebirdBlobInfoPacketType::valueOf;
            case INFO_SQL:
                return FirebirdSQLInfoPacketType::valueOf;
            default:
                throw new IllegalArgumentException(String.format("Unsupported Firebird INFO command type: %s", commandPacketType));
        }
    }
    
    /**
     * Get the handle of the object queried by the INFO command.
     *
     * @return object handle
     */
    public int getHandle() {
        return objectHandle;
    }
    
    /**
     * Get the requested information items.
     *
     * @return requested information items
     */
    public List<FirebirdInfoPacketType> getInfoItems() {
        return items;
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
