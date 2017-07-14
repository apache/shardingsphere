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

import com.dangdang.ddframe.rdb.sharding.merger.common.fixture.TestMemoryResultSetMerger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MemoryResultSetMergerTest {
    
    @Mock
    private MemoryResultSetRow memoryResultSetRow;
    
    private TestMemoryResultSetMerger memoryResultSetMerger;
    
    @Before
    public void setUp() {
        Map<String, Integer> labelAndIndexMap = new HashMap<>(1, 1);
        labelAndIndexMap.put("label", 1);
        memoryResultSetMerger = new TestMemoryResultSetMerger(labelAndIndexMap);
        memoryResultSetMerger.setCurrentResultSetRow(memoryResultSetRow);
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn("1");
        assertThat(memoryResultSetMerger.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForBlob() throws SQLException {
        memoryResultSetMerger.getValue(1, Blob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForClob() throws SQLException {
        memoryResultSetMerger.getValue(1, Clob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForReader() throws SQLException {
        memoryResultSetMerger.getValue(1, Reader.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForInputStream() throws SQLException {
        memoryResultSetMerger.getValue(1, InputStream.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForSQLXML() throws SQLException {
        memoryResultSetMerger.getValue(1, SQLXML.class);
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn("1");
        assertThat(memoryResultSetMerger.getValue("label", Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForBlob() throws SQLException {
        memoryResultSetMerger.getValue("label", Blob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForClob() throws SQLException {
        memoryResultSetMerger.getValue("label", Clob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForReader() throws SQLException {
        memoryResultSetMerger.getValue("label", Reader.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForInputStream() throws SQLException {
        memoryResultSetMerger.getValue("label", InputStream.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForSQLXML() throws SQLException {
        memoryResultSetMerger.getValue("label", SQLXML.class);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat((Date) memoryResultSetMerger.getCalendarValue(1, Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabel() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat((Date) memoryResultSetMerger.getCalendarValue("label", Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        memoryResultSetMerger.getInputStream(1, "ascii");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        memoryResultSetMerger.getInputStream("label", "ascii");
    }
}
