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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PostgreSQLUnspecifiedBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLUnspecifiedBinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), "val"));
    }
    
    @Test
    void assertRead() {
        String timestampStr = "2020-08-23 15:57:03+08";
        int expectedLength = 4 + timestampStr.length();
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeInt(timestampStr.length());
        byteBuf.writeCharSequence(timestampStr, StandardCharsets.ISO_8859_1);
        byteBuf.readInt();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        Object actual = new PostgreSQLUnspecifiedBinaryProtocolValue().read(payload, timestampStr.length());
        assertThat(actual, isA(PostgreSQLTypeUnspecifiedSQLParameter.class));
        assertThat(actual.toString(), is(timestampStr));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test
    void assertWrite() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLUnspecifiedBinaryProtocolValue().write(mock(PostgreSQLPacketPayload.class), "val"));
    }
}
