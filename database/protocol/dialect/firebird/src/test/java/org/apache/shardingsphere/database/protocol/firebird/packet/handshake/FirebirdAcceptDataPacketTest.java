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

import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FirebirdAcceptDataPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final byte[] salt, final String publicKey, final FirebirdAuthenticationMethod plugin,
                     final int authenticated, final String keys, final int expectedPayloadLength, final boolean expectedPayloadData) {
        FirebirdAcceptDataPacket packet = new FirebirdAcceptDataPacket(salt, publicKey, plugin, authenticated, keys);
        packet.write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt4(expectedPayloadLength);
        verifyPayloadData(order, payload, expectedPayloadData, salt, publicKey);
        order.verify(payload).writeString(plugin.getMethodName());
        order.verify(payload).writeInt4(authenticated);
        order.verify(payload).writeString(keys);
        order.verifyNoMoreInteractions();
    }
    
    private void verifyPayloadData(final InOrder order, final FirebirdPacketPayload payload, final boolean expectedPayloadData, final byte[] salt, final String publicKey) {
        if (!expectedPayloadData) {
            return;
        }
        order.verify(payload).writeInt2LE(salt.length);
        order.verify(payload).writeBytes(salt);
        order.verify(payload).writeInt2LE(publicKey.length());
        order.verify(payload).writeBytes(publicKey.getBytes(StandardCharsets.US_ASCII));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("salt_and_public_key", new byte[]{1, 2}, "key", FirebirdAuthenticationMethod.SRP, 1, "k", 9, true),
                Arguments.of("empty_salt", new byte[0], "key", FirebirdAuthenticationMethod.SRP224, 0, "", 0, false),
                Arguments.of("empty_public_key", new byte[]{1, 2}, "", FirebirdAuthenticationMethod.LEGACY_AUTH, 7, "keys", 0, false));
    }
}
