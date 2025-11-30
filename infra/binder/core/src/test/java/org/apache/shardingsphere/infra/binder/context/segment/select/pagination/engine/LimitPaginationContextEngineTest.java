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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
