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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class QueryResultUtilTest {
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    @Before
    public void setUp() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
    }
    
    @Test
    public void assertGetValueByBit() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BIT);
        byte[] bytes = {1};
        when(resultSet.getObject(1)).thenReturn(bytes);
        assertThat((byte[]) QueryResultUtil.getValue(resultSet, 1), is(bytes));
    }
    
    @Test
    public void assertGetValueByBoolean() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BOOLEAN);
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertTrue((boolean) QueryResultUtil.getValue(resultSet, 1));
    }
    
    @Test
    public void assertGetValueByTinyint() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.TINYINT);
        when(resultSet.getByte(1)).thenReturn(Byte.MAX_VALUE);
        assertThat((byte) QueryResultUtil.getValue(resultSet, 1), is(Byte.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueBySmallint() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.SMALLINT);
        when(resultSet.getShort(1)).thenReturn(Short.MAX_VALUE);
        assertThat((short) QueryResultUtil.getValue(resultSet, 1), is(Short.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueByInteger() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet.getInt(1)).thenReturn(Integer.MAX_VALUE);
        assertThat((int) QueryResultUtil.getValue(resultSet, 1), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueByBigint() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSet.getLong(1)).thenReturn(Long.MAX_VALUE);
        assertThat((long) QueryResultUtil.getValue(resultSet, 1), is(Long.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueByNumeric() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.NUMERIC);
        when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.TEN);
        assertThat((BigDecimal) QueryResultUtil.getValue(resultSet, 1), is(BigDecimal.TEN));
    }
    
    @Test
    public void assertGetValueByDecimal() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.DECIMAL);
        when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.TEN);
        assertThat((BigDecimal) QueryResultUtil.getValue(resultSet, 1), is(BigDecimal.TEN));
    }
    
    @Test
    public void assertGetValueByFloat() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.FLOAT);
        when(resultSet.getDouble(1)).thenReturn(Double.MAX_VALUE);
        assertThat((double) QueryResultUtil.getValue(resultSet, 1), is(Double.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueByDouble() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.DOUBLE);
        when(resultSet.getDouble(1)).thenReturn(Double.MAX_VALUE);
        assertThat((double) QueryResultUtil.getValue(resultSet, 1), is(Double.MAX_VALUE));
    }
    
    @Test
    public void assertGetValueByChar() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.CHAR);
        when(resultSet.getString(1)).thenReturn("x");
        assertThat((String) QueryResultUtil.getValue(resultSet, 1), is("x"));
    }
    
    @Test
    public void assertGetValueByVarchar() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("xxxxx");
        assertThat((String) QueryResultUtil.getValue(resultSet, 1), is("xxxxx"));
    }
    
    @Test
    public void assertGetValueByLongVarchar() throws SQLException {
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.LONGVARCHAR);
        when(resultSet.getString(1)).thenReturn("xxxxx");
        assertThat((String) QueryResultUtil.getValue(resultSet, 1), is("xxxxx"));
    }
    
    @Test
    public void assertGetValueByBinary() throws SQLException {
        Blob blob = mock(Blob.class);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BINARY);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat((Blob) QueryResultUtil.getValue(resultSet, 1), is(blob));
    }
    
    @Test
    public void assertGetValueByVarBinary() throws SQLException {
        Blob blob = mock(Blob.class);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARBINARY);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat((Blob) QueryResultUtil.getValue(resultSet, 1), is(blob));
    }
    
    @Test
    public void assertGetValueByLongVarBinary() throws SQLException {
        Blob blob = mock(Blob.class);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.LONGVARBINARY);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat((Blob) QueryResultUtil.getValue(resultSet, 1), is(blob));
    }
    
    @Test
    public void assertGetValueByDate() throws SQLException {
        long currentTime = System.currentTimeMillis();
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.DATE);
        when(resultSet.getDate(1)).thenReturn(new Date(currentTime));
        assertThat((Date) QueryResultUtil.getValue(resultSet, 1), is(new Date(currentTime)));
    }
    
    @Test
    public void assertGetValueByTime() throws SQLException {
        long currentTime = System.currentTimeMillis();
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.TIME);
        when(resultSet.getTime(1)).thenReturn(new Time(currentTime));
        assertThat((Time) QueryResultUtil.getValue(resultSet, 1), is(new Time(currentTime)));
    }
    
    @Test
    public void assertGetValueByTimestamp() throws SQLException {
        long currentTime = System.currentTimeMillis();
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(currentTime));
        assertThat((Timestamp) QueryResultUtil.getValue(resultSet, 1), is(new Timestamp(currentTime)));
    }
    
    @Test
    @SneakyThrows
    public void assertGetValueByClob() {
        Clob clob = mock(Clob.class);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.CLOB);
        when(resultSet.getClob(1)).thenReturn(clob);
        assertThat((Clob) QueryResultUtil.getValue(resultSet, 1), is(clob));
    }
    
    @Test
    public void assertGetValueByBlob() throws SQLException {
        Blob blob = mock(Blob.class);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BLOB);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat((Blob) QueryResultUtil.getValue(resultSet, 1), is(blob));
    }
    
    @Test
    public void assertGetValueByOther() throws SQLException {
        Object object = new Object();
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.OTHER);
        when(resultSet.getObject(1)).thenReturn(object);
        assertThat(QueryResultUtil.getValue(resultSet, 1), is(object));
    }
    
    @Test
    public void assertNullResultValue() throws SQLException {
        when(resultSet.getObject(1)).thenReturn(null);
        assertNull(QueryResultUtil.getValue(resultSet, 1));
    }
}
