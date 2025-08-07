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
    
    private FirebirdProtocol createProtocol(final int weight) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        buf.writeInt(FirebirdArchType.ARCH_GENERIC.getCode());
        buf.writeInt(0);
        buf.writeInt(5);
        buf.writeInt(weight);
        return new FirebirdProtocol(buf);
    }
    
    @Test
    void assertSelectHighestWeightProtocol() {
        List<FirebirdProtocol> list = new ArrayList<>();
        list.add(createProtocol(1));
        list.add(createProtocol(2));
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(list);
        assertEquals(FirebirdCommandPacketType.ACCEPT, packet.getOpCode());
        assertThat(packet.getProtocol().getWeight(), is(2));
    }
    
    @Test
    void assertWriteWithAcceptData() {
        List<FirebirdProtocol> list = new ArrayList<>();
        list.add(createProtocol(1));
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
