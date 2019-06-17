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

import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLHandshakeResponse41PacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNewWithPayloadWithDatabase() {
        when(payload.readInt1()).thenReturn(1, MySQLServerInfo.CHARSET);
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root", "sharding_db");
        when(payload.readStringNulByBytes()).thenReturn(new byte[] {1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        assertThat(actual.getDatabase(), is("sharding_db"));
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertNewWithPayloadWithClientPluginAuthLenencClientData() {
        when(payload.readInt1()).thenReturn(1, MySQLServerInfo.CHARSET);
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringLenencByBytes()).thenReturn(new byte[] {1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        assertNull(actual.getDatabase());
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertNewWithPayloadWithClientSecureConnection() {
        when(payload.readInt1()).thenReturn(1, MySQLServerInfo.CHARSET, 1);
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringFixByBytes(1)).thenReturn(new byte[] {1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[] {1}));
        assertNull(actual.getDatabase());
        verify(payload).skipReserved(23);
    }
    
    @Test
    public void assertWriteWithDatabase() {
        new MySQLHandshakeResponse41Packet(1, MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue(), 100, MySQLServerInfo.CHARSET, "root", new byte[] {1}, "sharding_db").write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringNul(new String(new byte[] {1}));
        verify(payload).writeStringNul("sharding_db");
    }
    
    @Test
    public void assertWriteWithClientPluginAuthLenencClientData() {
        new MySQLHandshakeResponse41Packet(1, MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue(), 100, MySQLServerInfo.CHARSET, "root", new byte[] {1}, null).write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringLenenc(new String(new byte[] {1}));
    }
    
    @Test
    public void assertWriteWithClientSecureConnection() {
        new MySQLHandshakeResponse41Packet(1, MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue(), 100, MySQLServerInfo.CHARSET, "root", new byte[] {1}, null).write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLServerInfo.CHARSET);
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeInt1(1);
        verify(payload).writeBytes(new byte[] {1});
    }
}
