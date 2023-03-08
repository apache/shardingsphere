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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    
    @Test
    public void assertGetValueForBlob() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getValue(1, Blob.class));
    }
    
    @Test
    public void assertGetValueForClob() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getValue(1, Clob.class));
    }
    
    @Test
    public void assertGetValueForReader() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getValue(1, Reader.class));
    }
    
    @Test
    public void assertGetValueForInputStream() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getValue(1, InputStream.class));
    }
    
    @Test
    public void assertGetValueForSQLXML() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getValue(1, SQLXML.class));
    }
    
    @Test
    public void assertGetCalendarValue() {
        when(memoryResultSetRow.getCell(1)).thenReturn(new Date(0L));
        assertThat(memoryMergedResult.getCalendarValue(1, Object.class, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    public void assertGetInputStream() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> memoryMergedResult.getInputStream(1, "ascii"));
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(memoryMergedResult.wasNull());
    }
}
