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

import org.apache.shardingsphere.db.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLHandshakePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private final byte[] part1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private final byte[] part2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Test
    void assertNewWithPayload() {
        when(payload.readInt1()).thenReturn(MySQLConstants.PROTOCOL_VERSION, MySQLConstants.DEFAULT_CHARSET.getId(), 0);
        when(payload.readStringNul()).thenReturn(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        when(payload.readStringNulByBytes()).thenReturn(part1, part2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(
                MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower(), MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        assertThat(actual.getServerVersion(), is(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
        assertThat(actual.getCapabilityFlagsLower(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower()));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT));
        assertThat(actual.getCapabilityFlagsUpper(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper()));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart1(), is(part1));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart2(), is(part2));
        verify(payload).skipReserved(10);
    }
    
    @Test
    void assertNewWithClientPluginAuthPayload() {
        when(payload.readInt1()).thenReturn(MySQLConstants.PROTOCOL_VERSION, MySQLConstants.DEFAULT_CHARSET.getId(), 0);
        when(payload.readStringNul())
                .thenReturn(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")), MySQLAuthenticationMethod.NATIVE.getMethodName());
        when(payload.readStringNulByBytes()).thenReturn(part1, part2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(
                MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower(), MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        assertThat(actual.getServerVersion(), is(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
        assertThat(actual.getCapabilityFlagsLower(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower()));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT));
        assertThat(actual.getCapabilityFlagsUpper(), is(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart1(), is(part1));
        assertThat(actual.getAuthPluginData().getAuthenticationPluginDataPart2(), is(part2));
        verify(payload).skipReserved(10);
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.NATIVE.getMethodName()));
    }
    
    @Test
    void assertNewWithSSLEnabled() {
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1, true, new MySQLAuthenticationPluginData());
        assertThat(actual.getCapabilityFlagsLower() & MySQLCapabilityFlag.CLIENT_SSL.getValue(), is(MySQLCapabilityFlag.CLIENT_SSL.getValue()));
    }
    
    @Test
    void assertNewWithSSLNotEnabled() {
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1, false, new MySQLAuthenticationPluginData());
        assertThat(actual.getCapabilityFlagsLower() & MySQLCapabilityFlag.CLIENT_SSL.getValue(), is(0));
    }
    
    @Test
    void assertWrite() {
        MySQLAuthenticationPluginData authPluginData = new MySQLAuthenticationPluginData(part1, part2);
        new MySQLHandshakePacket(1000, false, authPluginData).write(payload);
        verify(payload).writeInt1(MySQLConstants.PROTOCOL_VERSION);
        verify(payload).writeStringNul(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart1()));
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower());
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        verify(payload).writeInt1(authPluginData.getAuthenticationPluginData().length + 1);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart2()));
    }
    
    @Test
    void assertWriteWithClientPluginAuth() {
        MySQLAuthenticationPluginData authPluginData = new MySQLAuthenticationPluginData(part1, part2);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1000, false, authPluginData);
        actual.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        actual.write(payload);
        verify(payload).writeInt1(MySQLConstants.PROTOCOL_VERSION);
        verify(payload).writeStringNul(DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart1()));
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower());
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16);
        verify(payload).writeInt1(authPluginData.getAuthenticationPluginData().length + 1);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart2()));
        verify(payload).writeStringNul(MySQLAuthenticationMethod.NATIVE.getMethodName());
    }
}
