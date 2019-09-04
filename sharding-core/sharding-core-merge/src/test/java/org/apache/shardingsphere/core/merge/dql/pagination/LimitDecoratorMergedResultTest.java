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

package org.apache.shardingsphere.core.merge.dql.pagination;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptTransparentOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class LimitDecoratorMergedResultTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        List<ResultSet> resultSets = Lists.newArrayList(resultSet, mock(ResultSet.class), mock(ResultSet.class), mock(ResultSet.class));
        for (ResultSet each : resultSets) {
            when(each.next()).thenReturn(true, true, false);
        }
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
    }
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, Integer.MAX_VALUE), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutRowCount() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), 
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 2), null, Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 6; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithRowCount() throws SQLException {
        ShardingOptimizedStatement shardingStatement = new ShardingSelectOptimizedStatement(
                new SelectStatement(), Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null),
                new Pagination(new NumberLiteralLimitValueSegment(0, 0, 2), new NumberLiteralLimitValueSegment(0, 0, 2), Collections.emptyList()));
        SQLRouteResult routeResult = new SQLRouteResult(shardingStatement, new EncryptTransparentOptimizedStatement(new SelectStatement()));
        mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("MySQL"), routeResult, queryResults);
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
}
