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

import io.netty.buffer.ByteBuf;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandshakeResponse41PacketTest {
    
    private HandshakeResponse41Packet handshakeResponse41Packet;
    
    private MySQLPacketPayload payload;
    
    @Before
    public void setUp() {
        ByteBuf byteBuf = mock(ByteBuf.class);
        payload = new MySQLPacketPayload(byteBuf);
        when(byteBuf.writeByte(anyInt())).thenReturn(byteBuf);
        when(byteBuf.writeBytes(byteBuf)).thenReturn(byteBuf);
        byte b = 0;
        when(byteBuf.readByte()).thenReturn(b);
        when(byteBuf.readIntLE()).thenReturn(0);
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
        when(byteBuf.skipBytes(1)).thenReturn(byteBuf);
        when(byteBuf.readBytes(anyByte())).thenReturn(byteBuf);
        handshakeResponse41Packet = new HandshakeResponse41Packet(payload);
    }
    
    @Test
    public void testWrite() {
        handshakeResponse41Packet.write(payload);
        assertThat(handshakeResponse41Packet.getSequenceId(), is(0));
    }
    
    @Test
    public void testGetSequenceId() {
        assertThat(handshakeResponse41Packet.getSequenceId(), is(0));
    }
    
    @Test
    public void testGetCapabilityFlags() {
        assertThat(handshakeResponse41Packet.getCapabilityFlags(), is(0));
    }
    
    @Test
    public void testGetMaxPacketSize() {
        assertThat(handshakeResponse41Packet.getMaxPacketSize(), is(0));
    }
    
    @Test
    public void testGetCharacterSet() {
        assertThat(handshakeResponse41Packet.getCharacterSet(), is(0));
    }
    
    @Test
    public void testGetUsername() {
        assertThat(handshakeResponse41Packet.getUsername(), is(""));
    }
    
    @Test
    public void testGetAuthResponse() {
        byte[] expected = {};
        assertThat(handshakeResponse41Packet.getAuthResponse(), is(expected));
    }
    
    @Test
    public void testGetDatabase() {
        assertNull(handshakeResponse41Packet.getDatabase());
    }
}
