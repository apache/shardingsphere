/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResultSetMergeContextTest {
    
    @Test
    public void assertNewResultSetMergeContext() throws SQLException {
        ResultSetMergeContext actual = new ResultSetMergeContext(
                new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col", "count_col", "avg_col", "sharding_gen_1", "sharding_gen_2")))), 
                createMergeContext());
        assertThat(actual.getMergeContext().getOrderByColumns().get(0).getColumnIndex(), is(1));
        assertThat(actual.getMergeContext().getGroupByColumns().get(0).getColumnIndex(), is(2));
        assertThat(actual.getMergeContext().getAggregationColumns().get(0).getColumnIndex(), is(3));
        assertThat(actual.getMergeContext().getAggregationColumns().get(1).getColumnIndex(), is(4));
        assertThat(actual.getMergeContext().getAggregationColumns().get(1).getDerivedColumns().get(0).getColumnIndex(), is(5));
        assertThat(actual.getMergeContext().getAggregationColumns().get(1).getDerivedColumns().get(1).getColumnIndex(), is(6));
        assertThat(actual.getCurrentOrderByKeys(), is(actual.getMergeContext().getOrderByColumns()));
    }
    
    private MergeContext createMergeContext() {
        MergeContext result = new MergeContext();
        result.getOrderByColumns().add(new OrderByColumn("order_col", OrderByColumn.OrderByType.ASC));
        result.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "group_col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        result.getAggregationColumns().add(MergerTestUtil.createAggregationColumn(AggregationColumn.AggregationType.COUNT, "count_col", "count_col", -1));
        result.getAggregationColumns().add(MergerTestUtil.createAggregationColumn(AggregationColumn.AggregationType.AVG, "avg_col", "avg_col", -1));
        return result;
    }
    
    @Test
    public void assertIsNotNeedMemorySortForGroupByWithoutGroupBy() throws SQLException {
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Collections.<String>emptyList()))), new MergeContext());
        assertFalse(actual.isNeedMemorySortForGroupBy());
    }
    
    @Test
    public void assertIsNeedMemorySortForGroupByWithGroupByAndOrderBySame() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Collections.singletonList("col")))), mergeContext);
        assertFalse(actual.isNeedMemorySortForGroupBy());
    }
    
    @Test
    public void assertIsNeedMemorySortForGroupByWithGroupByAndOrderByDifferent() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("order_col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "group_col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col")))), mergeContext);
        assertTrue(actual.isNeedMemorySortForGroupBy());
    }
    
    @Test
    public void assertSetGroupByKeysToCurrentOrderByKeys() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("order_col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "group_col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col")))), mergeContext);
        actual.setGroupByKeysToCurrentOrderByKeys();
        assertThat(actual.getCurrentOrderByKeys().size(), is(1));
        assertThat(actual.getCurrentOrderByKeys().get(0).getColumnName().get(), is("group_col"));
    }
    
    @Test
    public void assertIsNotNeedMemorySortForOrderByWithoutOrderBy() throws SQLException {
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Collections.<String>emptyList()))), new MergeContext());
        assertFalse(actual.isNeedMemorySortForOrderBy());
    }
    
    @Test
    public void assertIsNeedMemorySortForOrderByWithGroupByAndOrderBySame() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Collections.singletonList("col")))), mergeContext);
        assertFalse(actual.isNeedMemorySortForOrderBy());
    }
    
    @Test
    public void assertIsNeedMemorySortForOrderByWithGroupByAndOrderByDifferent() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("order_col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "group_col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col")))), mergeContext);
        actual.setGroupByKeysToCurrentOrderByKeys();
        assertTrue(actual.isNeedMemorySortForOrderBy());
    }
    
    @Test
    public void assertSetOrderByKeysToCurrentOrderByKeys() throws SQLException {
        MergeContext mergeContext = new MergeContext();
        mergeContext.getOrderByColumns().add(new OrderByColumn("order_col", OrderByColumn.OrderByType.ASC));
        mergeContext.getGroupByColumns().add(new GroupByColumn(Optional.<String>absent(), "group_col", Optional.<String>absent(), OrderByColumn.OrderByType.ASC));
        ResultSetMergeContext actual = new ResultSetMergeContext(new ShardingResultSets(Collections.singletonList(MergerTestUtil.mockResult(Arrays.asList("order_col", "group_col")))), mergeContext);
        actual.setGroupByKeysToCurrentOrderByKeys();
        actual.setOrderByKeysToCurrentOrderByKeys();
        assertThat(actual.getCurrentOrderByKeys().size(), is(1));
        assertThat(actual.getCurrentOrderByKeys().get(0).getColumnName().get(), is("order_col"));
    }
}
