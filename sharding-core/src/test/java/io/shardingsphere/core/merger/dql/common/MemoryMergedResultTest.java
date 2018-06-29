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

package io.shardingsphere.core.merger.dql.common;

import io.shardingsphere.core.merger.dql.common.fixture.TestMemoryMergedResult;
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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MemoryMergedResultTest {
    
    @Mock
    private MemoryQueryResultRow memoryResultSetRow;
    
    private TestMemoryMergedResult memoryMergedResult;
    
    @Before
    public void setUp() {
        Map<String, Integer> labelAndIndexMap = new HashMap<>(1, 1);
        labelAndIndexMap.put("label", 1);
        memoryMergedResult = new TestMemoryMergedResult(labelAndIndexMap);
        memoryMergedResult.setCurrentResultSetRow(memoryResultSetRow);
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn("1");
        assertThat(memoryMergedResult.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForBlob() throws SQLException {
        memoryMergedResult.getValue(1, Blob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForClob() throws SQLException {
        memoryMergedResult.getValue(1, Clob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForReader() throws SQLException {
        memoryMergedResult.getValue(1, Reader.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForInputStream() throws SQLException {
        memoryMergedResult.getValue(1, InputStream.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnIndexForSQLXML() throws SQLException {
        memoryMergedResult.getValue(1, SQLXML.class);
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn("1");
        assertThat(memoryMergedResult.getValue("label", Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForBlob() throws SQLException {
        memoryMergedResult.getValue("label", Blob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForClob() throws SQLException {
        memoryMergedResult.getValue("label", Clob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForReader() throws SQLException {
        memoryMergedResult.getValue("label", Reader.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForInputStream() throws SQLException {
        memoryMergedResult.getValue("label", InputStream.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueWithColumnLabelForSQLXML() throws SQLException {
        memoryMergedResult.getValue("label", SQLXML.class);
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndex() {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat((Date) memoryMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabel() {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat((Date) memoryMergedResult.getCalendarValue("label", Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnIndex() throws SQLException {
        memoryMergedResult.getInputStream(1, "ascii");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStreamWithColumnLabel() throws SQLException {
        memoryMergedResult.getInputStream("label", "ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(memoryMergedResult.wasNull());
    }
}
