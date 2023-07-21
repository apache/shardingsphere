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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JDBCMemoryQueryResultTest {
    
    private final DatabaseType databaseType = new MySQLDatabaseType();
    
    @Test
    void assertConstructorWithSqlException() throws SQLException {
        ResultSet resultSet = mockResultSet();
        when(resultSet.next()).thenThrow(new SQLException(""));
        assertThrows(SQLException.class, () -> new JDBCMemoryQueryResult(resultSet, databaseType));
    }
    
    @Test
    void assertNext() throws SQLException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSet(), databaseType);
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    void assertGetValueByNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.wasNull()).thenReturn(true);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertNull(actual.getValue(1, boolean.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByBoolean() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BOOLEAN);
        when(resultSet.getBoolean(1)).thenReturn(true);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertTrue((boolean) actual.getValue(1, boolean.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByTinyInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TINYINT);
        when(resultSet.getInt(1)).thenReturn(1);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueBySmallInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.SMALLINT);
        when(resultSet.getInt(1)).thenReturn(1);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueBySignedInteger() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.INTEGER);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(true);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByUnsignedInteger() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.INTEGER);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(false);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, int.class), is(1L));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueBySignedBigInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BIGINT);
        when(resultSet.getLong(1)).thenReturn(1L);
        when(resultSet.getMetaData().isSigned(1)).thenReturn(true);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, long.class), is(1L));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByUnsignedBigInt() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BIGINT);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        when(resultSet.getMetaData().isSigned(1)).thenReturn(false);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, long.class), is(new BigDecimal("1").toBigInteger()));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByNumeric() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.NUMERIC);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, BigDecimal.class), is(new BigDecimal("1")));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByDecimal() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DECIMAL);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, BigDecimal.class), is(new BigDecimal("1")));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByFloat() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.FLOAT);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, double.class), is(1.0D));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByDouble() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DOUBLE);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, double.class), is(1.0D));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByChar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.CHAR);
        when(resultSet.getString(1)).thenReturn("value");
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByVarchar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByLongVarchar() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.LONGVARCHAR);
        when(resultSet.getString(1)).thenReturn("value");
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("value"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByDate() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.DATE);
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Date.class), is(new Date(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByTime() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TIME);
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Time.class), is(new Time(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByTimestamp() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.TIMESTAMP);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Timestamp.class), is(new Timestamp(0L)));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByClob() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.CLOB);
        Clob value = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Clob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByBlob() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BLOB);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Blob.class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.BINARY);
        byte[] value = new byte[10];
        when(resultSet.getBytes(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, byte[].class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByVarBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.VARBINARY);
        byte[] value = new byte[10];
        when(resultSet.getBytes(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, byte[].class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByLongVarBinary() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.LONGVARBINARY);
        byte[] value = new byte[10];
        when(resultSet.getBytes(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, byte[].class), is(value));
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueByArray() throws SQLException {
        ResultSet resultSet = getMockedResultSet(Types.ARRAY);
        Array value = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(value);
        JDBCMemoryQueryResult actual = new JDBCMemoryQueryResult(resultSet, databaseType);
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
    void assertGetCalendarValue() throws SQLException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSet(), databaseType);
        queryResult.next();
        assertThat(queryResult.getCalendarValue(1, Integer.class, Calendar.getInstance()), Is.is(1));
    }
    
    @Test
    void assertGetInputStream() throws SQLException, IOException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSet(), databaseType);
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
    void assertWasNullTrue() throws SQLException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSetForWasNull(true), databaseType);
        queryResult.next();
        queryResult.getValue(1, int.class);
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    void assertWasNullFalse() throws SQLException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSetForWasNull(false), databaseType);
        queryResult.next();
        queryResult.getValue(1, int.class);
        assertFalse(queryResult.wasNull());
    }
    
    private ResultSet mockResultSetForWasNull(final boolean wasNull) throws SQLException {
        ResultSet result = getMockedResultSet(Types.INTEGER);
        when(result.getInt(1)).thenReturn(0);
        when(result.getMetaData().isSigned(1)).thenReturn(true);
        when(result.wasNull()).thenReturn(wasNull);
        return result;
    }
    
    @Test
    void assertGetRowCount() throws SQLException {
        JDBCMemoryQueryResult queryResult = new JDBCMemoryQueryResult(mockResultSet(), databaseType);
        assertThat(queryResult.getRowCount(), is(1L));
        queryResult.next();
        assertThat(queryResult.getRowCount(), is(0L));
        queryResult.next();
        assertThat(queryResult.getRowCount(), is(0L));
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
