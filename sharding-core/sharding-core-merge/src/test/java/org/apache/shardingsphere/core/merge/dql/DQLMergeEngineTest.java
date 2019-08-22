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

package org.apache.shardingsphere.core.merge.dql;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByMemoryMergedResult;
import org.apache.shardingsphere.core.merge.dql.groupby.GroupByStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.orderby.OrderByStreamMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.RowNumberDecoratorMergedResult;
import org.apache.shardingsphere.core.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptTransparentOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DQLMergeEngineTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<QueryResult> singleQueryResult;
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(0);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("count(*)");
        singleQueryResult = Collections.<QueryResult>singletonList(new TestQueryResult(resultSet));
        List<ResultSet> resultSets = Lists.newArrayList(resultSet, mockResultSet(), mockResultSet(), mockResultSet());
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        return result;
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResult() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), new Pagination(null, null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, singleQueryResult);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithMySQLLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithOracleLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithSQLServerLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResult() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), new GroupBy(Collections.<OrderByItem>emptyList(), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), new Pagination(null, null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithMySQLLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), new GroupBy(Collections.<OrderByItem>emptyList(), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithOracleLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), new GroupBy(Collections.<OrderByItem>emptyList(), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithSQLServerLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), new GroupBy(Collections.<OrderByItem>emptyList(), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResult() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), new Pagination(null, null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithMySQLLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithOracleLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithSQLServerLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResult() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0), 
                new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(null, null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithMySQLLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0), 
                new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null),
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithOracleLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.DESC, OrderDirection.ASC))), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithSQLServerLimit() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Arrays.asList(
                        new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, OrderDirection.ASC)), 
                        new OrderByItem(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, OrderDirection.ASC))), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        SelectItems selectItems = new SelectItems(
                0, 0, false, Collections.<SelectItem>singletonList(new AggregationSelectItem(AggregationType.COUNT, "(*)", null)), Collections.<TableSegment>emptyList(), null);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(new SelectStatement(), 
                Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(null, null, Collections.emptyList())), new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithMySQLLimit() throws SQLException {
        SelectItems selectItems = new SelectItems(
                0, 0, false, Collections.<SelectItem>singletonList(new AggregationSelectItem(AggregationType.COUNT, "(*)", null)), Collections.<TableSegment>emptyList(), null);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(new SelectStatement(), 
                Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(new NumberLiteralLimitValueSegment(0, 0, 1), null, Collections.emptyList())), new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithOracleLimit() throws SQLException {
        SelectItems selectItems = new SelectItems(
                0, 0, false, Collections.<SelectItem>singletonList(new AggregationSelectItem(AggregationType.COUNT, "(*)", null)), Collections.<TableSegment>emptyList(), null);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(new SelectStatement(), 
                Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList())), new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("Oracle"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithSQLServerLimit() throws SQLException {
        SelectItems selectItems = new SelectItems(
                0, 0, false, Collections.<SelectItem>singletonList(new AggregationSelectItem(AggregationType.COUNT, "(*)", null)), Collections.<TableSegment>emptyList(), null);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(new SelectStatement(), 
                Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(), 
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(new NumberLiteralRowNumberValueSegment(0, 0, 1, true), null, Collections.emptyList())), new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
}
