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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FirebirdContinueAuthPacketTest {
    
    private static final byte[] PACKET = {0, 0, 0, 92, 0, 0, 0, 3, 0, 127, (byte) 255, 0, 0, 0, 0, 3, 'S', 'r', 'p', 0, 0, 0, 0, 3, 'A', ',', 'B', 0, 0, 0, 0, 1, (byte) 128, 0, 0, 0};
    
    @Test
    void assertRead() {
        ByteBuf buffer = Unpooled.wrappedBuffer(PACKET);
        try {
            buffer.readInt();
            FirebirdContinueAuthPacket actual = new FirebirdContinueAuthPacket(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(actual.getData(), is(new byte[]{0, 127, (byte) 255}));
            assertThat(actual.getPluginName(), is("Srp"));
            assertThat(actual.getPluginList(), is("A,B"));
            assertThat(actual.getKeys(), is(new byte[]{(byte) 128}));
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertWrite() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            FirebirdContinueAuthPacket packet = new FirebirdContinueAuthPacket(new byte[]{0, 127, (byte) 255}, "Srp", "A,B", new byte[]{(byte) 128});
            packet.write((PacketPayload) new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(ByteBufUtil.getBytes(buffer), is(PACKET));
        } finally {
            buffer.release();
        }
    }
}
