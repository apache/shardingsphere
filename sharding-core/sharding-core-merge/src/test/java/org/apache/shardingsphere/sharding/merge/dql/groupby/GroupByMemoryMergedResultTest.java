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

import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.sharding.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.core.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergedResult;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByMemoryMergedResultTest {
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        DQLMergeEngine mergeEngine = new DQLMergeEngine(
                DatabaseTypes.getActualDatabaseType("MySQL"), createSelectSQLStatementContext(), Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
        QueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        QueryResult queryResult2 = createQueryResult();
        QueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), createSelectSQLStatementContext(), Arrays.asList(queryResult1, queryResult2, queryResult3));
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertThat((BigDecimal) actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat((Integer) actual.getValue(3, Object.class), is(3));
        assertThat((BigDecimal) actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat((BigDecimal) actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat((BigDecimal) actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat((Integer) actual.getValue(3, Object.class), is(2));
        assertThat((BigDecimal) actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat((BigDecimal) actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private SelectSQLStatementContext createSelectSQLStatementContext() {
        AggregationProjection aggregationSelectItem1 = new AggregationProjection(AggregationType.COUNT, "(*)", null);
        aggregationSelectItem1.setIndex(1);
        AggregationProjection aggregationSelectItem2 = new AggregationProjection(AggregationType.AVG, "(num)", null);
        aggregationSelectItem2.setIndex(2);
        AggregationProjection derivedAggregationSelectItem1 = new AggregationProjection(AggregationType.COUNT, "(num)", "AVG_DERIVED_COUNT_0");
        aggregationSelectItem2.setIndex(4);
        aggregationSelectItem2.getDerivedAggregationProjections().add(derivedAggregationSelectItem1);
        AggregationProjection derivedAggregationSelectItem2 = new AggregationProjection(AggregationType.SUM, "(num)", "AVG_DERIVED_SUM_0");
        aggregationSelectItem2.setIndex(5);
        aggregationSelectItem2.getDerivedAggregationProjections().add(derivedAggregationSelectItem2);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Arrays.<Projection>asList(aggregationSelectItem1, aggregationSelectItem2), Collections.<String>emptyList());
        return new SelectSQLStatementContext(new SelectStatement(),
                new GroupByContext(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList()));
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.getColumnCount()).thenReturn(5);
        when(result.getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getColumnLabel(3)).thenReturn("id");
        when(result.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
}
