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
    
    private static final byte[] BYTES = LOGIC_TABLE_NAME.getBytes();
    
    private static final String DATE_COLUMN_LABEL = "DATE";
    
    private static final Date DATE = new Date(System.currentTimeMillis());
    
    private static final String INDEX_NAME_COLUMN_LABEL = "INDEX_NAME";
    
    private static final String ACTUAL_INDEX_NAME = "idx_index_test_table_0";
    
    private static final String LOGIC_INDEX_NAME = "idx_index";
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    private DatabaseMetaDataResultSet databaseMetaDataResultSet;
    
    @Before
    public void setUp() throws Exception {
        mockResultSetMetaData();
        databaseMetaDataResultSet = new DatabaseMetaDataResultSet(mockResultSet(), Collections.singletonList(mockShardingRule()));
    }
    
    private void mockResultSetMetaData() throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(6);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(TABLE_NAME_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(2)).thenReturn(NON_TABLE_NAME_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(3)).thenReturn(NUMBER_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(4)).thenReturn(BYTES_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(5)).thenReturn(DATE_COLUMN_LABEL);
        when(resultSetMetaData.getColumnLabel(6)).thenReturn(INDEX_NAME_COLUMN_LABEL);
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
        when(result.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
        when(result.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
        when(result.next()).thenReturn(true, true, false);
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.findLogicTableByActualTable(ACTUAL_TABLE_NAME)).thenReturn(Optional.of(LOGIC_TABLE_NAME));
        return result;
    }
    
    @Test
    public void assertNext() throws Exception {
        assertTrue(databaseMetaDataResultSet.next());
        assertFalse(databaseMetaDataResultSet.next());
    }
    
    @Test
    public void assertClose() throws Exception {
        assertFalse(databaseMetaDataResultSet.isClosed());
        databaseMetaDataResultSet.close();
        assertTrue(databaseMetaDataResultSet.isClosed());
    }
    
    @Test
    public void assertWasNull() throws Exception {
        assertFalse(databaseMetaDataResultSet.wasNull());
    }
    
    @Test
    public void assertGetStringWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(2), is("true"));
        assertThat(databaseMetaDataResultSet.getString(3), is("100"));
        assertThat(databaseMetaDataResultSet.getString(6), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    public void assertGetStringWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(NON_TABLE_NAME_COLUMN_LABEL), is("true"));
        assertThat(databaseMetaDataResultSet.getString(NUMBER_COLUMN_LABEL), is("100"));
        assertThat(databaseMetaDataResultSet.getString(INDEX_NAME_COLUMN_LABEL), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    public void assertGetBooleanWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(2));
    }
    
    @Test
    public void assertGetBooleanWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(NON_TABLE_NAME_COLUMN_LABEL));
    }
    
    @Test
    public void assertGetByteWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(3), is((byte) NUMBER));
    }
    
    @Test
    public void assertGetByteWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(NUMBER_COLUMN_LABEL), is((byte) NUMBER));
    }
    
    @Test
    public void assertGetShortWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(3), is((short) NUMBER));
    }
    
    @Test
    public void assertGetShortWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(NUMBER_COLUMN_LABEL), is((short) NUMBER));
    }
    
    @Test
    public void assertGetIntWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(3), is(NUMBER));
    }
    
    @Test
    public void assertGetIntWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(NUMBER_COLUMN_LABEL), is(NUMBER));
    }
    
    @Test
    public void assertGetLongWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(3), is((long) NUMBER));
    }
    
    @Test
    public void assertGetLongWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(NUMBER_COLUMN_LABEL), is((long) NUMBER));
    }
    
    @Test
    public void assertGetFloatWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(3), is((float) NUMBER));
    }
    
    @Test
    public void assertGetFloatWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(NUMBER_COLUMN_LABEL), is((float) NUMBER));
    }
    
    @Test
    public void assertGetDoubleWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(3), is((double) NUMBER));
    }
    
    @Test
    public void assertGetDoubleWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(NUMBER_COLUMN_LABEL), is((double) NUMBER));
    }
    
    @Test
    public void assertGetBytesWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(4), is(BYTES));
    }
    
    @Test
    public void assertGetBytesWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(BYTES_COLUMN_LABEL), is(BYTES));
    }
    
    @Test
    public void assertGetDateWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(5), is(DATE));
    }
    
    @Test
    public void assertGetDateWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(DATE_COLUMN_LABEL), is(DATE));
    }
    
    @Test
    public void assertGetTimeWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(5), is(new Time(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimeWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(DATE_COLUMN_LABEL), is(new Time(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimestampWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(5), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    public void assertGetTimestampWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(DATE_COLUMN_LABEL), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    public void assertGetMetaData() throws Exception {
        assertThat(databaseMetaDataResultSet.getMetaData(), is(resultSetMetaData));
    }
    
    @Test
    public void assertGetObjectWithIndex() throws Exception {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getObject(1).equals(LOGIC_TABLE_NAME));
        assertTrue(databaseMetaDataResultSet.getObject(2).equals(NON_TABLE_NAME));
        assertTrue(databaseMetaDataResultSet.getObject(3).equals(NUMBER));
    }
    
    @Test
    public void assertGetObjectWithLabel() throws Exception {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getObject(TABLE_NAME_COLUMN_LABEL).equals(LOGIC_TABLE_NAME));
        assertTrue(databaseMetaDataResultSet.getObject(NON_TABLE_NAME_COLUMN_LABEL).equals(NON_TABLE_NAME));
        assertTrue(databaseMetaDataResultSet.getObject(NUMBER_COLUMN_LABEL).equals(NUMBER));
    }
    
    @Test
    public void assertFindColumn() throws Exception {
        assertThat(databaseMetaDataResultSet.findColumn(TABLE_NAME_COLUMN_LABEL), is(1));
        assertThat(databaseMetaDataResultSet.findColumn(NON_TABLE_NAME_COLUMN_LABEL), is(2));
        assertThat(databaseMetaDataResultSet.findColumn(NUMBER_COLUMN_LABEL), is(3));
    }
    
    @Test
    public void assertGetType() throws Exception {
        assertThat(databaseMetaDataResultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertGetConcurrency() throws Exception {
        assertThat(databaseMetaDataResultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetObjectOutOfIndexRange() throws SQLException {
        databaseMetaDataResultSet.next();
        databaseMetaDataResultSet.getObject(7);
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
