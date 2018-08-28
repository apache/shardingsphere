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
import io.shardingsphere.proxy.transport.mysql.constant.ServerInfo;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HandshakeResponse41PacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private HandshakeResponse41Packet handshakeResponse41Packet;
    
//    @Before
//    public void setUp() {
//        ByteBuf byteBuf = mock(ByteBuf.class);
//        payload = new MySQLPacketPayload(byteBuf);
//        when(byteBuf.writeByte(anyInt())).thenReturn(byteBuf);
//        when(byteBuf.writeBytes(byteBuf)).thenReturn(byteBuf);
//        byte b = 0;
//        when(byteBuf.readByte()).thenReturn(b);
//        when(byteBuf.readIntLE()).thenReturn(0);
//        when(byteBuf.bytesBefore((byte) 0)).thenReturn(0);
//        when(byteBuf.skipBytes(1)).thenReturn(byteBuf);
//        when(byteBuf.readBytes(anyByte())).thenReturn(byteBuf);
//        handshakeResponse41Packet = new HandshakeResponse41Packet(payload);
//    }
    
    @Test
    public void assertNewWithPayloadWithDatabase() {
        when(payload.readInt1()).thenReturn(1, ServerInfo.CHARSET);
        when(payload.readInt4()).thenReturn(CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root", "sharding_db");
        when(payload.readStringNulByBytes()).thenReturn(new byte[] {1});
        HandshakeResponse41Packet actual = new HandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        verify(payload, times(2)).readInt1();
        verify(payload, times(2)).readInt4();
        verify(payload, times(2)).readStringNul();
        verify(payload).readStringNulByBytes();
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertNewWithPayloadWithClientPluginAuthLenencClientData() {
        when(payload.readInt1()).thenReturn(1, ServerInfo.CHARSET);
        when(payload.readInt4()).thenReturn(CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringLenencByBytes()).thenReturn(new byte[] {1});
        HandshakeResponse41Packet actual = new HandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        verify(payload, times(2)).readInt1();
        verify(payload, times(2)).readInt4();
        verify(payload).readStringNul();
        verify(payload).readStringLenencByBytes();
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertNewWithPayloadWithClientSecureConnection() {
        when(payload.readInt1()).thenReturn(1, ServerInfo.CHARSET, 1);
        when(payload.readInt4()).thenReturn(CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringFixByBytes(1)).thenReturn(new byte[] {1});
        HandshakeResponse41Packet actual = new HandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        verify(payload, times(3)).readInt1();
        verify(payload, times(2)).readInt4();
        verify(payload).readStringNul();
        verify(payload).readStringFixByBytes(1);
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertWriteWithDatabase() {
        new HandshakeResponse41Packet(1, CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue(), 100, ServerInfo.CHARSET, "root", new byte[] {1}, "sharding_db").write(payload);
        verify(payload).writeInt4(CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(ServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringNul(new String(new byte[] {1}));
        verify(payload).writeStringNul("sharding_db");
    }
    
    @Test
    public void assertWriteWithClientPluginAuthLenencClientData() {
        new HandshakeResponse41Packet(1, CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue(), 100, ServerInfo.CHARSET, "root", new byte[] {1}, null).write(payload);
        verify(payload).writeInt4(CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(ServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringLenenc(new String(new byte[] {1}));
    }
    
    @Test
    public void assertWriteWithClientSecureConnection() {
        new HandshakeResponse41Packet(1, CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue(), 100, ServerInfo.CHARSET, "root", new byte[] {1}, null).write(payload);
        verify(payload).writeInt4(CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(ServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeInt1(1);
        verify(payload).writeBytes(new byte[] {1});
    }
}
