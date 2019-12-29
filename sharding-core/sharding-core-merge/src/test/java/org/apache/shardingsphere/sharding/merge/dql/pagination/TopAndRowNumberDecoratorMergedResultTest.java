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

import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.sharding.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergedResult;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TopAndRowNumberDecoratorMergedResultTest {
    
    @Test
    public void assertNextForSkipAll() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, Integer.MAX_VALUE, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, 
                Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithoutOffsetWithRowCount() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(null, new NumberLiteralLimitValueSegment(0, 0, 5), Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, 
                Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 5; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetWithoutRowCount() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()), 
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), null, Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, 
                Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        for (int i = 0; i < 7; i++) {
            assertTrue(actual.next());
        }
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetBoundOpenedFalse() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, false), new NumberLiteralLimitValueSegment(0, 0, 4), Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, 
                Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextWithOffsetBoundOpenedTrue() throws SQLException {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(), 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(0, 0, 2, true), new NumberLiteralLimitValueSegment(0, 0, 4), Collections.emptyList()));
        DQLMergeEngine mergeEngine = new DQLMergeEngine(DatabaseTypes.getActualDatabaseType("SQLServer"), selectSQLStatementContext, 
                Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult(), createQueryResult()));
        MergedResult actual = mergeEngine.merge();
        assertTrue(actual.next());
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
