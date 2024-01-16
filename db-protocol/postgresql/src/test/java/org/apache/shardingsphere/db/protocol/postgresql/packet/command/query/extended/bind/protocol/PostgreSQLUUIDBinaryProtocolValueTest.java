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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLUUIDBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThat(new PostgreSQLUUIDBinaryProtocolValue().getColumnLength(UUID.randomUUID()), is(16));
    }
    
    @Test
    void assertRead() {
        UUID randomUUID = UUID.randomUUID();
        byte[] expected = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(expected);
        buffer.putLong(randomUUID.getMostSignificantBits());
        buffer.putLong(randomUUID.getLeastSignificantBits());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(expected);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new PostgreSQLUUIDBinaryProtocolValue().read(payload, 16), is(randomUUID));
    }
    
    @Test
    void assertWrite() {
        UUID randomUUID = UUID.randomUUID();
        byte[] expected = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(expected);
        buffer.putLong(randomUUID.getMostSignificantBits());
        buffer.putLong(randomUUID.getLeastSignificantBits());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(expected);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf.writerIndex(0), StandardCharsets.UTF_8);
        PostgreSQLUUIDBinaryProtocolValue actual = new PostgreSQLUUIDBinaryProtocolValue();
        actual.write(payload, randomUUID);
        assertThat(actual.read(payload, 16), is(randomUUID));
    }
}
