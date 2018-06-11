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

package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import io.shardingsphere.proxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * Handshake response above MySQL 4.1 packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse41">HandshakeResponse41</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@Getter
public final class HandshakeResponse41Packet extends MySQLPacket {
    
    private int capabilityFlags;
    
    private int maxPacketSize;
    
    private int characterSet;
    
    private String username;
    
    private byte[] authResponse;
    
    private String database;
    
    public HandshakeResponse41Packet(final MySQLPacketPayload mysqlPacketPayload) {
        super(mysqlPacketPayload.readInt1());
        capabilityFlags = mysqlPacketPayload.readInt4();
        maxPacketSize = mysqlPacketPayload.readInt4();
        characterSet = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.skipReserved(23);
        username = mysqlPacketPayload.readStringNul();
        readAuthResponse(mysqlPacketPayload);
        readDatabase(mysqlPacketPayload);
    }
    
    public HandshakeResponse41Packet(final int sequenceId, final int capabilityFlags, final int maxPacketSize, final int characterSet, final String username, final byte[] authResponse, final String
            database) {
        super(sequenceId);
        this.capabilityFlags = capabilityFlags;
        this.maxPacketSize = maxPacketSize;
        this.characterSet = characterSet;
        this.username = username;
        this.authResponse = authResponse;
        this.database = database;
    }
    
    private void readAuthResponse(final MySQLPacketPayload mysqlPacketPayload) {
        
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            authResponse = mysqlPacketPayload.readStringLenencByBytes();
        } else if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            int length = mysqlPacketPayload.readInt1();
            authResponse = mysqlPacketPayload.readStringFixByBytes(length);
        } else {
            authResponse = mysqlPacketPayload.readStringNulByBytes();
        }
    }
    
    private void readDatabase(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            database = mysqlPacketPayload.readStringNul();
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt4(capabilityFlags);
        mysqlPacketPayload.writeInt4(maxPacketSize);
        mysqlPacketPayload.writeInt1(characterSet);
        mysqlPacketPayload.writeReserved(23);
        mysqlPacketPayload.writeStringNul(username);
        writeAuthResponse(mysqlPacketPayload);
        writeDatabase(mysqlPacketPayload);
    }
    
    private void writeAuthResponse(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            mysqlPacketPayload.writeStringLenenc(new String(authResponse));
        } else if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            mysqlPacketPayload.writeInt1(authResponse.length);
            mysqlPacketPayload.writeBytes(authResponse);
        } else {
            mysqlPacketPayload.writeStringNul(new String(authResponse));
        }
    }
    
    private void writeDatabase(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            mysqlPacketPayload.writeStringNul(database);
        }
    }
}
