/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.common;

import com.dangdang.ddframe.rdb.sharding.merger.common.fixture.TestStreamResultSetMerger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class StreamResultSetMergerTest {
    
    @Mock
    private ResultSet resultSet;
    
    private TestStreamResultSetMerger streamResultSetMerger;
    
    @Before
    public void setUp() {
        streamResultSetMerger = new TestStreamResultSetMerger();
        streamResultSetMerger.setCurrentResultSet(resultSet);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCurrentResultSetIfNull() throws SQLException {
        streamResultSetMerger.setCurrentResultSet(null);
        streamResultSetMerger.getCurrentResultSet();
    } 
    
    @Test
    public void assertGetValueWithColumnIndexWithObject() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("1");
        assertThat(streamResultSetMerger.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithBoolean() throws SQLException {
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertTrue((Boolean) streamResultSetMerger.getValue(1, boolean.class));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithByte() throws SQLException {
        when(resultSet.getByte(1)).thenReturn((byte) 1);
        assertThat((byte) streamResultSetMerger.getValue(1, byte.class), is((byte) 1));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithShort() throws SQLException {
        when(resultSet.getShort(1)).thenReturn((short) 1);
        assertThat((short) streamResultSetMerger.getValue(1, short.class), is((short) 1));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithInt() throws SQLException {
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat((int) streamResultSetMerger.getValue(1, int.class), is(1));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithLong() throws SQLException {
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat((long) streamResultSetMerger.getValue(1, long.class), is(1L));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithFloat() throws SQLException {
        when(resultSet.getFloat(1)).thenReturn(1F);
        assertThat((float) streamResultSetMerger.getValue(1, float.class), is(1F));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithDouble() throws SQLException {
        when(resultSet.getDouble(1)).thenReturn(1D);
        assertThat((double) streamResultSetMerger.getValue(1, double.class), is(1D));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithString() throws SQLException {
        when(resultSet.getString(1)).thenReturn("1");
        assertThat((String) streamResultSetMerger.getValue(1, String.class), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithBigDecimal() throws SQLException {
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat((BigDecimal) streamResultSetMerger.getValue(1, BigDecimal.class), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithByteArray() throws SQLException {
        when(resultSet.getBytes(1)).thenReturn(new byte[] {(byte) 1});
        assertThat((byte[]) streamResultSetMerger.getValue(1, byte[].class), is(new byte[] {(byte) 1}));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithDate() throws SQLException {
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        assertThat((Date) streamResultSetMerger.getValue(1, Date.class), is(new Date(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithTime() throws SQLException {
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        assertThat((Time) streamResultSetMerger.getValue(1, Time.class), is(new Time(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithTimestamp() throws SQLException {
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        assertThat((Timestamp) streamResultSetMerger.getValue(1, Timestamp.class), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithURL() throws SQLException, MalformedURLException {
        when(resultSet.getURL(1)).thenReturn(new URL("http://xxx.xxx"));
        assertThat((URL) streamResultSetMerger.getValue(1, URL.class), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithBlob() throws SQLException, MalformedURLException {
        Blob blob = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat((Blob) streamResultSetMerger.getValue(1, Blob.class), is(blob));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithClob() throws SQLException, MalformedURLException {
        Clob clob = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(clob);
        assertThat((Clob) streamResultSetMerger.getValue(1, Clob.class), is(clob));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithSQLXML() throws SQLException, MalformedURLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(resultSet.getSQLXML(1)).thenReturn(sqlxml);
        assertThat((SQLXML) streamResultSetMerger.getValue(1, SQLXML.class), is(sqlxml));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithReader() throws SQLException, MalformedURLException {
        Reader reader = mock(Reader.class);
        when(resultSet.getCharacterStream(1)).thenReturn(reader);
        assertThat((Reader) streamResultSetMerger.getValue(1, Reader.class), is(reader));
    }
    
    @Test
    public void assertGetValueWithColumnIndexWithOtherObject() throws SQLException, MalformedURLException {
        when(resultSet.getObject(1)).thenReturn("1");
        assertThat((String) streamResultSetMerger.getValue(1, Collection.class), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithObject() throws SQLException {
        when(resultSet.getObject("label")).thenReturn("1");
        assertThat(streamResultSetMerger.getValue("label", Object.class).toString(), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithBoolean() throws SQLException {
        when(resultSet.getBoolean("label")).thenReturn(true);
        assertTrue((Boolean) streamResultSetMerger.getValue("label", boolean.class));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithByte() throws SQLException {
        when(resultSet.getByte("label")).thenReturn((byte) 1);
        assertThat((byte) streamResultSetMerger.getValue("label", byte.class), is((byte) 1));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithShort() throws SQLException {
        when(resultSet.getShort("label")).thenReturn((short) 1);
        assertThat((short) streamResultSetMerger.getValue("label", short.class), is((short) 1));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithInt() throws SQLException {
        when(resultSet.getInt("label")).thenReturn(1);
        assertThat((int) streamResultSetMerger.getValue("label", int.class), is(1));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithLong() throws SQLException {
        when(resultSet.getLong("label")).thenReturn(1L);
        assertThat((long) streamResultSetMerger.getValue("label", long.class), is(1L));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithFloat() throws SQLException {
        when(resultSet.getFloat("label")).thenReturn(1F);
        assertThat((float) streamResultSetMerger.getValue("label", float.class), is(1F));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithDouble() throws SQLException {
        when(resultSet.getDouble("label")).thenReturn(1D);
        assertThat((double) streamResultSetMerger.getValue("label", double.class), is(1D));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithString() throws SQLException {
        when(resultSet.getString("label")).thenReturn("1");
        assertThat((String) streamResultSetMerger.getValue("label", String.class), is("1"));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithBigDecimal() throws SQLException {
        when(resultSet.getBigDecimal("label")).thenReturn(new BigDecimal("1"));
        assertThat((BigDecimal) streamResultSetMerger.getValue("label", BigDecimal.class), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithByteArray() throws SQLException {
        when(resultSet.getBytes("label")).thenReturn(new byte[] {(byte) 1});
        assertThat((byte[]) streamResultSetMerger.getValue("label", byte[].class), is(new byte[] {(byte) 1}));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithDate() throws SQLException {
        when(resultSet.getDate("label")).thenReturn(new Date(0L));
        assertThat((Date) streamResultSetMerger.getValue("label", Date.class), is(new Date(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithTime() throws SQLException {
        when(resultSet.getTime("label")).thenReturn(new Time(0L));
        assertThat((Time) streamResultSetMerger.getValue("label", Time.class), is(new Time(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithTimestamp() throws SQLException {
        when(resultSet.getTimestamp("label")).thenReturn(new Timestamp(0L));
        assertThat((Timestamp) streamResultSetMerger.getValue("label", Timestamp.class), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithURL() throws SQLException, MalformedURLException {
        when(resultSet.getURL("label")).thenReturn(new URL("http://xxx.xxx"));
        assertThat((URL) streamResultSetMerger.getValue("label", URL.class), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithBlob() throws SQLException, MalformedURLException {
        Blob blob = mock(Blob.class);
        when(resultSet.getBlob("label")).thenReturn(blob);
        assertThat((Blob) streamResultSetMerger.getValue("label", Blob.class), is(blob));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithClob() throws SQLException, MalformedURLException {
        Clob clob = mock(Clob.class);
        when(resultSet.getClob("label")).thenReturn(clob);
        assertThat((Clob) streamResultSetMerger.getValue("label", Clob.class), is(clob));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithSQLXML() throws SQLException, MalformedURLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(resultSet.getSQLXML("label")).thenReturn(sqlxml);
        assertThat((SQLXML) streamResultSetMerger.getValue("label", SQLXML.class), is(sqlxml));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithReader() throws SQLException, MalformedURLException {
        Reader reader = mock(Reader.class);
        when(resultSet.getCharacterStream("label")).thenReturn(reader);
        assertThat((Reader) streamResultSetMerger.getValue("label", Reader.class), is(reader));
    }
    
    @Test
    public void assertGetValueWithColumnLabelWithOtherObject() throws SQLException, MalformedURLException {
        when(resultSet.getObject("label")).thenReturn("1");
        assertThat((String) streamResultSetMerger.getValue("label", Collection.class), is("1"));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexWithDate() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getDate(1, calendar)).thenReturn(new Date(0L));
        assertThat((Date) streamResultSetMerger.getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexWithTime() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getTime(1, calendar)).thenReturn(new Time(0L));
        assertThat((Time) streamResultSetMerger.getCalendarValue(1, Time.class, calendar), is(new Time(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndexWithTimestamp() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getTimestamp(1, calendar)).thenReturn(new Timestamp(0L));
        assertThat((Timestamp) streamResultSetMerger.getCalendarValue(1, Timestamp.class, calendar), is(new Timestamp(0L)));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithColumnIndexWithInvalidType() throws SQLException {
        streamResultSetMerger.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelWithDate() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getDate("label", calendar)).thenReturn(new Date(0L));
        assertThat((Date) streamResultSetMerger.getCalendarValue("label", Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelWithTime() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getTime("label", calendar)).thenReturn(new Time(0L));
        assertThat((Time) streamResultSetMerger.getCalendarValue("label", Time.class, calendar), is(new Time(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabelWithTimestamp() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(resultSet.getTimestamp("label", calendar)).thenReturn(new Timestamp(0L));
        assertThat((Timestamp) streamResultSetMerger.getCalendarValue("label", Timestamp.class, calendar), is(new Timestamp(0L)));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithColumnLabelWithInvalidType() throws SQLException {
        streamResultSetMerger.getCalendarValue("label", Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndexWithAscii() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getAsciiStream(1)).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream(1, "Ascii"), is(inputStream));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithColumnIndexWithUnicode() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getUnicodeStream(1)).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream(1, "Unicode"), is(inputStream));
    }
    
    @Test
    public void assertGetInputStreamWithColumnIndexWithBinary() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getBinaryStream(1)).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream(1, "Binary"), is(inputStream));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithColumnIndexWithInvalidType() throws SQLException {
        streamResultSetMerger.getInputStream(1, "Invalid");
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabelWithAscii() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getAsciiStream("label")).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream("label", "Ascii"), is(inputStream));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithColumnLabelWithUnicode() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getUnicodeStream("label")).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream("label", "Unicode"), is(inputStream));
    }
    
    @Test
    public void assertGetInputStreamWithColumnLabelWithBinary() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(resultSet.getBinaryStream("label")).thenReturn(inputStream);
        assertThat(streamResultSetMerger.getInputStream("label", "Binary"), is(inputStream));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithColumnLabelWithInvalidType() throws SQLException {
        streamResultSetMerger.getInputStream("label", "Invalid");
    }
}
