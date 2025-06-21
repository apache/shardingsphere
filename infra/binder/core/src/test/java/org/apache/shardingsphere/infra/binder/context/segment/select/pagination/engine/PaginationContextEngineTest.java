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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PaginationContextEngineTest {
    
    @Test
    void assertCreatePaginationContextWhenLimitSegmentIsPresent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine(getDatabaseType("SQL92")).createPaginationContext(
                selectStatement, mock(ProjectionsContext.class), Collections.emptyList(), Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenLimitSegmentAbsentAndTopSegmentPresent() {
        SelectStatement subquerySelectStatement = new SelectStatement();
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        subquerySelectStatement.getProjections().getProjections().add(new TopProjectionSegment(0, 10, null, "rowNumberAlias"));
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.getProjections().getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, subquerySelectStatement, ""), ""));
        PaginationContext paginationContext = new PaginationContextEngine(getDatabaseType("SQLServer")).createPaginationContext(
                selectStatement, mock(ProjectionsContext.class), Collections.emptyList(), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenLimitSegmentTopSegmentAbsentAndWhereSegmentPresent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        WhereSegment where = new WhereSegment(0, 10, null);
        selectStatement.setWhere(where);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine(getDatabaseType("SQLServer")).createPaginationContext(
                selectStatement, projectionsContext, Collections.emptyList(), Collections.singletonList(where));
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenResultIsPaginationContext() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        assertThat(new PaginationContextEngine(getDatabaseType("SQL92")).createPaginationContext(
                selectStatement, projectionsContext, Collections.emptyList(), Collections.emptyList()), instanceOf(PaginationContext.class));
    }
    
    private DatabaseType getDatabaseType(final String databaseType) {
        return TypedSPILoader.getService(DatabaseType.class, databaseType);
    }
}
