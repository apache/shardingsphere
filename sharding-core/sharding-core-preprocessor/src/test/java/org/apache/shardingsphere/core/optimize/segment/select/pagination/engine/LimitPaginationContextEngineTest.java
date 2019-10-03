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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class LimitPaginationContextEngineTest {

    private LimitPaginationContextEngine limitPaginationContextEngine;

    @Before
    public void setUp() {
        limitPaginationContextEngine = new LimitPaginationContextEngine();
    }

    @Test
    public void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsNumberLiteralPaginationValueSegment() throws NoSuchFieldException, IllegalAccessException {
        NumberLiteralLimitValueSegment offset = new NumberLiteralLimitValueSegment(0, 10, 1L);
        NumberLiteralLimitValueSegment rowCount = new NumberLiteralLimitValueSegment(10, 20, 2L);
        LimitSegment limitSegment = new LimitSegment(0, 10, offset, rowCount);
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = limitPaginationContextEngine.createPaginationContext(limitSegment, parameters);
        assertThat(paginationContext.isHasPagination(), is(true));
        Field offsetSegmentField = paginationContext.getClass().getDeclaredField("offsetSegment");
        offsetSegmentField.setAccessible(true);
        NumberLiteralLimitValueSegment offsetSegmentValue = (NumberLiteralLimitValueSegment) offsetSegmentField.get(paginationContext);
        assertThat(offsetSegmentValue, is(offset));
        Field rowCountField = paginationContext.getClass().getDeclaredField("rowCountSegment");
        rowCountField.setAccessible(true);
        NumberLiteralLimitValueSegment rowCountValue = (NumberLiteralLimitValueSegment) rowCountField.get(paginationContext);
        assertThat(rowCountValue, is(rowCount));
        Field actualOffsetField = paginationContext.getClass().getDeclaredField("actualOffset");
        actualOffsetField.setAccessible(true);
        long actualOffsetFieldValue = (long) actualOffsetField.get(paginationContext);
        assertThat(actualOffsetFieldValue, is(offset.getValue()));
        Field actualRowCountField = paginationContext.getClass().getDeclaredField("actualRowCount");
        actualRowCountField.setAccessible(true);
        Long actualRowCountFieldValue = (Long) actualRowCountField.get(paginationContext);
        assertThat(actualRowCountFieldValue, is(rowCount.getValue()));
    }

    @Test
    public void assertPaginationContextCreatedProperlyWhenOffsetAndRowCountAreBothNull() throws NoSuchFieldException, IllegalAccessException {
        LimitSegment limitSegment = new LimitSegment(0, 10, null, null);
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = limitPaginationContextEngine.createPaginationContext(limitSegment, parameters);
        assertThat(paginationContext.isHasPagination(), is(false));
        Field offsetSegmentField = paginationContext.getClass().getDeclaredField("offsetSegment");
        offsetSegmentField.setAccessible(true);
        NumberLiteralLimitValueSegment offsetSegmentValue = (NumberLiteralLimitValueSegment) offsetSegmentField.get(paginationContext);
        assertNull(offsetSegmentValue);
        Field rowCountField = paginationContext.getClass().getDeclaredField("rowCountSegment");
        rowCountField.setAccessible(true);
        NumberLiteralLimitValueSegment rowCountValue = (NumberLiteralLimitValueSegment) rowCountField.get(paginationContext);
        assertNull(rowCountValue);
        Field actualOffsetField = paginationContext.getClass().getDeclaredField("actualOffset");
        actualOffsetField.setAccessible(true);
        long actualOffsetFieldValue = (long) actualOffsetField.get(paginationContext);
        assertThat(actualOffsetFieldValue, is((long) 0));
        Field actualRowCountField = paginationContext.getClass().getDeclaredField("actualRowCount");
        actualRowCountField.setAccessible(true);
        Long actualRowCountFieldValue = (Long) actualRowCountField.get(paginationContext);
        assertNull(actualRowCountFieldValue);
    }

    @Test
    public void assertPaginationContextCreatedProperlyWhenPaginationValueSegmentIsParameterMarkerPaginationValueSegment() throws NoSuchFieldException, IllegalAccessException {
        ParameterMarkerPaginationValueSegment offset = new ParameterMarkerLimitValueSegment(0, 10, 0);
        ParameterMarkerPaginationValueSegment rowCount = new ParameterMarkerLimitValueSegment(10, 20, 1);
        LimitSegment limitSegment = new LimitSegment(0, 10, offset, rowCount);
        List<Object> parameters = Lists.<Object>newArrayList(15L, 20L);
        PaginationContext paginationContext = limitPaginationContextEngine.createPaginationContext(limitSegment, parameters);
        assertThat(paginationContext.isHasPagination(), is(true));
        Field offsetSegmentField = paginationContext.getClass().getDeclaredField("offsetSegment");
        offsetSegmentField.setAccessible(true);
        ParameterMarkerPaginationValueSegment offsetSegmentValue = (ParameterMarkerPaginationValueSegment) offsetSegmentField.get(paginationContext);
        assertThat(offsetSegmentValue, is(offset));
        Field rowCountField = paginationContext.getClass().getDeclaredField("rowCountSegment");
        rowCountField.setAccessible(true);
        ParameterMarkerPaginationValueSegment rowCountValue = (ParameterMarkerPaginationValueSegment) rowCountField.get(paginationContext);
        assertThat(rowCountValue, is(rowCount));
        Field actualOffsetField = paginationContext.getClass().getDeclaredField("actualOffset");
        actualOffsetField.setAccessible(true);
        long actualOffsetFieldValue = (long) actualOffsetField.get(paginationContext);
        assertThat(actualOffsetFieldValue, is(parameters.get(0)));
        Field actualRowCountField = paginationContext.getClass().getDeclaredField("actualRowCount");
        actualRowCountField.setAccessible(true);
        Long actualRowCountFieldValue = (Long) actualRowCountField.get(paginationContext);
        assertThat(actualRowCountFieldValue, is(parameters.get(1)));
    }
}
