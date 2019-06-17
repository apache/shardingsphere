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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;

/**
 * Handshake packet protocol for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake">Handshake</a>
 * 
 * @author zhangliang
 * @author linjiaqi
 */
public final class MySQLHandshakePacket implements MySQLPacket {
    
    private final int protocolVersion = MySQLServerInfo.PROTOCOL_VERSION;
    
    private final String serverVersion = MySQLServerInfo.SERVER_VERSION;
    
    private final int capabilityFlagsLower = MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower();
    
    private final MySQLStatusFlag statusFlag = MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT;
    
    private final int capabilityFlagsUpper = MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper();
    
    @Getter
    private final int connectionId;
    
    @Getter
    private final MySQLAuthPluginData authPluginData;
    
    public MySQLHandshakePacket(final int connectionId, final MySQLAuthPluginData authPluginData) {
        this.connectionId = connectionId;
        this.authPluginData = authPluginData;
    }
    
    public MySQLHandshakePacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(0 == payload.readInt1(), "Sequence ID of MySQL handshake packet must be `0`.");
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
        authPluginData = new MySQLAuthPluginData(authPluginDataPart1, authPluginDataPart2);
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(protocolVersion);
        payload.writeStringNul(serverVersion);
        payload.writeInt4(connectionId);
        payload.writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        payload.writeInt2(capabilityFlagsLower);
        payload.writeInt1(MySQLServerInfo.CHARSET);
        payload.writeInt2(statusFlag.getValue());
        payload.writeInt2(capabilityFlagsUpper);
        payload.writeInt1(0);
        payload.writeReserved(10);
        payload.writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
