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

package org.apache.shardingsphere.db.protocol.mysql.packet.handshake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Handshake response above MySQL 4.1 packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse41">HandshakeResponse41</a>
 */
@RequiredArgsConstructor
@Setter
@Getter
public final class MySQLHandshakeResponse41Packet implements MySQLPacket {
    
    private final int sequenceId;
    
    private final int maxPacketSize;
    
    private final int characterSet;
    
    private final String username;
    
    private byte[] authResponse;
    
    private int capabilityFlags;
    
    private String database;
    
    private String authPluginName;
    
    public MySQLHandshakeResponse41Packet(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
        capabilityFlags = payload.readInt4();
        maxPacketSize = payload.readInt4();
        characterSet = payload.readInt1();
        payload.skipReserved(23);
        username = payload.readStringNul();
        authResponse = readAuthResponse(payload);
        database = readDatabase(payload);
        authPluginName = readAuthPluginName(payload);
    }
    
    private byte[] readAuthResponse(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            return payload.readStringLenencByBytes();
        }
        if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            int length = payload.readInt1();
            return payload.readStringFixByBytes(length);
        }
        return payload.readStringNulByBytes();
    }
    
    private String readDatabase(final MySQLPacketPayload payload) {
        return 0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue()) ? payload.readStringNul() : null;
    }
    
    private String readAuthPluginName(final MySQLPacketPayload payload) {
        return 0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue()) ? payload.readStringNul() : null;
    }
    
    /**
     * Set database.
     *
     * @param database database
     */
    public void setDatabase(final String database) {
        this.database = database;
        capabilityFlags |= MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue();
    }
    
    /**
     * Set auth plugin name.
     *
     * @param mysqlAuthenticationMethod MySQL authentication method
     */
    public void setAuthPluginName(final MySQLAuthenticationMethod mysqlAuthenticationMethod) {
        authPluginName = mysqlAuthenticationMethod.getMethodName();
        capabilityFlags |= MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue();
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
        writeAuthPluginName(payload);
    }
    
    private void writeAuthResponse(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            payload.writeStringLenenc(new String(authResponse));
        } else if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            payload.writeInt1(authResponse.length);
            payload.writeBytes(authResponse);
        } else {
            payload.writeStringNul(new String(authResponse));
        }
    }
    
    private void writeDatabase(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            payload.writeStringNul(database);
        }
    }
    
    private void writeAuthPluginName(final MySQLPacketPayload payload) {
        if (0 != (capabilityFlags & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue())) {
            payload.writeStringNul(authPluginName);
        }
    }
}
