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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDateBinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertReadWithZeroByte() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> new MySQLDateBinaryProtocolValue().read(payload, false));
    }
    
    @Test
    void assertReadWithFourBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(4, 12, 31);
        when(payload.readInt2()).thenReturn(2018);
        LocalDateTime actual = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload, false)).getTime()), ZoneId.systemDefault());
        assertThat(actual.getYear(), is(2018));
        assertThat(actual.getMonthValue(), is(12));
        assertThat(actual.getDayOfMonth(), is(31));
    }
    
    @Test
    void assertReadWithSevenBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(7, 12, 31, 10, 59, 0);
        when(payload.readInt2()).thenReturn(2018);
        LocalDateTime actual = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload, false)).getTime()), ZoneId.systemDefault());
        assertThat(actual.getYear(), is(2018));
        assertThat(actual.getMonthValue(), is(12));
        assertThat(actual.getDayOfMonth(), is(31));
        assertThat(actual.getHour(), is(10));
        assertThat(actual.getMinute(), is(59));
        assertThat(actual.getSecond(), is(0));
    }
    
    @Test
    void assertReadWithElevenBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(11, 12, 31, 10, 59, 0);
        when(payload.readInt2()).thenReturn(2018);
        when(payload.readInt4()).thenReturn(230000);
        LocalDateTime actual = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload, false)).getTime()), ZoneId.systemDefault());
        assertThat(actual.getYear(), is(2018));
        assertThat(actual.getMonthValue(), is(12));
        assertThat(actual.getDayOfMonth(), is(31));
        assertThat(actual.getHour(), is(10));
        assertThat(actual.getMinute(), is(59));
        assertThat(actual.getSecond(), is(0));
        assertThat(actual.getNano(), is(230000000));
    }
    
    @Test
    void assertReadWithIllegalArgument() {
        when(payload.readInt1()).thenReturn(100);
        assertThrows(SQLFeatureNotSupportedException.class, () -> new MySQLDateBinaryProtocolValue().read(payload, false));
    }
    
    @Test
    void assertWriteLocalDateTimeTypeFourBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        actual.write(payload, LocalDateTime.of(1970, 1, 14, 0, 0, 0));
        verify(payload).writeInt1(4);
        verify(payload).writeInt2(1970);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(14);
    }
    
    @Test
    void assertWriteLocalDateTimeTypeSevenBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        actual.write(payload, LocalDateTime.of(1970, 1, 14, 12, 10, 30));
        verify(payload).writeInt1(7);
        verify(payload).writeInt2(1970);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(14);
        verify(payload).writeInt1(12);
        verify(payload).writeInt1(10);
        verify(payload).writeInt1(30);
    }
    
    @Test
    void assertWriteWithFourBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        actual.write(payload, Timestamp.valueOf("1970-01-14 0:0:0"));
        verify(payload).writeInt1(4);
        verify(payload).writeInt2(1970);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(14);
    }
    
    @Test
    void assertWriteWithSevenBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        actual.write(payload, Timestamp.valueOf("1970-01-14 12:10:30"));
        verify(payload).writeInt1(7);
        verify(payload).writeInt2(1970);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(14);
        verify(payload).writeInt1(12);
        verify(payload).writeInt1(10);
        verify(payload).writeInt1(30);
    }
    
    @Test
    void assertWriteWithElevenBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        actual.write(payload, Timestamp.valueOf("1970-01-14 12:10:30.1"));
        verify(payload).writeInt1(11);
        verify(payload).writeInt2(1970);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(14);
        verify(payload).writeInt1(12);
        verify(payload).writeInt1(10);
        verify(payload).writeInt1(30);
        verify(payload).writeInt4(100000);
    }
}
