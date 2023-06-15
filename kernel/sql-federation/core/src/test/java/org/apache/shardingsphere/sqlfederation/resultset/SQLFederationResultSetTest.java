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
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLFederationResultSetTest {
    
    private Enumerator<Object> enumerator;
    
    private SQLFederationResultSet federationResultSet;
    
    @BeforeEach
    void setUp() {
        enumerator = createEnumerator();
        federationResultSet = new SQLFederationResultSet(enumerator, mock(ShardingSphereSchema.class), mock(SQLFederationSchema.class), createSelectStatementContext(), mock(RelDataType.class));
    }
    
    private SelectStatementContext createSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        List<Projection> projections = Arrays.asList(new ColumnProjection("o", "order_id", null), new ColumnProjection("o", "user_id", null), new ColumnProjection("o", "status", null),
                new ColumnProjection("i", "item_id", null));
        when(result.getProjectionsContext().getExpandProjections()).thenReturn(projections);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getTableNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Enumerator<Object> createEnumerator() {
        Enumerator<Object> result = Mockito.mock(Enumerator.class);
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
        assertThat(federationResultSet.getArray(1), instanceOf(Array.class));
    }
    
    @Test
    void assertGetArrayWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Array.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getArray("order_id"), instanceOf(Array.class));
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
}
