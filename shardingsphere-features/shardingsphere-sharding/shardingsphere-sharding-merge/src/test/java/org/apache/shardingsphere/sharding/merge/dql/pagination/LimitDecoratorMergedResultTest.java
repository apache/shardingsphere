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

import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.result.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class LimitDecoratorMergedResultTest {
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new MySQLSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, Integer.MAX_VALUE), null, Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()), selectStatementContext, null);
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutRowCount() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new MySQLSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.emptyList()), 
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 2), null, Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()), selectStatementContext, null);
        for (int i = 0; i < 6; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithRowCount() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        SelectStatementContext selectStatementContext = new SelectStatementContext(new MySQLSelectStatement(), 
                new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.emptyList()),
                new PaginationContext(new NumberLiteralLimitValueSegment(0, 0, 2), new NumberLiteralLimitValueSegment(0, 0, 2), Collections.emptyList()));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()), selectStatementContext, null);
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, true, false);
        return result;
    }
}
