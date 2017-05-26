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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MockResultSet;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MemoryOrderByResultSetTest {
    
    @Test
    public void assertSort() throws SQLException {
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(Arrays.<ResultSet>asList(new MockResultSet<>(1, 3, 5, 6, 6), new MockResultSet<>(8, 6, 4, 2)), 
                Collections.singletonList(new OrderByContext(1, OrderType.ASC))) { };
        List<Integer> actualList = new ArrayList<>();
        while (rs.next()) {
            actualList.add(rs.getInt(1));
        }
        assertThat(actualList, is(Arrays.asList(1, 2, 3, 4, 5, 6, 6, 6, 8)));
        rs.close();
        assertTrue(rs.isClosed());
        
        rs = new AbstractMemoryOrderByResultSet(Arrays.<ResultSet>asList(new MockResultSet<>(1, 3, 5, 6, 6), new MockResultSet<>(8, 6, 4, 2)), 
                Collections.singletonList(new OrderByContext(1, OrderType.DESC))) { };
        actualList.clear();
        while (rs.next()) {
            actualList.add(rs.getInt("nAmE"));
        }
        assertThat(actualList, is(Arrays.asList(8, 6, 6, 6, 5, 4, 3, 2, 1)));
    }
    
    @Test
    public void assertSortMultiColumn() throws SQLException {
        Map<String, Object> rs1 = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.JANUARY, 11);
        
        rs1.put("name", "name");
        rs1.put("time", cal.getTime());
        rs1.put("id", 11);
        
        Map<String, Object> rs2 = new LinkedHashMap<>();
        cal.set(2016, Calendar.JANUARY, 9);
        rs2.put("name", "dbc");
        rs2.put("time", cal.getTime());
        rs2.put("id", 12);
        
        Map<String, Object> rs3 = new LinkedHashMap<>();
        cal.set(2016, Calendar.JANUARY, 8);
        rs3.put("name", "dbc");
        rs3.put("time", cal.getTime());
        rs3.put("id", 13);
        OrderByContext orderByContext1 = new OrderByContext("name", OrderType.ASC, Optional.<String>absent());
        orderByContext1.setColumnIndex(1);
        OrderByContext orderByContext2 = new OrderByContext("time", OrderType.DESC, Optional.<String>absent());
        orderByContext2.setColumnIndex(2);
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(
                Collections.<ResultSet>singletonList(new MockResultSet<>(Arrays.asList(rs1, rs2, rs3))), Arrays.asList(orderByContext1, orderByContext2)) { };
        List<Map<String, Object>> actualList = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> map = new TreeMap<>();
            map.put("name", rs.getObject("name"));
            map.put("time", rs.getObject("time"));
            map.put("id", rs.getObject("id"));
            actualList.add(map);
        }
        assertThat(actualList, is(Arrays.asList(rs2, rs3, rs1)));
    }
    
    @Test
    public void assertFindColumnSuccess() throws SQLException {
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(Arrays.<ResultSet>asList(new MockResultSet<>(1, 3, 5, 6, 6), new MockResultSet<>(8, 6, 4, 2)), 
                Collections.singletonList(new OrderByContext(1, OrderType.ASC))) { };
        assertThat(rs.findColumn("name"), is(1));
    }
    
    @Test(expected = SQLException.class)
    public void assertFindColumnError() throws SQLException {
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(Arrays.<ResultSet>asList(new MockResultSet<>(1, 3, 5, 6, 6), new MockResultSet<>(8, 6, 4, 2)), 
                Collections.singletonList(new OrderByContext(1, OrderType.ASC))) { };
        rs.findColumn("unknown");
    }
    
    @Test
    public void assertNullValue() throws SQLException {
        Map<String, Object> rs1 = new TreeMap<>();
        rs1.put("name", "name");
        rs1.put("time", null);
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(Collections.<ResultSet>singletonList(new MockResultSet<>(Collections.singletonList(rs1))), 
                Collections.singletonList(new OrderByContext(1, OrderType.ASC))) { };
        assertTrue(rs.next());
        assertThat(rs.getObject(2), nullValue());
        assertTrue(rs.wasNull());
    }
    
    @Test
    public void assertOthers() throws SQLException {
        AbstractMemoryOrderByResultSet rs = new AbstractMemoryOrderByResultSet(Arrays.<ResultSet>asList(new MockResultSet<>(1, 3, 5, 6, 6), new MockResultSet<>(8, 6, 4, 2)), 
                Collections.singletonList(new OrderByContext(1, OrderType.ASC))) { };
        assertTrue(rs.next());
        assertThat(rs.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
        assertThat(rs.getFetchSize(), is(9));
        assertThat(rs.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
        assertThat(rs.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
        rs.clearWarnings();
        assertThat(rs.getWarnings(), nullValue());
        assertThat(rs.getMetaData(), instanceOf(MockResultSet.MockResultSetMetaData.class));
    }
}
