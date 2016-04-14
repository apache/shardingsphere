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

import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    
    /**
     * 判断是否为分组或者聚合计算.
     * 此处将聚合计算想象成为特殊的分组计算,统一进行处理.
     *
     * @return true:是分组或者聚合计算 false:不是分组且不是聚合计算
     */
    public boolean hasGroupByOrAggregation() {
        return !groupByColumns.isEmpty() || !aggregationColumns.isEmpty();
    }
    
    /**
     * 判断是否为排序计算.
     *
     * @return true:是排序计算 false:不是排序计算
     */
    public boolean hasOrderBy() {
        return !orderByColumns.isEmpty();
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
     * 将分组列转换为排序列的形式.
     * 
     * @return 排序列
     */
    public List<OrderByColumn> transformGroupByColumnToOrderByColumn() {
        return Lists.transform(groupByColumns, new Function<GroupByColumn, OrderByColumn>() {
            
            @Override
            public OrderByColumn apply(final GroupByColumn input) {
                OrderByColumn result = new OrderByColumn(input.getName(), input.getAlias(), input.getOrderByType());
                result.setColumnIndex(input.getColumnIndex());
                return result;
            }
        });
    }
    
    /**
     * 获取结果集类型.
     * 
     * @return 结果集类型
     */
    public ResultSetType getResultSetType() {
        if (!groupByColumns.isEmpty()) {
            return ResultSetType.GroupBy;
        }
        if (!aggregationColumns.isEmpty()) {
            return ResultSetType.Aggregate;
        }
        if (!orderByColumns.isEmpty()) {
            return ResultSetType.OrderBy;
        }
        return ResultSetType.Iterator;
    }
    
    /**
     * 获取所有结果归并需要关注的列集合.
     * 
     * @return 结果归并需要关注的列集合
     */
    public List<IndexColumn> getMergeFocusedColumns() {
        List<IndexColumn> result = new LinkedList<>();
        result.addAll(groupByColumns);
        result.addAll(orderByColumns);
        result.addAll(getAggregationColumnsWithDerivedColumns());
        return result;
    }
    
    // TODO 派生列是否有递归?
    private List<AggregationColumn> getAggregationColumnsWithDerivedColumns() {
        List<AggregationColumn> result = new ArrayList<>();
        for (AggregationColumn each : aggregationColumns) {
            result.add(each);
            if (!each.getDerivedColumns().isEmpty()) {
                result.addAll(each.getDerivedColumns());
            }
        }
        return result;
    }
    
    /**
     * 结果集类型.
     * 
     * @author zhangliang
     */
    public enum ResultSetType {
        Iterator, OrderBy, Aggregate, GroupBy
    }
}
