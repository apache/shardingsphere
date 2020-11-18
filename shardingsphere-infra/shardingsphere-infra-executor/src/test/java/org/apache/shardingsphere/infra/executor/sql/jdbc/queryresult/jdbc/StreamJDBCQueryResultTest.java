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

package org.apache.shardingsphere.infra.executor.sql.jdbc.queryresult.jdbc;

import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.jdbc.StreamJDBCQueryResult;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StreamJDBCQueryResultTest {
    
    @Test
    public void assertNext() throws SQLException {
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValueByBoolean() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertTrue((boolean) new StreamJDBCQueryResult(resultSet).getValue(1, boolean.class));
    }
    
    @Test
    public void assertGetValueByByte() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getByte(1)).thenReturn((byte) 0x00);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, byte.class), is((byte) 0x00));
    }
    
    @Test
    public void assertGetValueByShort() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getShort(1)).thenReturn((short) 1);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, short.class), is((short) 1));
    }
    
    @Test
    public void assertGetValueByInt() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, int.class), is(1));
    }
    
    @Test
    public void assertGetValueByLong() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, long.class), is(1L));
    }
    
    @Test
    public void assertGetValueByFloat() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getFloat(1)).thenReturn(1.0F);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, float.class), is(1.0F));
    }
    
    @Test
    public void assertGetValueByDouble() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, double.class), is(1.0D));
    }
    
    @Test
    public void assertGetValueByString() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("value");
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, String.class), is("value"));
    }
    
    @Test
    public void assertGetValueByBigDecimal() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("0"));
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, BigDecimal.class), is(new BigDecimal("0")));
    }
    
    @Test
    public void assertGetValueByBytes() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        byte[] value = {1};
        when(resultSet.getBytes(1)).thenReturn(value);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, byte[].class), is(value));
    }
    
    @Test
    public void assertGetValueByDate() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Date.class), is(new Date(0L)));
    }
    
    @Test
    public void assertGetValueByTime() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Time.class), is(new Time(0L)));
    }
    
    @Test
    public void assertGetValueByBlob() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Blob.class), is(value));
    }
    
    @Test
    public void assertGetValueByClob() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Clob value = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(value);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Clob.class), is(value));
    }
    
    @Test
    public void assertGetValueByArray() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Array value = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(value);
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Array.class), is(value));
    }
    
    @Test
    public void assertGetValueByTimestamp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        assertThat(new StreamJDBCQueryResult(resultSet).getValue(1, Timestamp.class), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithDate() throws SQLException {
        ResultSet result = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(result);
        queryResult.next();
        queryResult.getCalendarValue(1, Date.class, calendar);
        verify(result).getDate(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithTime() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Time.class, calendar);
        verify(resultSet).getTime(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithTimestamp() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Timestamp.class, calendar);
        verify(resultSet).getTimestamp(1, calendar);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithUnsupportedType() throws SQLException {
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetInputStreamWithAscii() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Ascii");
        verify(resultSet).getAsciiStream(1);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithUnicode() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Unicode");
        verify(resultSet).getUnicodeStream(1);
    }
    
    @Test
    public void assertGetInputStreamWithBinary() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Binary");
        verify(resultSet).getBinaryStream(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithUnsupportedType() throws SQLException {
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream(1, "Unsupported Type");
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        StreamJDBCQueryResult queryResult = new StreamJDBCQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    private ResultSet getResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true).thenReturn(false);
        when(result.getInt(1)).thenReturn(1);
        when(result.wasNull()).thenReturn(false).thenReturn(true);
        return result;
    }
}
