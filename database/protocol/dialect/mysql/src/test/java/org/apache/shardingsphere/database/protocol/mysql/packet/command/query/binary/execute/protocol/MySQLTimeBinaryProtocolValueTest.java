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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimeBinaryProtocolValueTest {
    
    private final MySQLTimeBinaryProtocolValue binaryProtocolValue = new MySQLTimeBinaryProtocolValue();
    
    @Mock
    private MySQLPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("readArguments")
    void assertRead(final String name, final int[] int1Values, final int[] int4Values, final Timestamp expected) throws SQLException {
        when(payload.readInt1()).thenReturn(int1Values[0], int1Values[1], int1Values[2], int1Values[3], int1Values[4]);
        when(payload.readInt4()).thenReturn(int4Values[0], int4Values[1]);
        assertThat(binaryProtocolValue.read(payload, false), is(expected));
    }
    
    @Test
    void assertReadWithUnsupportedLength() {
        when(payload.readInt1()).thenReturn(100, 0);
        when(payload.readInt4()).thenReturn(0);
        SQLFeatureNotSupportedException actual = assertThrows(SQLFeatureNotSupportedException.class, () -> binaryProtocolValue.read(payload, false));
        assertThat(actual.getMessage(), is("Wrong length `100` of MYSQL_TYPE_DATE"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Time value, final List<Integer> expectedInt1Values, final List<Integer> expectedInt4Values) {
        binaryProtocolValue.write(payload, value);
        ArgumentCaptor<Integer> int1Captor = ArgumentCaptor.forClass(Integer.class);
        verify(payload, times(expectedInt1Values.size())).writeInt1(int1Captor.capture());
        List<Integer> actualInt1Values = int1Captor.getAllValues();
        assertThat(actualInt1Values, is(expectedInt1Values));
        if (expectedInt4Values.isEmpty()) {
            verify(payload, never()).writeInt4(anyInt());
        } else {
            ArgumentCaptor<Integer> int4Captor = ArgumentCaptor.forClass(Integer.class);
            verify(payload, times(expectedInt4Values.size())).writeInt4(int4Captor.capture());
            List<Integer> actualInt4Values = int4Captor.getAllValues();
            assertThat(actualInt4Values, is(expectedInt4Values));
        }
        verifyNoMoreInteractions(payload);
    }
    
    private static Stream<Arguments> readArguments() {
        return Stream.of(
                Arguments.of("zero_length", new int[]{0, 0, 0, 0, 0}, new int[]{0, 0}, new Timestamp(0L)),
                Arguments.of("eight_bytes", new int[]{8, 0, 10, 59, 0}, new int[]{0, 0}, createTimestamp(0)),
                Arguments.of("twelve_bytes", new int[]{12, 0, 10, 59, 0}, new int[]{0, 1000}, createTimestamp(1000)));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("time_and_nanos_absent", Time.valueOf(LocalTime.of(0, 0)), Collections.singletonList(0), Collections.emptyList()),
                Arguments.of("hour_present_without_nanos", Time.valueOf(LocalTime.of(1, 30, 10)), Arrays.asList(8, 0, 1, 30, 10), Collections.singletonList(0)),
                Arguments.of("minutes_present_without_nanos", Time.valueOf(LocalTime.of(0, 30)), Arrays.asList(8, 0, 0, 30, 0), Collections.singletonList(0)),
                Arguments.of("seconds_present_without_nanos", Time.valueOf(LocalTime.of(0, 0, 10)), Arrays.asList(8, 0, 0, 0, 10), Collections.singletonList(0)),
                Arguments.of("nanos_only", createTimeAtLocalDateStart(), Arrays.asList(12, 0, 0, 0, 0), Arrays.asList(0, 1000)));
    }
    
    private static Timestamp createTimestamp(final int nanos) {
        Timestamp result = Timestamp.valueOf(LocalDateTime.of(0, 1, 1, 10, 59, 0));
        result.setNanos(nanos);
        return result;
    }
    
    private static Time createTimeAtLocalDateStart() {
        return new Time(LocalDate.of(1970, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1);
    }
}
