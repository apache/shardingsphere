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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.IndexColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;

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
    
    private final SQLStatement sqlStatement;
    
    private final List<OrderBy> currentOrderByKeys;
    
    public ResultSetMergeContext(final ShardingResultSets shardingResultSets, final SQLStatement sqlStatement) {
        this.shardingResultSets = shardingResultSets;
        this.sqlStatement = sqlStatement;
        currentOrderByKeys = new LinkedList<>();
        init();
    }
    
    private void init() {
        setColumnIndex(((AbstractResultSetAdapter) shardingResultSets.getResultSets().get(0)).getColumnLabelIndexMap());
        currentOrderByKeys.addAll(sqlStatement.getOrderByList());
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
        result.addAll(sqlStatement.getGroupByList());
        result.addAll(sqlStatement.getOrderByList());
        LinkedList<AggregationSelectItem> allAggregationColumns = Lists.newLinkedList(sqlStatement.getAggregationSelectItems());
        while (!allAggregationColumns.isEmpty()) {
            AggregationSelectItem firstElement = allAggregationColumns.poll();
            result.add(firstElement);
            if (!firstElement.getDerivedAggregationSelectItems().isEmpty()) {
                allAggregationColumns.addAll(firstElement.getDerivedAggregationSelectItems());
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
        return !sqlStatement.getGroupByList().isEmpty() && !currentOrderByKeys.equals(transformGroupByColumnsToOrderByColumns());
    }
    
    /**
     * 将分组顺序设置为排序序列.
     */
    public void setGroupByKeysToCurrentOrderByKeys() {
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(transformGroupByColumnsToOrderByColumns());
    }
    
    private List<OrderBy> transformGroupByColumnsToOrderByColumns() {
        return Lists.transform(sqlStatement.getGroupByList(), new Function<GroupBy, OrderBy>() {
            
            @Override
            public OrderBy apply(final GroupBy input) {
                OrderBy result = input.getOwner().isPresent() ? new OrderBy(input.getOwner().get(), input.getName(), input.getOrderByType(), input.getAlias())
                        : new OrderBy(input.getName(), input.getOrderByType(), input.getAlias());
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
        return !sqlStatement.getOrderByList().isEmpty() && !currentOrderByKeys.equals(sqlStatement.getOrderByList());
    }
    
    /**
     * 将排序顺序设置为排序序列.
     */
    public void setOrderByKeysToCurrentOrderByKeys() {
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(sqlStatement.getOrderByList());
    }
}
