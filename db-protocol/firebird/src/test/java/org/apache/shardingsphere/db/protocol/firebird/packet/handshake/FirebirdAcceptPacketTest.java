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
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebirdAcceptPacketTest {

    @Test
    void assertAcceptPacket() {
        List<FirebirdProtocol> list = new ArrayList<>();
        ByteBuf byteBuf1 = Unpooled.buffer();
        byteBuf1.writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        byteBuf1.writeInt(FirebirdArchType.ARCH_GENERIC.getCode());
        byteBuf1.writeInt(0);
        byteBuf1.writeInt(5);
        byteBuf1.writeInt(1);
        list.add(new FirebirdProtocol(byteBuf1));
        ByteBuf byteBuf2 = Unpooled.buffer();
        byteBuf2.writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        byteBuf2.writeInt(FirebirdArchType.ARCH_GENERIC.getCode());
        byteBuf2.writeInt(0);
        byteBuf2.writeInt(5);
        byteBuf2.writeInt(2);
        list.add(new FirebirdProtocol(byteBuf2));
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(list);
        assertEquals(FirebirdCommandPacketType.ACCEPT, packet.getOpCode());
        assertThat(packet.getProtocol().getWeight(), is(2));
    }
    
    @Test
    void assertWriteWithAcceptDataPacket() {
        List<FirebirdProtocol> list = new ArrayList<>();
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        byteBuf.writeInt(FirebirdArchType.ARCH_GENERIC.getCode());
        byteBuf.writeInt(0);
        byteBuf.writeInt(5);
        byteBuf.writeInt(1);
        list.add(new FirebirdProtocol(byteBuf));
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(list);
        packet.setAcceptDataPacket(new byte[0], "", FirebirdAuthenticationMethod.SRP, 0, "");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt4(), is(FirebirdCommandPacketType.ACCEPT_DATA.getValue()));
        assertThat(result.readInt4(), is(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode()));
        assertThat(result.readInt4(), is(FirebirdArchType.ARCH_GENERIC.getCode()));
        assertThat(result.readInt4(), is(5));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readString(), is("Srp"));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readString(), is(""));
    }
}
