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

package org.apache.shardingsphere.database.connector.mysql.resultset;

import org.apache.shardingsphere.database.connector.core.resultset.ResultSetTimeValueReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimeValueReaderTest {
    
    @Mock
    private ResultSet resultSet;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("timeArguments")
    void assertReadTimeValue(final String name, final LocalTime value) throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenReturn(value);
        assertThat(new MySQLTimeValueReader().read(resultSet, 1), is(value));
    }
    
    private static Stream<Arguments> timeArguments() {
        return Stream.of(
                Arguments.of("whole-minute", LocalTime.of(12, 34)),
                Arguments.of("milliseconds", LocalTime.of(12, 34, 56, 123000000)),
                Arguments.of("microseconds", LocalTime.of(12, 34, 56, 123456000)),
                Arguments.of("midnight-microsecond", LocalTime.of(0, 0, 0, 1000)),
                Arguments.of("last-microsecond", LocalTime.of(23, 59, 59, 999999000)));
    }
    
    @Test
    void assertReadNullTimeValue() throws SQLException {
        assertNull(new MySQLTimeValueReader().read(resultSet, 1));
        verify(resultSet, never()).getTime(1);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("fallbackTimeArguments")
    void assertReadTimeWithUnsupportedGetter(final String name, final Time value, final LocalTime expected) throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class);
        when(resultSet.getTime(1)).thenReturn(value);
        assertThat(new MySQLTimeValueReader().read(resultSet, 1), is(expected));
    }
    
    private static Stream<Arguments> fallbackTimeArguments() {
        return Stream.of(
                Arguments.of("whole-second", Time.valueOf("12:34:56"), LocalTime.of(12, 34, 56)),
                Arguments.of("milliseconds", new Time(Time.valueOf("12:34:56").getTime() + 123), LocalTime.of(12, 34, 56, 123000000)),
                Arguments.of("midnight-millisecond", new Time(Time.valueOf("00:00:00").getTime() + 1), LocalTime.of(0, 0, 0, 1000000)));
    }
    
    @Test
    void assertReadTimeCachesUnsupportedColumns() throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class);
        when(resultSet.getObject(2, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class);
        LocalTime expected = LocalTime.of(12, 34, 56, 123456000);
        when(resultSet.getObject(3, LocalTime.class)).thenReturn(expected);
        ResultSetTimeValueReader reader = new MySQLTimeValueReader();
        assertNull(reader.read(resultSet, 1));
        assertNull(reader.read(resultSet, 2));
        assertNull(reader.read(resultSet, 2));
        assertThat(reader.read(resultSet, 3), is(expected));
        verify(resultSet).getObject(2, LocalTime.class);
        verify(resultSet, times(2)).getTime(2);
    }
    
    @Test
    void assertReadWithIsolatedState() throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class);
        ResultSetTimeValueReader reader = new MySQLTimeValueReader();
        assertNull(reader.read(resultSet, 1));
        assertNull(reader.read(resultSet, 1));
        ResultSet nextResultSet = mock(ResultSet.class);
        LocalTime expected = LocalTime.of(12, 34, 56, 123456000);
        when(nextResultSet.getObject(1, LocalTime.class)).thenReturn(expected);
        assertThat(new MySQLTimeValueReader().read(nextResultSet, 1), is(expected));
        verify(resultSet).getObject(1, LocalTime.class);
    }
    
    @Test
    void assertReadTimeWithNonEpochFallback() throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class);
        Time expected = new Time(Timestamp.valueOf("1970-01-02 00:00:00").getTime());
        when(resultSet.getTime(1)).thenReturn(expected);
        assertThat(new MySQLTimeValueReader().read(resultSet, 1), sameInstance(expected));
    }
    
    @Test
    void assertReadTimeAfterFallbackFailure() throws SQLException {
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(SQLFeatureNotSupportedException.class).thenReturn(LocalTime.NOON);
        SQLException expected = new SQLException("foo_time_failure");
        when(resultSet.getTime(1)).thenThrow(expected);
        ResultSetTimeValueReader reader = new MySQLTimeValueReader();
        assertThat(assertThrows(SQLException.class, () -> reader.read(resultSet, 1)), sameInstance(expected));
        assertThat(reader.read(resultSet, 1), is(LocalTime.NOON));
    }
    
    @Test
    void assertReadTimeWithSQLFailure() throws SQLException {
        SQLException expected = new SQLException("foo_time_failure");
        when(resultSet.getObject(1, LocalTime.class)).thenThrow(expected);
        ResultSetTimeValueReader reader = new MySQLTimeValueReader();
        assertThat(assertThrows(SQLException.class, () -> reader.read(resultSet, 1)), sameInstance(expected));
    }
}
