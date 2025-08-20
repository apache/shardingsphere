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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class PostgreSQLInt8BinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThat(new PostgreSQLInt8BinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), 1L), is(8));
    }
    
    @Test
    void assertRead() {
        byte[] input = new byte[]{
                (byte) 0x80, 0, 0, 0, 0, 0, 0, 0,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                0, 0, 0, 0, 0, 0, 0, 0,
                (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(input), StandardCharsets.UTF_8);
        assertThat(new PostgreSQLInt8BinaryProtocolValue().read(payload, 8), is(Long.MIN_VALUE));
        assertThat(new PostgreSQLInt8BinaryProtocolValue().read(payload, 8), is(-1L));
        assertThat(new PostgreSQLInt8BinaryProtocolValue().read(payload, 8), is(0L));
        assertThat(new PostgreSQLInt8BinaryProtocolValue().read(payload, 8), is(Long.MAX_VALUE));
    }
    
    @Test
    void assertWrite() {
        byte[] actual = new byte[24];
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(actual).writerIndex(0), StandardCharsets.UTF_8);
        new PostgreSQLInt8BinaryProtocolValue().write(payload, -1);
        new PostgreSQLInt8BinaryProtocolValue().write(payload, Long.MAX_VALUE);
        new PostgreSQLInt8BinaryProtocolValue().write(payload, BigDecimal.valueOf(Long.MIN_VALUE));
        byte[] expected = new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x80, 0, 0, 0, 0, 0, 0, 0};
        assertThat(actual, is(expected));
    }
}
