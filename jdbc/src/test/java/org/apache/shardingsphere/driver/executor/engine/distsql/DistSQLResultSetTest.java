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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.next(), is(false));
    }
    
    @Test
    void assertNextForEmptyResultSet() {
        DistSQLResultSet emptyResultSet = new DistSQLResultSet(Collections.emptyList(), Collections.emptyList(), statement);
        assertThat(emptyResultSet.next(), is(false));
    }
    
    @Test
    void assertClose() {
        resultSet.close();
        assertThat(resultSet.isClosed(), is(true));
    }
    
    @Test
    void assertClosedResultSetThrowsException() {
        resultSet.close();
        assertThrows(IllegalStateException.class, resultSet::getMetaData);
    }
    
    @Test
    void assertGetMetaData() {
        assertThat(resultSet.getMetaData(), is(notNullValue()));
    }
    
    @Test
    void assertWasNull() {
        assertThat(resultSet.next(), is(true));
        resultSet.getString(1);
        assertThat(resultSet.wasNull(), is(false));
    }
    
    @Test
    void assertGetStringByColumnIndex() {
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getString(1), is("row1_name"));
        assertThat(resultSet.getString(2), is("row1_value"));
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getString(1), is("row2_name"));
    }
    
    @Test
    void assertGetStringByColumnLabel() {
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getString("name"), is("row1_name"));
        assertThat(resultSet.getString("value"), is("row1_value"));
    }
    
    @Test
    void assertGetNString() {
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getNString(1), is("row1_name"));
        assertThat(resultSet.getNString("name"), is("row1_name"));
    }
    
    @Test
    void assertGetByte() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("42"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getByte(1), is((byte) 42));
        assertThat(numericResultSet.getByte("num"), is((byte) 42));
    }
    
    @Test
    void assertGetShort() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("1000"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getShort(1), is((short) 1000));
        assertThat(numericResultSet.getShort("num"), is((short) 1000));
    }
    
    @Test
    void assertGetInt() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("100000"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getInt(1), is(100000));
        assertThat(numericResultSet.getInt("num"), is(100000));
    }
    
    @Test
    void assertGetLong() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("9999999999"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getLong(1), is(9999999999L));
        assertThat(numericResultSet.getLong("num"), is(9999999999L));
    }
    
    @Test
    void assertGetFloat() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("3.14"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getFloat(1), is(3.14F));
        assertThat(numericResultSet.getFloat("num"), is(3.14F));
    }
    
    @Test
    void assertGetDouble() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("3.14159265"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getDouble(1), is(3.14159265D));
        assertThat(numericResultSet.getDouble("num"), is(3.14159265D));
    }
    
    @Test
    void assertGetBigDecimal() {
        List<String> columnNames = Collections.singletonList("num");
        List<LocalDataQueryResultRow> rows = Collections.singletonList(new LocalDataQueryResultRow("123.456"));
        DistSQLResultSet numericResultSet = new DistSQLResultSet(columnNames, rows, statement);
        assertThat(numericResultSet.next(), is(true));
        assertThat(numericResultSet.getBigDecimal(1), is(new BigDecimal("123.456")));
        assertThat(numericResultSet.getBigDecimal("num"), is(new BigDecimal("123.456")));
        assertThat(numericResultSet.getBigDecimal(1, 2), is(new BigDecimal("123.456").setScale(2, RoundingMode.HALF_UP)));
        assertThat(numericResultSet.getBigDecimal("num", 2), is(new BigDecimal("123.456").setScale(2, RoundingMode.HALF_UP)));
    }
    
    @Test
    void assertGetBytes() {
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getBytes(1), is("row1_name".getBytes(StandardCharsets.UTF_8)));
        assertThat(resultSet.getBytes("name"), is("row1_name".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertGetObject() {
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getObject(1), is("row1_name"));
        assertThat(resultSet.getObject("name"), is("row1_name"));
    }
    
    @Test
    void assertFindColumn() {
        assertThat(resultSet.findColumn("name"), is(1));
        assertThat(resultSet.findColumn("value"), is(2));
        assertThat(resultSet.findColumn("NAME"), is(1));
    }
    
    @Test
    void assertFindColumnNotFound() {
        assertThrows(IllegalArgumentException.class, () -> resultSet.findColumn("nonexistent"));
    }
    
    @Test
    void assertGetType() {
        assertThat(resultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertGetConcurrency() {
        assertThat(resultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertGetStatement() {
        assertThat(resultSet.getStatement(), is(statement));
    }
    
    @Test
    void assertGetDataBeforeNextThrowsException() {
        assertThrows(NullPointerException.class, () -> resultSet.getString(1));
    }
    
    @Test
    void assertNextReturnsFalseWhenClosed() {
        resultSet.close();
        assertThat(resultSet.next(), is(false));
    }
}
