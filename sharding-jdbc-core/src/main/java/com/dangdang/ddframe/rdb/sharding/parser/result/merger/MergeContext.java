/**
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

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.merger.component.other.WrapperResultSet;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 结果归并上下文.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class MergeContext {
    
    private final List<OrderByColumn> orderByColumns = new ArrayList<>();
    
    private final List<GroupByColumn> groupByColumns = new ArrayList<>();
    
    private final List<AggregationColumn> aggregationColumns = new ArrayList<>();
    
    @Setter
    private Limit limit;
    
    @Setter
    private ExecutorEngine executorEngine;
    
    private Map<String, Integer> columnLabelIndexMap;
    
    private final List<OrderByColumn> currentOrderByKeys = new LinkedList<>();
    
    /**
     * 是否包含分组.
     * 
     * @return 是否包含分组
     */
    public boolean hasGroupBy() {
        return !groupByColumns.isEmpty();
    }
    
    /**
     * 判断是否为分组或者聚合计算.
     * 此处将聚合计算想象成为特殊的分组计算,统一进行处理.
     *
     * @return true:是分组或者聚合计算 false:不是分组且不是聚合计算
     */
    public boolean hasGroupByOrAggregation() {
        return hasGroupBy() || !aggregationColumns.isEmpty();
    }
    
    /**
     * 是否包含排序列.
     * 
     * @return true 包含 false 不包含
     */
    public boolean hasOrderBy() {
        return !orderByColumns.isEmpty();
    }
    
    /**
     * 判断是否为排序计算并且是否需要进行排序.
     *
     * @return true:是排序计算 false:不是排序计算
     */
    public boolean needToSort() {
        return hasOrderBy() && !equalsOrderByKeys(orderByColumns);
    }
    
    /**
     * 分组排序结果与底层排序是否相同.
     * 不同就修改底层排序键为分组排序键
     *
     * @return true 相同 false 不同
     */
    public boolean groupByKeysEqualsOrderByKeys() {
        List<OrderByColumn> orderByKeysFromGroupByColumns = Lists.transform(groupByColumns, new Function<GroupByColumn, OrderByColumn>() {
            
            @Override
            public OrderByColumn apply(final GroupByColumn input) {
                OrderByColumn result = new OrderByColumn(input.getName(), input.getAlias(), input.getOrderByType());
                result.setColumnIndex(input.getColumnIndex());
                return result;
            }
        });
        return equalsOrderByKeys(orderByKeysFromGroupByColumns);
    }
    
    private boolean equalsOrderByKeys(final List<OrderByColumn> expectedOrderByKeys) {
        if (currentOrderByKeys.equals(expectedOrderByKeys)) {
            return true;
        }
        currentOrderByKeys.clear();
        currentOrderByKeys.addAll(expectedOrderByKeys);
        return false;
    }
    
    /**
     * 判断是否有限定结果集计算.
     * 
     * @return true:是限定结果集计算 false:不是限定结果集计算
     */
    public boolean hasLimit() {
        return limit != null;
    }
    
    /**
     * 使用结果集构造合并上下文信息.
     *
     * @param resultSet 结果集
     * @throws SQLException 访问结果集可能抛出的异常抛出
     */
    public void buildContextWithResultSet(final WrapperResultSet resultSet) throws SQLException {
        columnLabelIndexMap = resultSet.getColumnLabelIndexMap();
        setColumnIndex();
        currentOrderByKeys.addAll(orderByColumns);
    }
    
    private void setColumnIndex() {
        for (IndexColumn each : extractFocusedColumns()) {
            if (each.getColumnIndex() > 0) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel().orNull()) || columnLabelIndexMap.containsKey(each.getColumnName().orNull()),
                    String.format("%s has not index", each));
            if (each.getColumnLabel().isPresent() && columnLabelIndexMap.containsKey(each.getColumnLabel().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnLabel().get()));
            } else if (each.getColumnName().isPresent() && columnLabelIndexMap.containsKey(each.getColumnName().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnName().get()));
            }
        }
    }
    
    private List<IndexColumn> extractFocusedColumns() {
        List<IndexColumn> result = new LinkedList<>();
        result.addAll(groupByColumns);
        result.addAll(orderByColumns);
        LinkedList<AggregationColumn> allAggregationColumns = Lists.newLinkedList(aggregationColumns);
        while (allAggregationColumns.size() > 0) {
            AggregationColumn head = allAggregationColumns.poll();
            result.add(head);
            if (head.getDerivedColumns().isEmpty()) {
                continue;
            }
            allAggregationColumns.addAll(head.getDerivedColumns());
        }
        return result;
    }
}
