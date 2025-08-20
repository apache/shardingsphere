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
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLInt8ArrayBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLInt8ArrayBinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), "val"));
    }
    
    @Test
    void assertRead() {
        String parameterValue = "{\"11\",\"12\"}";
        int expectedLength = 4 + parameterValue.length();
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeInt(parameterValue.length());
        byteBuf.writeCharSequence(parameterValue, StandardCharsets.ISO_8859_1);
        byteBuf.readInt();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        Object actual = new PostgreSQLInt8ArrayBinaryProtocolValue().read(payload, parameterValue.length());
        assertThat(actual, is(new long[]{11L, 12L}));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test
    void assertWrite() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLInt8ArrayBinaryProtocolValue().write(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), "val"));
    }
}
