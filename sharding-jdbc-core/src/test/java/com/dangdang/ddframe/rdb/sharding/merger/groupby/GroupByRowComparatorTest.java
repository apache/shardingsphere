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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.common.MemoryResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByRowComparatorTest {
    
    @Test
    public void assertCompareToForAscWithOrderByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.ASC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithOrderByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithOrderByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("1", "2"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.DESC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    @Test
    public void assertCompareToForAscWithGroupByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithGroupByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.DESC), new OrderItem(2, OrderType.DESC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithGroupByItems() throws SQLException {
        MemoryResultSetRow o1 = new MemoryResultSetRow(mockResult("1", "2"));
        MemoryResultSetRow o2 = new MemoryResultSetRow(mockResult("1", "2"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderType.ASC), new OrderItem(2, OrderType.DESC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement, OrderType.ASC);
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private ResultSet mockResult(final Object... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(values.length);
        int index = 0;
        for (Object each : values) {
            when(result.getObject(++index)).thenReturn(each);
        }
        return result;
    }
}
