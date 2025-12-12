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

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLAuthSwitchRequestPacketTest {
    
    @Mock
    private MySQLAuthenticationPluginData authPluginData;
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewWithInvalidHeader() {
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        assertThrows(IllegalArgumentException.class, () -> new MySQLAuthSwitchRequestPacket(payload));
    }
    
    @Test
    void assertNewWithValidHeader() {
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        when(payload.readInt1()).thenReturn(MySQLAuthSwitchRequestPacket.HEADER);
        when(payload.readStringNul()).thenReturn("foo_auth");
        MySQLAuthSwitchRequestPacket packet = new MySQLAuthSwitchRequestPacket(payload);
        assertThat(packet.getAuthPluginName(), is("foo_auth"));
    }
    
    @Test
    void assertWrite() {
        when(authPluginData.getAuthenticationPluginData()).thenReturn(new byte[]{0x11, 0x22});
        MySQLAuthSwitchRequestPacket authSwitchRequestPacket = new MySQLAuthSwitchRequestPacket("plugin", authPluginData);
        authSwitchRequestPacket.write(payload);
        verify(payload).writeInt1(0xfe);
        verify(payload, times(2)).writeStringNul(anyString());
    }
}
