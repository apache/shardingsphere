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

package org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LimitPaginationContextEngineTest {
    
    @Test
    public void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsNumberLiteralPaginationValueSegment() {
        LimitSegment limitSegment = new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 1L), new NumberLiteralLimitValueSegment(10, 20, 2L));
        PaginationContext paginationContext = new LimitPaginationContextEngine().createPaginationContext(limitSegment, Collections.emptyList());
        assertTrue(paginationContext.isHasPagination());
    }

    @Test
    public void assertPaginationContextCreatedProperlyWhenOffsetAndRowCountAreBothNull() {
        PaginationContext paginationContext = new LimitPaginationContextEngine().createPaginationContext(new LimitSegment(0, 10, null, null), Collections.emptyList());
        assertFalse(paginationContext.isHasPagination());
    }

    @Test
    public void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsParameterMarkerPaginationValueSegment() {
        LimitSegment limitSegment = new LimitSegment(0, 10, new ParameterMarkerLimitValueSegment(0, 10, 0), new ParameterMarkerLimitValueSegment(10, 20, 1));
        PaginationContext paginationContext = new LimitPaginationContextEngine().createPaginationContext(limitSegment, Lists.newArrayList(15L, 20L));
        assertTrue(paginationContext.isHasPagination());
    }
}
