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
import org.postgresql.jdbc.TimestampUtils;
import org.postgresql.util.PSQLException;

import java.nio.charset.StandardCharsets;
import java.sql.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class PostgreSQLDateBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThat(new PostgreSQLDateBinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), ""), is(4));
    }
    
    @Test
    void assertRead() throws PSQLException {
        byte[] payloadBytes = new byte[4];
        Date expected = Date.valueOf("2023-01-30");
        new TimestampUtils(false, null).toBinDate(null, payloadBytes, expected);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(payloadBytes), StandardCharsets.UTF_8);
        assertThat(new PostgreSQLDateBinaryProtocolValue().read(payload, 4), is(expected));
    }
    
    @Test
    void assertWrite() throws PSQLException {
        byte[] actual = new byte[4];
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(actual).writerIndex(0), StandardCharsets.UTF_8);
        Date input = Date.valueOf("2023-01-30");
        new PostgreSQLDateBinaryProtocolValue().write(payload, input);
        byte[] expected = new byte[4];
        new TimestampUtils(false, null).toBinDate(null, expected, input);
        assertThat(actual, is(expected));
    }
}
