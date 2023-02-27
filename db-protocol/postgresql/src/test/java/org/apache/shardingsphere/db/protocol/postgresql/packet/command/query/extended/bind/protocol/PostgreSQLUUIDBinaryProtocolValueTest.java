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
import org.junit.Test;
import java.util.UUID;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PostgreSQLUUIDBinaryProtocolValueTest {
    
    private final byte[] expected = new byte[5];
    
    @Test
    public void assertGetColumnLength() {
        assertThat(new PostgreSQLUUIDBinaryProtocolValue().getColumnLength(expected), is(11));
    }
    
    @Test
    public void assertRead() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(expected);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new PostgreSQLUUIDBinaryProtocolValue().read(payload, 5), is(UUID.nameUUIDFromBytes(expected)));
    }
    
    @Test
    public void assertWrite() {
        byte[] bytes = new byte[5];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes).writerIndex(0);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new PostgreSQLUUIDBinaryProtocolValue().write(payload, expected);
        assertThat(bytes, is(expected));
    }
}
