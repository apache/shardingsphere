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

package org.apache.shardingsphere.core.optimize.segment.select.pagination.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.optimize.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.top.TopSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PaginationContextEngineTest {
    
    private PaginationContextEngine paginationContextEngine;
    
    @Before
    public void setUp() {
        paginationContextEngine = new PaginationContextEngine();
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        PaginationValueSegment offset = new NumberLiteralLimitValueSegment(0, 10, 100L);
        PaginationValueSegment rowCount = new NumberLiteralLimitValueSegment(0, 10, 100L);
        LimitSegment limitSegment = new LimitSegment(0, 10, offset, rowCount);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.of(limitSegment));
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(selectStatement, null, parameters);
        Optional<PaginationValueSegment> offsetSegment = paginationContext.getOffsetSegment();
        assertTrue(offsetSegment.isPresent());
        Optional<PaginationValueSegment> rowCountSegment = paginationContext.getRowCountSegment();
        assertTrue(rowCountSegment.isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentAbsentAndTopSegmentPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.<LimitSegment>absent());
        TopSegment topSegment = new TopSegment(0, 10, "text", null, "rowNumberAlias");
        when(selectStatement.findSQLSegment(TopSegment.class)).thenReturn(Optional.of(topSegment));
        when(selectStatement.findSQLSegment(WhereSegment.class)).thenReturn(Optional.<WhereSegment>absent());
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(selectStatement, null, parameters);
        Optional<PaginationValueSegment> offsetSegment = paginationContext.getOffsetSegment();
        assertFalse(offsetSegment.isPresent());
        Optional<PaginationValueSegment> rowCountSegment = paginationContext.getRowCountSegment();
        assertFalse(rowCountSegment.isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentTopSegmentAbsentAndWhereSegmentPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.<LimitSegment>absent());
        when(selectStatement.findSQLSegment(TopSegment.class)).thenReturn(Optional.<TopSegment>absent());
        WhereSegment whereSegment = new WhereSegment(0, 10, 10);
        when(selectStatement.findSQLSegment(WhereSegment.class)).thenReturn(Optional.of(whereSegment));
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.findAlias(anyString())).thenReturn(Optional.<String>absent());
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(selectStatement, projectionsContext, parameters);
        Optional<PaginationValueSegment> offsetSegment = paginationContext.getOffsetSegment();
        assertFalse(offsetSegment.isPresent());
        Optional<PaginationValueSegment> rowCountSegment = paginationContext.getRowCountSegment();
        assertFalse(rowCountSegment.isPresent());
    }
}
