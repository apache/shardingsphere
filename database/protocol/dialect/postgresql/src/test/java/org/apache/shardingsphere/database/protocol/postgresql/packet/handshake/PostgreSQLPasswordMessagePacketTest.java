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

package org.apache.shardingsphere.database.protocol.postgresql.packet.handshake;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLPasswordMessagePacketTest {
    
    @Test
    void assertReadWrite() {
        String md5Digest = "ce98bac7fc97f20584ea9536e744dabb";
        int expectedLength = 4 + md5Digest.length() + 1;
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        payload.writeInt4(expectedLength);
        payload.writeStringNul(md5Digest);
        PostgreSQLPasswordMessagePacket packet = new PostgreSQLPasswordMessagePacket(payload);
        assertThat(packet.getIdentifier(), is(PostgreSQLMessagePacketType.PASSWORD_MESSAGE));
        assertThat(packet.getDigest(), is(md5Digest));
        packet.write(payload);
        assertThat(byteBuf.writerIndex(), is(expectedLength));
    }
}
