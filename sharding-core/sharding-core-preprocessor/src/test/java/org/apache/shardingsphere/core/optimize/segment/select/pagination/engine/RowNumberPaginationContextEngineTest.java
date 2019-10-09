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
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RowNumberPaginationContextEngineTest {
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberAliasNotPresent() {
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.findAlias(anyString())).thenReturn(Optional.<String>absent());
        PaginationContext paginationContext = new RowNumberPaginationContextEngine().createPaginationContext(null, projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesIsEmpty() {
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.findAlias(anyString())).thenReturn(Optional.of("predicateRowNumberAlias"));
        PaginationContext paginationContext = new RowNumberPaginationContextEngine().createPaginationContext(Collections.<AndPredicate>emptyList(), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesNotEmpty() {
        PredicateCompareRightValue predicateCompareRightValue = mock(PredicateCompareRightValue.class);
        when(predicateCompareRightValue.getOperator()).thenReturn("<");
        when(predicateCompareRightValue.getExpression()).thenReturn(new LiteralExpressionSegment(0, 10, 100));
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 10, new ColumnSegment(0, 10, "rownum"), predicateCompareRightValue));
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.findAlias(anyString())).thenReturn(Optional.of("predicateRowNumberAlias"));
        PaginationContext paginationContext = new RowNumberPaginationContextEngine().createPaginationContext(Collections.singleton(andPredicate), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        Optional<PaginationValueSegment> paginationValueSegmentOptional = paginationContext.getRowCountSegment();
        assertTrue(paginationValueSegmentOptional.isPresent());
        PaginationValueSegment paginationValueSegment = paginationValueSegmentOptional.get();
        assertTrue(paginationValueSegment instanceof NumberLiteralRowNumberValueSegment);
        NumberLiteralRowNumberValueSegment numberLiteralRowNumberValueSegment = (NumberLiteralRowNumberValueSegment) paginationValueSegment;
        assertThat(numberLiteralRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(numberLiteralRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(numberLiteralRowNumberValueSegment.getValue(), is(100L));
    }
}
