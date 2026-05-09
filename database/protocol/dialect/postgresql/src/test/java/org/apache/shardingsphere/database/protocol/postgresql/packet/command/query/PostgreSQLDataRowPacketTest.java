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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLDataRowPacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private SQLXML sqlxml;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("textValueCases")
    void assertWriteWithTextValue(final String name, final Object value, final byte[] expectedBytes) {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(value),
                Collections.singleton(Types.INTEGER));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(expectedBytes.length);
        verify(payload).writeBytes(expectedBytes);
    }
    
    @Test
    void assertWriteWithNullValue() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(null),
                Collections.singleton(Types.INTEGER));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteWithByteArrayValue() {
        byte[] value = new byte[]{'a'};
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(value),
                Collections.singleton(Types.VARCHAR));
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
    void assertWriteWithSQLXML() throws SQLException {
        when(sqlxml.getString()).thenReturn("value");
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(sqlxml), Collections.singleton(Types.SQLXML));
        actual.write(payload);
        byte[] valueBytes = "value".getBytes(StandardCharsets.UTF_8);
        verify(payload).writeInt4(valueBytes.length);
        verify(payload).writeBytes(valueBytes);
    }
    
    @Test
    void assertWriteWithLocalDateTime() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                Timestamp.valueOf(LocalDateTime.of(2022, 10, 12, 10, 0, 0)),
                Timestamp.valueOf(LocalDateTime.of(2022, 10, 12, 10, 0, 0, 0)),
                Timestamp.valueOf(LocalDateTime.of(2022, 10, 12, 10, 0, 0, 100_000_001)),
                Timestamp.valueOf(LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000)),
                LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_450_000),
                "2022-10-12 10:10:10.0000",
                "2022-10-12 10:10:10.1000")),
                new LinkedList<>(Arrays.asList(
                        Types.TIMESTAMP,
                        Types.TIMESTAMP,
                        Types.TIMESTAMP,
                        Types.TIMESTAMP,
                        Types.TIMESTAMP,
                        Types.TIMESTAMP,
                        Types.TIMESTAMP)));
        actual.write(payload);
        InOrder inOrder = Mockito.inOrder(payload);
        byte[] res1 = "2022-10-12 10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "2022-10-12 10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "2022-10-12 10:00:00.1".getBytes(StandardCharsets.UTF_8);
        byte[] res4 = "2022-10-12 10:00:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res5 = "2022-10-12 10:00:00.12345".getBytes(StandardCharsets.UTF_8);
        byte[] res6 = "2022-10-12 10:10:10".getBytes(StandardCharsets.UTF_8);
        byte[] res7 = "2022-10-12 10:10:10.1".getBytes(StandardCharsets.UTF_8);
        inOrder.verify(payload).writeInt4(res1.length);
        inOrder.verify(payload).writeBytes(res1);
        inOrder.verify(payload).writeInt4(res2.length);
        inOrder.verify(payload).writeBytes(res2);
        inOrder.verify(payload).writeInt4(res3.length);
        inOrder.verify(payload).writeBytes(res3);
        inOrder.verify(payload).writeInt4(res4.length);
        inOrder.verify(payload).writeBytes(res4);
        inOrder.verify(payload).writeInt4(res5.length);
        inOrder.verify(payload).writeBytes(res5);
        inOrder.verify(payload).writeInt4(res6.length);
        inOrder.verify(payload).writeBytes(res6);
        inOrder.verify(payload).writeInt4(res7.length);
        inOrder.verify(payload).writeBytes(res7);
    }
    
    @Test
    void assertWriteWithOffsetDateTime() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 0, ZoneOffset.ofHoursMinutesSeconds(5, 30, 1)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 100_000_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 123_450_000, ZoneOffset.ofHoursMinutes(2, 30)),
                Timestamp.valueOf(LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000)),
                OffsetDateTime.of(2022, 10, 12, 10, 0, 0, 0, ZoneOffset.ofHoursMinutes(-2, -30)),
                "2022-10-12 10:00:00.1200+00:00",
                "2022-10-12 10:00:00.0+00:00",
                LocalDateTime.of(2022, 10, 12, 10, 0, 0, 123_456_000))),
                new LinkedList<>(Arrays.asList(
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE,
                        Types.TIMESTAMP_WITH_TIMEZONE)));
        actual.write(payload);
        InOrder inOrder = Mockito.inOrder(payload);
        byte[] res1 = "2022-10-12 04:29:59".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "2022-10-12 04:30:00.1".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "2022-10-12 04:30:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res4 = "2022-10-12 07:30:00.12345".getBytes(StandardCharsets.UTF_8);
        byte[] res5 = "2022-10-12 10:00:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res6 = "2022-10-12 12:30:00".getBytes(StandardCharsets.UTF_8);
        byte[] res7 = "2022-10-12 10:00:00.12".getBytes(StandardCharsets.UTF_8);
        byte[] res8 = "2022-10-12 10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res9 = "2022-10-12 10:00:00.123456".getBytes(StandardCharsets.UTF_8);
        
        inOrder.verify(payload).writeInt4(res1.length);
        inOrder.verify(payload).writeBytes(res1);
        inOrder.verify(payload).writeInt4(res2.length);
        inOrder.verify(payload).writeBytes(res2);
        inOrder.verify(payload).writeInt4(res3.length);
        inOrder.verify(payload).writeBytes(res3);
        inOrder.verify(payload).writeInt4(res4.length);
        inOrder.verify(payload).writeBytes(res4);
        inOrder.verify(payload).writeInt4(res5.length);
        inOrder.verify(payload).writeBytes(res5);
        inOrder.verify(payload).writeInt4(res6.length);
        inOrder.verify(payload).writeBytes(res6);
        inOrder.verify(payload).writeInt4(res7.length);
        inOrder.verify(payload).writeBytes(res7);
        inOrder.verify(payload).writeInt4(res8.length);
        inOrder.verify(payload).writeBytes(res8);
        inOrder.verify(payload).writeInt4(res9.length);
        inOrder.verify(payload).writeBytes(res9);
        
    }
    
    @Test
    void assertWriteWithOffsetTime() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                OffsetTime.of(10, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetTime.of(10, 0, 0, 100_000_000, ZoneOffset.ofHoursMinutes(-2, -30)),
                OffsetTime.of(10, 0, 0, 100_000_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetTime.of(10, 0, 0, 123_456_000, ZoneOffset.ofHoursMinutes(5, 30)),
                OffsetTime.of(10, 0, 0, 123_450_000, ZoneOffset.ofHoursMinutes(5, 30)),
                "10:00:00.1200+00:00",
                "10:00:00.0+00:00",
                LocalTime.of(10, 0, 12, 123_000_000))),
                new LinkedList<>(Arrays.asList(
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE,
                        Types.TIME_WITH_TIMEZONE)));
        actual.write(payload);
        InOrder inOrder = Mockito.inOrder(payload);
        byte[] res1 = "04:30:00".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "12:30:00.1".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "04:30:00.1".getBytes(StandardCharsets.UTF_8);
        byte[] res4 = "04:30:00.123456".getBytes(StandardCharsets.UTF_8);
        byte[] res5 = "04:30:00.12345".getBytes(StandardCharsets.UTF_8);
        byte[] res6 = "10:00:00.12".getBytes(StandardCharsets.UTF_8);
        byte[] res7 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        inOrder.verify(payload).writeInt4(res1.length);
        inOrder.verify(payload).writeBytes(res1);
        inOrder.verify(payload).writeInt4(res2.length);
        inOrder.verify(payload).writeBytes(res2);
        inOrder.verify(payload).writeInt4(res3.length);
        inOrder.verify(payload).writeBytes(res3);
        inOrder.verify(payload).writeInt4(res4.length);
        inOrder.verify(payload).writeBytes(res4);
        inOrder.verify(payload).writeInt4(res5.length);
        inOrder.verify(payload).writeBytes(res5);
        inOrder.verify(payload).writeInt4(res6.length);
        inOrder.verify(payload).writeBytes(res6);
        inOrder.verify(payload).writeInt4(res7.length);
        inOrder.verify(payload).writeBytes(res7);
        
    }
    
    @Test
    void assertWriteWithLocalTime() {
        when(payload.getCharset()).thenReturn(StandardCharsets.UTF_8);
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(new LinkedList<>(Arrays.asList(
                Time.valueOf(LocalTime.of(10, 0, 0, 0)),
                LocalTime.of(10, 0, 0, 100_000_000),
                Time.valueOf(LocalTime.of(10, 0)),
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 0, 0, 123_450_000),
                "10:00:00.1200",
                "10:00:00.0")),
                new LinkedList<>(Arrays.asList(
                        Types.TIME,
                        Types.TIME,
                        Types.TIME,
                        Types.TIME,
                        Types.TIME,
                        Types.TIME,
                        Types.TIME)));
        actual.write(payload);
        InOrder inOrder = Mockito.inOrder(payload);
        byte[] res1 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res2 = "10:00:00.1".getBytes(StandardCharsets.UTF_8);
        byte[] res3 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res4 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        byte[] res5 = "10:00:00.12345".getBytes(StandardCharsets.UTF_8);
        byte[] res6 = "10:00:00.12".getBytes(StandardCharsets.UTF_8);
        byte[] res7 = "10:00:00".getBytes(StandardCharsets.UTF_8);
        inOrder.verify(payload).writeInt4(res1.length);
        inOrder.verify(payload).writeBytes(res1);
        inOrder.verify(payload).writeInt4(res2.length);
        inOrder.verify(payload).writeBytes(res2);
        inOrder.verify(payload).writeInt4(res3.length);
        inOrder.verify(payload).writeBytes(res3);
        inOrder.verify(payload).writeInt4(res4.length);
        inOrder.verify(payload).writeBytes(res4);
        inOrder.verify(payload).writeInt4(res5.length);
        inOrder.verify(payload).writeBytes(res5);
        inOrder.verify(payload).writeInt4(res6.length);
        inOrder.verify(payload).writeBytes(res6);
        inOrder.verify(payload).writeInt4(res7.length);
        inOrder.verify(payload).writeBytes(res7);
    }
    
    @Test
    void assertWriteWithSQLXMLError() throws SQLException {
        when(sqlxml.getString()).thenThrow(new SQLException("mock"));
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singletonList(sqlxml), Collections.singleton(Types.SQLXML));
        assertThrows(IllegalStateException.class, () -> actual.write((PacketPayload) payload));
    }
    
    @Test
    void assertWriteWithBinaryNullValue() {
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.nCopies(1, new BinaryCell(PostgreSQLBinaryColumnType.INT4, null)),
                Collections.singleton(PostgreSQLBinaryColumnType.INT4.getValue()));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(0xFFFFFFFF);
    }
    
    @Test
    void assertWriteWithBinaryInt4Value() {
        int value = 12345678;
        PostgreSQLDataRowPacket actual = new PostgreSQLDataRowPacket(Collections.singleton(new BinaryCell(PostgreSQLBinaryColumnType.INT4, value)),
                Collections.singleton(PostgreSQLBinaryColumnType.INT4.getValue()));
        actual.write((PacketPayload) payload);
        verify(payload).writeInt2(1);
        verify(payload).writeInt4(4);
        verify(payload).writeInt4(value);
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(new PostgreSQLDataRowPacket(Collections.emptyList(), Collections.emptyList()).getIdentifier(), is(PostgreSQLMessagePacketType.DATA_ROW));
    }
    
    private static Stream<Arguments> textValueCases() {
        return Stream.of(
                Arguments.of("boolean_true", true, "t".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("boolean_false", false, "f".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("string_value", "value", "value".getBytes(StandardCharsets.UTF_8)));
    }
}
