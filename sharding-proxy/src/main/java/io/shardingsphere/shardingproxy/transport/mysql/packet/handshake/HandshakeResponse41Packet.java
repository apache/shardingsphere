/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingproxy.transport.mysql.packet.handshake;

import io.shardingsphere.shardingproxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Handshake response above MySQL 4.1 packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse41">HandshakeResponse41</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@RequiredArgsConstructor
public final class HandshakeResponse41Packet implements MySQLPacket {
    
    @Getter
    private final int sequenceId;
    
    private final int capabilityFlags;
    
    private final int maxPacketSize;
    
    private final int characterSet;
    
    @Getter
    private final String username;
    
    @Getter
    private final byte[] authResponse;
    
    @Getter
    private final String database;
    
    public HandshakeResponse41Packet(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
        capabilityFlags = payload.readInt4();
        maxPacketSize = payload.readInt4();
        characterSet = payload.readInt1();
        payload.skipReserved(23);
        username = payload.readStringNul();
        authResponse = readAuthResponse(payload);
        database = readDatabase(payload);
    }
    
    private byte[] readAuthResponse(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            return payload.readStringLenencByBytes();
        }
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            int length = payload.readInt1();
            return payload.readStringFixByBytes(length);
        }
        return payload.readStringNulByBytes();
    }
    
    private String readDatabase(final MySQLPacketPayload payload) {
        return 0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue()) ? payload.readStringNul() : null;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt4(capabilityFlags);
        payload.writeInt4(maxPacketSize);
        payload.writeInt1(characterSet);
        payload.writeReserved(23);
        payload.writeStringNul(username);
        writeAuthResponse(payload);
        writeDatabase(payload);
    }
    
    private void writeAuthResponse(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            payload.writeStringLenenc(new String(authResponse));
        } else if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            payload.writeInt1(authResponse.length);
            payload.writeBytes(authResponse);
        } else {
            payload.writeStringNul(new String(authResponse));
        }
    }
    
    private void writeDatabase(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            payload.writeStringNul(database);
        }
    }
}
