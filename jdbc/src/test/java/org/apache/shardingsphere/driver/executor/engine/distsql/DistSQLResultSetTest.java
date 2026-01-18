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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DistSQLResultSetTest {
    
    private final Statement statement = mock(Statement.class);
    
    private DistSQLResultSet resultSet;
    
    @BeforeEach
    void setUp() {
        List<String> columnNames = Arrays.asList("name", "value");
        List<LocalDataQueryResultRow> rows = Arrays.asList(
                new LocalDataQueryResultRow("row1_name", "row1_value"),
                new LocalDataQueryResultRow("row2_name", "row2_value"));
        resultSet = new DistSQLResultSet(columnNames, rows, statement);
    }
    
    @Test
    void assertNext() {
        assertTrue(resultSet.next());
        assertTrue(resultSet.next());
        assertFalse(resultSet.next());
    }
    
    @Test
    void assertNextForEmptyResultSet() {
        DistSQLResultSet emptyResultSet = new DistSQLResultSet(Collections.emptyList(), Collections.emptyList(), statement);
        assertFalse(emptyResultSet.next());
    }
    
    @Test
    void assertClose() {
        resultSet.close();
        assertTrue(resultSet.isClosed());
    }
    
    @Test
    void assertClosedResultSetThrowsException() {
        resultSet.close();
        assertThrows(IllegalStateException.class, resultSet::getMetaData);
    }
    
    @Test
    void assertGetMetaData() {
        assertThat(resultSet.getMetaData(), CoreMatchers.is(CoreMatchers.notNullValue()));
    }
    
    @Test
    void assertWasNull() {
        assertTrue(resultSet.next());
        resultSet.getString(1);
        assertFalse(resultSet.wasNull());
    }
    
    @Test
    void assertGetStringByColumnIndex() {
        assertTrue(resultSet.next());
        assertThat(resultSet.getString(1), CoreMatchers.is("row1_name"));
        assertThat(resultSet.getString(2), CoreMatchers.is("row1_value"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getString(1), CoreMatchers.is("row2_name"));
    }
    
    @Test
    void assertGetStringByColumnLabel() {
        assertTrue(resultSet.next());
        assertThat(resultSet.getString("name"), CoreMatchers.is("row1_name"));
        assertThat(resultSet.getString("value"), CoreMatchers.is("row1_value"));
    }
    
    @Test
    void assertGetNString() {
        assertTrue(resultSet.next());
        assertThat(resultSet.getNString(1), CoreMatchers.is("row1_name"));
        assertThat(resultSet.getNString("name"), CoreMatchers.is("row1_name"));
    }
    
    @Test
    void assertGetByte() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("42"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getByte(1), CoreMatchers.is((byte) 42));
        assertThat(numericResultSet.getByte("num"), CoreMatchers.is((byte) 42));
    }
    
    @Test
    void assertGetShort() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("1000"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getShort(1), CoreMatchers.is((short) 1000));
        assertThat(numericResultSet.getShort("num"), CoreMatchers.is((short) 1000));
    }
    
    @Test
    void assertGetInt() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("100000"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getInt(1), CoreMatchers.is(100000));
        assertThat(numericResultSet.getInt("num"), CoreMatchers.is(100000));
    }
    
    @Test
    void assertGetLong() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("9999999999"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getLong(1), CoreMatchers.is(9999999999L));
        assertThat(numericResultSet.getLong("num"), CoreMatchers.is(9999999999L));
    }
    
    @Test
    void assertGetFloat() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("3.14"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getFloat(1), CoreMatchers.is(3.14F));
        assertThat(numericResultSet.getFloat("num"), CoreMatchers.is(3.14F));
    }
    
    @Test
    void assertGetDouble() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("3.14159265"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getDouble(1), CoreMatchers.is(3.14159265D));
        assertThat(numericResultSet.getDouble("num"), CoreMatchers.is(3.14159265D));
    }
    
    @Test
    void assertGetBigDecimal() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("123.456"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertTrue(numericResultSet.next());
        assertThat(numericResultSet.getBigDecimal(1), CoreMatchers.is(new BigDecimal("123.456")));
        assertThat(numericResultSet.getBigDecimal("num"), CoreMatchers.is(new BigDecimal("123.456")));
        assertThat(numericResultSet.getBigDecimal(1, 2), CoreMatchers.is(new BigDecimal("123.456").setScale(2, RoundingMode.HALF_UP)));
        assertThat(numericResultSet.getBigDecimal("num", 2), CoreMatchers.is(new BigDecimal("123.456").setScale(2, RoundingMode.HALF_UP)));
    }
    
    @Test
    void assertGetBytes() {
        assertTrue(resultSet.next());
        assertThat(resultSet.getBytes(1), CoreMatchers.is("row1_name".getBytes(StandardCharsets.UTF_8)));
        assertThat(resultSet.getBytes("name"), CoreMatchers.is("row1_name".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertGetObject() {
        assertTrue(resultSet.next());
        assertThat(resultSet.getObject(1), CoreMatchers.is("row1_name"));
        assertThat(resultSet.getObject("name"), CoreMatchers.is("row1_name"));
    }
    
    @Test
    void assertFindColumn() {
        assertThat(resultSet.findColumn("name"), CoreMatchers.is(1));
        assertThat(resultSet.findColumn("value"), CoreMatchers.is(2));
        assertThat(resultSet.findColumn("NAME"), CoreMatchers.is(1));
    }
    
    @Test
    void assertFindColumnNotFound() {
        assertThrows(IllegalArgumentException.class, () -> resultSet.findColumn("nonexistent"));
    }
    
    @Test
    void assertGetType() {
        assertThat(resultSet.getType(), CoreMatchers.is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertGetConcurrency() {
        assertThat(resultSet.getConcurrency(), CoreMatchers.is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertGetStatement() {
        assertThat(resultSet.getStatement(), CoreMatchers.is(statement));
    }
    
    @Test
    void assertGetDataBeforeNextThrowsException() {
        assertThrows(NullPointerException.class, () -> resultSet.getString(1));
    }
    
    @Test
    void assertNextReturnsFalseWhenClosed() {
        resultSet.close();
        assertFalse(resultSet.next());
    }
}
