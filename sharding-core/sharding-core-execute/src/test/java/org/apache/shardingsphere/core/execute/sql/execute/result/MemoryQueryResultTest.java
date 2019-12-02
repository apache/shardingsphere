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

import lombok.SneakyThrows;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MemoryQueryResultTest {
    
    @Test(expected = SQLException.class)
    @SneakyThrows
    public void assertConstructorWithSqlException() {
        ResultSet resultSet = getResultSet();
        when(resultSet.next()).thenThrow(new SQLException());
        new MemoryQueryResult(resultSet);
    }
    
    @Test
    public void assertNext() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValueWithColumnIndex() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue(1, Integer.class), Is.<Object>is(1L));
    }
    
    @Test
    public void assertGetValueWithColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getValue("order_id", Integer.class), Is.<Object>is(1L));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnIndex() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getCalendarValue(1, Integer.class, Calendar.getInstance()), Is.<Object>is(1L));
    }
    
    @Test
    public void assertGetCalendarValueWithColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertThat(queryResult.getCalendarValue("order_id", Integer.class, Calendar.getInstance()), Is.<Object>is(1L));
    }
    
    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnIndex() {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        InputStream inputStream = queryResult.getInputStream(1, "Unicode");
        assertThat(inputStream.read(), is(getInputStream(1).read()));
    }
    
    @Test
    @SneakyThrows
    public void assertGetInputStreamWithColumnLabel() {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        InputStream inputStream = queryResult.getInputStream("order_id", "Unicode");
        assertThat(inputStream.read(), is(getInputStream(1).read()));
    }
    
    @SneakyThrows
    private InputStream getInputStream(final Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    public void assertIsCaseSensitive() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertFalse(queryResult.isCaseSensitive(1));
    }
    
    @Test
    public void assertGetColumnCount() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertThat(queryResult.getColumnCount(), is(1));
    }
    
    @Test
    public void assertGetColumnLabel() throws SQLException {
        MemoryQueryResult queryResult = new MemoryQueryResult(getResultSet());
        assertThat(queryResult.getColumnLabel(1), is("order_id"));
    }
    
    private ResultSet getResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true).thenReturn(false);
        when(result.getInt(1)).thenReturn(1);
        when(result.wasNull()).thenReturn(false);
        doReturn(getResultSetMetaData()).when(result).getMetaData();
        return result;
    }
    
    private ResultSetMetaData getResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isCaseSensitive(1)).thenReturn(false);
        return result;
    }
}
