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

package org.apache.shardingsphere.sql.parser.relation.segment.select.pagination;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PaginationContextTest {
    
    @Test
    public void assertSegmentWithNullOffsetSegment() {
        PaginationValueSegment rowCountSegment = getRowCountSegment();
        PaginationContext paginationContext = new PaginationContext(null, rowCountSegment, getParameters());
        assertTrue(paginationContext.isHasPagination());
        assertNull(paginationContext.getOffsetSegment().orNull());
        assertThat(paginationContext.getRowCountSegment().orNull(), is(rowCountSegment));
    }
    
    @Test
    public void assertGetSegmentWithRowCountSegment() {
        PaginationValueSegment offsetSegment = getOffsetSegment();
        PaginationContext paginationContext = new PaginationContext(offsetSegment, null, getParameters());
        assertTrue(paginationContext.isHasPagination());
        assertThat(paginationContext.getOffsetSegment().orNull(), is(offsetSegment));
        assertNull(paginationContext.getRowCountSegment().orNull());
    }
    
    @Test
    public void assertGetActualOffset() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    public void assertGetActualOffsetWithNumberLiteralPaginationValueSegment() {
        assertThat(new PaginationContext(getOffsetSegmentWithNumberLiteralPaginationValueSegment(), 
                getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    public void assertGetActualOffsetWithNullOffsetSegment() {
        assertThat(new PaginationContext(null, getRowCountSegment(), getParameters()).getActualOffset(), is(0L));
    }
    
    @Test
    public void assertGetActualRowCount() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualRowCount().orNull(), is(20L));
    }
    
    @Test
    public void assertGetActualRowCountWithNumberLiteralPaginationValueSegment() {
        assertThat(new PaginationContext(getOffsetSegmentWithNumberLiteralPaginationValueSegment(),
            getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualRowCount().orNull(), is(20L));
    }
    
    @Test
    public void assertGetActualRowCountWithNullRowCountSegment() {
        assertNull(new PaginationContext(getOffsetSegment(), null, getParameters()).getActualRowCount().orNull());
    }
    
    private PaginationValueSegment getOffsetSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(28, 30, 30);
    }
    
    private PaginationValueSegment getRowCountSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(32, 34, 20);
    }
    
    @Test
    public void assertGetOffsetParameterIndex() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getOffsetParameterIndex().orNull(), is(0));
    }
    
    @Test
    public void assertGetRowCountParameterIndex() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRowCountParameterIndex().orNull(), is(1));
    }
    
    private PaginationValueSegment getOffsetSegment() {
        return new ParameterMarkerLimitValueSegment(28, 30, 0);
    }
    
    private PaginationValueSegment getRowCountSegment() {
        return new ParameterMarkerLimitValueSegment(32, 34, 1);
    }
    
    private List<Object> getParameters() {
        return Lists.<Object>newArrayList(30, 20);
    }
    
    @Test
    public void assertGetRevisedOffset() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedOffset(), is(0L));
    }
    
    @Test
    public void getRevisedRowCount() {
        SelectSQLStatementContext selectSQLStatementContext = mock(SelectSQLStatementContext.class);
        when(selectSQLStatementContext.getProjectionsContext()).thenReturn(mock(ProjectionsContext.class));
        when(selectSQLStatementContext.getGroupByContext()).thenReturn(new GroupByContext(Collections.<OrderByItem>emptyList(), 0));
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(selectSQLStatementContext), is(50L));
    }
    
    @Test
    public void getRevisedRowCountWithMax() {
        SelectSQLStatementContext selectSQLStatementContext = mock(SelectSQLStatementContext.class);
        when(selectSQLStatementContext.getProjectionsContext()).thenReturn(mock(ProjectionsContext.class));
        when(selectSQLStatementContext.getGroupByContext()).thenReturn(new GroupByContext(Collections.singletonList(mock(OrderByItem.class)), 1));
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(selectSQLStatementContext), is((long) Integer.MAX_VALUE));
    }
}
