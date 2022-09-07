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

package org.apache.shardingsphere.sqlfederation.advanced.result;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sqlfederation.advanced.resultset.FederationResultSet;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter.FilterableSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FederationResultSetTest {
    
    private Enumerator<Object[]> enumerator;
    
    private FederationResultSet federationResultSet;
    
    @Before
    public void setUp() throws SQLException {
        enumerator = createEnumerator();
        federationResultSet = new FederationResultSet(enumerator, mock(ShardingSphereSchema.class), mock(FilterableSchema.class), createSelectStatementContext());
    }
    
    private static SelectStatementContext createSelectStatementContext() {
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
    private Enumerator<Object[]> createEnumerator() throws SQLException {
        Enumerator<Object[]> result = mock(Enumerator.class);
        when(result.moveNext()).thenReturn(true, false);
        when(result.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        return result;
    }
    
    @Test
    public void assertNext() throws SQLException {
        assertTrue(federationResultSet.next());
    }
    
    @Test
    public void assertWasNull() {
        assertFalse(federationResultSet.wasNull());
    }
    
    @Test
    public void assertGetBooleanWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean(3));
    }
    
    @Test
    public void assertGetBooleanWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean("status"));
    }
    
    @Test
    public void assertGetBooleanWithColumnLabelCaseInsensitive() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertTrue(federationResultSet.getBoolean("STATUS"));
    }
    
    @Test
    public void assertGetByteWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(byte) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getByte(1), is((byte) 1));
    }
    
    @Test
    public void assertGetByteWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(byte) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getByte("order_id"), is((byte) 1));
    }
    
    @Test
    public void assertGetShortWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(short) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getShort(1), is((short) 1));
    }
    
    @Test
    public void assertGetShortWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{(short) 1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getShort("order_id"), is((short) 1));
    }
    
    @Test
    public void assertGetIntWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getInt(1), is(1));
    }
    
    @Test
    public void assertGetIntWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getInt("order_id"), is(1));
    }
    
    @Test
    public void assertGetLongWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1L, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getLong(1), is(1L));
    }
    
    @Test
    public void assertGetLongWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1L, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getLong("order_id"), is(1L));
    }
    
    @Test
    public void assertGetFloatWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0F, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getFloat(1), is(1.0F));
    }
    
    @Test
    public void assertGetFloatWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0F, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getFloat("order_id"), is(1.0F));
    }
    
    @Test
    public void assertGetDoubleWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0D, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDouble(1), is(1.0D));
    }
    
    @Test
    public void assertGetDoubleWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1.0D, 1, true, 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDouble("order_id"), is(1.0D));
    }
    
    @Test
    public void assertGetStringWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getString(3), is("OK"));
    }
    
    @Test
    public void assertGetStringWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getString("status"), is("OK"));
    }
    
    @Test
    public void assertGetNStringWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getNString(3), is("OK"));
    }
    
    @Test
    public void assertGetNStringWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{1, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getNString("status"), is("OK"));
    }
    
    @Test
    public void assertGetBigDecimalWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal(1), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetBigDecimalWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal("order_id"), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetBigDecimalAndScaleWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal(1, 10), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetBigDecimalAndScaleWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new BigDecimal("1"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBigDecimal("order_id", 10), is(new BigDecimal("1")));
    }
    
    @Test
    public void assertGetBytesWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new byte[]{(byte) 1}, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBytes(1), is(new byte[]{(byte) 1}));
    }
    
    @Test
    public void assertGetBytesWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new byte[]{(byte) 1}, 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBytes("order_id"), is(new byte[]{(byte) 1}));
    }
    
    @Test
    public void assertGetDateWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate(1), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate("order_id"), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate(1, Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    public void assertGetDateAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Date(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getDate("order_id", Calendar.getInstance()), is(new Date(0L)));
    }
    
    @Test
    public void assertGetTimeWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime(1), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime("order_id"), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime(1, Calendar.getInstance()), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimeAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Time(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTime("order_id", Calendar.getInstance()), is(new Time(0L)));
    }
    
    @Test
    public void assertGetTimestampWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp(1), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp("order_id"), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampAndCalendarWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp(1, Calendar.getInstance()), is(new Timestamp(0L)));
    }
    
    @Test
    public void assertGetTimestampAndCalendarWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{new Timestamp(0L), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getTimestamp("order_id", Calendar.getInstance()), is(new Timestamp(0L)));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getAsciiStream(1), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getAsciiStream("order_id"), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getUnicodeStream(1), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getUnicodeStream("order_id"), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBinaryStream(1), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(InputStream.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getBinaryStream("order_id"), instanceOf(InputStream.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Reader.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Reader.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getCharacterStream("order_id");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Blob.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Blob.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getBlob("order_id");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Clob.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Clob.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getClob("order_id");
    }
    
    @Test
    public void assertGetArrayWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Array.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getArray(1), instanceOf(Array.class));
    }
    
    @Test
    public void assertGetArrayWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(Array.class), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getArray("order_id"), instanceOf(Array.class));
    }
    
    @Test
    public void assertGetURLWithColumnIndex() throws SQLException, MalformedURLException {
        when(enumerator.current()).thenReturn(new Object[]{new URL("http://xxx.xxx"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getURL(1), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    public void assertGetURLWithColumnLabel() throws SQLException, MalformedURLException {
        when(enumerator.current()).thenReturn(new Object[]{new URL("http://xxx.xxx"), 1, "OK", 1});
        federationResultSet.next();
        assertThat(federationResultSet.getURL("order_id"), is(new URL("http://xxx.xxx")));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLWithColumnIndex() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(SQLXML.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLWithColumnLabel() throws SQLException {
        when(enumerator.current()).thenReturn(new Object[]{mock(SQLXML.class), 1, "OK", 1});
        federationResultSet.next();
        federationResultSet.getSQLXML("order_id");
    }
    
    @After
    public void clean() throws SQLException {
        enumerator.close();
        federationResultSet.close();
    }
}
