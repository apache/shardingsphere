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

package org.apache.shardingsphere.core.merge.dql.groupby;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRow;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByRowComparatorTest {
    
    private final List<Boolean> caseSensitives = Lists.newArrayList(false, false, false);
    
    @Test
    public void assertCompareToForAscWithOrderByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithOrderByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithOrderByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    @Test
    public void assertCompareToForAscWithGroupByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithGroupByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithGroupByItems() throws SQLException {
        ShardingSelectOptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(optimizedStatement, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(mockQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
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
