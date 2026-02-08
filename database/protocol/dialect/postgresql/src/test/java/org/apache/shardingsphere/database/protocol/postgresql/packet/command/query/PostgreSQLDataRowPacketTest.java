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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLDataRowPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private SQLXML sqlxml;
    
    @BeforeEach
    void setup() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertWriteWithNull() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(null));
        actual.write(payload);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteWithBytes() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(new byte[]{'a'}));
        actual.write(payload);
        byte[] expectedBytes = buildExpectedByteaText(new byte[]{'a'});
        verify(payload).writeInt4(expectedBytes.length);
        verify(payload).writeBytes(expectedBytes);
    }
    
    @Test
    void assertWriteWithSQLXML() throws SQLException {
        when(sqlxml.getString()).thenReturn("value");
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(sqlxml));
        actual.write(payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test
    void assertWriteWithString() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton("value"));
        assertThat(actual.getData(), is(Collections.singleton("value")));
        actual.write(payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test
    void assertWriteWithLocalTime() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                LocalTime.of(10, 0, 0, 123_456_000),
                LocalTime.of(10, 0, 0, 000_000_001),
                LocalTime.of(10, 0, 0, 123_450_000))));
        actual.write(payload);
        byte[] res1 = "10:00:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "10:00:00.12345".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(res1.length);
        verify(payload).writeBytes(res1);
        verify(payload).writeInt4(res2.length);
        verify(payload).writeBytes(res2);
        verify(payload).writeInt4(res3.length);
        verify(payload).writeBytes(res3);
    }
    
    @Test
    void assertWriteWithLocalDateTime() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000),
                LocalDateTime.of(2022, 10, 12, 10, 0, 0, 000_000_000),
                LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_450_000))));
        actual.write(payload);
        byte[] res1 = "2022-10-12 10:00:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "2022-10-12 10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "2022-10-12 10:00:00.12345".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(res1.length);
        verify(payload).writeBytes(res1);
        verify(payload).writeInt4(res2.length);
        verify(payload).writeBytes(res2);
        verify(payload).writeInt4(res3.length);
        verify(payload).writeBytes(res3);
    }
    
    @Test
    void assertWriteWithOffsetDateTime() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 000_000_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 123_450_000, ZoneOffset.ofHoursMinutes(5, 30)))));
        actual.write(payload);
        byte[] res1 = "2022-10-12 04:30:00.123456+00".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "2022-10-12 04:30:00+00".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "2022-10-12 04:30:00.12345+00".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(res1.length);
        verify(payload).writeBytes(res1);
        verify(payload).writeInt4(res2.length);
        verify(payload).writeBytes(res2);
        verify(payload).writeInt4(res3.length);
        verify(payload).writeBytes(res3);
    }
    
    @Test
    void assertWriteWithOffsetTime() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                OffsetTime.of(10, 0, 0, 123_450_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetTime.of(10, 0, 0, 000_000_000, ZoneOffset.ofHoursMinutes(5, 30)))));
        actual.write(payload);
        byte[] res1 = "10:00:00.123450+05:30".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "10:00:00+05:30".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(res1.length);
        verify(payload).writeBytes(res1);
        verify(payload).writeInt4(res2.length);
        verify(payload).writeBytes(res2);
    }
    
    @Test
    void assertWriteWithSQLXML4Error() throws SQLException {
        when(sqlxml.getString()).thenThrow(new SQLException("mock"));
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(sqlxml));
        assertThrows(RuntimeException.class, () -> actual.write(payload));
        verify(payload, never()).writeStringEOF(any());
    }
    
    @Test
    void assertWriteBinaryNull() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(new BinaryCell(PostgreSQLColumnType.INT4, null)));
        actual.write(payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteBinaryInt4() {
        final int value = 12345678;
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(new BinaryCell(PostgreSQLColumnType.INT4, value)));
        actual.write(payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(4);
        verify(payload).writeInt4(value);
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(new PostgreSQLDataRowPacket(Collections.emptyList()).getIdentifier(), is(PostgreSQLMessagePacketType.DATA_ROW));
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
}
