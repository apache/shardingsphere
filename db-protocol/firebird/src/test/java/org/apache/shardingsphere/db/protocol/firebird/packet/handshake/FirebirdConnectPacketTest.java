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

package org.apache.shardingsphere.db.protocol.firebird.packet.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdUserDataType;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebirdConnectPacketTest {
    
    private ByteBuf createUserInfo() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(FirebirdUserDataType.CNCT_USER.getCode());
        buf.writeByte(4);
        buf.writeBytes("user".getBytes(StandardCharsets.UTF_8));
        buf.writeByte(FirebirdUserDataType.CNCT_PLUGIN_NAME.getCode());
        buf.writeByte(3);
        buf.writeBytes("srp".getBytes(StandardCharsets.UTF_8));
        buf.writeByte(FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode());
        buf.writeByte(2);
        buf.writeByte(0);
        buf.writeByte('A');
        buf.writeByte(FirebirdUserDataType.CNCT_SPECIFIC_DATA.getCode());
        buf.writeByte(2);
        buf.writeByte(1);
        buf.writeByte('B');
        return buf;
    }
    
    private ByteBuf createProtocol() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION10.getCode());
        buf.writeInt(FirebirdArchType.ARCH_GENERIC.getCode());
        buf.writeInt(0);
        buf.writeInt(5);
        buf.writeInt(1);
        return buf;
    }
    
    @Test
    void assertParseConnectPacket() {
        ByteBuf buf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(buf, StandardCharsets.UTF_8);
        payload.writeInt4(FirebirdCommandPacketType.CONNECT.getValue());
        payload.writeInt4(1);
        payload.writeInt4(FirebirdArchType.ARCH_GENERIC.getCode());
        payload.writeString("db");
        payload.writeInt4(1);
        payload.writeBuffer(createUserInfo());
        buf.writeBytes(createProtocol());
        buf.readerIndex(0);
        FirebirdConnectPacket packet = new FirebirdConnectPacket(new FirebirdPacketPayload(buf, StandardCharsets.UTF_8));
        assertEquals(FirebirdCommandPacketType.CONNECT, packet.getOpCode());
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
