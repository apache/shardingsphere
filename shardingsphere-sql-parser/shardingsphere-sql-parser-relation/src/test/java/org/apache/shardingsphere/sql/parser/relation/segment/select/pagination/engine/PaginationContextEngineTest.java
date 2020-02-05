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

package org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PaginationContextEngineTest {
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.of(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L))));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentAbsentAndTopSegmentPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.<LimitSegment>absent());
        when(selectStatement.findSQLSegment(TopProjectionSegment.class)).thenReturn(Optional.of(new TopProjectionSegment(0, 10, "text", null, "rowNumberAlias")));
        when(selectStatement.findSQLSegment(WhereSegment.class)).thenReturn(Optional.<WhereSegment>absent());
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentTopSegmentAbsentAndWhereSegmentPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.<LimitSegment>absent());
        when(selectStatement.findSQLSegment(TopProjectionSegment.class)).thenReturn(Optional.<TopProjectionSegment>absent());
        WhereSegment whereSegment = new WhereSegment(0, 10);
        whereSegment.setParametersCount(10);
        when(selectStatement.findSQLSegment(WhereSegment.class)).thenReturn(Optional.of(whereSegment));
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.findAlias(anyString())).thenReturn(Optional.<String>absent());
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContext() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.<LimitSegment>absent());
        when(selectStatement.findSQLSegment(TopProjectionSegment.class)).thenReturn(Optional.<TopProjectionSegment>absent());
        when(selectStatement.findSQLSegment(WhereSegment.class)).thenReturn(Optional.<WhereSegment>absent());
        assertThat(new PaginationContextEngine().createPaginationContext(selectStatement, mock(ProjectionsContext.class), Collections.emptyList()), instanceOf(PaginationContext.class));
    }
}
