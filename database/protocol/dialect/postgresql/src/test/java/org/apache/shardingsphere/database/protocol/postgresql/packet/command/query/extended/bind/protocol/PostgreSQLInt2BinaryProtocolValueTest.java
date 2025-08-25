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

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLInt2BinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThat(new PostgreSQLInt2BinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), null), is(2));
    }
    
    @Test
    void assertRead() {
        byte[] data = {(byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F, (byte) 0xFF};
        PostgreSQLInt2BinaryProtocolValue actual = new PostgreSQLInt2BinaryProtocolValue();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        assertThat(actual.read(payload, 2), is(Short.MIN_VALUE));
        assertThat(actual.read(payload, 2), is((short) -1));
        assertThat(actual.read(payload, 2), is(Short.MAX_VALUE));
    }
    
    @Test
    void assertWrite() {
        byte[] actualData = new byte[6];
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(actualData).writerIndex(0), StandardCharsets.UTF_8);
        PostgreSQLInt2BinaryProtocolValue actual = new PostgreSQLInt2BinaryProtocolValue();
        actual.write(payload, Short.MIN_VALUE);
        actual.write(payload, -1);
        actual.write(payload, (int) Short.MAX_VALUE);
        assertThat(actualData, is(new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F, (byte) 0xFF}));
    }
}
