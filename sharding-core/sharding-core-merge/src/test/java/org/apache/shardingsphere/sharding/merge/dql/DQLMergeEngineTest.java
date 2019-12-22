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

package org.apache.shardingsphere.sharding.merge.dql;

import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
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
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergedResult;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DQLMergeEngineTest {
    
    @Test
    public void assertBuildIteratorStreamMergedResult() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, Collections.singletonList(createQueryResult()));
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithMySQLLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithOracleLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithSQLServerLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResult() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        assertThat(mergeEngine.merge(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithMySQLLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithOracleLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithSQLServerLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResult() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(),   
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        assertThat(mergeEngine.merge(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithMySQLLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithOracleLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithSQLServerLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResult() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0), 
                new OrderByContext(Collections.<OrderByItem>emptyList(), false), new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(null, null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithMySQLLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0), 
                new OrderByContext(Collections.<OrderByItem>emptyList(), false), new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithOracleLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithSQLServerLimit() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Arrays.asList(
                        new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)), 
                        new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC))), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        ProjectionsContext projectionsContext = new ProjectionsContext(
                0, 0, false, Collections.<Projection>singletonList(new AggregationProjection(AggregationType.COUNT, "(*)", null)), Collections.<String>emptyList());
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithMySQLLimit() throws SQLException {
        ProjectionsContext projectionsContext = new ProjectionsContext(
                0, 0, false, Collections.<Projection>singletonList(new AggregationProjection(AggregationType.COUNT, "(*)", null)), Collections.<String>emptyList());
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithOracleLimit() throws SQLException {
        ProjectionsContext projectionsContext = new ProjectionsContext(
                0, 0, false, Collections.<Projection>singletonList(new AggregationProjection(AggregationType.COUNT, "(*)", null)), Collections.<String>emptyList());
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                        projectionsContext, new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithSQLServerLimit() throws SQLException {
        ProjectionsContext projectionsContext = new ProjectionsContext(
                0, 0, false, Collections.<Projection>singletonList(new AggregationProjection(AggregationType.COUNT, "(*)", null)), Collections.<String>emptyList());
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(
                new SelectStatement(), new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, createQueryResults());
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    private List<QueryResult> createQueryResults() throws SQLException {
        List<QueryResult> result = new LinkedList<>();
        QueryResult queryResult = createQueryResult();
        result.add(queryResult);
        result.add(mock(QueryResult.class));
        result.add(mock(QueryResult.class));
        result.add(mock(QueryResult.class));
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnLabel(1)).thenReturn("count(*)");
        when(result.getValue(1, Object.class)).thenReturn(0);
        return result;
    }
}
