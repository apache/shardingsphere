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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.OrderDirection;
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
import org.apache.shardingsphere.core.parse.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(0);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("count(*)");
        singleQueryResult = Collections.<QueryResult>singletonList(new TestQueryResult(resultSet));
        List<ResultSet> resultSets = Lists.newArrayList(resultSet, mock(ResultSet.class), mock(ResultSet.class), mock(ResultSet.class));
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
        selectStatement = new SelectStatement();
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResult() throws SQLException {
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, singleQueryResult);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithMySQLLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithOracleLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        mergeEngine = new DQLMergeEngine(DatabaseType.Oracle, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithSQLServerLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResult() throws SQLException {
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithMySQLLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithOracleLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.Oracle, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithSQLServerLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResult() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithMySQLLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithOracleLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.Oracle, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithSQLServerLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResult() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithMySQLLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithOracleLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(2, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.Oracle, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithSQLServerLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithMySQLLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(DatabaseType.MySQL, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithOracleLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(DatabaseType.Oracle, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(RowNumberDecoratorMergedResult.class));
        assertThat(((RowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithSQLServerLimit() throws SQLException {
        selectStatement.setLimit(new Limit());
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(DatabaseType.SQLServer, selectStatement, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(TopAndRowNumberDecoratorMergedResult.class));
        assertThat(((TopAndRowNumberDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
}
