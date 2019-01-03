/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.groupby;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRow;
import io.shardingsphere.core.merger.fixture.TestQueryResult;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByRowComparatorTest {
    
    @Test
    public void assertCompareToForAscWithOrderByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithOrderByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithOrderByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getOrderByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    @Test
    public void assertCompareToForAscWithGroupByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.ASC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithGroupByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithGroupByItems() throws SQLException {
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getGroupByItems().addAll(Arrays.asList(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC), new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC)));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectStatement);
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private QueryResult mockQueryResult(final Object... values) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(values.length);
        int index = 0;
        for (Object each : values) {
            when(result.getObject(++index)).thenReturn(each);
        }
        return new TestQueryResult(result);
    }
}
