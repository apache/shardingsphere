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

import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseMetaDataResultSetTest {
    
    private static final String TABLE_NAME_COLUMN_LABEL = "TABLE_NAME";
    
    private static final String ACTUAL_TABLE_NAME = "test_table_0";
    
    private static final String LOGIC_TABLE_NAME = "test_table";
    
    private static final String NON_TABLE_NAME_COLUMN_LABEL = "NON_TABLE_NAME";
    
    private static final boolean NON_TABLE_NAME = true;
    
    private static final String NUMBER_COLUMN_LABEL = "NUMBER";
    
    private static final int NUMBER = 100;
    
    private static final String BYTES_COLUMN_LABEL = "BYTES";
    
    private static final byte[] BYTES = LOGIC_TABLE_NAME.getBytes(StandardCharsets.UTF_8);
    
    private static final String DATE_COLUMN_LABEL = "DATE";
    
    private static final Date DATE = new Date(System.currentTimeMillis());
    
    private static final String URL_COLUMN_LABEL = "URL";
    
    private static URL url;
    
    private static final String BIG_DECIMAL_COLUMN_LABEL = "BIGDECIMAL";
    
    private static final BigDecimal BIGDECIMAL = BigDecimal.valueOf(12.22);
    
    private static final BigDecimal BIGDECIMAL_SCALA_ONE = BigDecimal.valueOf(12.2);
    
    private static final String INDEX_NAME_COLUMN_LABEL = "INDEX_NAME";
    
    private static final String ACTUAL_INDEX_NAME = "idx_index_test_table_0";
    
    private static final String LOGIC_INDEX_NAME = "idx_index";
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    private DatabaseMetaDataResultSet databaseMetaDataResultSet;
    
    @Before
    public void setUp() throws SQLException, MalformedURLException {
        url = new URL("http://apache.org/");
        mockResultSetMetaData();
        databaseMetaDataResultSet = new DatabaseMetaDataResultSet(mockResultSet(), Collections.singletonList(mockShardingRule()));
    }
    
    private void mockResultSetMetaData() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(8);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(TABLE_NAME_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(2)).thenReturn(NON_TABLE_NAME_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(3)).thenReturn(NUMBER_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(4)).thenReturn(BYTES_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(5)).thenReturn(DATE_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(6)).thenReturn(INDEX_NAME_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(7)).thenReturn(URL_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(8)).thenReturn(BIG_DECIMAL_COLUMN_LABEL);
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(result.getString(1)).thenReturn(ACTUAL_TABLE_NAME);
        when(result.getObject(2)).thenReturn(NON_TABLE_NAME);
        when(result.getObject(3)).thenReturn(NUMBER);
        when(result.getObject(4)).thenReturn(BYTES);
        when(result.getObject(5)).thenReturn(DATE);
        when(result.getString(6)).thenReturn(ACTUAL_INDEX_NAME);
        when(result.getObject(7)).thenReturn(url);
        when(result.getObject(8)).thenReturn(BIGDECIMAL);
        when(result.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
        when(result.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
        when(result.next()).thenReturn(true, true, false);
        when(result.getFetchDirection()).thenReturn(ResultSet.FETCH_FORWARD);
        when(result.getFetchSize()).thenReturn(3);
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.findLogicTableByActualTable(ACTUAL_TABLE_NAME)).thenReturn(Optional.of(LOGIC_TABLE_NAME));
        return result;
    }
    
    @Test
    public void assertNext() throws SQLException {
        assertTrue(databaseMetaDataResultSet.next());
        assertFalse(databaseMetaDataResultSet.next());
    }
    
    @Test
    public void assertClose() throws SQLException {
        assertFalse(databaseMetaDataResultSet.isClosed());
        databaseMetaDataResultSet.close();
        assertTrue(databaseMetaDataResultSet.isClosed());
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(databaseMetaDataResultSet.wasNull());
    }
    
    @Test
    public void assertGetStringWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(2), is("true"));
        assertThat(databaseMetaDataResultSet.getString(3), is("100"));
        assertThat(databaseMetaDataResultSet.getString(6), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    public void assertGetStringWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(NON_TABLE_NAME_COLUMN_LABEL), is("true"));
        assertThat(databaseMetaDataResultSet.getString(NUMBER_COLUMN_LABEL), is("100"));
        assertThat(databaseMetaDataResultSet.getString(INDEX_NAME_COLUMN_LABEL), is(LOGIC_INDEX_NAME));
    }

    @Test
    public void assertGetNStringWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getNString(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getNString(2), is("true"));
        assertThat(databaseMetaDataResultSet.getNString(3), is("100"));
        assertThat(databaseMetaDataResultSet.getNString(6), is(LOGIC_INDEX_NAME));
    }

    @Test
    public void assertGetNStringWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getNString(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getNString(NON_TABLE_NAME_COLUMN_LABEL), is("true"));
        assertThat(databaseMetaDataResultSet.getNString(NUMBER_COLUMN_LABEL), is("100"));
        assertThat(databaseMetaDataResultSet.getNString(INDEX_NAME_COLUMN_LABEL), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    public void assertGetBooleanWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(2));
    }
    
    @Test
    public void assertGetBooleanWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(NON_TABLE_NAME_COLUMN_LABEL));
    }
    
    @Test
    public void assertGetByteWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(3), is((byte) NUMBER));
    }
    
    @Test
    public void assertGetByteWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(NUMBER_COLUMN_LABEL), is((byte) NUMBER));
    }
    
    @Test
    public void assertGetShortWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(3), is((short) NUMBER));
    }
    
    @Test
    public void assertGetShortWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(NUMBER_COLUMN_LABEL), is((short) NUMBER));
    }
    
    @Test
    public void assertGetIntWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(3), is(NUMBER));
    }
    
    @Test
    public void assertGetIntWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(NUMBER_COLUMN_LABEL), is(NUMBER));
    }
    
    @Test
    public void assertGetLongWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(3), is((long) NUMBER));
    }
    
    @Test
    public void assertGetLongWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(NUMBER_COLUMN_LABEL), is((long) NUMBER));
    }
    
    @Test
    public void assertGetFloatWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(3), is((float) NUMBER));
    }
    
    @Test
    public void assertGetFloatWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(NUMBER_COLUMN_LABEL), is((float) NUMBER));
    }
    
    @Test
    public void assertGetDoubleWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(3), is((double) NUMBER));
    }
    
    @Test
    public void assertGetDoubleWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(NUMBER_COLUMN_LABEL), is((double) NUMBER));
    }
    
    @Test
    public void assertGetBytesWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(4), is(BYTES));
    }
    
    @Test
    public void assertGetBytesWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(BYTES_COLUMN_LABEL), is(BYTES));
    }
    
    @Test
    public void assertGetDateWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(5), is(DATE));
    }
    
    @Test
    public void assertGetDateWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(DATE_COLUMN_LABEL), is(DATE));
    }
    
    @Test
    public void assertGetTimeWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(5), is(new Time(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimeWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(DATE_COLUMN_LABEL), is(new Time(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimestampWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(5), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimestampWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(DATE_COLUMN_LABEL), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    public void assertGetURLWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getURL(7), is(url));
    }
    
    @Test
    public void assertGetURLWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getURL(URL_COLUMN_LABEL), is(url));
    }
    
    @Test
    public void assertGetBigDecimalWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(8), is(BIGDECIMAL));
    }
    
    @Test
    public void assertGetBigDecimalWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(BIG_DECIMAL_COLUMN_LABEL), is(BIGDECIMAL));
    }
    
    @Test
    public void assertGetBigDecimalWithIndexAndScale() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(8, 1), is(BIGDECIMAL_SCALA_ONE));
    }
    
    @Test
    public void assertGetBigDecimalWithLabelAndScale() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(BIG_DECIMAL_COLUMN_LABEL, 1), is(BIGDECIMAL_SCALA_ONE));
    }
    
    @Test
    public void assertGetMetaData() throws SQLException {
        assertThat(databaseMetaDataResultSet.getMetaData(), is(resultSetMetaData));
    }
    
    @Test
    public void assertGetObjectWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getObject(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(2), is(NON_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(3), is(NUMBER));
    }
    
    @Test
    public void assertGetObjectWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getObject(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(NON_TABLE_NAME_COLUMN_LABEL), is(NON_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(NUMBER_COLUMN_LABEL), is(NUMBER));
    }
    
    @Test
    public void assertFindColumn() throws SQLException {
        assertThat(databaseMetaDataResultSet.findColumn(TABLE_NAME_COLUMN_LABEL), is(1));
        assertThat(databaseMetaDataResultSet.findColumn(NON_TABLE_NAME_COLUMN_LABEL), is(2));
        assertThat(databaseMetaDataResultSet.findColumn(NUMBER_COLUMN_LABEL), is(3));
    }
    
    @Test
    public void assertGetType() throws SQLException {
        assertThat(databaseMetaDataResultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertGetConcurrency() throws SQLException {
        assertThat(databaseMetaDataResultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertGetFetchDirection() throws SQLException {
        databaseMetaDataResultSet.setFetchDirection(ResultSet.FETCH_FORWARD);
        assertThat(databaseMetaDataResultSet.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    public void assertGetFetchSize() throws SQLException {
        databaseMetaDataResultSet.setFetchSize(3);
        assertThat(databaseMetaDataResultSet.getFetchSize(), is(3));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetObjectOutOfIndexRange() throws SQLException {
        databaseMetaDataResultSet.next();
        databaseMetaDataResultSet.getObject(9);
    }
    
    @Test(expected = SQLException.class)
    public void assertGetObjectInvalidLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        databaseMetaDataResultSet.getObject("Invalid");
    }
    
    @Test(expected = SQLException.class)
    public void assertOperationWithClose() throws SQLException {
        databaseMetaDataResultSet.close();
        databaseMetaDataResultSet.next();
        databaseMetaDataResultSet.getObject(1);
    }
}
