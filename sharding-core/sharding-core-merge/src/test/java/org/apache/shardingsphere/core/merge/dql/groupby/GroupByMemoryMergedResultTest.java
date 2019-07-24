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
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByMemoryMergedResultTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<ResultSet> resultSets;
    
    private List<QueryResult> queryResults;
    
    private SQLRouteResult routeResult;
    
    @Before
    public void setUp() throws SQLException {
        resultSets = Lists.newArrayList(mockResultSet(), mockResultSet(), mockResultSet());
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
        AggregationSelectItem aggregationSelectItem1 = new AggregationSelectItem(AggregationType.COUNT, "(*)", null);
        aggregationSelectItem1.setIndex(1);
        AggregationSelectItem aggregationSelectItem2 = new AggregationSelectItem(AggregationType.AVG, "(num)", null);
        aggregationSelectItem2.setIndex(2);
        AggregationSelectItem derivedAggregationSelectItem1 = new AggregationSelectItem(AggregationType.COUNT, "(num)", "AVG_DERIVED_COUNT_0");
        aggregationSelectItem2.setIndex(4);
        aggregationSelectItem2.getDerivedAggregationItems().add(derivedAggregationSelectItem1);
        AggregationSelectItem derivedAggregationSelectItem2 = new AggregationSelectItem(AggregationType.SUM, "(num)", "AVG_DERIVED_SUM_0");
        aggregationSelectItem2.setIndex(5);
        aggregationSelectItem2.getDerivedAggregationItems().add(derivedAggregationSelectItem2);
        SelectItems selectItems = new SelectItems(Arrays.<SelectItem>asList(aggregationSelectItem1, aggregationSelectItem2), false, 0);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(createOrderByItem(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))), false),
                selectItems, new Pagination(null, null, Collections.emptyList()));
        routeResult = new SQLRouteResult(optimizedStatement);
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("COUNT(*)");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("AVG(num)");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("id");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(resultSetMetaData.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
    
    private OrderByItem createOrderByItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        OrderByItem result = new OrderByItem(indexOrderByItemSegment);
        result.setIndex(indexOrderByItemSegment.getColumnIndex());
        return result;
    }
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        when(resultSets.get(0).next()).thenReturn(true, false);
        when(resultSets.get(0).getObject(1)).thenReturn(20);
        when(resultSets.get(0).getObject(2)).thenReturn(0);
        when(resultSets.get(0).getObject(3)).thenReturn(2);
        when(resultSets.get(0).getObject(4)).thenReturn(2);
        when(resultSets.get(0).getObject(5)).thenReturn(20);
        when(resultSets.get(2).next()).thenReturn(true, true, false);
        when(resultSets.get(2).getObject(1)).thenReturn(20, 30);
        when(resultSets.get(2).getObject(2)).thenReturn(0);
        when(resultSets.get(2).getObject(3)).thenReturn(2, 3);
        when(resultSets.get(2).getObject(4)).thenReturn(2, 2, 3);
        when(resultSets.get(2).getObject(5)).thenReturn(20, 20, 30);
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
}
