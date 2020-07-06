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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLHandshakePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private final byte[] part1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private final byte[] part2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Test
    public void assertNewWithPayload() {
        when(payload.readInt1()).thenReturn(0, MySQLServerInfo.PROTOCOL_VERSION, MySQLServerInfo.CHARSET, 0);
        when(payload.readStringNul()).thenReturn(MySQLServerInfo.getServerVersion());
        when(payload.readStringNulByBytes()).thenReturn(part1, part2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(
                MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower(), MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        assertThat(actual.getSequenceId(), is(0));
        assertThat(actual.getServerVersion(), is(MySQLServerInfo.getServerVersion()));
        assertThat(actual.getCapabilityFlagsLower(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower()));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLServerInfo.CHARSET));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT));
        assertThat(actual.getCapabilityFlagsUpper(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper()));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart1(), is(part1));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart2(), is(part2));
        verify(payload).skipReserved(10);
    }
    
    @Test
    public void assertNewWithClientPluginAuthPayload() {
        when(payload.readInt1()).thenReturn(0, MySQLServerInfo.PROTOCOL_VERSION, MySQLServerInfo.CHARSET, 0);
        when(payload.readStringNul()).thenReturn(MySQLServerInfo.getServerVersion(), MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName());
        when(payload.readStringNulByBytes()).thenReturn(part1, part2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(
            MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower(), MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(payload);
        assertThat(actual.getSequenceId(), is(0));
        assertThat(actual.getServerVersion(), is(MySQLServerInfo.getServerVersion()));
        assertThat(actual.getCapabilityFlagsLower(), is(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower()));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLServerInfo.CHARSET));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT));
        assertThat(actual.getCapabilityFlagsUpper(), is(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart1(), is(part1));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart2(), is(part2));
        verify(payload).skipReserved(10);
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName()));
    }
    
    @Test
    public void assertWrite() {
        MySQLAuthPluginData authPluginData = new MySQLAuthPluginData(part1, part2);
        new MySQLHandshakePacket(1000, authPluginData).write(payload);
        verify(payload).writeInt1(MySQLServerInfo.PROTOCOL_VERSION);
        verify(payload).writeStringNul(MySQLServerInfo.getServerVersion());
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower());
        verify(payload).writeInt1(MySQLServerInfo.CHARSET);
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        verify(payload).writeInt1(authPluginData.getAuthPluginData().length + 1);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
    
    @Test
    public void assertWriteWithClientPluginAuth() {
        MySQLAuthPluginData authPluginData = new MySQLAuthPluginData(part1, part2);
        MySQLHandshakePacket actual = new MySQLHandshakePacket(1000, authPluginData);
        actual.setAuthPluginName(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION);
        actual.write(payload);
        verify(payload).writeInt1(MySQLServerInfo.PROTOCOL_VERSION);
        verify(payload).writeStringNul(MySQLServerInfo.getServerVersion());
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        verify(payload).writeInt2(MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower());
        verify(payload).writeInt1(MySQLServerInfo.CHARSET);
        verify(payload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16);
        verify(payload).writeInt1(authPluginData.getAuthPluginData().length + 1);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
        verify(payload).writeStringNul(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName());
    }
}
