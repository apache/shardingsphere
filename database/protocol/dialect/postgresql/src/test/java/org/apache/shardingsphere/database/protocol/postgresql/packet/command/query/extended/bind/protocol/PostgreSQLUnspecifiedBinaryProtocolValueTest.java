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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostgreSQLUnspecifiedBinaryProtocolValueTest {

    @Mock
    private ByteBuf byteBuf;

    private PostgreSQLPacketPayload payload;

    @BeforeEach
    void setup() {
        payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }

    @Test
    void assertGetColumnLength() {
        PostgreSQLUnspecifiedBinaryProtocolValue actual = new PostgreSQLUnspecifiedBinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, "val"), is(3));
        assertThat(actual.getColumnLength(payload, new PostgreSQLTypeUnspecifiedSQLParameter("test")), is(4));
    }

    @Test
    void assertGetColumnLengthWithMultiByteCharset() {
        PostgreSQLUnspecifiedBinaryProtocolValue actual = new PostgreSQLUnspecifiedBinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, "中文"), is(6));
    }

    @Test
    void assertRead() {
        String timestampStr = "2020-08-23 15:57:03+08";
        int expectedLength = 4 + timestampStr.length();
        ByteBuf readBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        readBuf.writeInt(timestampStr.length());
        readBuf.writeCharSequence(timestampStr, StandardCharsets.ISO_8859_1);
        readBuf.readInt();
        PostgreSQLPacketPayload readPayload = new PostgreSQLPacketPayload(readBuf, StandardCharsets.UTF_8);
        Object actual = new PostgreSQLUnspecifiedBinaryProtocolValue().read(readPayload, timestampStr.length());
        assertThat(actual, isA(PostgreSQLTypeUnspecifiedSQLParameter.class));
        assertThat(actual.toString(), is(timestampStr));
        assertThat(readBuf.readerIndex(), is(expectedLength));
    }

    @Test
    void assertWrite() {
        new PostgreSQLUnspecifiedBinaryProtocolValue().write(payload, "val");
        verify(byteBuf).writeBytes("val".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void assertWriteWithTypeUnspecifiedParameter() {
        new PostgreSQLUnspecifiedBinaryProtocolValue().write(payload, new PostgreSQLTypeUnspecifiedSQLParameter("test"));
        verify(byteBuf).writeBytes("test".getBytes(StandardCharsets.UTF_8));
    }
}
