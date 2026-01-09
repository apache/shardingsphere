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

package org.apache.shardingsphere.sqlfederation.resultset;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLFederationResultSetTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private Enumerator<Object> enumerator;
    
    private SQLFederationResultSet federationResultSet;
    
    @BeforeEach
    void setUp() {
        enumerator = createEnumerator();
        federationResultSet = new SQLFederationResultSet(enumerator, mock(SQLFederationSchema.class), createExpandProjections(), databaseType, mock(RelDataType.class), "1");
    }
    
    private List<Projection> createExpandProjections() {
        return Arrays.asList(new ColumnProjection("o", "order_id", null, databaseType),
                new ColumnProjection("o", "user_id", null, databaseType),
                new ColumnProjection("o", "status", null, databaseType),
                new ColumnProjection("i", "item_id", null, databaseType));
    }
    
    @SuppressWarnings("unchecked")
    private Enumerator<Object> createEnumerator() {
        Enumerator<Object> result = mock(Enumerator.class);
        when(result.moveNext()).thenReturn(true, false);
        when(result.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        return result;
    }
    
    @AfterEach
    void clean() {
        enumerator.close();
        federationResultSet.close();
    }
    
    @Test
    void assertNext() {
        assertTrue(federationResultSet.next());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertNextThrowsWillCloseAndRethrow() {
        Enumerator<Object> throwingEnumerator = mock(Enumerator.class);
        when(throwingEnumerator.moveNext()).thenThrow(IllegalStateException.class);
        SQLFederationResultSet resultSet = new SQLFederationResultSet(throwingEnumerator, mock(SQLFederationSchema.class), createExpandProjections(), databaseType, mock(RelDataType.class), "p");
        assertThrows(IllegalStateException.class, resultSet::next);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertNextReturnsFalseWillCompleteProcess() {
        enumerator = mock(Enumerator.class);
        federationResultSet = new SQLFederationResultSet(enumerator, mock(SQLFederationSchema.class), createExpandProjections(), databaseType, mock(RelDataType.class), "p");
        assertFalse(federationResultSet.next());
    }
    
    @Test
    void assertNextHandlesNullCurrent() throws SQLException {
        when(enumerator.current()).thenReturn(null);
        when(enumerator.moveNext()).thenReturn(true, false);
        assertTrue(federationResultSet.next());
        federationResultSet.getObject(1);
        assertTrue(federationResultSet.wasNull());
    }
    
    @Test
    void assertNextHandlesSingleNonArrayValue() throws SQLException {
        when(enumerator.current()).thenReturn("val");
        when(enumerator.moveNext()).thenReturn(true, false);
        assertTrue(federationResultSet.next());
        assertThat(federationResultSet.getString(1), is("val"));
    }
    
    @Test
    void assertNextHandlesByteArrayValue() throws SQLException {
        byte[] bytes = new byte[]{1, 2};
        when(enumerator.current()).thenReturn(bytes);
        when(enumerator.moveNext()).thenReturn(true, false);
        assertTrue(federationResultSet.next());
        assertThat(federationResultSet.getBytes(1), is(bytes));
    }
    
    @Test
    void assertWasNull() {
        assertFalse(federationResultSet.wasNull());
    }
    
    @Test
    void assertGetBooleanWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean(3));
    }
    
    @Test
    void assertGetBooleanWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean("status"));
    }
    
    @Test
    void assertGetBooleanWithColumnLabelCaseInsensitive() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean("STATUS"));
    }
    
    @Test
    void assertGetByteWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(byte) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getByte(1), is((byte) 1));
    }
    
    @Test
    void assertGetByteWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(byte) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getByte("order_id"), is((byte) 1));
    }
    
    @Test
    void assertGetShortWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(short) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getShort(1), is((short) 1));
    }
    
    @Test
    void assertGetShortWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(short) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getShort("order_id"), is((short) 1));
    }
    
    @Test
    void assertGetIntWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getInt(1), is(1));
    }
    
    @Test
    void assertGetIntWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getInt("order_id"), is(1));
    }
    
    @Test
    void assertGetLongWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1L, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getLong(1), is(1L));
    }
    
    @Test
    void assertGetLongWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1L, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getLong("order_id"), is(1L));
    }
    
    @Test
    void assertGetFloatWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0F, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getFloat(1), is(1.0F));
    }
    
    @Test
    void assertGetFloatWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0F, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getFloat("order_id"), is(1.0F));
    }
    
    @Test
    void assertGetDoubleWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0D, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDouble(1), is(1.0D));
    }
    
    @Test
    void assertGetDoubleWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0D, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDouble("order_id"), is(1.0D));
    }
    
    @Test
    void assertGetStringWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getString(3), is("OK"));
    }
    
    @Test
    void assertGetStringWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getString("status"), is("OK"));
    }
    
    @Test
    void assertGetNStringWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getNString(3), is("OK"));
    }
    
    @Test
    void assertGetNStringWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getNString("status"), is("OK"));
    }
    
    @Test
    void assertGetBigDecimalWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal(1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal("order_id"), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal(1, 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal("order_id", 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBytesWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new byte[]{(byte) 1}, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBytes(1), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetBytesWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new byte[]{(byte) 1}, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBytes("order_id"), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetDateWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate(1), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate("order_id"), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate(1, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate("order_id", Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime(1), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime("order_id"), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime(1, Calendar.getInstance()), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime("order_id", Calendar.getInstance()), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimestampWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp(1), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp("order_id"), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp(1, Calendar.getInstance()), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp("order_id", Calendar.getInstance()), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getAsciiStream(1));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getAsciiStream("order_id"));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getUnicodeStream(1));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getUnicodeStream("order_id"));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getBinaryStream(1));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getBinaryStream("order_id"));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Reader.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getCharacterStream(1));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Reader.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getCharacterStream("order_id"));
    }
    
    @Test
    void assertGetBlobWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Blob.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getBlob(1));
    }
    
    @Test
    void assertGetBlobWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Blob.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getBlob("order_id"));
    }
    
    @Test
    void assertGetClobWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Clob.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getClob(1));
    }
    
    @Test
    void assertGetClobWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(Clob.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getClob("order_id"));
    }
    
    @Test
    void assertGetArrayWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Array.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getArray(1), isA(Array.class));
    }
    
    @Test
    void assertGetArrayWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Array.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getArray("order_id"), isA(Array.class));
    }
    
    @Test
    void assertGetArrayNullSetsWasNull() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{null, 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getArray(1);
        assertTrue(federationResultSet.wasNull());
    }
    
    @Test
    void assertGetURLWithColumnIndex() throws SQLException, MalformedURLException {
        when(enumerator.current()).thenReturn(new Object[]{new URL("http://xxx.xxx"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getURL(1), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetURLWithColumnLabel() throws SQLException, MalformedURLException {
        when(enumerator.current()).thenReturn(new Object[]{new URL("http://xxx.xxx"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getURL("order_id"), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetSQLXMLWithColumnIndex() {
        when(enumerator.current()).thenReturn(new Object[]{mock(SQLXML.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getSQLXML(1));
    }
    
    @Test
    void assertGetSQLXMLWithColumnLabel() {
        when(enumerator.current()).thenReturn(new Object[]{mock(SQLXML.class), 1, "OK", 1});
        federationResultSet.next();
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.getSQLXML("order_id"));
    }
    
    @Test
    void assertFindColumnNotFoundThrows() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> federationResultSet.findColumn("missing"));
    }
    
    @Test
    void assertClearAndWarningsAndFetch() {
        federationResultSet.clearWarnings();
        assertThat(federationResultSet.getWarnings(), is((SQLWarning) null));
        federationResultSet.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertThat(federationResultSet.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
        federationResultSet.setFetchSize(10);
        assertThat(federationResultSet.getFetchSize(), is(0));
    }
    
    @Test
    void assertTypeConcurrencyStatementAndClosed() {
        assertThat(federationResultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
        assertThat(federationResultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
        assertThat(federationResultSet.getStatement(), is((Statement) null));
        assertFalse(federationResultSet.isClosed());
        federationResultSet.close();
        assertTrue(federationResultSet.isClosed());
    }
    
    @Test
    void assertGetMetaData() {
        assertThat(federationResultSet.getMetaData(), isA(ResultSetMetaData.class));
    }
    
    @Test
    void assertGetObjectWithIndexAndLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{10, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getObject(1), is(10));
        assertFalse(federationResultSet.wasNull());
        assertThat(federationResultSet.getObject("order_id"), is(10));
    }
    
    @Test
    void assertGetCalendarValueNullSetsWasNull() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{null, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate(1, Calendar.getInstance()), is((Date) null));
        assertTrue(federationResultSet.wasNull());
    }
    
    @Test
    void assertGetValueWithoutColumnTypeConverter() throws SQLException {
        DatabaseType unknownDatabaseType = mock(DatabaseType.class);
        Enumerator<Object> singleRowEnumerator = mock(Enumerator.class);
        when(singleRowEnumerator.moveNext()).thenReturn(true, false);
        when(singleRowEnumerator.current()).thenReturn(new Object[]{10});
        SQLFederationResultSet resultSet = new SQLFederationResultSet(singleRowEnumerator, mock(SQLFederationSchema.class),
                Collections.singletonList(new ColumnProjection("o", "order_id", null, unknownDatabaseType)), unknownDatabaseType, mock(RelDataType.class), "noConverter");
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(10));
        assertFalse(resultSet.wasNull());
        resultSet.close();
    }
}
