/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GeneratedKeysResultSetTest {
    
    private static final Statement STATEMENT = Mockito.mock(Statement.class);
    
    private GeneratedKeysResultSet actualResultSet;
    
    static GeneratedKeysResultSet createMock() {
        Map<String, Integer> columnMap = new HashMap<>();
        columnMap.put("order_id", 0);
        columnMap.put("order_no", 1);
        Table<Integer, Integer, Object> valueTable = TreeBasedTable.create();
        valueTable.put(0, 0, 1L);
        valueTable.put(0, 1, "OL_1");
        valueTable.put(1, 0, 2L);
        valueTable.put(1, 1, "OL_2");
        return new GeneratedKeysResultSet(valueTable, columnMap, STATEMENT);
    }
    
    @Before
    public void init() {
        actualResultSet = createMock();
    }
    
    @Test
    public void next() throws Exception {
        assertTrue(actualResultSet.next());
        assertTrue(actualResultSet.next());
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void assertClose() throws Exception {
        actualResultSet.close();
        assertTrue(actualResultSet.isClosed());
        GeneratedKeysResultSet actual = new GeneratedKeysResultSet();
        assertTrue(actual.isClosed());
        assertFalse(actual.next());
    }
    
    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenInvokeClosedResultSet() throws Exception {
        new GeneratedKeysResultSet().getType();
    }
    
    @Test
    public void wasNull() throws Exception {
        assertFalse(actualResultSet.wasNull());
    }
    
    @Test
    public void getString() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getString(2), is("OL_1"));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getString("order_no"), is("OL_2"));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getByte() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getByte(1), is((byte) 1L));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getByte("order_id"), is((byte) 2L));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getShort() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getShort(1), is((short) 1L));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getShort("order_id"), is((short) 2L));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getInt() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getInt(1), is(1));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getInt("order_id"), is(2));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getLong() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getLong(1), is(1L));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getLong("order_id"), is(2L));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getFloat() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getFloat(1), is(1F));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getFloat("order_id"), is(2F));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getDouble() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getDouble(1), is(1D));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getDouble("order_id"), is(2D));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getBigDecimal() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getBigDecimal(1), is(new BigDecimal("1")));
        assertThat(actualResultSet.getBigDecimal(1, 2), is(new BigDecimal("1").setScale(BigDecimal.ROUND_CEILING, BigDecimal.ROUND_HALF_UP)));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getBigDecimal("order_id"), is(new BigDecimal("2")));
        assertThat(actualResultSet.getBigDecimal("order_id", 2), is(new BigDecimal("2").setScale(BigDecimal.ROUND_CEILING, BigDecimal.ROUND_HALF_UP)));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getBytes() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getBytes(2), is("OL_1".getBytes()));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getBytes("order_no"), is("OL_2".getBytes()));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getObject() throws Exception {
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getObject(2), is((Object) "OL_1"));
        assertTrue(actualResultSet.next());
        assertThat(actualResultSet.getObject("order_no"), is((Object) "OL_2"));
        assertFalse(actualResultSet.next());
    }
    
    @Test
    public void getType() throws Exception {
        assertThat(actualResultSet.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void getConcurrency() throws Exception {
        assertThat(actualResultSet.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void getStatement() throws Exception {
        assertThat(actualResultSet.getStatement(), is(STATEMENT));
    }
    
}
