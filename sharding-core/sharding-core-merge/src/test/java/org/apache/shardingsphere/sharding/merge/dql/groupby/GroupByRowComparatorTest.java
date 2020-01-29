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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.sql.parser.core.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
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
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)), 
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithOrderByItems() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithOrderByItems() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    @Test
    public void assertCompareToForAscWithGroupByItems() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, OrderDirection.ASC))), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) < 0);
    }
    
    @Test
    public void assertCompareToForDecsWithGroupByItems() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("3", "4"));
        assertTrue(groupByRowComparator.compare(o1, o2) > 0);
    }
    
    @Test
    public void assertCompareToForEqualWithGroupByItems() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC)),
                        createOrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        GroupByRowComparator groupByRowComparator = new GroupByRowComparator(selectSQLStatementContext, caseSensitives);
        MemoryQueryResultRow o1 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        MemoryQueryResultRow o2 = new MemoryQueryResultRow(createQueryResult("1", "2"));
        assertThat(groupByRowComparator.compare(o1, o2), is(0));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    private QueryResult createQueryResult(final Object... values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(values.length);
        QueryResult result = mock(QueryResult.class);
        when(result.getColumnCount()).thenReturn(values.length);
        int index = 0;
        for (Object each : values) {
            when(result.getValue(++index, Object.class)).thenReturn(each);
        }
        return result;
    }
}
