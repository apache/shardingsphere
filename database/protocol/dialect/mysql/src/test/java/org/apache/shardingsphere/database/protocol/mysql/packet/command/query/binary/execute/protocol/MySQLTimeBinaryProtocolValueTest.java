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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimeBinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertReadWithZeroByte() throws SQLException {
        assertThat(new MySQLTimeBinaryProtocolValue().read(payload, false), is(new Timestamp(0L)));
    }
    
    @Test
    void assertReadWithEightBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(8, 0, 10, 59, 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new MySQLTimeBinaryProtocolValue().read(payload, false)).getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test
    void assertReadWithTwelveBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(12, 0, 10, 59, 0);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new MySQLTimeBinaryProtocolValue().read(payload, false)).getTime());
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test
    void assertReadWithIllegalArgument() {
        when(payload.readInt1()).thenReturn(100);
        assertThrows(SQLFeatureNotSupportedException.class, () -> new MySQLTimeBinaryProtocolValue().read(payload, false));
    }
    
    @Test
    void assertWriteWithZeroByte() {
        MySQLTimeBinaryProtocolValue actual = new MySQLTimeBinaryProtocolValue();
        actual.write(payload, Time.valueOf("00:00:00"));
        verify(payload).writeInt1(0);
    }
    
    @Test
    void assertWriteWithEightBytes() {
        MySQLTimeBinaryProtocolValue actual = new MySQLTimeBinaryProtocolValue();
        actual.write(payload, Time.valueOf("01:30:10"));
        verify(payload).writeInt1(8);
        verify(payload).writeInt1(0);
        verify(payload).writeInt4(0);
        payload.writeInt1(0);
        payload.writeInt4(0);
        verify(payload).writeInt1(1);
        verify(payload).writeInt1(30);
        verify(payload).writeInt1(10);
    }
    
    @Test
    void assertWriteWithTwelveBytes() {
        MySQLTimeBinaryProtocolValue actual = new MySQLTimeBinaryProtocolValue();
        actual.write(payload, new Time(1L));
        verify(payload, atLeastOnce()).writeInt1(12);
        verify(payload, times(5)).writeInt1(anyInt());
        verify(payload).writeInt4(0);
        verify(payload).writeInt4(1000000);
    }
}
