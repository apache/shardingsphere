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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ResultSetGetterAdapterTest {
    
    @Test
    public void assertGetBooleanForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(mockShardingSphereResultSet(mergedResult).getBoolean(1));
    }
    
    @Test
    public void assertGetBooleanForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(mockShardingSphereResultSet(mergedResult).getBoolean("col"));
    }
    
    @Test
    public void assertGetByteForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, byte.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getByte(1), is((byte) 10));
    }
    
    @Test
    public void assertGetByteForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, byte.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getByte("col"), is((byte) 10));
    }
    
    @Test
    public void assertGetShortForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, short.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getShort(1), is((short) 10));
    }
    
    @Test
    public void assertGetShortForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, short.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getShort("col"), is((short) 10));
    }
    
    @Test
    public void assertGetIntForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, int.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getInt(1), is(10));
    }
    
    @Test
    public void assertGetIntForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, int.class)).thenReturn(10);
        assertThat(mockShardingSphereResultSet(mergedResult).getInt("col"), is(10));
    }
    
    @Test
    public void assertGetLongForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, long.class)).thenReturn(10L);
        assertThat(mockShardingSphereResultSet(mergedResult).getLong(1), is(10L));
    }
    
    @Test
    public void assertGetLongForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, long.class)).thenReturn(10L);
        assertThat(mockShardingSphereResultSet(mergedResult).getLong("col"), is(10L));
    }
    
    @Test
    public void assertGetFloatForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, float.class)).thenReturn(10.0F);
        assertThat(mockShardingSphereResultSet(mergedResult).getFloat(1), is(10.0F));
    }
    
    @Test
    public void assertGetFloatForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, float.class)).thenReturn(10.0F);
        assertThat(mockShardingSphereResultSet(mergedResult).getFloat("col"), is(10.0F));
    }
    
    @Test
    public void assertGetDoubleForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, double.class)).thenReturn(10.0D);
        assertThat(mockShardingSphereResultSet(mergedResult).getDouble(1), is(10.0D));
    }
    
    @Test
    public void assertGetDoubleForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, double.class)).thenReturn(10.0D);
        assertThat(mockShardingSphereResultSet(mergedResult).getDouble("col"), is(10.0D));
    }
    
    @Test
    public void assertGetStringForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, String.class)).thenReturn("10");
        assertThat(mockShardingSphereResultSet(mergedResult).getString(1), is("10"));
    }
    
    @Test
    public void assertGetStringForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, String.class)).thenReturn("10");
        assertThat(mockShardingSphereResultSet(mergedResult).getString("col"), is("10"));
    }
    
    @Test
    public void assertGetBigDecimalForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("10"));
        assertThat(mockShardingSphereResultSet(mergedResult).getBigDecimal(1), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBigDecimalForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("10"));
        assertThat(mockShardingSphereResultSet(mergedResult).getBigDecimal("col"), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBigDecimalColumnIndexWithScale() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("10"));
        assertThat(mockShardingSphereResultSet(mergedResult).getBigDecimal(1, 2), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBigDecimalColumnLabelWithScale() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("10"));
        assertThat(mockShardingSphereResultSet(mergedResult).getBigDecimal("col", 2), is(new BigDecimal("10")));
    }
    
    @Test
    public void assertGetBytesForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, byte[].class)).thenReturn(new byte[]{1});
        assertThat(mockShardingSphereResultSet(mergedResult).getBytes(1), is(new byte[]{1}));
    }
    
    @Test
    public void assertGetBytesForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, byte[].class)).thenReturn(new byte[]{1});
        assertThat(mockShardingSphereResultSet(mergedResult).getBytes("col"), is(new byte[]{1}));
    }
    
    @Test
    public void assertGetDateForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getDate(1), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getDate("col"), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateColumnIndexWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getDate(1, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateColumnLabelWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getDate("col", calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetTimeForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTime(1), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTime("col"), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeColumnIndexWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTime(1, calendar), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeColumnLabelWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTime("col", calendar), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimestampForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTimestamp(1), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTimestamp("col"), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampColumnIndexWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTimestamp(1, calendar), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampColumnLabelWithCalendar() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(mockShardingSphereResultSet(mergedResult).getTimestamp("col", calendar), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetAsciiStreamForColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getAsciiStream(1), is(inputStream));
    }
    
    @Test
    public void assertGetAsciiStreamForColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getAsciiStream("col"), is(inputStream));
    }
    
    @Test
    public void assertGetUnicodeStreamForColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getUnicodeStream(1), is(inputStream));
    }
    
    @Test
    public void assertGetUnicodeStreamForColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getUnicodeStream("col"), is(inputStream));
    }
    
    @Test
    public void assertGetBinaryStreamForColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getBinaryStream(1), is(inputStream));
    }
    
    @Test
    public void assertGetBinaryStreamForColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(mockShardingSphereResultSet(mergedResult).getBinaryStream("col"), is(inputStream));
    }
    
    @Test
    public void assertGetCharacterStreamForColumnIndex() throws SQLException {
        Reader reader = mock(Reader.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Reader.class)).thenReturn(reader);
        assertThat(mockShardingSphereResultSet(mergedResult).getCharacterStream(1), is(reader));
    }
    
    @Test
    public void assertGetCharacterStreamForColumnLabel() throws SQLException {
        Reader reader = mock(Reader.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Reader.class)).thenReturn(reader);
        assertThat(mockShardingSphereResultSet(mergedResult).getCharacterStream("col"), is(reader));
    }
    
    @Test
    public void assertGetBlobForColumnIndex() throws SQLException {
        Blob blob = mock(Blob.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(mockShardingSphereResultSet(mergedResult).getBlob(1), is(blob));
    }
    
    @Test
    public void assertGetBlobForColumnLabel() throws SQLException {
        Blob blob = mock(Blob.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(mockShardingSphereResultSet(mergedResult).getBlob("col"), is(blob));
    }
    
    @Test
    public void assertGetClobForColumnIndex() throws SQLException {
        Clob clob = mock(Clob.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(mockShardingSphereResultSet(mergedResult).getClob(1), is(clob));
    }
    
    @Test
    public void assertGetClobForColumnLabel() throws SQLException {
        Clob clob = mock(Clob.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(mockShardingSphereResultSet(mergedResult).getClob("col"), is(clob));
    }
    
    @Test
    public void assertGetURLForColumnIndex() throws SQLException {
        URL url = mock(URL.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, URL.class)).thenReturn(url);
        assertThat(mockShardingSphereResultSet(mergedResult).getURL(1), is(url));
    }
    
    @Test
    public void assertGetURLForColumnLabel() throws SQLException {
        URL url = mock(URL.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, URL.class)).thenReturn(url);
        assertThat(mockShardingSphereResultSet(mergedResult).getURL("col"), is(url));
    }
    
    @Test
    public void assertGetSQLXMLForColumnIndex() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(mockShardingSphereResultSet(mergedResult).getSQLXML(1), is(sqlxml));
    }
    
    @Test
    public void assertGetSQLXMLForColumnLabel() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(mockShardingSphereResultSet(mergedResult).getSQLXML("col"), is(sqlxml));
    }
    
    @Test
    public void assertGetObjectForColumnIndex() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Object.class)).thenReturn("obj");
        assertThat(mockShardingSphereResultSet(mergedResult).getObject(1), is("obj"));
    }
    
    @Test
    public void assertGetObjectForColumnLabel() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.getValue(1, Object.class)).thenReturn("obj");
        assertThat(mockShardingSphereResultSet(mergedResult).getObject("col"), is("obj"));
    }
    
    private ShardingSphereResultSet mockShardingSphereResultSet(final MergedResult mergedResult) throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        return new ShardingSphereResultSet(Collections.singletonList(resultSet), mergedResult, mock(Statement.class), mock(ExecutionContext.class));
    }
}
