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

import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatabaseMetaDataResultSetTest {
    
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
    
    private static final BigDecimal BIG_DECIMAL_SCALA_ONE = BigDecimal.valueOf(12.2);
    
    private static final String INDEX_NAME_COLUMN_LABEL = "INDEX_NAME";
    
    private static final String ACTUAL_INDEX_NAME = "idx_index_test_table_0";
    
    private static final String LOGIC_INDEX_NAME = "idx_index";
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    private DatabaseMetaDataResultSet databaseMetaDataResultSet;
    
    @BeforeEach
    void setUp() throws SQLException, MalformedURLException {
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
        ShardingRule result = mock(ShardingRule.class, RETURNS_DEEP_STUBS);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.findLogicTableByActualTable(ACTUAL_TABLE_NAME)).thenReturn(Optional.of(LOGIC_TABLE_NAME));
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    @Test
    void assertNext() throws SQLException {
        assertTrue(databaseMetaDataResultSet.next());
        assertFalse(databaseMetaDataResultSet.next());
    }
    
    @Test
    void assertClose() throws SQLException {
        assertFalse(databaseMetaDataResultSet.isClosed());
        databaseMetaDataResultSet.close();
        assertTrue(databaseMetaDataResultSet.isClosed());
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(databaseMetaDataResultSet.wasNull());
    }
    
    @Test
    void assertGetStringWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(2), is(Boolean.TRUE.toString()));
        assertThat(databaseMetaDataResultSet.getString(3), is("100"));
        assertThat(databaseMetaDataResultSet.getString(6), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    void assertGetStringWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(NON_TABLE_NAME_COLUMN_LABEL), is(Boolean.TRUE.toString()));
        assertThat(databaseMetaDataResultSet.getString(NUMBER_COLUMN_LABEL), is("100"));
        assertThat(databaseMetaDataResultSet.getString(INDEX_NAME_COLUMN_LABEL), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    void assertGetStringWithLabelCaseInsensitive() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getString(TABLE_NAME_COLUMN_LABEL.toLowerCase()), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getString(NON_TABLE_NAME_COLUMN_LABEL.toLowerCase()), is(Boolean.TRUE.toString()));
        assertThat(databaseMetaDataResultSet.getString(NUMBER_COLUMN_LABEL.toLowerCase()), is("100"));
        assertThat(databaseMetaDataResultSet.getString(INDEX_NAME_COLUMN_LABEL.toLowerCase()), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    void assertGetNStringWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getNString(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getNString(2), is(Boolean.TRUE.toString()));
        assertThat(databaseMetaDataResultSet.getNString(3), is("100"));
        assertThat(databaseMetaDataResultSet.getNString(6), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    void assertGetNStringWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getNString(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getNString(NON_TABLE_NAME_COLUMN_LABEL), is(Boolean.TRUE.toString()));
        assertThat(databaseMetaDataResultSet.getNString(NUMBER_COLUMN_LABEL), is("100"));
        assertThat(databaseMetaDataResultSet.getNString(INDEX_NAME_COLUMN_LABEL), is(LOGIC_INDEX_NAME));
    }
    
    @Test
    void assertGetBooleanWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(2));
    }
    
    @Test
    void assertGetBooleanWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertTrue(databaseMetaDataResultSet.getBoolean(NON_TABLE_NAME_COLUMN_LABEL));
    }
    
    @Test
    void assertGetByteWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(3), is((byte) NUMBER));
    }
    
    @Test
    void assertGetByteWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getByte(NUMBER_COLUMN_LABEL), is((byte) NUMBER));
    }
    
    @Test
    void assertGetShortWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(3), is((short) NUMBER));
    }
    
    @Test
    void assertGetShortWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getShort(NUMBER_COLUMN_LABEL), is((short) NUMBER));
    }
    
    @Test
    void assertGetIntWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(3), is(NUMBER));
    }
    
    @Test
    void assertGetIntWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getInt(NUMBER_COLUMN_LABEL), is(NUMBER));
    }
    
    @Test
    void assertGetLongWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(3), is((long) NUMBER));
    }
    
    @Test
    void assertGetLongWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getLong(NUMBER_COLUMN_LABEL), is((long) NUMBER));
    }
    
    @Test
    void assertGetFloatWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(3), is((float) NUMBER));
    }
    
    @Test
    void assertGetFloatWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getFloat(NUMBER_COLUMN_LABEL), is((float) NUMBER));
    }
    
    @Test
    void assertGetDoubleWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(3), is((double) NUMBER));
    }
    
    @Test
    void assertGetDoubleWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDouble(NUMBER_COLUMN_LABEL), is((double) NUMBER));
    }
    
    @Test
    void assertGetBytesWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(4), is(BYTES));
    }
    
    @Test
    void assertGetBytesWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBytes(BYTES_COLUMN_LABEL), is(BYTES));
    }
    
    @Test
    void assertGetDateWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(5), is(DATE));
    }
    
    @Test
    void assertGetDateWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getDate(DATE_COLUMN_LABEL), is(DATE));
    }
    
    @Test
    void assertGetTimeWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(5), is(new Time(DATE.getTime())));
    }
    
    @Test
    void assertGetTimeWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTime(DATE_COLUMN_LABEL), is(new Time(DATE.getTime())));
    }
    
    @Test
    void assertGetTimestampWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(5), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    void assertGetTimestampWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getTimestamp(DATE_COLUMN_LABEL), is(new Timestamp(DATE.getTime())));
    }
    
    @Test
    void assertGetURLWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getURL(7), is(url));
    }
    
    @Test
    void assertGetURLWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getURL(URL_COLUMN_LABEL), is(url));
    }
    
    @Test
    void assertGetBigDecimalWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(8), is(BIGDECIMAL));
    }
    
    @Test
    void assertGetBigDecimalWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(BIG_DECIMAL_COLUMN_LABEL), is(BIGDECIMAL));
    }
    
    @Test
    void assertGetBigDecimalWithIndexAndScale() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(8, 1), is(BIG_DECIMAL_SCALA_ONE));
    }
    
    @Test
    void assertGetBigDecimalWithLabelAndScale() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getBigDecimal(BIG_DECIMAL_COLUMN_LABEL, 1), is(BIG_DECIMAL_SCALA_ONE));
    }
    
    @Test
    void assertGetMetaData() throws SQLException {
        assertThat(databaseMetaDataResultSet.getMetaData(), is(resultSetMetaData));
    }
    
    @Test
    void assertGetObjectWithIndex() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getObject(1), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(2), is(NON_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(3), is(NUMBER));
    }
    
    @Test
    void assertGetObjectWithLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThat(databaseMetaDataResultSet.getObject(TABLE_NAME_COLUMN_LABEL), is(LOGIC_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(NON_TABLE_NAME_COLUMN_LABEL), is(NON_TABLE_NAME));
        assertThat(databaseMetaDataResultSet.getObject(NUMBER_COLUMN_LABEL), is(NUMBER));
    }
    
    @Test
    void assertFindColumn() throws SQLException {
        assertThat(databaseMetaDataResultSet.findColumn(TABLE_NAME_COLUMN_LABEL), is(1));
        assertThat(databaseMetaDataResultSet.findColumn(NON_TABLE_NAME_COLUMN_LABEL), is(2));
        assertThat(databaseMetaDataResultSet.findColumn(NUMBER_COLUMN_LABEL), is(3));
    }
    
    @Test
    void assertGetType() throws SQLException {
        assertThat(databaseMetaDataResultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertGetConcurrency() throws SQLException {
        assertThat(databaseMetaDataResultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertGetFetchDirection() throws SQLException {
        databaseMetaDataResultSet.setFetchDirection(ResultSet.FETCH_FORWARD);
        assertThat(databaseMetaDataResultSet.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertGetFetchSize() throws SQLException {
        databaseMetaDataResultSet.setFetchSize(3);
        assertThat(databaseMetaDataResultSet.getFetchSize(), is(3));
    }
    
    @Test
    void assertGetObjectOutOfIndexRange() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThrows(SQLException.class, () -> databaseMetaDataResultSet.getObject(9));
    }
    
    @Test
    void assertGetObjectInvalidLabel() throws SQLException {
        databaseMetaDataResultSet.next();
        assertThrows(SQLException.class, () -> databaseMetaDataResultSet.getObject("Invalid"));
    }
    
    @Test
    void assertOperationWithClose() throws SQLException {
        databaseMetaDataResultSet.close();
        assertThrows(SQLException.class, () -> databaseMetaDataResultSet.next());
    }
}
