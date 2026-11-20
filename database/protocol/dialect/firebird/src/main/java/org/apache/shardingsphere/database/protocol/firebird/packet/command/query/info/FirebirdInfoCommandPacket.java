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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Firebird info command packet.
 *
 * @see <a href="https://github.com/FirebirdSQL/firebird/blob/master/src/remote/protocol.cpp#L507-L525">Firebird xdr_protocol</a>
 */
@Getter
public final class FirebirdInfoCommandPacket extends FirebirdCommandPacket {
    
    private final int infoObject;
    
    private final int incarnation;
    
    private final List<FirebirdInfoItem> items = new ArrayList<>();
    
    private final int bufferLength;
    
    public FirebirdInfoCommandPacket(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        infoObject = payload.readInt4();
        incarnation = payload.readInt4();
        parseItems(payload.readBuffer(), getItemFactory(commandPacketType));
        bufferLength = payload.readInt4();
    }
    
    private void parseItems(final ByteBuf buffer, final IntFunction<FirebirdInfoItem> valueOf) {
        while (buffer.isReadable()) {
            int code = buffer.readByte();
            FirebirdInfoItem item = valueOf.apply(code);
            items.add(null == item ? FirebirdCommonInfoItems.valueOf(code) : item);
        }
    }
    
    private static IntFunction<FirebirdInfoItem> getItemFactory(final FirebirdCommandPacketType commandPacketType) {
        switch (commandPacketType) {
            case INFO_DATABASE:
                return FirebirdDatabaseInfoItems::valueOf;
            case INFO_BLOB:
                return FirebirdBlobInfoItems::valueOf;
            case INFO_SQL:
                return FirebirdSQLInfoItems::valueOf;
            default:
                throw new IllegalArgumentException("Unsupported Firebird info command type: " + commandPacketType.name());
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
