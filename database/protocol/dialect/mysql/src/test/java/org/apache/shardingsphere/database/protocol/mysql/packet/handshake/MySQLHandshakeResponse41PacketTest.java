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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLHandshakeResponse41PacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewWithPayloadWithDatabase() {
        when(payload.readInt1()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId());
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root", "sharding_db");
        when(payload.readStringNulByBytes()).thenReturn(new byte[]{1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getMaxPacketSize(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[]{1}));
        assertThat(actual.getCapabilityFlags(), is(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue()));
        assertThat(actual.getDatabase(), is("sharding_db"));
        assertNull(actual.getAuthPluginName());
        verify(payload).skipReserved(23);
    }
    
    @Test
    void assertNewWithPayloadWithClientMultiStatements() {
        when(payload.readInt1()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId());
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root", "sharding_db");
        when(payload.readStringNulByBytes()).thenReturn(new byte[]{1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getMultiStatementsOption(), is(MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON));
        assertThat(actual.getMaxPacketSize(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[]{1}));
        assertThat(actual.getCapabilityFlags(), is(MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS.getValue()));
        assertNull(actual.getDatabase());
        assertNull(actual.getAuthPluginName());
        verify(payload).skipReserved(23);
    }
    
    @Test
    void assertNewWithPayloadWithAuthPluginName() {
        when(payload.readInt1()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId());
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root", MySQLAuthenticationMethod.NATIVE.getMethodName());
        when(payload.readStringNulByBytes()).thenReturn(new byte[]{1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getMaxPacketSize(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[]{1}));
        assertThat(actual.getCapabilityFlags(), is(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue()));
        assertNull(actual.getDatabase());
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.NATIVE.getMethodName()));
        verify(payload).skipReserved(23);
    }
    
    @Test
    void assertNewWithPayloadWithClientPluginAuthLenencClientData() {
        when(payload.readInt1()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId());
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringLenencByBytes()).thenReturn(new byte[]{1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getMaxPacketSize(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[]{1}));
        assertThat(actual.getCapabilityFlags(), is(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue()));
        assertNull(actual.getDatabase());
        assertNull(actual.getAuthPluginName());
        verify(payload).skipReserved(23);
    }
    
    @Test
    void assertNewWithPayloadWithClientSecureConnection() {
        when(payload.readInt1()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId(), 1);
        when(payload.readInt4()).thenReturn(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue(), 1000);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringFixByBytes(1)).thenReturn(new byte[]{1});
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getMaxPacketSize(), is(1000));
        assertThat(actual.getCharacterSet(), is(MySQLConstants.DEFAULT_CHARSET.getId()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getAuthResponse(), is(new byte[]{1}));
        assertThat(actual.getCapabilityFlags(), is(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue()));
        assertNull(actual.getDatabase());
        assertNull(actual.getAuthPluginName());
        verify(payload).skipReserved(23);
    }
    
    @Test
    void assertNewWithPayloadWithConnectionAttributes() {
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(MySQLCapabilityFlag.CLIENT_CONNECT_ATTRS.getValue());
        payload.writeInt4(1000);
        payload.writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        payload.writeReserved(23);
        payload.writeStringNul("root");
        payload.writeStringNul("");
        payload.writeIntLenenc("program_name".length() + "mysql".length() + 2L);
        payload.writeStringLenenc("program_name");
        payload.writeStringLenenc("mysql");
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getConnectionAttributes(), is(Collections.singletonMap("program_name", "mysql")));
    }
    
    @Test
    void assertWriteWithDatabase() {
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(100, MySQLConstants.DEFAULT_CHARSET.getId(), "root");
        actual.setAuthResponse(new byte[]{1});
        actual.setDatabase("sharding_db");
        actual.write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringNul(new String(new byte[]{1}));
        verify(payload).writeStringNul("sharding_db");
    }
    
    @Test
    void assertWriteWithAuthPluginName() {
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(100, MySQLConstants.DEFAULT_CHARSET.getId(), "root");
        actual.setAuthResponse(new byte[]{1});
        actual.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        actual.write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringNul(new String(new byte[]{1}));
        verify(payload).writeStringNul(MySQLAuthenticationMethod.NATIVE.getMethodName());
    }
    
    @Test
    void assertWriteWithClientPluginAuthLenencClientData() {
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(100, MySQLConstants.DEFAULT_CHARSET.getId(), "root");
        actual.setCapabilityFlags(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue());
        actual.setAuthResponse(new byte[]{1});
        actual.write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeStringLenenc(new String(new byte[]{1}));
    }
    
    @Test
    void assertWriteWithClientSecureConnection() {
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(100, MySQLConstants.DEFAULT_CHARSET.getId(), "root");
        actual.setCapabilityFlags(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue());
        actual.setAuthResponse(new byte[]{1});
        actual.write(payload);
        verify(payload).writeInt4(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue());
        verify(payload).writeInt4(100);
        verify(payload).writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        verify(payload).writeReserved(23);
        verify(payload).writeStringNul("root");
        verify(payload).writeInt1(1);
        verify(payload).writeBytes(new byte[]{1});
    }
    
    @Test
    void assertNewWithPayloadWithEmptyAuthResponseAndAuthPluginName() {
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        payload.writeInt4(MySQLCapabilityFlag.calculateCapabilityFlags(MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION, MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH,
                MySQLCapabilityFlag.CLIENT_CONNECT_ATTRS, MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA));
        payload.writeInt4(1000);
        payload.writeInt1(MySQLConstants.DEFAULT_CHARSET.getId());
        payload.writeReserved(23);
        payload.writeStringNul("root");
        payload.writeInt1(0);
        payload.writeInt1(0);
        payload.writeStringNul(MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName());
        payload.writeIntLenenc("_client_name".length() + "MariaDB Connector/J".length() + 2L);
        payload.writeStringLenenc("_client_name");
        payload.writeStringLenenc("MariaDB Connector/J");
        MySQLHandshakeResponse41Packet actual = new MySQLHandshakeResponse41Packet(payload);
        assertThat(actual.getAuthResponse(), is(new byte[]{}));
        assertThat(actual.getAuthPluginName(), is(MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName()));
        assertThat(actual.getConnectionAttributes(), is(Collections.singletonMap("_client_name", "MariaDB Connector/J")));
    }
}
