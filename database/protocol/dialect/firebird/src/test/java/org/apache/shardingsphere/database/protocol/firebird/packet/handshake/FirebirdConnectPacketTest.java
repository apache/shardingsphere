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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdConnectPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertConstructor() {
        FirebirdConnectPacket actual = createPacketWithSpecificData();
        assertThat(actual.getOpCode(), is(FirebirdCommandPacketType.CONNECT));
        assertThat(actual.getConnectVersion(), is(1));
        assertThat(actual.getArchType(), is(FirebirdArchType.ARCH_GENERIC));
        assertThat(actual.getDatabase(), is("db"));
        assertThat(actual.getProtocolsCount(), is(1));
        List<FirebirdProtocol> actualProtocols = actual.getUserProtocols();
        assertThat(actualProtocols.size(), is(1));
        assertThat(actualProtocols.get(0).getVersion(), is(FirebirdProtocolVersion.PROTOCOL_VERSION10));
    }
    
    @Test
    void assertGetUsername() {
        assertThat(createPacketWithSpecificData().getUsername(), is("user"));
    }
    
    @Test
    void assertGetPluginName() {
        assertThat(createPacketWithSpecificData().getPluginName(), is("srp"));
    }
    
    @Test
    void assertGetPlugin() {
        assertThat(createPacketWithSpecificData().getPlugin(), is(FirebirdAuthenticationMethod.SRP));
    }
    
    @Test
    void assertGetAuthData() {
        assertThat(createPacketWithSpecificData().getAuthData(), is("AB"));
    }
    
    @Test
    void assertGetHost() {
        assertThat(createPacketWithoutSpecificData().getHost(), is("host"));
    }
    
    @Test
    void assertGetLogin() {
        assertThat(createPacketWithoutSpecificData().getLogin(), is("login"));
    }
    
    @Test
    void assertWrite() {
        assertDoesNotThrow(() -> createPacketWithoutSpecificData().write(payload));
    }
    
    private FirebirdConnectPacket createPacketWithSpecificData() {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.CONNECT.getValue(), 1, FirebirdArchType.ARCH_GENERIC.getCode(), 1);
        when(payload.readString()).thenReturn("db");
        ByteBuf userBuf = mock(ByteBuf.class);
        when(userBuf.toString(StandardCharsets.UTF_8)).thenReturn("user");
        ByteBuf pluginBuf = mock(ByteBuf.class);
        when(pluginBuf.toString(StandardCharsets.UTF_8)).thenReturn("srp");
        ByteBuf specificDataFirstChunk = mock(ByteBuf.class);
        when(specificDataFirstChunk.readUnsignedByte()).thenReturn((short) 0);
        when(specificDataFirstChunk.toString(StandardCharsets.US_ASCII)).thenReturn("A");
        ByteBuf specificDataSecondChunk = mock(ByteBuf.class);
        when(specificDataSecondChunk.readUnsignedByte()).thenReturn((short) 1);
        when(specificDataSecondChunk.toString(StandardCharsets.US_ASCII)).thenReturn("B");
        ByteBuf userInfo = mock(ByteBuf.class);
        when(userInfo.isReadable()).thenReturn(true, true, true, true, false);
        when(userInfo.readUnsignedByte()).thenReturn((short) FirebirdUserDataType.CNCT_USER.getCode(), (short) 4, (short) FirebirdUserDataType.CNCT_PLUGIN_NAME.getCode(), (short) 3,
                (short) FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode(), (short) 2, (short) FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode(), (short) 2);
        when(userInfo.readSlice(4)).thenReturn(userBuf);
        when(userInfo.readSlice(3)).thenReturn(pluginBuf);
        when(userInfo.readSlice(2)).thenReturn(specificDataFirstChunk, specificDataSecondChunk);
        when(payload.readBuffer()).thenReturn(userInfo);
        ByteBuf protocolBuf = mock(ByteBuf.class);
        when(protocolBuf.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10.getCode(), FirebirdArchType.ARCH_GENERIC.getCode(), 0, 5, 1);
        when(payload.getByteBuf()).thenReturn(protocolBuf);
        return new FirebirdConnectPacket(payload);
    }
    
    private FirebirdConnectPacket createPacketWithoutSpecificData() {
        when(payload.readInt4()).thenReturn(FirebirdCommandPacketType.CONNECT.getValue(), 1, FirebirdArchType.ARCH_GENERIC.getCode(), 0);
        when(payload.readString()).thenReturn("db");
        ByteBuf hostBuf = mock(ByteBuf.class);
        when(hostBuf.toString(StandardCharsets.UTF_8)).thenReturn("host");
        ByteBuf loginBuf = mock(ByteBuf.class);
        when(loginBuf.toString(StandardCharsets.UTF_8)).thenReturn("login");
        ByteBuf userInfo = mock(ByteBuf.class);
        when(userInfo.isReadable()).thenReturn(true, true, false);
        when(userInfo.readUnsignedByte()).thenReturn((short) FirebirdUserDataType.CNCT_HOST.getCode(), (short) 4, (short) FirebirdUserDataType.CNCT_LOGIN.getCode(), (short) 5);
        when(userInfo.readSlice(4)).thenReturn(hostBuf);
        when(userInfo.readSlice(5)).thenReturn(loginBuf);
        when(payload.readBuffer()).thenReturn(userInfo);
        return new FirebirdConnectPacket(payload);
    }
}
