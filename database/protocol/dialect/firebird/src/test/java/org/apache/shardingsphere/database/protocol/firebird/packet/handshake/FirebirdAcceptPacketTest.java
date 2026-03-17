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
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdAcceptPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("constructArguments")
    void assertConstruct(final String name, final FirebirdProtocol firstProtocol, final FirebirdProtocol secondProtocol, final FirebirdArchType expectedArch, final int expectedWeight) {
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(new LinkedList<>(Arrays.asList(firstProtocol, secondProtocol)));
        assertThat(packet.getOpCode(), is(FirebirdCommandPacketType.ACCEPT));
        assertThat(packet.getProtocol().getArch(), is(expectedArch));
        assertThat(packet.getProtocol().getWeight(), is(expectedWeight));
    }
    
    @Test
    void assertSetAcceptDataPacket() {
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(new LinkedList<>(Collections.singletonList(createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 1))));
        packet.setAcceptDataPacket(new byte[]{1, 2}, "key", FirebirdAuthenticationMethod.SRP, 7, "keys");
        assertThat(packet.getOpCode(), is(FirebirdCommandPacketType.ACCEPT_DATA));
        assertThat(packet.getAcceptDataPacket(), isA(FirebirdAcceptDataPacket.class));
        assertThat(packet.getAcceptDataPacket().getPlugin(), is(FirebirdAuthenticationMethod.SRP));
        assertThat(packet.getAcceptDataPacket().getAuthenticated(), is(7));
        assertThat(packet.getAcceptDataPacket().getKeys(), is("keys"));
    }
    
    @Test
    void assertWriteWithoutAcceptDataPacket() {
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(new LinkedList<>(Collections.singletonList(createProtocol(FirebirdArchType.ARCH_GENERIC, 7, 1))));
        InOrder order = inOrder(payload);
        packet.write((PacketPayload) payload);
        order.verify(payload).writeInt4(FirebirdCommandPacketType.ACCEPT.getValue());
        order.verify(payload).writeInt4(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        order.verify(payload).writeInt4(FirebirdArchType.ARCH_GENERIC.getCode());
        order.verify(payload).writeInt4(5);
        order.verifyNoMoreInteractions();
    }
    
    @Test
    void assertWriteWithAcceptDataPacket() {
        FirebirdAcceptPacket packet = new FirebirdAcceptPacket(new LinkedList<>(Collections.singleton(createProtocol(FirebirdArchType.ARCH_GENERIC, 0x107, 1))));
        packet.setAcceptDataPacket(new byte[0], "", FirebirdAuthenticationMethod.SRP, 0, "");
        InOrder order = inOrder(payload);
        packet.write((PacketPayload) payload);
        order.verify(payload).writeInt4(FirebirdCommandPacketType.ACCEPT_DATA.getValue());
        order.verify(payload).writeInt4(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode());
        order.verify(payload).writeInt4(FirebirdArchType.ARCH_GENERIC.getCode());
        order.verify(payload).writeInt4(0x105);
        order.verify(payload).writeInt4(0);
        order.verify(payload).writeString("Srp");
        order.verify(payload).writeInt4(0);
        order.verify(payload).writeString("");
        order.verifyNoMoreInteractions();
    }
    
    private static Stream<Arguments> constructArguments() {
        return Stream.of(
                Arguments.of("valid_higher_weight", createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 1), createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 2), FirebirdArchType.ARCH_GENERIC, 2),
                Arguments.of("invalid_architecture", createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 1), createProtocol(FirebirdArchType.ARCH_MAX, 5, 3), FirebirdArchType.ARCH_GENERIC, 1),
                Arguments.of("lower_weight", createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 2), createProtocol(FirebirdArchType.ARCH_GENERIC, 5, 1), FirebirdArchType.ARCH_GENERIC, 2));
    }
    
    private static FirebirdProtocol createProtocol(final FirebirdArchType arch, final int maxType, final int weight) {
        ByteBuf buffer = mock(ByteBuf.class);
        when(buffer.readInt()).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION11.getCode(), arch.getCode(), 0, maxType, weight);
        return new FirebirdProtocol(buffer);
    }
}
