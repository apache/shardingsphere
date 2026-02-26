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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query;

import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLDataRowPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private SQLXML sqlxml;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("textValueCases")
    void assertWriteWithTextValue(final String name, final Object value, final byte[] expectedBytes) {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(value));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(expectedBytes.length);
        verify(payload).writeBytes(expectedBytes);
    }
    
    @Test
    void assertWriteWithNullValue() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(null));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteWithByteArrayValue() {
        byte[] value = new byte[]{'a'};
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(value));
        actual.write((PacketPayload) payload);
        byte[] expectedBytes = buildExpectedByteaText(value);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(expectedBytes.length);
        verify(payload).writeBytes(expectedBytes);
    }
    
    private byte[] buildExpectedByteaText(final byte[] value) {
        byte[] result = new byte[value.length * 2 + 2];
        result[0] = '\\';
        result[1] = 'x';
        byte[] hexDigits = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < value.length; i++) {
            int unsignedByte = value[i] & 0xFF;
            result[2 + i * 2] = hexDigits[unsignedByte >>> 4];
            result[3 + i * 2] = hexDigits[unsignedByte & 0x0F];
        }
        return result;
    }
    
    @Test
    void assertWriteWithSQLXMLValue() throws SQLException {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(sqlxml.getString()).thenReturn("value");
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(sqlxml));
        actual.write((PacketPayload) payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test
    void assertWriteWithSQLXMLError() throws SQLException {
        when(sqlxml.getString()).thenThrow(new SQLException("mock"));
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml));
        assertThrows(IllegalStateException.class, () -> actual.write((PacketPayload) payload));
    }
    
    @Test
    void assertWriteWithBinaryNullValue() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.nCopies(1, new BinaryCell(PostgreSQLColumnType.INT4, null)));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteWithBinaryInt4Value() {
        int value = 12345678;
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(new BinaryCell(PostgreSQLColumnType.INT4, value)));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(4);
        verify(payload).writeInt4(value);
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(new PostgreSQLDataRowPacket(Collections.emptyList()).getIdentifier(), is(PostgreSQLMessagePacketType.DATA_ROW));
    }
    
    private static Stream<Arguments> textValueCases() {
        return Stream.of(
                Arguments.of("boolean_true", true, "t".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("boolean_false", false, "f".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("string_value", "value", "value".getBytes(StandardCharsets.UTF_8)));
    }
}
