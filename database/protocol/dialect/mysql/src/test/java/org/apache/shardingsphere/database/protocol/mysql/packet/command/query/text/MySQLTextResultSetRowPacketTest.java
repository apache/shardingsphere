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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTextResultSetRowPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewWithColumnCount() {
        when(payload.readStringLenenc()).thenReturn("value_a", null, "value_c");
        assertThat(new MySQLTextResultSetRowPacket(payload, 3).getData(), is(Arrays.asList("value_a", null, "value_c")));
        verify(payload, times(3)).readStringLenenc();
    }
    
    @Test
    void assertNewWithZeroColumnCount() {
        assertTrue(new MySQLTextResultSetRowPacket(payload, 0).getData().isEmpty());
        verify(payload, never()).readStringLenenc();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeBasicValueArguments")
    void assertWriteBasicValue(final String name, final Object value, final boolean writeNullMarker, final String expectedStringValue, final byte[] expectedBytesValue) {
        new MySQLTextResultSetRowPacket(Collections.singletonList(value)).write((PacketPayload) payload);
        if (writeNullMarker) {
            verify(payload).writeInt1(0xfb);
        }
        if (null != expectedStringValue) {
            verify(payload).writeStringLenenc(expectedStringValue);
        }
        if (null != expectedBytesValue) {
            verify(payload).writeBytesLenenc(argThat(actual -> Arrays.equals(actual, expectedBytesValue)));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeTimestampArguments")
    void assertWriteTimestamp(final String name, final Timestamp value, final String expectedValue) {
        new MySQLTextResultSetRowPacket(Collections.singletonList(value)).write((PacketPayload) payload);
        verify(payload).writeStringLenenc(expectedValue);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeLocalDateTimeArguments")
    void assertWriteLocalDateTime(final String name, final LocalDateTime value, final String expectedValue) {
        new MySQLTextResultSetRowPacket(Collections.singletonList(value)).write((PacketPayload) payload);
        verify(payload).writeStringLenenc(expectedValue);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeLocalTimeArguments")
    void assertWriteLocalTime(final String name, final LocalTime value, final String expectedValue) {
        new MySQLTextResultSetRowPacket(Collections.singletonList(value)).write((PacketPayload) payload);
        verify(payload).writeStringLenenc(expectedValue);
    }
    
    @Test
    void assertWriteClob() throws SQLException {
        byte[] expectedBytes = new byte[]{10, 20};
        Clob clob = mock(Clob.class);
        when(clob.getAsciiStream()).thenReturn(new ByteArrayInputStream(expectedBytes));
        new MySQLTextResultSetRowPacket(Collections.singletonList(clob)).write((PacketPayload) payload);
        verify(payload).writeBytesLenenc(argThat(actual -> Arrays.equals(actual, expectedBytes)));
    }
    
    @Test
    void assertWriteClobWithIOException() throws SQLException {
        IOException expectedCause = new IOException("read error");
        InputStream inputStream = new InputStream() {
            
            @Override
            public int read() throws IOException {
                throw expectedCause;
            }
        };
        Clob clob = mock(Clob.class);
        when(clob.getAsciiStream()).thenReturn(inputStream);
        MySQLTextResultSetRowPacket packet = new MySQLTextResultSetRowPacket(Collections.singletonList(clob));
        UnknownSQLException actual = assertThrows(UnknownSQLException.class, () -> packet.write((PacketPayload) payload));
        assertThat(actual.getCause(), is(expectedCause));
    }
    
    @Test
    void assertWriteClobWithSQLException() throws SQLException {
        SQLException expectedCause = new SQLException("sql error");
        Clob clob = mock(Clob.class);
        when(clob.getAsciiStream()).thenThrow(expectedCause);
        UnknownSQLException actual = assertThrows(UnknownSQLException.class, () -> new MySQLTextResultSetRowPacket(Collections.singletonList(clob)).write((PacketPayload) payload));
        assertThat(actual.getCause(), is(expectedCause));
    }
    
    private static Stream<Arguments> writeBasicValueArguments() {
        byte[] binary = new byte[]{1, 2, 3};
        return Stream.of(
                Arguments.of("Null", null, true, null, null),
                Arguments.of("ByteArray", binary, false, null, binary),
                Arguments.of("BigDecimal", new BigDecimal("123.4500"), false, "123.4500", null),
                Arguments.of("BooleanTrue", Boolean.TRUE, false, null, new byte[]{1}),
                Arguments.of("BooleanFalse", Boolean.FALSE, false, null, new byte[]{0}),
                Arguments.of("DefaultToString", "value_a", false, "value_a", null));
    }
    
    private static Stream<Arguments> writeTimestampArguments() {
        Timestamp noNanos = Timestamp.valueOf("2024-05-01 11:22:33");
        Timestamp withMicros = Timestamp.valueOf("2024-05-01 11:22:33.123456");
        Timestamp withNanos = Timestamp.valueOf("2024-05-01 11:22:33.123456789");
        return Stream.of(
                Arguments.of("WithoutNanos", noNanos, noNanos.toString().split("\\.")[0]),
                Arguments.of("WithMicros", withMicros, withMicros.toString()),
                Arguments.of("WithNanos", withNanos, withNanos.toString()));
    }
    
    private static Stream<Arguments> writeLocalDateTimeArguments() {
        return Stream.of(
                Arguments.of("WithoutNanos", LocalDateTime.of(2024, 5, 1, 11, 22, 33), "2024-05-01 11:22:33"),
                Arguments.of("NoTrailingMicrosecondZero", LocalDateTime.of(2024, 5, 1, 11, 22, 33, 123456000), "2024-05-01 11:22:33.123456"),
                Arguments.of("WithTrailingMicrosecondZero", LocalDateTime.of(2024, 5, 1, 11, 22, 33, 123450000), "2024-05-01 11:22:33.12345"),
                Arguments.of("AllMicrosecondsZero", LocalDateTime.of(2024, 5, 1, 11, 22, 33, 1), "2024-05-01 11:22:33"));
    }
    
    private static Stream<Arguments> writeLocalTimeArguments() {
        return Stream.of(
                Arguments.of("WithoutNanos", LocalTime.of(11, 22, 33), "11:22:33"),
                Arguments.of("NoTrailingMicrosecondZero", LocalTime.of(11, 22, 33, 123456000), "11:22:33.123456"),
                Arguments.of("WithTrailingMicrosecondZero", LocalTime.of(11, 22, 33, 123450000), "11:22:33.12345"),
                Arguments.of("AllMicrosecondsZero", LocalTime.of(11, 22, 33, 1), "11:22:33"));
    }
}
