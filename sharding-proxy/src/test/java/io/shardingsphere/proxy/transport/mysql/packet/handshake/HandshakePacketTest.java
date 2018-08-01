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

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.shardingsphere.proxy.transport.mysql.constant.CapabilityFlag;
import io.shardingsphere.proxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandshakePacketTest {
    
    private HandshakePacket handshakePacket;
    
    private final byte[] part1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private final byte[] part2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Before
    public void setUp() {
        AuthPluginData authPluginData = new AuthPluginData(part1, part2);
        handshakePacket = new HandshakePacket(1, authPluginData);
    }
    
    @Test
    public void assertWrite() {
        ByteBuf byteBuf = mock(ByteBuf.class);
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf);
        when(byteBuf.writeByte(anyInt())).thenReturn(byteBuf);
        when(byteBuf.writeBytes(byteBuf)).thenReturn(byteBuf);
        handshakePacket.write(payload);
        assertThat(handshakePacket.getSequenceId(), is(0));
    }
    
    @Test
    public void assertGetProtocolVersion() {
        assertThat(handshakePacket.getProtocolVersion(), is(ServerInfo.PROTOCOL_VERSION));
    }
    
    @Test
    public void assertGetServerVersion() {
        assertThat(handshakePacket.getServerVersion(), is(ServerInfo.SERVER_VERSION));
    }
    
    @Test
    public void assertGetCapabilityFlagsLower() {
        assertThat(handshakePacket.getCapabilityFlagsLower(), is(CapabilityFlag.calculateHandshakeCapabilityFlagsLower()));
    }
    
    @Test
    public void assertGetCapabilityFlagsUpper() {
        assertThat(handshakePacket.getCapabilityFlagsUpper(), is(CapabilityFlag.calculateHandshakeCapabilityFlagsUpper()));
    }
    
    @Test
    public void assertGetCharacterSet() {
        assertThat(handshakePacket.getCharacterSet(), is(ServerInfo.CHARSET));
    }
    
    @Test
    public void assertGetStatusFlag() {
        assertThat(handshakePacket.getStatusFlag(), is(StatusFlag.SERVER_STATUS_AUTOCOMMIT));
    }
    
    @Test
    public void assertGetSequenceId() {
        assertThat(handshakePacket.getSequenceId(), is(0));
    }
    
    @Test
    public void assertGetConnectionId() {
        assertThat(handshakePacket.getConnectionId(), is(1));
    }
    
    @Test
    public void assertGetAuthPluginData() {
        byte[] actual = Bytes.concat(part1, part2);
        assertTrue(Arrays.equals(handshakePacket.getAuthPluginData().getAuthPluginData(), actual));
    }
}
