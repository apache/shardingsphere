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

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.google.common.base.Preconditions;
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
    
    private final DatabaseType dbType;
    
    private final ShardingResultSets shardingResultSets;
    
    private final SQLStatement sqlStatement;
    
    private final List<OrderItem> currentOrderByKeys;
    
    public ResultSetMergeContext(final DatabaseType dbType, final ShardingResultSets shardingResultSets, final SQLStatement sqlStatement) {
        this.dbType = dbType;
        this.shardingResultSets = shardingResultSets;
        this.sqlStatement = sqlStatement;
        currentOrderByKeys = new LinkedList<>(sqlStatement.getOrderByList());
        Map<String, Integer> columnLabelIndexMap = ((AbstractResultSetAdapter) shardingResultSets.getResultSets().get(0)).getColumnLabelIndexMap();
        setIndexForAggregationItem(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, sqlStatement.getOrderByList());
        setIndexForOrderItem(columnLabelIndexMap, sqlStatement.getGroupByList());
    }
    
    private void setIndexForAggregationItem(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : sqlStatement.getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("%s has not index", each));
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), String.format("%s has not index", derived));
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            if (-1 != each.getIndex()) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("%s has not index", each));
            if (columnLabelIndexMap.containsKey(each.getColumnLabel())) {
                each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            }
        }
    }
    
    /**
     * 判断分组归并是否需要内存排序.
     *
     * @return 分组归并是否需要内存排序
     */
    public boolean isNeedMemorySortForGroupBy() {
        return !sqlStatement.getGroupByList().isEmpty() && !sqlStatement.getOrderByList().equals(sqlStatement.getGroupByList());
    }
    
    /**
     * 将分组顺序设置为排序序列.
     */
    public void setGroupByKeysToCurrentOrderByKeys() {
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(sqlStatement.getGroupByList());
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
