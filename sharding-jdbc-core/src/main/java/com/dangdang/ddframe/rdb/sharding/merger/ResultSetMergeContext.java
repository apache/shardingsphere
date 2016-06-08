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

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 结果集归并上下文.
 *
 * @author zhangliang
 */
@Getter
public final class ResultSetMergeContext {
    
    private final ShardingResultSets shardingResultSets;
    
    private final MergeContext mergeContext;
    
    private final List<OrderByColumn> currentOrderByKeys;
    
    public ResultSetMergeContext(final ShardingResultSets shardingResultSets, final MergeContext mergeContext) throws SQLException {
        this.shardingResultSets = shardingResultSets;
        this.mergeContext = mergeContext;
        currentOrderByKeys = new LinkedList<>();
        init();
    }
    
    private void init() throws SQLException {
        setColumnIndex(((AbstractResultSetAdapter) shardingResultSets.getResultSets().get(0)).getColumnLabelIndexMap());
        currentOrderByKeys.addAll(mergeContext.getOrderByColumns());
    }
    
    private void setColumnIndex(final Map<String, Integer> columnLabelIndexMap) {
        for (IndexColumn each : getAllFocusedColumns()) {
            if (each.getColumnIndex() > 0) {
                continue;
            }
            Preconditions.checkState(
                    columnLabelIndexMap.containsKey(each.getColumnLabel().orNull()) || columnLabelIndexMap.containsKey(each.getColumnName().orNull()), String.format("%s has not index", each));
            if (each.getColumnLabel().isPresent() && columnLabelIndexMap.containsKey(each.getColumnLabel().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnLabel().get()));
            } else if (each.getColumnName().isPresent() && columnLabelIndexMap.containsKey(each.getColumnName().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnName().get()));
            }
        }
    }
    
    private List<IndexColumn> getAllFocusedColumns() {
        List<IndexColumn> result = new LinkedList<>();
        result.addAll(mergeContext.getGroupByColumns());
        result.addAll(mergeContext.getOrderByColumns());
        LinkedList<AggregationColumn> allAggregationColumns = Lists.newLinkedList(mergeContext.getAggregationColumns());
        while (!allAggregationColumns.isEmpty()) {
            AggregationColumn firstElement = allAggregationColumns.poll();
            result.add(firstElement);
            if (!firstElement.getDerivedColumns().isEmpty()) {
                allAggregationColumns.addAll(firstElement.getDerivedColumns());
            }
        }
        return result;
    }
    
    /**
     * 判断分组归并是否需要内存排序.
     *
     * @return 分组归并是否需要内存排序
     */
    public boolean isNeedMemorySortForGroupBy() {
        return mergeContext.hasGroupBy() && !currentOrderByKeys.equals(transformGroupByColumnsToOrderByColumns());
    }
    
    /**
     * 将分组顺序设置为排序序列.
     */
    public void setGroupByKeysToCurrentOrderByKeys() {
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(transformGroupByColumnsToOrderByColumns());
    }
    
    private List<OrderByColumn> transformGroupByColumnsToOrderByColumns() {
        return Lists.transform(mergeContext.getGroupByColumns(), new Function<GroupByColumn, OrderByColumn>() {
            
            @Override
            public OrderByColumn apply(final GroupByColumn input) {
                OrderByColumn result = new OrderByColumn(input.getOwner(), input.getName().get(), input.getAlias(), input.getOrderByType());
                result.setColumnIndex(input.getColumnIndex());
                return result;
            }
        });
    }
    
    /**
     * 判断排序归并是否需要内存排序.
     *
     * @return 排序归并是否需要内存排序
     */
    public boolean isNeedMemorySortForOrderBy() {
        return mergeContext.hasOrderBy() && !currentOrderByKeys.equals(mergeContext.getOrderByColumns());
    }
    
    /**
     * 将排序顺序设置为排序序列.
     */
    public void setOrderByKeysToCurrentOrderByKeys() {
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(mergeContext.getOrderByColumns());
    }
}
