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

package org.apache.shardingsphere.sharding.merge;

import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.merge.dal.DALMergeEngine;
import org.apache.shardingsphere.sharding.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MergeEngineFactoryTest {
    
    private final List<QueryResult> queryResults = new LinkedList<>();
    
    @Before
    public void setUp() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getColumnCount()).thenReturn(1);
        when(queryResult.getColumnLabel(1)).thenReturn("label");
        queryResults.add(queryResult);
    }
    
    @Test
    public void assertNewInstanceWithSelectStatement() {
        SQLRouteResult routeResult = new SQLRouteResult(
                new SelectSQLStatementContext(new SelectStatement(), 
                        new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                        new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), new PaginationContext(null, null, Collections.emptyList())), 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, mock(RelationMetas.class), queryResults), instanceOf(DQLMergeEngine.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() {
        SQLRouteResult routeResult = new SQLRouteResult(new CommonSQLStatementContext(new DALStatement()), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, mock(RelationMetas.class), queryResults), instanceOf(DALMergeEngine.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "col"));
        SQLRouteResult routeResult = new SQLRouteResult(
                new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        assertThat(MergeEngineFactory.newInstance(DatabaseTypes.getActualDatabaseType("MySQL"), null, routeResult, mock(RelationMetas.class), queryResults), instanceOf(TransparentMergeEngine.class));
    }
}
