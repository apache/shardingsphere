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
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitPaginationContextEngineTest {
    
    private final LimitPaginationContextEngine paginationContextEngine = new LimitPaginationContextEngine();
    
    @Test
    void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsNumberLiteralPaginationValueSegment() {
        LimitSegment limitSegment = new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 1L), new NumberLiteralLimitValueSegment(10, 20, 2L));
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(limitSegment, Collections.emptyList());
        assertTrue(paginationContext.isHasPagination());
    }
    
    @Test
    void assertPaginationContextCreatedProperlyWhenOffsetAndRowCountAreBothNull() {
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(new LimitSegment(0, 10, null, null), Collections.emptyList());
        assertFalse(paginationContext.isHasPagination());
    }
    
    @Test
    void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsParameterMarkerPaginationValueSegment() {
        LimitSegment limitSegment = new LimitSegment(0, 10, new ParameterMarkerLimitValueSegment(0, 10, 0), new ParameterMarkerLimitValueSegment(10, 20, 1));
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(limitSegment, Arrays.asList(15L, 20L));
        assertTrue(paginationContext.isHasPagination());
    }
    
    @Test
    void assertCreatePaginationContextWithImplicitRowCountWithoutOffset() {
        LimitSegment limitSegment = new LimitSegment(0, 10, null, null);
        limitSegment.setImplicitRowCount(true);
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class,
                () -> paginationContextEngine.createPaginationContext(limitSegment, Collections.emptyList()));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Implicit FETCH row count."));
    }
    
    @Test
    void assertCreatePaginationContextWithImplicitRowCountAndOffset() {
        NumberLiteralLimitValueSegment offset = new NumberLiteralLimitValueSegment(0, 10, 2L);
        LimitSegment limitSegment = new LimitSegment(0, 20, offset, null);
        limitSegment.setImplicitRowCount(true);
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class,
                () -> paginationContextEngine.createPaginationContext(limitSegment, Collections.emptyList()));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Implicit FETCH row count."));
    }
    
    @Test
    void assertCreatePaginationContextWithOffsetOnly() {
        NumberLiteralLimitValueSegment offset = new NumberLiteralLimitValueSegment(0, 10, 2L);
        PaginationContext actual = paginationContextEngine.createPaginationContext(new LimitSegment(0, 20, offset, null), Collections.emptyList());
        assertThat(actual.getOffsetSegment().orElseThrow(AssertionError::new), sameInstance(offset));
        assertFalse(actual.getRowCountSegment().isPresent());
    }
}
