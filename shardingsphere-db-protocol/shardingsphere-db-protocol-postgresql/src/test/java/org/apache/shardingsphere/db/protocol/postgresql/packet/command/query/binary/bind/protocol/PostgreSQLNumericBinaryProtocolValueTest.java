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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PostgreSQLNumericBinaryProtocolValueTest {
    
    @Test
    public void assertGetColumnLength() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        assertThat(binaryProtocolValue.getColumnLength(null), is(0));
        assertThat(binaryProtocolValue.getColumnLength(new BigDecimal("1234567890.12")), is(13));
    }
    
    @Test
    public void assertRead() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        String decimalText = "1234567890.12";
        BigDecimal expectedDecimal = new BigDecimal(decimalText);
        int columnLength = binaryProtocolValue.getColumnLength(expectedDecimal);
        int expectedLength = 4 + columnLength;
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeInt(columnLength);
        byteBuf.writeBytes(decimalText.getBytes(StandardCharsets.UTF_8));
        byteBuf.readInt();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        Object result = binaryProtocolValue.read(payload, columnLength);
        assertNotNull(result);
        assertTrue(result instanceof BigDecimal);
        assertThat(result, is(expectedDecimal));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test
    public void assertWrite() {
        PostgreSQLNumericBinaryProtocolValue binaryProtocolValue = new PostgreSQLNumericBinaryProtocolValue();
        String decimalText = "1234567890.12";
        BigDecimal decimal = new BigDecimal(decimalText);
        int columnLength = binaryProtocolValue.getColumnLength(decimal);
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(columnLength);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        binaryProtocolValue.write(payload, decimal);
        byte[] actualBytes = new byte[columnLength];
        byteBuf.readBytes(actualBytes);
        assertThat(new String(actualBytes, StandardCharsets.UTF_8), is(decimalText));
    }
    
}
