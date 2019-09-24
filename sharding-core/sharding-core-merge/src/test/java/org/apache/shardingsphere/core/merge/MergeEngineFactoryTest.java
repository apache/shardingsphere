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

package org.apache.shardingsphere.core.merge;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dal.DALMergeEngine;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.merge.fixture.TestQueryResult;
import org.apache.shardingsphere.core.optimize.api.statement.CommonOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.api.statement.SelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
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

public final class MergeEngineFactoryTest {
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        List<ResultSet> resultSets = Lists.newArrayList(resultSet);
        queryResults = new ArrayList<>(resultSets.size());
        queryResults.add(new TestQueryResult(resultSets.get(0)));
    }
    
    @Test
    public void assertNewInstanceWithSelectStatement() throws SQLException {
        SQLRouteResult routeResult = new SQLRouteResult(
                new SelectOptimizedStatement(new SelectStatement(), 
                        new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                        new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null), new Pagination(null, null, Collections.emptyList())), 
                new CommonOptimizedStatement(new SelectStatement()), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, null, queryResults), instanceOf(DQLMergeEngine.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() throws SQLException {
        SQLRouteResult routeResult = new SQLRouteResult(new CommonOptimizedStatement(new DALStatement()), 
                new CommonOptimizedStatement(new DALStatement()), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, null, queryResults), instanceOf(DALMergeEngine.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() throws SQLException {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "col"));
        SQLRouteResult routeResult = new SQLRouteResult(
                new InsertOptimizedStatement(null, Collections.emptyList(), insertStatement), 
                new CommonOptimizedStatement(insertStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, null, queryResults), instanceOf(TransparentMergeEngine.class));
    }
}
