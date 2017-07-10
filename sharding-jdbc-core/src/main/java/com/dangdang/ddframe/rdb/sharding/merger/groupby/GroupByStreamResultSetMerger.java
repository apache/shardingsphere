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

import com.dangdang.ddframe.rdb.sharding.merger.common.AbstractStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 流式分组归并结果集接口.
 *
 * @author zhangliang
 */
public final class GroupByStreamResultSetMerger extends AbstractStreamResultSetMerger {
    
    private final Map<String, Integer> labelAndIndexMap;
    
    private final SelectStatement selectStatement;
    
    private final Map<Integer, Object> regularData;
    
    private final Map<Integer, Comparable<?>> aggregationData;
    
    private final Queue<OrderByValue> orderByValuesQueue;
    
    private boolean isFirstNext;
    
    private List<Comparable<?>> currentGroupByValues;
    
    public GroupByStreamResultSetMerger(final Map<String, Integer> labelAndIndexMap, final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        this.labelAndIndexMap = labelAndIndexMap;
        this.selectStatement = selectStatement;
        regularData = new HashMap<>(labelAndIndexMap.size(), 1);
        aggregationData = new HashMap<>(labelAndIndexMap.size(), 1);
        this.orderByValuesQueue = new PriorityQueue<>(resultSets.size());
        orderResultSetsToQueue(resultSets);
        isFirstNext = true;
        currentGroupByValues = orderByValuesQueue.isEmpty()
                ? Collections.<Comparable<?>>emptyList() : new GroupByValue(orderByValuesQueue.peek().getResultSet(), selectStatement.getGroupByItems()).getGroupValues();
    }
    
    private void orderResultSetsToQueue(final Collection<ResultSet> resultSets) throws SQLException {
        for (ResultSet each : resultSets) {
            OrderByValue orderByValue = new OrderByValue(each, selectStatement.getOrderByItems());
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
        if (!orderByValuesQueue.isEmpty()) {
            setCurrentResultSet(orderByValuesQueue.peek().getResultSet());
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap = Maps.toMap(selectStatement.getAggregationSelectItems(), new Function<AggregationSelectItem, AggregationUnit>() {
            
            @Override
            public AggregationUnit apply(final AggregationSelectItem input) {
                return AggregationUnitFactory.create(input.getType());
            }
        });
        if (isFirstNext) {
            nextInternal();
        }
        boolean hasNext = false;
        while (!orderByValuesQueue.isEmpty() && currentGroupByValues.equals(new GroupByValue(orderByValuesQueue.peek().getResultSet(), selectStatement.getGroupByItems()).getGroupValues())) {
            for (Entry<AggregationSelectItem, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
                List<Comparable<?>> values = new ArrayList<>(2);
                if (entry.getKey().getDerivedAggregationSelectItems().isEmpty()) {
                    values.add(getAggregationValue(entry.getKey()));
                } else {
                    for (AggregationSelectItem each : entry.getKey().getDerivedAggregationSelectItems()) {
                        values.add(getAggregationValue(each));
                    }
                }
                entry.getValue().merge(values);
            }
            for (int i = 0; i < orderByValuesQueue.peek().getResultSet().getMetaData().getColumnCount(); i++) {
                regularData.put(i + 1, orderByValuesQueue.peek().getResultSet().getObject(i + 1));
            }
            hasNext = nextInternal();
            if (!hasNext) {
                break;
            }
        }
        for (Entry<AggregationSelectItem, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
            aggregationData.put(entry.getKey().getIndex(), entry.getValue().getResult());
        }
        if (hasNext) {
            currentGroupByValues = new GroupByValue(orderByValuesQueue.peek().getResultSet(), selectStatement.getGroupByItems()).getGroupValues();
        }
        return true;
    }
    
    private boolean nextInternal() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        if (isFirstNext) {
            isFirstNext = false;
            return true;
        }
        OrderByValue firstOrderByValue = orderByValuesQueue.poll();
        if (firstOrderByValue.next()) {
            orderByValuesQueue.offer(firstOrderByValue);
        }
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        setCurrentResultSet(orderByValuesQueue.peek().getResultSet());
        return true;
    }
    
    private Comparable<?> getAggregationValue(final AggregationSelectItem aggregationSelectItem) throws SQLException {
        Object result = orderByValuesQueue.peek().getResultSet().getObject(aggregationSelectItem.getIndex());
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return aggregationData.containsKey(columnIndex) ? aggregationData.get(columnIndex) : regularData.get(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Preconditions.checkState(labelAndIndexMap.containsKey(columnLabel), String.format("Can't find columnLabel: %s", columnLabel));
        return aggregationData.containsKey(labelAndIndexMap.get(columnLabel)) ? aggregationData.get(labelAndIndexMap.get(columnLabel)) : regularData.get(labelAndIndexMap.get(columnLabel));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return aggregationData.containsKey(columnIndex) ? aggregationData.get(columnIndex) : regularData.get(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        Preconditions.checkState(labelAndIndexMap.containsKey(columnLabel), String.format("Can't find columnLabel: %s", columnLabel));
        return aggregationData.containsKey(labelAndIndexMap.get(columnLabel)) ? aggregationData.get(labelAndIndexMap.get(columnLabel)) : regularData.get(labelAndIndexMap.get(columnLabel));
    }
}
