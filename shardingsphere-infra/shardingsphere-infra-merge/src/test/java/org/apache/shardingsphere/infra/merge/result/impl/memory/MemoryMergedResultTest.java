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

package org.apache.shardingsphere.infra.merge.result.impl.memory;

import org.apache.shardingsphere.infra.merge.result.impl.memory.fixture.TestMemoryMergedResult;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class MemoryMergedResultTest {
    
    private TestMemoryMergedResult memoryMergedResult;
    
    private MemoryQueryResultRow memoryResultSetRow;
    
    @Before
    public void setUp() throws SQLException {
        memoryMergedResult = new TestMemoryMergedResult();
        memoryResultSetRow = memoryMergedResult.getMemoryQueryResultRow();
    }
    
    @Test
    public void assertNext() {
        assertTrue(memoryMergedResult.next());
        assertFalse(memoryMergedResult.next());
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        when(memoryResultSetRow.getCell(1)).thenReturn("1");
        assertThat(memoryMergedResult.getValue(1, Object.class).toString(), is("1"));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueForBlob() throws SQLException {
        memoryMergedResult.getValue(1, Blob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueForClob() throws SQLException {
        memoryMergedResult.getValue(1, Clob.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueForReader() throws SQLException {
        memoryMergedResult.getValue(1, Reader.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueForInputStream() throws SQLException {
        memoryMergedResult.getValue(1, InputStream.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetValueForSQLXML() throws SQLException {
        memoryMergedResult.getValue(1, SQLXML.class);
    }
    
    @Test
    public void assertGetCalendarValue() {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat(memoryMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetInputStream() throws SQLException {
        memoryMergedResult.getInputStream(1, "ascii");
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(memoryMergedResult.wasNull());
    }
}
