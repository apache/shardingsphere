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
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.shardingproxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HandshakePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private final byte[] part1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private final byte[] part2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Test
    public void assertNewWithPayload() {
        when(payload.readInt1()).thenReturn(1, ServerInfo.PROTOCOL_VERSION, ServerInfo.CHARSET, 0);
        when(payload.readStringNul()).thenReturn(ServerInfo.SERVER_VERSION);
        when(payload.readStringNulByBytes()).thenReturn(part1, part2);
        when(payload.readInt4()).thenReturn(1000);
        when(payload.readInt2()).thenReturn(
                CapabilityFlag.calculateHandshakeCapabilityFlagsLower(), StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), CapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        HandshakePacket actual = new HandshakePacket(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getConnectionId(), is(1000));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart1(), is(part1));
        assertThat(actual.getAuthPluginData().getAuthPluginDataPart2(), is(part2));
        verify(payload).skipReserved(10);
    }
    
    @Test
    public void assertWrite() {
        AuthPluginData authPluginData = new AuthPluginData(part1, part2);
        new HandshakePacket(1000, authPluginData).write(payload);
        verify(payload).writeInt1(ServerInfo.PROTOCOL_VERSION);
        verify(payload).writeStringNul(ServerInfo.SERVER_VERSION);
        verify(payload).writeInt4(1000);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        verify(payload).writeInt2(CapabilityFlag.calculateHandshakeCapabilityFlagsLower());
        verify(payload).writeInt1(ServerInfo.CHARSET);
        verify(payload).writeInt2(StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(payload).writeInt2(CapabilityFlag.calculateHandshakeCapabilityFlagsUpper());
        verify(payload).writeInt1(0);
        verify(payload).writeReserved(10);
        verify(payload).writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
}
