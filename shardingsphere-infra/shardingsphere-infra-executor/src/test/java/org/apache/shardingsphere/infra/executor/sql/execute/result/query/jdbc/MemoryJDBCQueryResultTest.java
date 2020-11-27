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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.jdbc;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MemoryJDBCQueryResultTest {
    
    @Test(expected = SQLException.class)
    public void assertConstructorWithSqlException() throws SQLException {
        ResultSet resultSet = mockResultSet();
        when(resultSet.next()).thenThrow(new SQLException(""));
        new MemoryJDBCExecuteQueryResult(resultSet);
    }
    
    @Test
    public void assertNext() throws SQLException {
        MemoryJDBCExecuteQueryResult queryResult = new MemoryJDBCExecuteQueryResult(mockResultSet());
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    public void assertGetValueByNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.wasNull()).thenReturn(true);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertNull(actual.getValue(1, boolean.class));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByBoolean() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BOOLEAN);
        when(resultSet.getBoolean(1)).thenReturn(true);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertTrue((boolean) actual.getValue(1, boolean.class));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByTinyInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TINYINT);
        when(resultSet.getInt(1)).thenReturn(1);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueBySmallInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.SMALLINT);
        when(resultSet.getInt(1)).thenReturn(1);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueBySignedInteger() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.INTEGER);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(true);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByUnsignedInteger() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.INTEGER);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(false);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1L));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueBySignedBigInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BIGINT);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(true);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, long.class), is(1L));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByUnsignedBigInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BIGINT);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        when(resultSet.getMetaData().isSigned(1)).thenReturn(false);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, long.class), is(new BigDecimal("1").toBigInteger()));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByNumeric() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.NUMERIC);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, BigDecimal.class), is(new BigDecimal("1")));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByDecimal() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DECIMAL);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, BigDecimal.class), is(new BigDecimal("1")));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByFloat() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.FLOAT);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, double.class), is(1.0D));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByDouble() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DOUBLE);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, double.class), is(1.0D));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByChar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.CHAR);
        when(resultSet.getString(1)).thenReturn("value");
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByVarchar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByLongVarchar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.LONGVARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByDate() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DATE);
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Date.class), is(new Date(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByTime() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TIME);
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Time.class), is(new Time(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByTimestamp() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TIMESTAMP);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Timestamp.class), is(new Timestamp(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByClob() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.CLOB);
        Clob value = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Clob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByBlob() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BLOB);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Blob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BINARY);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Blob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByVarBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.VARBINARY);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Blob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByLongVarBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.LONGVARBINARY);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Blob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertGetValueByArray() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.ARRAY);
        Array value = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(value);
        MemoryJDBCExecuteQueryResult actual = new MemoryJDBCExecuteQueryResult(resultSet);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Array.class), is(value));
        assertFalse(actual.next());
    }
    
    private ResultSet getMockedResultSet(final int columnTypes) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnType(1)).thenReturn(columnTypes);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        return resultSet;
    }
    
    @Test
    public void assertGetCalendarValue() throws SQLException {
        MemoryJDBCExecuteQueryResult queryResult = new MemoryJDBCExecuteQueryResult(mockResultSet());
        queryResult.next();
        assertThat(queryResult.getCalendarValue(1, Integer.class, Calendar.getInstance()), Is.is(1));
    }
    
    @Test
    public void assertGetInputStream() throws SQLException, IOException {
        MemoryJDBCExecuteQueryResult queryResult = new MemoryJDBCExecuteQueryResult(mockResultSet());
        queryResult.next();
        InputStream inputStream = queryResult.getInputStream(1, "Unicode");
        assertThat(inputStream.read(), is(getInputStream(1).read()));
    }
    
    private InputStream getInputStream(final Object value) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        MemoryJDBCExecuteQueryResult queryResult = new MemoryJDBCExecuteQueryResult(mockResultSet());
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true).thenReturn(false);
        when(result.getInt(1)).thenReturn(1);
        when(result.wasNull()).thenReturn(false);
        doReturn(mockResultSetMetaData()).when(result).getMetaData();
        return result;
    }
    
    private ResultSetMetaData mockResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        return result;
    }
}
