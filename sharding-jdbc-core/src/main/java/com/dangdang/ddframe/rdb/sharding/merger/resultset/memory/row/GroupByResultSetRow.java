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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 具有分组功能的数据行对象.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class GroupByResultSetRow extends AbstractResultSetRow implements Comparable<GroupByResultSetRow> {
    
    private final ResultSet resultSet;
    
    private final List<OrderItem> groupByItems;
    
    private final List<OrderItem> orderByItems;
    
    private final List<Comparable<?>> groupItemValues;
    
    private final List<Comparable<?>> orderItemValues;
    
    private final Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap;
    
    public GroupByResultSetRow(
            final ResultSet resultSet, final List<OrderItem> groupByItems, final List<OrderItem> orderByItems, final List<AggregationSelectItem> aggregationSelectItems) throws SQLException {
        super(resultSet);
        this.resultSet = resultSet;
        this.groupByItems = groupByItems;
        this.orderByItems = orderByItems;
        groupItemValues = getGroupItemValues();
        orderItemValues = getOrderItemValues();
        aggregationUnitMap = Maps.toMap(aggregationSelectItems, new Function<AggregationSelectItem, AggregationUnit>() {
            
            @Override
            public AggregationUnit apply(final AggregationSelectItem input) {
                return AggregationUnitFactory.create(input.getType());
            }
        });
    }
    
    /**
     * 获取分组值.
     *
     * @return 分组值集合
     * @throws SQLException SQL异常
     */
    public List<Comparable<?>> getGroupItemValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(groupByItems.size());
        for (OrderItem each : groupByItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(value instanceof Comparable, "Group by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    private List<Comparable<?>> getOrderItemValues() {
        List<Comparable<?>> result = new ArrayList<>(orderByItems.size());
        for (OrderItem each : orderByItems) {
            Object value = getCell(each.getIndex());
            Preconditions.checkState(value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    /**
     * 处理聚合函数结果集.
     * 
     * @param resultSet 结果集
     * @throws SQLException SQL异常
     */
    public void aggregate(final ResultSet resultSet) throws SQLException {
        for (Entry<AggregationSelectItem, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
            entry.getValue().merge(getAggregationValues(resultSet, 
                    entry.getKey().getDerivedAggregationSelectItems().isEmpty() ? Collections.singletonList(entry.getKey()) : entry.getKey().getDerivedAggregationSelectItems()));
        }
    }
    
    private List<Comparable<?>> getAggregationValues(final ResultSet resultSet, final List<AggregationSelectItem> aggregationSelectItems) throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(aggregationSelectItems.size());
        for (AggregationSelectItem each : aggregationSelectItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(null == value || value instanceof Comparable, "Aggregation value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    /**
     * 生成结果.
     */
    public void generateResult() {
        for (AggregationSelectItem each : aggregationUnitMap.keySet()) {
            setCell(each.getIndex(), aggregationUnitMap.get(each).getResult());
        }
    }
    
    @Override
    public int compareTo(final GroupByResultSetRow o) {
        if (!orderByItems.isEmpty()) {
            for (int i = 0; i < orderByItems.size(); i++) {
                int result = compareTo(orderItemValues.get(i), o.orderItemValues.get(i), orderByItems.get(i).getType());
                if (0 != result) {
                    return result;
                }
            }
        } else {
            for (int i = 0; i < groupByItems.size(); i++) {
                int result = compareTo(groupItemValues.get(i), o.groupItemValues.get(i), groupByItems.get(i).getType());
                if (0 != result) {
                    return result;
                }
            }
        }
        return 0;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderType type) {
        return OrderType.ASC == type ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
    }
}
