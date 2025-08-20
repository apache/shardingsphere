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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Handshake response above MySQL 4.1 packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">HandshakeResponse41</a>
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class MySQLHandshakeResponse41Packet extends MySQLPacket {
    
    private final int maxPacketSize;
    
    private final int characterSet;
    
    private final String username;
    
    private byte[] authResponse;
    
    private int capabilityFlags;
    
    private String database;
    
    private String authPluginName;
    
    private int multiStatementsOption;
    
    public MySQLHandshakeResponse41Packet(final MySQLPacketPayload payload) {
        capabilityFlags = payload.readInt4();
        multiStatementsOption = readMultiStatementsOption(capabilityFlags);
        maxPacketSize = payload.readInt4();
        characterSet = payload.readInt1();
        payload.skipReserved(23);
        username = payload.readStringNul();
        authResponse = readAuthResponse(payload);
        database = readDatabase(payload);
        authPluginName = readAuthPluginName(payload);
    }
    
    private int readMultiStatementsOption(final int capabilityFlags) {
        return 0 == (capabilityFlags & MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS.getValue()) ? MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_OFF
                : MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON;
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
        return 0 == (capabilityFlags & MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue()) ? null : payload.readStringNul();
    }
    
    private String readAuthPluginName(final MySQLPacketPayload payload) {
        return 0 == (capabilityFlags & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue()) ? null : payload.readStringNul();
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
     * Set authentication plugin name.
     *
     * @param authenticationMethod authentication method of MySQL
     */
    public void setAuthPluginName(final MySQLAuthenticationMethod authenticationMethod) {
        authPluginName = authenticationMethod.getMethodName();
        capabilityFlags |= MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue();
    }
    
    @Override
    protected void write(final MySQLPacketPayload payload) {
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
