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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream;

import org.apache.shardingsphere.database.connector.core.resultset.DialectResultSetMapper;
import org.apache.shardingsphere.database.connector.core.resultset.ResultSetTimeValueReader;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class JDBCStreamQueryResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNext() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        assertTrue(queryResult.next());
        assertFalse(queryResult.next());
    }
    
    @Test
    void assertGetValueByBoolean() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertTrue((boolean) new JDBCStreamQueryResult(resultSet).getValue(1, boolean.class));
    }
    
    @Test
    void assertGetValueByByte() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getByte(1)).thenReturn((byte) 0x00);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, byte.class), is((byte) 0x00));
    }
    
    @Test
    void assertGetValueByShort() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getShort(1)).thenReturn((short) 1);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, short.class), is((short) 1));
    }
    
    @Test
    void assertGetValueByInt() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, int.class), is(1));
    }
    
    @Test
    void assertGetValueByLong() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, long.class), is(1L));
    }
    
    @Test
    void assertGetValueByFloat() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getFloat(1)).thenReturn(1.0F);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, float.class), is(1.0F));
    }
    
    @Test
    void assertGetValueByDouble() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getDouble(1)).thenReturn(1.0D);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, double.class), is(1.0D));
    }
    
    @Test
    void assertGetValueByString() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("value");
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, String.class), is("value"));
    }
    
    @Test
    void assertGetValueByBigDecimal() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("0"));
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, BigDecimal.class), is(new BigDecimal("0")));
    }
    
    @Test
    void assertGetValueByBytes() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        byte[] value = {(byte) 1};
        when(resultSet.getBytes(1)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, byte[].class), is(value));
    }
    
    @Test
    void assertGetValueByDate() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Date.class), is(new Date(0L)));
    }
    
    @Test
    void assertGetValueByTime() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Time.class), is(new Time(0L)));
    }
    
    @Test
    void assertGetValueByLocalTime() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        LocalTime value = LocalTime.of(1, 2, 3);
        when(resultSet.getObject(1, LocalTime.class)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, LocalTime.class), is(value));
    }
    
    @Test
    void assertGetValueByBlob() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Blob value = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Blob.class), is(value));
    }
    
    @Test
    void assertGetValueByClob() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Clob value = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Clob.class), is(value));
    }
    
    @Test
    void assertGetValueBySQLXML() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLXML value = mock(SQLXML.class);
        when(resultSet.getSQLXML(1)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, SQLXML.class), is(value));
    }
    
    @Test
    void assertGetValueByArray() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Array value = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(value);
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Array.class), is(value));
    }
    
    @Test
    void assertGetValueByTimestamp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Timestamp.class), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetValueByZonedDateTime() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1, ZonedDateTime.class)).thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneId.systemDefault()));
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, ZonedDateTime.class), is(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneId.systemDefault())));
    }
    
    @Test
    void assertGetValueByObject() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn("value");
        assertThat(new JDBCStreamQueryResult(resultSet).getValue(1, Object.class), is("value"));
    }
    
    @Test
    void assertGetCalendarValueWithDate() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Calendar calendar = Calendar.getInstance();
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(result);
        queryResult.next();
        queryResult.getCalendarValue(1, Date.class, calendar);
        verify(result).getDate(1, calendar);
    }
    
    @Test
    void assertGetCalendarValueWithTime() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Calendar calendar = Calendar.getInstance();
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Time.class, calendar);
        verify(resultSet).getTime(1, calendar);
    }
    
    @Test
    void assertGetCalendarValueWithTimestamp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Calendar calendar = Calendar.getInstance();
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCalendarValue(1, Timestamp.class, calendar);
        verify(resultSet).getTimestamp(1, calendar);
    }
    
    @Test
    void assertGetCalendarValueWithUnsupportedType() throws SQLException {
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(mock(ResultSet.class));
        queryResult.next();
        assertThrows(SQLException.class, () -> queryResult.getCalendarValue(1, Object.class, Calendar.getInstance()));
    }
    
    @Test
    void assertGetInputStreamWithAscii() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Ascii");
        verify(resultSet).getAsciiStream(1);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    void assertGetInputStreamWithUnicode() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Unicode");
        verify(resultSet).getUnicodeStream(1);
    }
    
    @Test
    void assertGetInputStreamWithBinary() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getInputStream(1, "Binary");
        verify(resultSet).getBinaryStream(1);
    }
    
    @Test
    void assertGetInputStreamWithUnsupportedType() throws SQLException {
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(mock(ResultSet.class));
        queryResult.next();
        assertThrows(SQLException.class, () -> queryResult.getInputStream(1, "Unsupported Type"));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        queryResult.getCharacterStream(1);
        verify(resultSet).getCharacterStream(1);
    }
    
    @Test
    void assertWasNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.wasNull()).thenReturn(false, true);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.next();
        assertFalse(queryResult.wasNull());
        queryResult.next();
        assertTrue(queryResult.wasNull());
    }
    
    @Test
    void assertGetResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.wasNull()).thenReturn(false, true);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        ResultSet actual = queryResult.getResultSet();
        assertNotNull(actual);
        actual.next();
        assertFalse(actual.wasNull());
        actual.next();
        assertTrue(actual.wasNull());
    }
    
    @Test
    void assertGetJDBCResultSetWhenContained() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        assertThat(new JDBCStreamQueryResult(resultSet, true).getJDBCResultSet().orElseThrow(IllegalStateException::new), is(resultSet));
    }
    
    @Test
    void assertGetJDBCResultSetWhenNotContained() throws SQLException {
        assertFalse(new JDBCStreamQueryResult(mock(ResultSet.class)).getJDBCResultSet().isPresent());
    }
    
    @Test
    void assertClose() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JDBCStreamQueryResult queryResult = new JDBCStreamQueryResult(resultSet);
        queryResult.close();
        verify(resultSet).close();
    }
    
    @Test
    void assertGetValueWithDialectTimeReader() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        ResultSetTimeValueReader timeReader = mock(ResultSetTimeValueReader.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        when(dialectMapper.createTimeValueReader()).thenReturn(Optional.of(timeReader));
        LocalTime expected = LocalTime.of(12, 34, 56, 123456000);
        when(timeReader.read(resultSet, 1)).thenReturn(expected);
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Object.class), is(expected));
    }
    
    @Test
    void assertGetNullValueFromDialectTimeReader() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        when(dialectMapper.createTimeValueReader()).thenReturn(Optional.of(mock(ResultSetTimeValueReader.class)));
        assertNull(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Object.class));
        verify(resultSet, never()).getObject(1);
    }
    
    @Test
    void assertGetValueWithoutDialectTimeReader() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        Time expected = new Time(Timestamp.valueOf("1970-01-02 00:00:00").getTime());
        when(resultSet.getObject(1)).thenReturn(expected);
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Object.class), sameInstance(expected));
    }
    
    @Test
    void assertGetValueWithNativeGetter() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTime(1)).thenReturn(Time.valueOf("12:34:56"));
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        when(dialectMapper.createTimeValueReader()).thenReturn(Optional.of(mock(ResultSetTimeValueReader.class)));
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Time.class), is(Time.valueOf("12:34:56")));
    }
    
    @Test
    void assertGetValueWithNonTimeColumn() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getObject(1)).thenReturn("foo_value");
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        when(dialectMapper.createTimeValueReader()).thenReturn(Optional.of(mock(ResultSetTimeValueReader.class)));
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Object.class), is("foo_value"));
    }
    
    @Test
    void assertGetValueWithAssignableHint() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn("foo_value");
        DialectResultSetMapper dialectMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectMapper));
        when(dialectMapper.createTimeValueReader()).thenReturn(Optional.of(mock(ResultSetTimeValueReader.class)));
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType).getValue(1, Comparable.class), is("foo_value"));
    }
    
    @Test
    void assertGetJDBCResultSetWithStorageType() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.empty());
        assertThat(new JDBCStreamQueryResult(resultSet, databaseType, true).getJDBCResultSet().get(), sameInstance(resultSet));
    }
}
