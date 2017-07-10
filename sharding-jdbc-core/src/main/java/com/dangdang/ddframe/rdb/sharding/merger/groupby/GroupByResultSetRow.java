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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
public final class GroupByResultSetRow implements Comparable<GroupByResultSetRow> {
    
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private ResultSet resultSet;
    
    private final SelectStatement selectStatement;
    
    private final List<Comparable<?>> groupItemValues;
    
    private final List<Comparable<?>> orderItemValues;
    
    private final Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap;
    
    private final Object[] data;
    
    public GroupByResultSetRow(final ResultSet resultSet, final SelectStatement selectStatement) throws SQLException {
        this.resultSet = resultSet;
        data = load();
        this.resultSet = resultSet;
        this.selectStatement = selectStatement;
        groupItemValues = getGroupItemValues();
        orderItemValues = getOrderItemValues();
        aggregationUnitMap = Maps.toMap(selectStatement.getAggregationSelectItems(), new Function<AggregationSelectItem, AggregationUnit>() {
            
            @Override
            public AggregationUnit apply(final AggregationSelectItem input) {
                return AggregationUnitFactory.create(input.getType());
            }
        });
    }
    
    private Object[] load() throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        Object[] result = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            result[i] = resultSet.getObject(i + 1);
        }
        return result;
    }
    
    /**
     * 获取数据.
     * 
     * @param columnIndex 列索引
     * @return 数据
     */
    public Object getCell(final int columnIndex) {
        Preconditions.checkArgument(columnIndex > 0 && columnIndex < data.length + 1);
        return data[columnIndex - 1];
    }
    
    /**
     * 获取分组值.
     *
     * @return 分组值集合
     * @throws SQLException SQL异常
     */
    public List<Comparable<?>> getGroupItemValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(selectStatement.getGroupByItems().size());
        for (OrderItem each : selectStatement.getGroupByItems()) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(value instanceof Comparable, "Group by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
    
    private List<Comparable<?>> getOrderItemValues() {
        List<Comparable<?>> result = new ArrayList<>(selectStatement.getOrderByItems().size());
        for (OrderItem each : selectStatement.getOrderByItems()) {
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
        setResultSet(resultSet);
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
            data[each.getIndex() - 1] = aggregationUnitMap.get(each).getResult();
        }
    }
    
    @Override
    public int compareTo(final GroupByResultSetRow o) {
        if (!selectStatement.getOrderByItems().isEmpty()) {
            for (int i = 0; i < selectStatement.getOrderByItems().size(); i++) {
                int result = compareTo(orderItemValues.get(i), o.orderItemValues.get(i), selectStatement.getOrderByItems().get(i).getType());
                if (0 != result) {
                    return result;
                }
            }
        } else {
            for (int i = 0; i < selectStatement.getGroupByItems().size(); i++) {
                int result = compareTo(groupItemValues.get(i), o.groupItemValues.get(i), selectStatement.getGroupByItems().get(i).getType());
                if (0 != result) {
                    return result;
                }
            }
        }
        return 0;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderType type) {
        return OrderType.ASC == type ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
    }
}
