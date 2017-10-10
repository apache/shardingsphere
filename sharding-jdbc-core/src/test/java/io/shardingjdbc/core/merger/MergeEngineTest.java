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

package io.shardingjdbc.core.merger;

import io.shardingjdbc.core.constant.AggregationType;
import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.merger.groupby.GroupByMemoryResultSetMerger;
import io.shardingjdbc.core.merger.groupby.GroupByStreamResultSetMerger;
import io.shardingjdbc.core.merger.iterator.IteratorStreamResultSetMerger;
import io.shardingjdbc.core.merger.limit.LimitDecoratorResultSetMerger;
import io.shardingjdbc.core.merger.orderby.OrderByStreamResultSetMerger;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MergeEngineTest {
    
    private MergeEngine mergeEngine;
    
    private List<ResultSet> resultSets;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(0);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("count(*)");
        resultSets = Collections.singletonList(resultSet);
        selectStatement = new SelectStatement();
    }
    
    @Test
    public void assertBuildIteratorStreamResultSetMerger() throws SQLException {
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildIteratorStreamResultSetMergerWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(true));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorResultSetMerger.class));
        assertThat(((LimitDecoratorResultSetMerger) actual).getResultSetMerger(), instanceOf(IteratorStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildOrderByStreamResultSetMerger() throws SQLException {
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(OrderByStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildOrderByStreamResultSetMergerWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(true));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorResultSetMerger.class));
        assertThat(((LimitDecoratorResultSetMerger) actual).getResultSetMerger(), instanceOf(OrderByStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByStreamResultSetMerger() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByStreamResultSetMergerWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(true));
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorResultSetMerger.class));
        assertThat(((LimitDecoratorResultSetMerger) actual).getResultSetMerger(), instanceOf(GroupByStreamResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryResultSetMerger() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryResultSetMergerWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(true));
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderType.DESC, OrderType.ASC));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorResultSetMerger.class));
        assertThat(((LimitDecoratorResultSetMerger) actual).getResultSetMerger(), instanceOf(GroupByMemoryResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryResultSetMergerWithAggregationOnly() throws SQLException {
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryResultSetMerger.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryResultSetMergerWithAggregationOnlyWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(true));
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new MergeEngine(resultSets, selectStatement);
        ResultSetMerger actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorResultSetMerger.class));
        assertThat(((LimitDecoratorResultSetMerger) actual).getResultSetMerger(), instanceOf(GroupByMemoryResultSetMerger.class));
    }
}
