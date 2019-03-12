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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLDateBinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertReadWithZeroByte() throws SQLException {
        new MySQLDateBinaryProtocolValue().read(payload);
    }
    
    @Test
    public void assertReadWithFourBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(4, 12, 31);
        when(payload.readInt2()).thenReturn(2018);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload)).getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
    }
    
    @Test
    public void assertReadWithSevenBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(7, 12, 31, 10, 59, 0);
        when(payload.readInt2()).thenReturn(2018);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload)).getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test
    public void assertReadWithElevenBytes() throws SQLException {
        when(payload.readInt1()).thenReturn(11, 12, 31, 10, 59, 0);
        when(payload.readInt2()).thenReturn(2018);
        when(payload.readInt4()).thenReturn(500);
        Calendar actual = Calendar.getInstance();
        actual.setTimeInMillis(((Timestamp) new MySQLDateBinaryProtocolValue().read(payload)).getTime());
        assertThat(actual.get(Calendar.YEAR), is(2018));
        assertThat(actual.get(Calendar.MONTH), is(Calendar.DECEMBER));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), is(31));
        assertThat(actual.get(Calendar.HOUR_OF_DAY), is(10));
        assertThat(actual.get(Calendar.MINUTE), is(59));
        assertThat(actual.get(Calendar.SECOND), is(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReadWithIllegalArgument() throws SQLException {
        when(payload.readInt1()).thenReturn(100);
        new MySQLDateBinaryProtocolValue().read(payload);
    }
    
    @Test
    public void assertWriteWithSevenBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        Timestamp timestamp = new Timestamp(1L);
        timestamp.setNanos(0);
        actual.write(payload, timestamp);
        verify(payload).writeInt1(7);
        verify(payload).writeInt2(1970);
        verify(payload, times(6)).writeInt1(anyInt());
    }
    
    @Test
    public void assertWriteWithElevenBytes() {
        MySQLDateBinaryProtocolValue actual = new MySQLDateBinaryProtocolValue();
        Timestamp timestamp = new Timestamp(1L);
        timestamp.setNanos(1000);
        actual.write(payload, timestamp);
        verify(payload).writeInt1(11);
        verify(payload).writeInt2(1970);
        verify(payload, times(6)).writeInt1(anyInt());
        verify(payload).writeInt4(1000);
    }
}
