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

package org.apache.shardingsphere.sharding.merge.dql.pagination;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TopAndRowNumberDecoratorMergedResultTest {
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        SelectStatementContext selectStatementContext = new SelectStatementContext(new SQLServerSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, Integer.MAX_VALUE, true), null, Collections.emptyList()));
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("SQLServer"));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, null);
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutOffsetWithRowCount() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("SQLServer"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new SQLServerSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), 
                new PaginationContext(null, new NumberLiteralLimitValueSegment(0, 0, 5), Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, null);
        for (int i = 0; i < 5; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetWithoutRowCount() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("SQLServer"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new SQLServerSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), null, Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, null);
        for (int i = 0; i < 7; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetBoundOpenedFalse() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("SQLServer"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new SQLServerSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, false), new NumberLiteralLimitValueSegment(0, 0, 4), Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, null);
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetBoundOpenedTrue() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("SQLServer"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new SQLServerSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), new NumberLiteralLimitValueSegment(0, 0, 4), Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult(), mockQueryResult()), selectStatementContext, null);
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, false);
        return result;
    }
}
