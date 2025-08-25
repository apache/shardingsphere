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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FirebirdAcceptDataPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertWriteWithData() {
        byte[] salt = {1, 2};
        String publicKey = "key";
        FirebirdAcceptDataPacket packet = new FirebirdAcceptDataPacket(salt, publicKey,
                FirebirdAuthenticationMethod.SRP, 1, "k");
        packet.write(payload);
        verify(payload).writeInt4(salt.length + publicKey.length() + 4);
        verify(payload).writeInt2LE(salt.length);
        verify(payload).writeBytes(salt);
        verify(payload).writeInt2LE(publicKey.length());
        verify(payload).writeBytes(publicKey.getBytes(StandardCharsets.US_ASCII));
        verify(payload).writeString("Srp");
        verify(payload).writeInt4(1);
        verify(payload).writeString("k");
    }
    
    @Test
    void assertWriteWithoutData() {
        FirebirdAcceptDataPacket packet = new FirebirdAcceptDataPacket(new byte[0], "", FirebirdAuthenticationMethod.SRP, 0, "");
        packet.write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt4(0);
        order.verify(payload).writeString("Srp");
        order.verify(payload).writeInt4(0);
        order.verify(payload).writeString("");
    }
}
