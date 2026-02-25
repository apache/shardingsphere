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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDateBinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertReadWithZeroByte() {
        when(payload.readInt1()).thenReturn(0);
        assertThrows(SQLFeatureNotSupportedException.class, () -> new MySQLDateBinaryProtocolValue().read(payload, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("readArguments")
    void assertRead(final String name, final int[] readInt1Values, final int year, final int microseconds, final LocalDateTime expected) throws SQLException {
        Integer[] tailValues = new Integer[readInt1Values.length - 1];
        for (int i = 1; i < readInt1Values.length; i++) {
            tailValues[i - 1] = readInt1Values[i];
        }
        when(payload.readInt1()).thenReturn(readInt1Values[0], tailValues);
        when(payload.readInt2()).thenReturn(year);
        if (0 <= microseconds) {
            when(payload.readInt4()).thenReturn(microseconds);
        }
        Timestamp actual = (Timestamp) new MySQLDateBinaryProtocolValue().read(payload, false);
        assertThat(actual.toLocalDateTime(), is(expected));
    }
    
    @Test
    void assertReadWithIllegalArgument() {
        when(payload.readInt1()).thenReturn(100);
        assertThrows(SQLFeatureNotSupportedException.class, () -> new MySQLDateBinaryProtocolValue().read(payload, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final int expectedLength, final int year, final int month,
                     final int dayOfMonth, final int hour, final int minute, final int second, final Integer expectedMicroseconds) {
        new MySQLDateBinaryProtocolValue().write(payload, value);
        verify(payload).writeInt2(year);
        List<Integer> expectedValues = new LinkedList<>();
        expectedValues.add(expectedLength);
        expectedValues.add(month);
        expectedValues.add(dayOfMonth);
        if (4 != expectedLength) {
            expectedValues.add(hour);
            expectedValues.add(minute);
            expectedValues.add(second);
        }
        ArgumentCaptor<Integer> int1Captor = ArgumentCaptor.forClass(Integer.class);
        verify(payload, times(expectedValues.size())).writeInt1(int1Captor.capture());
        if (11 == expectedLength) {
            verify(payload).writeInt4(expectedMicroseconds);
        }
        assertThat(int1Captor.getAllValues(), is(expectedValues));
    }
    
    @Test
    void assertWriteWithNanoOverflowWithoutSecondCarry() {
        LocalDateTime dateTime = mock(LocalDateTime.class);
        when(dateTime.getYear()).thenReturn(2026);
        when(dateTime.getMonthValue()).thenReturn(1);
        when(dateTime.getDayOfMonth()).thenReturn(2);
        when(dateTime.getHour()).thenReturn(3);
        when(dateTime.getMinute()).thenReturn(4);
        when(dateTime.getSecond()).thenReturn(10);
        when(dateTime.getNano()).thenReturn(1_000_000_001);
        new MySQLDateBinaryProtocolValue().write(payload, dateTime);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt1(11);
        inOrder.verify(payload).writeInt2(2026);
        inOrder.verify(payload).writeInt1(1);
        inOrder.verify(payload).writeInt1(2);
        inOrder.verify(payload).writeInt1(3);
        inOrder.verify(payload).writeInt1(4);
        inOrder.verify(payload).writeInt1(11);
        inOrder.verify(payload).writeInt4(0);
    }
    
    @Test
    void assertWriteWithNanoOverflowAndSecondCarry() {
        LocalDateTime dateTime = mock(LocalDateTime.class);
        LocalDateTime normalized = LocalDateTime.of(2026, 1, 2, 3, 4, 1);
        when(dateTime.getYear()).thenReturn(2026);
        when(dateTime.getMonthValue()).thenReturn(1);
        when(dateTime.getDayOfMonth()).thenReturn(2);
        when(dateTime.getHour()).thenReturn(3);
        when(dateTime.getMinute()).thenReturn(4);
        when(dateTime.getSecond()).thenReturn(59);
        when(dateTime.getNano()).thenReturn(2_000_000_000);
        when(dateTime.plusSeconds(2)).thenReturn(normalized);
        new MySQLDateBinaryProtocolValue().write(payload, dateTime);
        verify(dateTime).plusSeconds(2);
        InOrder inOrder = inOrder(payload);
        inOrder.verify(payload).writeInt1(7);
        inOrder.verify(payload).writeInt2(2026);
        inOrder.verify(payload).writeInt1(1);
        inOrder.verify(payload).writeInt1(2);
        inOrder.verify(payload).writeInt1(3);
        inOrder.verify(payload).writeInt1(4);
        inOrder.verify(payload).writeInt1(1);
    }
    
    private static Stream<Arguments> readArguments() {
        return Stream.of(
                Arguments.of("four-bytes", new int[]{4, 12, 31}, 2018, -1, LocalDateTime.of(2018, 12, 31, 0, 0, 0)),
                Arguments.of("seven-bytes", new int[]{7, 12, 31, 10, 59, 0}, 2018, -1, LocalDateTime.of(2018, 12, 31, 10, 59, 0)),
                Arguments.of("eleven-bytes", new int[]{11, 12, 31, 10, 59, 0}, 2018, 230000, LocalDateTime.of(2018, 12, 31, 10, 59, 0, 230000000)));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("local-date", LocalDate.of(1970, 1, 14), 4, 1970, 1, 14, 0, 0, 0, null),
                Arguments.of("local-date-time-date-only", LocalDateTime.of(1970, 1, 14, 0, 0, 0), 4, 1970, 1, 14, 0, 0, 0, null),
                Arguments.of("local-date-time-minute-only", LocalDateTime.of(1970, 1, 14, 0, 1, 0), 7, 1970, 1, 14, 0, 1, 0, null),
                Arguments.of("local-date-time-second-only", LocalDateTime.of(1970, 1, 14, 0, 0, 1), 7, 1970, 1, 14, 0, 0, 1, null),
                Arguments.of("local-date-time-with-time", LocalDateTime.of(1970, 1, 14, 12, 10, 30), 7, 1970, 1, 14, 12, 10, 30, null),
                Arguments.of("timestamp-with-time", Timestamp.valueOf("1970-01-14 12:10:30"), 7, 1970, 1, 14, 12, 10, 30, null),
                Arguments.of("timestamp-with-microseconds", Timestamp.valueOf("1970-01-14 12:10:30.123"), 11, 1970, 1, 14, 12, 10, 30, 123000),
                Arguments.of("time-absent-with-nanos", LocalDateTime.of(1970, 1, 14, 0, 0, 0, 1_000), 11, 1970, 1, 14, 0, 0, 0, 1));
    }
}
