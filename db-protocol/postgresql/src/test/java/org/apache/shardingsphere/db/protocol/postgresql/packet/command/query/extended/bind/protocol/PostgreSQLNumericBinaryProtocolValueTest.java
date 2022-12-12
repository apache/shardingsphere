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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class PostgreSQLNumericBinaryProtocolValueTest {
    
    private final BigDecimal bigDecimal;
    
    private final byte[] expected;
    
    @Parameters(name = "{0}")
    public static Iterable<Object[]> textValues() {
        return Arrays.asList(
                new Object[]{new BigDecimal("0"), new byte[]{0, 0, -1, -1, 0, 0, 0, 0}},
                new Object[]{new BigDecimal("0.00"), new byte[]{0, 0, -1, -1, 0, 0, 0, 2}},
                new Object[]{new BigDecimal("0.0001"), new byte[]{0, 1, -1, -1, 0, 0, 0, 4, 0, 1}},
                new Object[]{new BigDecimal("9999"), new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 39, 15}},
                new Object[]{new BigDecimal("9999.0"), new byte[]{0, 1, 0, 0, 0, 0, 0, 1, 39, 15}},
                new Object[]{new BigDecimal("9999.9999"), new byte[]{0, 2, 0, 0, 0, 0, 0, 4, 39, 15, 39, 15}});
    }
    
    @Test
    public void assertGetColumnLength() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        assertThat(binaryProtocolValue.getColumnLength(bigDecimal), is(expected.length));
    }
    
    @Test
    public void assertRead() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        int expectedLength = expected.length;
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeBytes(expected);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        Object result = binaryProtocolValue.read(payload, expectedLength);
        assertThat(result, is(bigDecimal));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test
    public void assertWrite() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        int columnLength = binaryProtocolValue.getColumnLength(bigDecimal);
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(columnLength);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        binaryProtocolValue.write(payload, bigDecimal);
        byte[] actualBytes = new byte[columnLength];
        byteBuf.readBytes(actualBytes);
        assertThat(actualBytes, is(expected));
    }
}
