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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaginationTest {
    
    @Test
    public void assertSegmentWithNullOffsetSegment() {
        PaginationValueSegment rowCountSegment = getRowCountSegment();
        Pagination pagination = new Pagination(null, rowCountSegment, getParameters());
        assertTrue(pagination.isHasPagination());
        assertNull(pagination.getOffsetSegment().orNull());
        assertThat(pagination.getRowCountSegment().orNull(), is(rowCountSegment));
    }
    
    @Test
    public void assertGetSegmentWithRowCountSegment() {
        PaginationValueSegment offsetSegment = getOffsetSegment();
        Pagination pagination = new Pagination(offsetSegment, null, getParameters());
        assertTrue(pagination.isHasPagination());
        assertThat(pagination.getOffsetSegment().orNull(), is(offsetSegment));
        assertNull(pagination.getRowCountSegment().orNull());
    }
    
    @Test
    public void assertGetActualOffset() {
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    public void assertGetActualOffsetWithNumberLiteralPaginationValueSegment() {
        assertThat(new Pagination(getOffsetSegmentWithNumberLiteralPaginationValueSegment(), getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    public void assertGetActualOffsetWithNullOffsetSegment() {
        assertThat(new Pagination(null, getRowCountSegment(), getParameters()).getActualOffset(), is(0L));
    }
    
    @Test
    public void assertGetActualRowCount() {
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualRowCount().orNull(), is(20L));
    }
    
    @Test
    public void assertGetActualRowCountWithNumberLiteralPaginationValueSegment() {
        assertThat(new Pagination(getOffsetSegmentWithNumberLiteralPaginationValueSegment(),
            getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualRowCount().orNull(), is(20L));
    }
    
    @Test
    public void assertGetActualRowCountWithNullRowCountSegment() {
        assertNull(new Pagination(getOffsetSegment(), null, getParameters()).getActualRowCount().orNull());
    }
    
    private PaginationValueSegment getOffsetSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(28, 30, 30);
    }
    
    private PaginationValueSegment getRowCountSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(32, 34, 20);
    }
    
    @Test
    public void assertGetOffsetParameterIndex() {
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getOffsetParameterIndex().orNull(), is(0));
    }
    
    @Test
    public void assertGetRowCountParameterIndex() {
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getRowCountParameterIndex().orNull(), is(1));
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
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedOffset(), is(0L));
    }
    
    @Test
    public void getRevisedRowCount() {
        ShardingSelectOptimizedStatement shardingStatement = mock(ShardingSelectOptimizedStatement.class);
        doReturn(getSelectItemsWithEmptyAggregationSelectItems()).when(shardingStatement).getSelectItems();
        doReturn(getGroupByWithEmptyItems()).when(shardingStatement).getGroupBy();
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(shardingStatement), is(50L));
    }
    
    private GroupBy getGroupByWithEmptyItems() {
        GroupBy groupBy = mock(GroupBy.class);
        when(groupBy.getItems()).thenReturn(Collections.<OrderByItem>emptyList());
        return groupBy;
    }
    
    private SelectItems getSelectItemsWithEmptyAggregationSelectItems() {
        SelectItems selectItems = mock(SelectItems.class);
        when(selectItems.getAggregationSelectItems()).thenReturn(Collections.<AggregationSelectItem>emptyList());
        return selectItems;
    }
    
    @Test
    public void getRevisedRowCountWithMax() {
        ShardingSelectOptimizedStatement shardingStatement = mock(ShardingSelectOptimizedStatement.class);
        doReturn(getSelectItemsWithEmptyAggregationSelectItems()).when(shardingStatement).getSelectItems();
        doReturn(getGroupBy()).when(shardingStatement).getGroupBy();
        doReturn(false).when(shardingStatement).isSameGroupByAndOrderByItems();
        assertThat(new Pagination(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(shardingStatement), is((long) Integer.MAX_VALUE));
    }
    
    private GroupBy getGroupBy() {
        GroupBy groupBy = mock(GroupBy.class);
        List items = mock(List.class);
        when(items.isEmpty()).thenReturn(false);
        when(groupBy.getItems()).thenReturn(items);
        return groupBy;
    }
}
