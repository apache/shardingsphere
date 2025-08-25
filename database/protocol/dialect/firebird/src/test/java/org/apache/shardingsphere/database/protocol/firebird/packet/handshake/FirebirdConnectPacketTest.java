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

package org.apache.shardingsphere.database.protocol.firebird.packet.handshake;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdUserDataType;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdConnectPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertConnectPacket() {
        ByteBuf userInfo = mock(ByteBuf.class);
        ByteBuf userBuf = mock(ByteBuf.class);
        ByteBuf pluginBuf = mock(ByteBuf.class);
        ByteBuf specBuf1 = mock(ByteBuf.class);
        ByteBuf specBuf2 = mock(ByteBuf.class);
        ByteBuf protocolBuf = mock(ByteBuf.class);
        when(payload.readInt4()).thenReturn(
                FirebirdCommandPacketType.CONNECT.getValue(),
                1,
                FirebirdArchType.ARCH_GENERIC.getCode(),
                1);
        when(payload.readString()).thenReturn("db");
        when(payload.readBuffer()).thenReturn(userInfo);
        when(payload.getByteBuf()).thenReturn(protocolBuf);
        when(userInfo.isReadable()).thenReturn(true, true, true, true, false);
        when(userInfo.readUnsignedByte()).thenReturn(
                (short) FirebirdUserDataType.CNCT_USER.getCode(), (short) 4,
                (short) FirebirdUserDataType.CNCT_PLUGIN_NAME.getCode(), (short) 3,
                (short) FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode(), (short) 2,
                (short) FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode(), (short) 2);
        when(userInfo.readSlice(4)).thenReturn(userBuf);
        when(userInfo.readSlice(3)).thenReturn(pluginBuf);
        when(userInfo.readSlice(2)).thenReturn(specBuf1, specBuf2);
        when(userBuf.toString(StandardCharsets.UTF_8)).thenReturn("user");
        when(pluginBuf.toString(StandardCharsets.UTF_8)).thenReturn("srp");
        when(specBuf1.readUnsignedByte()).thenReturn((short) 0);
        when(specBuf1.toString(StandardCharsets.US_ASCII)).thenReturn("A");
        when(specBuf2.readUnsignedByte()).thenReturn((short) 1);
        when(specBuf2.toString(StandardCharsets.US_ASCII)).thenReturn("B");
        when(protocolBuf.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10.getCode(), FirebirdArchType.ARCH_GENERIC.getCode(), 0, 5, 1);
        FirebirdConnectPacket packet = new FirebirdConnectPacket(payload);
        assertThat(packet.getOpCode(), is(FirebirdCommandPacketType.CONNECT));
        assertThat(packet.getConnectVersion(), is(1));
        assertThat(packet.getArchType(), is(FirebirdArchType.ARCH_GENERIC));
        assertThat(packet.getDatabase(), is("db"));
        assertThat(packet.getProtocolsCount(), is(1));
        assertThat(packet.getUsername(), is("user"));
        assertThat(packet.getPluginName(), is("srp"));
        assertThat(packet.getPlugin(), is(FirebirdAuthenticationMethod.SRP));
        assertThat(packet.getAuthData(), is("AB"));
        List<FirebirdProtocol> protocols = packet.getUserProtocols();
        assertThat(protocols.size(), is(1));
        assertThat(protocols.get(0).getVersion(), is(FirebirdProtocolVersion.PROTOCOL_VERSION10));
    }
}
