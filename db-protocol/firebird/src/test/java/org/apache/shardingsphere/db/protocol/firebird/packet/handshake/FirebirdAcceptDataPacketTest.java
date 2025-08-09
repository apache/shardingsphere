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

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdAcceptDataPacketTest {
    
    @Test
    void assertWriteWithData() {
        byte[] salt = {1, 2};
        String publicKey = "key";
        FirebirdAcceptDataPacket packet = new FirebirdAcceptDataPacket(salt, publicKey,
                FirebirdAuthenticationMethod.SRP, 1, "k");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt4(), is(salt.length + publicKey.length() + 4));
        assertThat(result.readInt2LE(), is(salt.length));
        byte[] readSalt = new byte[salt.length];
        result.getByteBuf().readBytes(readSalt);
        assertThat(readSalt, is(salt));
        assertThat(result.readInt2LE(), is(publicKey.length()));
        byte[] readKey = new byte[publicKey.length()];
        result.getByteBuf().readBytes(readKey);
        assertThat(new String(readKey, StandardCharsets.US_ASCII), is(publicKey));
        assertThat(result.readString(), is("Srp"));
        assertThat(result.readInt4(), is(1));
        assertThat(result.readString(), is("k"));
    }
    
    @Test
    void assertWriteWithoutData() {
        FirebirdAcceptDataPacket packet = new FirebirdAcceptDataPacket(new byte[0], "",
                FirebirdAuthenticationMethod.SRP, 0, "");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        packet.write(payload);
        payload.getByteBuf().readerIndex(0);
        FirebirdPacketPayload result = new FirebirdPacketPayload(payload.getByteBuf(), StandardCharsets.UTF_8);
        assertThat(result.readInt4(), is(0));
        assertThat(result.readString(), is("Srp"));
        assertThat(result.readInt4(), is(0));
        assertThat(result.readString(), is(""));
    }
}
