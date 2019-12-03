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

package org.apache.shardingsphere.core.execute.sql.execute.result;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StreamQueryResultTest {
    
    @Test
    public void assertNext() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValue() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue(1, int.class), Is.<Object>is(1));
    }
    
    @Test
    public void assertGetCalendarValueWithDate() throws SQLException {
        ResultSet result = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(result);
        queryResult.next();
        queryResult.getCalendarValue(1, Date.class, calendar);
        verify(result).getDate(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithTime() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Time.class, calendar);
        verify(resultSet).getTime(1, calendar);
    }
    
    @Test
    public void assertGetCalendarValueWithTimestamp() throws SQLException {
        ResultSet resultSet = getResultSet();
        Calendar calendar = Calendar.getInstance();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Timestamp.class, calendar);
        verify(resultSet).getTimestamp(1, calendar);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetCalendarValueWithUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getCalendarValue(1, Object.class, Calendar.getInstance());
    }
    
    @Test
    public void assertGetInputStreamWithAscii() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Ascii");
        verify(resultSet).getAsciiStream(1);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void assertGetInputStreamWithUnicode() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Unicode");
        verify(resultSet).getUnicodeStream(1);
    }
    
    @Test
    public void assertGetInputStreamWithBinary() throws SQLException {
        ResultSet resultSet = getResultSet();
        StreamQueryResult queryResult = new StreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Binary");
        verify(resultSet).getBinaryStream(1);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetInputStreamWithUnsupportedType() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        queryResult.getInputStream(1, "Unsupported Type");
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertFalse(queryResult.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnCount(), Is.is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        StreamQueryResult queryResult = new StreamQueryResult(getResultSet());
        assertThat(queryResult.getColumnLabel(1), Is.is("order_id"));
    }
    
    private ResultSet getResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.wasNull()).thenReturn(false).thenReturn(true);
        doReturn(getResultSetMetaData()).when(resultSet).getMetaData();
        return resultSet;
    }
    
    private ResultSetMetaData getResultSetMetaData() throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("order_id");
        when(metaData.getColumnName(1)).thenReturn("order_id");
        when(metaData.isCaseSensitive(1)).thenReturn(false);
        return metaData;
    }
}
