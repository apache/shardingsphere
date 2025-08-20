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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLByteaBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThat(new PostgreSQLByteaBinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), new byte[10]), is(10));
    }
    
    @Test
    void assertRead() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer("value".getBytes(StandardCharsets.UTF_8));
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(new PostgreSQLByteaBinaryProtocolValue().read(payload, 5), is("value".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertWrite() {
        byte[] bytes = new byte[5];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes).writerIndex(0);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        byte[] expected = "value".getBytes(StandardCharsets.UTF_8);
        new PostgreSQLByteaBinaryProtocolValue().write(payload, expected);
        assertThat(bytes, is(expected));
    }
}
