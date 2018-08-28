/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute;

import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BinaryProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertReadStringLenenc() {
        when(payload.readStringLenenc()).thenReturn("value");
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_STRING, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_VARCHAR, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_VAR_STRING, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_ENUM, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_SET, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG_BLOB, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_MEDIUM_BLOB, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_BLOB, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY_BLOB, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_GEOMETRY, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_BIT, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DECIMAL, payload).read(), is((Object) "value"));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_NEWDECIMAL, payload).read(), is((Object) "value"));
    }
    
    @Test
    public void assertReadInt8() {
        when(payload.readInt8()).thenReturn(1L);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONGLONG, payload).read(), is((Object) 1L));
    }
    
    @Test
    public void assertReadInt4() {
        when(payload.readInt4()).thenReturn(1);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG, payload).read(), is((Object) 1));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_INT24, payload).read(), is((Object) 1));
    }
    
    @Test
    public void assertReadInt2() {
        when(payload.readInt2()).thenReturn(1);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_SHORT, payload).read(), is((Object) 1));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_YEAR, payload).read(), is((Object) 1));
    }
    
    @Test
    public void assertReadInt1() {
        when(payload.readInt1()).thenReturn(1);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY, payload).read(), is((Object) 1));
    }
    
    @Test
    public void assertReadDouble() {
        when(payload.readDouble()).thenReturn(1d);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DOUBLE, payload).read(), is((Object) 1d));
    }
    
    @Test
    public void assertReadFloat() {
        when(payload.readFloat()).thenReturn(1f);
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_FLOAT, payload).read(), is((Object) 1f));
    }
    
    @Test
    public void assertReadDate() {
        when(payload.readDate()).thenReturn(new Timestamp(0L));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DATE, payload).read(), is((Object) new Timestamp(0L)));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DATETIME, payload).read(), is((Object) new Timestamp(0L)));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TIMESTAMP, payload).read(), is((Object) new Timestamp(0L)));
    }
    
    @Test
    public void assertReadTime() {
        when(payload.readTime()).thenReturn(new Timestamp(0L));
        assertThat(new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TIME, payload).read(), is((Object) new Timestamp(0L)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertReadUnsupported() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_NULL, payload).read();
    }
    
    @Test
    public void assertWriteStringLenenc() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_STRING, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_VARCHAR, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_VAR_STRING, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_ENUM, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_SET, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG_BLOB, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_MEDIUM_BLOB, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_BLOB, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY_BLOB, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_GEOMETRY, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_BIT, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DECIMAL, payload).write("value");
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_NEWDECIMAL, payload).write("value");
        verify(payload, times(13)).writeStringLenenc("value");
    }
    
    @Test
    public void assertWriteInt8() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONGLONG, payload).write(1L);
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONGLONG, payload).write(new BigDecimal(1L));
        verify(payload, times(2)).writeInt8(1L);
    }
    
    @Test
    public void assertWriteInt4() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_LONG, payload).write(1);
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_INT24, payload).write(1);
        verify(payload, times(2)).writeInt4(1);
    }
    
    @Test
    public void assertWriteInt2() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_SHORT, payload).write(1);
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_YEAR, payload).write(1);
        verify(payload, times(2)).writeInt2(1);
    }
    
    @Test
    public void assertWriteInt1() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TINY, payload).write(1);
        verify(payload).writeInt1(1);
    }
    
    @Test
    public void assertWriteDouble() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DOUBLE, payload).write(1d);
        verify(payload).writeDouble(1d);
    }
    
    @Test
    public void assertWriteFloat() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_FLOAT, payload).write(1f);
        verify(payload).writeFloat(1f);
    }
    
    @Test
    public void assertWriteDate() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DATE, payload).write(new Timestamp(0L));
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_DATETIME, payload).write(new Timestamp(0L));
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TIMESTAMP, payload).write(new Timestamp(0L));
        verify(payload, times(3)).writeDate(new Timestamp(0L));
    }
    
    @Test
    public void assertWriteTime() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_TIME, payload).write(new Timestamp(0L));
        verify(payload).writeTime(new Timestamp(0L));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertWriteUnsupported() {
        new BinaryProtocolValue(ColumnType.MYSQL_TYPE_NULL, payload).write(payload);
    }
}
