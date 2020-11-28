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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.ExecuteQueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByMemoryMergedResultTest {
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult()), createSelectStatementContext(), null);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(0));
        assertThat(actual.getValue(2, Object.class), is(nullValue()));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
        ExecuteQueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        ExecuteQueryResult queryResult2 = createQueryResult();
        ExecuteQueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), null);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private SelectStatementContext createSelectStatementContext() {
        AggregationProjection aggregationProjection1 = new AggregationProjection(AggregationType.COUNT, "(*)", null);
        aggregationProjection1.setIndex(1);
        AggregationProjection aggregationProjection2 = new AggregationProjection(AggregationType.AVG, "(num)", null);
        aggregationProjection2.setIndex(2);
        AggregationProjection derivedAggregationProjection1 = new AggregationProjection(AggregationType.COUNT, "(num)", "AVG_DERIVED_COUNT_0");
        aggregationProjection2.setIndex(4);
        aggregationProjection2.getDerivedAggregationProjections().add(derivedAggregationProjection1);
        AggregationProjection derivedAggregationProjection2 = new AggregationProjection(AggregationType.SUM, "(num)", "AVG_DERIVED_SUM_0");
        aggregationProjection2.setIndex(5);
        aggregationProjection2.getDerivedAggregationProjections().add(derivedAggregationProjection2);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Arrays.asList(aggregationProjection1, aggregationProjection2));
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        return new SelectStatementContext(selectStatement,
                new GroupByContext(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList()));
    }

    @Test
    public void assertNextForAggregationResultSetsEmpty() throws SQLException {
        ExecuteQueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        ExecuteQueryResult queryResult2 = createQueryResult();
        ExecuteQueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), null);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    private ExecuteQueryResult createQueryResult() throws SQLException {
        ExecuteQueryResult result = mock(ExecuteQueryResult.class);
        when(result.getColumnCount()).thenReturn(5);
        when(result.getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getColumnLabel(3)).thenReturn("id");
        when(result.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
}
