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

package io.shardingjdbc.proxy.transport.mysql.packet.handshake;

import io.shardingjdbc.proxy.transport.mysql.constant.CapabilityFlag;
import io.shardingjdbc.proxy.transport.mysql.constant.ServerInfo;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * Handshake packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake">Handshake</a>
 * 
 * @author zhangliang
 */
@Getter
public class HandshakePacket extends MySQLPacket {
    
    private final int protocolVersion = ServerInfo.PROTOCOL_VERSION;
    
    private final String serverVersion = ServerInfo.SERVER_VERSION;
    
    private final int capabilityFlagsLower = CapabilityFlag.calculateHandshakeCapabilityFlagsLower();
    
    private final int characterSet = ServerInfo.CHARSET;
    
    private final StatusFlag statusFlag = StatusFlag.SERVER_STATUS_AUTOCOMMIT;
    
    private final int capabilityFlagsUpper = CapabilityFlag.calculateHandshakeCapabilityFlagsUpper();
    
    private final int connectionId;
    
    private final AuthPluginData authPluginData;
    
    public HandshakePacket(final int connectionId, final AuthPluginData authPluginData) {
        super(0);
        this.connectionId = connectionId;
        this.authPluginData = authPluginData;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(protocolVersion);
        mysqlPacketPayload.writeStringNul(serverVersion);
        mysqlPacketPayload.writeInt4(connectionId);
        mysqlPacketPayload.writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        mysqlPacketPayload.writeInt2(capabilityFlagsLower);
        mysqlPacketPayload.writeInt1(ServerInfo.CHARSET);
        mysqlPacketPayload.writeInt2(statusFlag.getValue());
        mysqlPacketPayload.writeInt2(capabilityFlagsUpper);
        mysqlPacketPayload.writeInt1(0);
        mysqlPacketPayload.writeReserved(10);
        mysqlPacketPayload.writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
}
