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
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocol;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdAcceptPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertAcceptPacket() {
        when(byteBuf.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode(), FirebirdArchType.ARCH_GENERIC.getCode(), 0, 5, 1);
        ByteBuf buf2 = mock(ByteBuf.class);
        when(buf2.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode(), FirebirdArchType.ARCH_GENERIC.getCode(), 0, 5, 2);
        List<FirebirdProtocol> list = new LinkedList<>();
        list.add(new FirebirdProtocol(byteBuf));
        list.add(new FirebirdProtocol(buf2));
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(list);
        assertThat(packet.getOpCode(), is(FirebirdCommandPacketType.ACCEPT));
        assertThat(packet.getProtocol().getWeight(), is(2));
    }
    
    @Test
    void assertWriteWithAcceptDataPacket() {
        when(byteBuf.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode(), FirebirdArchType.ARCH_GENERIC.getCode(), 0, 5, 1);
        List<FirebirdProtocol> list = new LinkedList<>();
        list.add(new FirebirdProtocol(byteBuf));
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(list);
        packet.setAcceptDataPacket(new byte[0], "", FirebirdAuthenticationMethod.SRP, 0, "");
        InOrder io = inOrder(payload);
        packet.write(payload);
        io.verify(payload).writeInt4(FirebirdCommandPacketType.ACCEPT_DATA.getValue());
        io.verify(payload).writeInt4(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        io.verify(payload).writeInt4(FirebirdArchType.ARCH_GENERIC.getCode());
        io.verify(payload).writeInt4(5);
        io.verify(payload).writeInt4(0);
        io.verify(payload).writeString("Srp");
        io.verify(payload).writeInt4(0);
        io.verify(payload).writeString("");
    }
}
