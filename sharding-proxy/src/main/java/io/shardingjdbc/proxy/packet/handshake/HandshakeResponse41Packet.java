/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.packet.handshake;

import io.shardingjdbc.proxy.constant.CapabilityFlag;
import io.shardingjdbc.proxy.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.packet.AbstractMySQLReceivedPacket;
import lombok.Getter;

/**
 * Handshake response above MySQL 4.1 packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse41">HandshakeResponse41</a>
 * 
 * @author zhangliang
 */
@Getter
public final class HandshakeResponse41Packet extends AbstractMySQLReceivedPacket {
    
    private int capabilityFlags;
    
    private int maxPacketSize;
    
    private int characterSet;
    
    private String username;
    
    private byte[] authResponse;
    
    private String database;
    
    @Override
    public HandshakeResponse41Packet read(final MySQLPacketPayload mysqlPacketPayload) {
        setSequenceId(mysqlPacketPayload.readInt1());
        capabilityFlags = mysqlPacketPayload.readInt4();
        maxPacketSize = mysqlPacketPayload.readInt4();
        characterSet = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.skipReserved(23);
        username = mysqlPacketPayload.readStringNul();
        readAuthResponse(mysqlPacketPayload);
        readDatabase(mysqlPacketPayload);
        return this;
    }
    
    private void readAuthResponse(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            authResponse = mysqlPacketPayload.readStringLenenc().getBytes();
        } else if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            int length = mysqlPacketPayload.readInt1();
            authResponse = mysqlPacketPayload.readStringFix(length).getBytes();
        } else {
            authResponse = mysqlPacketPayload.readStringNul().getBytes();
        }
    }
    
    private void readDatabase(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            database = mysqlPacketPayload.readStringNul();
        }
    }
}
