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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetTest {
    
    private MergedResult mergeResultSet;
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        mergeResultSet = mock(MergedResult.class);
        shardingSphereResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, getShardingSphereStatement(), createSQLStatementContext());
    }
    
    private SQLStatementContext createSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getTableNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        return Collections.singletonList(resultSet);
    }
    
    private ShardingSphereStatement getShardingSphereStatement() {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(connection.getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
        ShardingSphereStatement result = mock(ShardingSphereStatement.class);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    @Test
    void assertNext() throws SQLException {
        when(mergeResultSet.next()).thenReturn(true);
        assertTrue(shardingSphereResultSet.next());
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(shardingSphereResultSet.wasNull());
    }
    
    @Test
    void assertGetBooleanWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean(1));
    }
    
    @Test
    void assertGetBooleanWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean("label"));
    }
    
    @Test
    void assertGetBooleanWithColumnLabelCaseInsensitive() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean("lABel"));
    }
    
    @Test
    void assertGetByteWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, byte.class)).thenReturn((byte) 1);
        assertThat(shardingSphereResultSet.getByte(1), is((byte) 1));
    }
    
    @Test
    void assertGetByteWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, byte.class)).thenReturn((byte) 1);
        assertThat(shardingSphereResultSet.getByte("label"), is((byte) 1));
    }
    
    @Test
    void assertGetShortWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, short.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getShort(1), is((short) 1));
    }
    
    @Test
    void assertGetShortWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, short.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getShort("label"), is((short) 1));
    }
    
    @Test
    void assertGetIntWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, int.class)).thenReturn(1);
        assertThat(shardingSphereResultSet.getInt(1), is(1));
    }
    
    @Test
    void assertGetIntWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, int.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getInt("label"), is(1));
    }
    
    @Test
    void assertGetLongWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, long.class)).thenReturn(1L);
        assertThat(shardingSphereResultSet.getLong(1), is(1L));
    }
    
    @Test
    void assertGetLongWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, long.class)).thenReturn(1L);
        assertThat(shardingSphereResultSet.getLong("label"), is(1L));
    }
    
    @Test
    void assertGetFloatWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, float.class)).thenReturn(1.0F);
        assertThat(shardingSphereResultSet.getFloat(1), is(1.0F));
    }
    
    @Test
    void assertGetFloatWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, float.class)).thenReturn(1.0F);
        assertThat(shardingSphereResultSet.getFloat("label"), is(1.0F));
    }
    
    @Test
    void assertGetDoubleWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, double.class)).thenReturn(1.0D);
        assertThat(shardingSphereResultSet.getDouble(1), is(1.0D));
    }
    
    @Test
    void assertGetDoubleWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, double.class)).thenReturn(1.0D);
        assertThat(shardingSphereResultSet.getDouble("label"), is(1.0D));
    }
    
    @Test
    void assertGetStringWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        LocalDateTime tempTime = LocalDateTime.of(2022, 12, 14, 0, 0);
        when(mergeResultSet.getValue(2, String.class)).thenReturn(tempTime);
        when(mergeResultSet.getValue(3, String.class)).thenReturn(Timestamp.valueOf(tempTime));
        assertThat(shardingSphereResultSet.getString(1), is("value"));
        assertThat(shardingSphereResultSet.getString(2), is("2022-12-14T00:00"));
        assertThat(shardingSphereResultSet.getString(3), is("2022-12-14 00:00:00.0"));
    }
    
    @Test
    void assertGetStringWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getString("label"), is("value"));
    }
    
    @Test
    void assertGetNStringWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getNString(1), is("value"));
    }
    
    @Test
    void assertGetNStringWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getNString("label"), is("value"));
    }
    
    @Test
    void assertGetBigDecimalWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal(1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal("label"), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal(1, 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal("label", 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBytesWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(new byte[]{(byte) 1});
        assertThat(shardingSphereResultSet.getBytes(1), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetBytesWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(new byte[]{(byte) 1});
        assertThat(shardingSphereResultSet.getBytes("label"), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetDateWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate(1), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateConvertedByLocalDateWithColumnIndex() throws SQLException {
        LocalDate localDate = LocalDate.now();
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(localDate);
        assertThat(shardingSphereResultSet.getDate(1), is(Date.valueOf(localDate)));
    }
    
    @Test
    void assertGetDateWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate("label"), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate(1, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate("label", calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime(1), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime("label"), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime(1, calendar), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime("label", calendar), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimestampWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp(1), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampConvertedByLocalDateWithColumnIndex() throws SQLException {
        LocalDate localDate = LocalDate.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(localDate);
        assertThat(shardingSphereResultSet.getTimestamp(1), is(Timestamp.valueOf(localDate.atStartOfDay())));
    }
    
    @Test
    void assertGetTimestampWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp("label"), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp(1, calendar), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp("label", calendar), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getAsciiStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getAsciiStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getUnicodeStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getUnicodeStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getBinaryStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getBinaryStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnIndex() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergeResultSet.getCharacterStream(1)).thenReturn(reader);
        assertThat(shardingSphereResultSet.getCharacterStream(1), is(reader));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnLabel() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergeResultSet.getCharacterStream(1)).thenReturn(reader);
        assertThat(shardingSphereResultSet.getCharacterStream("label"), is(reader));
    }
    
    @Test
    void assertGetBlobWithColumnIndex() throws SQLException {
        Blob blob = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(shardingSphereResultSet.getBlob(1), is(blob));
    }
    
    @Test
    void assertGetBlobWithColumnLabel() throws SQLException {
        Blob blob = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(shardingSphereResultSet.getBlob("label"), is(blob));
    }
    
    @Test
    void assertGetClobWithColumnIndex() throws SQLException {
        Clob clob = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(shardingSphereResultSet.getClob(1), is(clob));
    }
    
    @Test
    void assertGetClobWithColumnLabel() throws SQLException {
        Clob clob = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(shardingSphereResultSet.getClob("label"), is(clob));
    }
    
    @Test
    void assertGetArrayWithColumnIndex() throws SQLException {
        Array array = mock(Array.class);
        when(mergeResultSet.getValue(1, Array.class)).thenReturn(array);
        assertThat(shardingSphereResultSet.getArray(1), is(array));
    }
    
    @Test
    void assertGetArrayWithColumnLabel() throws SQLException {
        Array array = mock(Array.class);
        when(mergeResultSet.getValue(1, Array.class)).thenReturn(array);
        assertThat(shardingSphereResultSet.getArray("label"), is(array));
    }
    
    @Test
    void assertGetURLWithColumnIndex() throws SQLException, MalformedURLException {
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(new URL("http://xxx.xxx"));
        assertThat(shardingSphereResultSet.getURL(1), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetURLWithColumnLabel() throws SQLException, MalformedURLException {
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(new URL("http://xxx.xxx"));
        assertThat(shardingSphereResultSet.getURL("label"), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetSQLXMLWithColumnIndex() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(mergeResultSet.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(shardingSphereResultSet.getSQLXML(1), is(sqlxml));
    }
    
    @Test
    void assertGetSQLXMLWithColumnLabel() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(mergeResultSet.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(shardingSphereResultSet.getSQLXML("label"), is(sqlxml));
    }
    
    @Test
    void assertGetObjectWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Object.class)).thenReturn("object_value");
        assertThat(shardingSphereResultSet.getObject(1), is("object_value"));
    }
    
    @Test
    void assertGetObjectWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Object.class)).thenReturn("object_value");
        assertThat(shardingSphereResultSet.getObject("label"), is("object_value"));
    }
    
    @Test
    void assertGetObjectWithString() throws SQLException {
        String result = "foo";
        when(mergeResultSet.getValue(1, String.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, String.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBoolean() throws SQLException {
        boolean result = true;
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(result);
        assertTrue(shardingSphereResultSet.getObject(1, boolean.class));
        when(mergeResultSet.getValue(1, Boolean.class)).thenReturn(result);
        assertTrue(shardingSphereResultSet.getObject(1, Boolean.class));
    }
    
    @Test
    void assertGetObjectWithByte() throws SQLException {
        Byte result = (byte) 1;
        when(mergeResultSet.getValue(1, byte.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, byte.class), is(result));
    }
    
    @Test
    void assertGetObjectWithByteArray() throws SQLException {
        byte[] result = new byte[0];
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, byte[].class), is(result));
    }
    
    @Test
    void assertGetObjectWithBigDecimal() throws SQLException {
        BigDecimal result = new BigDecimal("0");
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, BigDecimal.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBigInteger() throws SQLException {
        BigInteger result = BigInteger.valueOf(0L);
        when(mergeResultSet.getValue(1, BigInteger.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, BigInteger.class), is(result));
    }
    
    @Test
    void assertGetObjectWithDouble() throws SQLException {
        double result = 0.0;
        when(mergeResultSet.getValue(1, double.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, double.class), is(result));
        when(mergeResultSet.getValue(1, Double.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Double.class), is(result));
    }
    
    @Test
    void assertGetObjectWithFloat() throws SQLException {
        float result = 0.0F;
        when(mergeResultSet.getValue(1, float.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, float.class), is(result));
        when(mergeResultSet.getValue(1, Float.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Float.class), is(result));
    }
    
    @Test
    void assertGetObjectWithInteger() throws SQLException {
        int result = 0;
        when(mergeResultSet.getValue(1, int.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, int.class), is(result));
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Integer.class), is(result));
    }
    
    @Test
    void assertGetObjectWithLong() throws SQLException {
        long result = 0L;
        when(mergeResultSet.getValue(1, long.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, long.class), is(result));
        when(mergeResultSet.getValue(1, Long.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Long.class), is(result));
    }
    
    @Test
    void assertGetObjectWithShort() throws SQLException {
        short result = 0;
        when(mergeResultSet.getValue(1, short.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, short.class), is(result));
        when(mergeResultSet.getValue(1, Short.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Short.class), is(result));
    }
    
    @Test
    void assertGetObjectWithDate() throws SQLException {
        Date result = mock(Date.class);
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Date.class), is(result));
    }
    
    @Test
    void assertGetObjectWithTime() throws SQLException {
        Time result = mock(Time.class);
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Time.class), is(result));
    }
    
    @Test
    void assertGetObjectWithTimestamp() throws SQLException {
        Timestamp result = mock(Timestamp.class);
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Timestamp.class), is(result));
    }
    
    @Test
    void assertGetObjectWithLocalDateTime() throws SQLException {
        LocalDateTime result = LocalDateTime.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(Timestamp.valueOf(result));
        assertThat(shardingSphereResultSet.getObject(1, LocalDateTime.class), is(result));
    }
    
    @Test
    void assertGetObjectWithOffsetDateTime() throws SQLException {
        OffsetDateTime result = OffsetDateTime.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, OffsetDateTime.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBlob() throws SQLException {
        Blob result = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Blob.class), is(result));
    }
    
    @Test
    void assertGetObjectWithClob() throws SQLException {
        Clob result = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Clob.class), is(result));
    }
    
    @Test
    void assertGetObjectWithRef() throws SQLException {
        Ref result = mock(Ref.class);
        when(mergeResultSet.getValue(1, Ref.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Ref.class), is(result));
    }
    
    @Test
    void assertGetObjectWithURL() throws SQLException {
        URL result = mock(URL.class);
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, URL.class), is(result));
    }
}
