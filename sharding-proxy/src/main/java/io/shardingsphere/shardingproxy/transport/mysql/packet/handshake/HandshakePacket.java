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

import com.google.common.base.Preconditions;

import io.shardingsphere.shardingproxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.shardingproxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import lombok.Getter;

/**
 * Handshake packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake">Handshake</a>
 * 
 * @author zhangliang
 * @author linjiaqi
 */
public final class HandshakePacket implements MySQLPacket {
    
    private final int protocolVersion = ServerInfo.PROTOCOL_VERSION;
    
    private final String serverVersion = ServerInfo.SERVER_VERSION;
    
    private final int capabilityFlagsLower = CapabilityFlag.calculateHandshakeCapabilityFlagsLower();
    
    private final StatusFlag statusFlag = StatusFlag.SERVER_STATUS_AUTOCOMMIT;
    
    private final int capabilityFlagsUpper = CapabilityFlag.calculateHandshakeCapabilityFlagsUpper();
    
    @Getter
    private final int sequenceId;
    
    @Getter
    private final int connectionId;
    
    @Getter
    private final AuthPluginData authPluginData;
    
    public HandshakePacket(final int connectionId, final AuthPluginData authPluginData) {
        sequenceId = 0;
        this.connectionId = connectionId;
        this.authPluginData = authPluginData;
    }
    
    public HandshakePacket(final MySQLPacketPayload payload) {
        sequenceId = payload.readInt1();
        Preconditions.checkArgument(protocolVersion == payload.readInt1());
        payload.readStringNul();
        connectionId = payload.readInt4();
        final byte[] authPluginDataPart1 = payload.readStringNulByBytes();
        payload.readInt2();
        payload.readInt1();
        Preconditions.checkArgument(statusFlag.getValue() == payload.readInt2());
        payload.readInt2();
        payload.readInt1();
        payload.skipReserved(10);
        byte[] authPluginDataPart2 = payload.readStringNulByBytes();
        authPluginData = new AuthPluginData(authPluginDataPart1, authPluginDataPart2);
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(protocolVersion);
        payload.writeStringNul(serverVersion);
        payload.writeInt4(connectionId);
        payload.writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        payload.writeInt2(capabilityFlagsLower);
        payload.writeInt1(ServerInfo.CHARSET);
        payload.writeInt2(statusFlag.getValue());
        payload.writeInt2(capabilityFlagsUpper);
        payload.writeInt1(0);
        payload.writeReserved(10);
        payload.writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
}
