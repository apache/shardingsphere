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
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByStreamResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 流式分组归并结果集接口.
 *
 * @author zhangliang
 */
public final class GroupByStreamResultSetMerger extends OrderByStreamResultSetMerger {
    
    private final Map<String, Integer> labelAndIndexMap;
    
    private final SelectStatement selectStatement;
    
    private final List<Object> currentRow;
    
    private List<?> currentGroupByValues;
    
    public GroupByStreamResultSetMerger(
            final Map<String, Integer> labelAndIndexMap, final List<ResultSet> resultSets, final SelectStatement selectStatement, final OrderType nullOrderType) throws SQLException {
        super(resultSets, selectStatement.getOrderByItems(), nullOrderType);
        this.labelAndIndexMap = labelAndIndexMap;
        this.selectStatement = selectStatement;
        currentRow = new ArrayList<>(labelAndIndexMap.size());
        currentGroupByValues = getOrderByValuesQueue().isEmpty() ? Collections.emptyList() : new GroupByValue(getCurrentResultSet(), selectStatement.getGroupByItems()).getGroupValues();
    }
    
    @Override
    public boolean next() throws SQLException {
        currentRow.clear();
        if (getOrderByValuesQueue().isEmpty()) {
            return false;
        }
        if (isFirstNext()) {
            super.next();
        }
        if (aggregateCurrentGroupByRowAndNext()) {
            currentGroupByValues = new GroupByValue(getCurrentResultSet(), selectStatement.getGroupByItems()).getGroupValues();
        }
        return true;
    }
    
    private boolean aggregateCurrentGroupByRowAndNext() throws SQLException {
        boolean result = false;
        Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap = Maps.toMap(selectStatement.getAggregationSelectItems(), new Function<AggregationSelectItem, AggregationUnit>() {
            
            @Override
            public AggregationUnit apply(final AggregationSelectItem input) {
                return AggregationUnitFactory.create(input.getType());
            }
        });
        while (currentGroupByValues.equals(new GroupByValue(getCurrentResultSet(), selectStatement.getGroupByItems()).getGroupValues())) {
            aggregate(aggregationUnitMap);
            cacheCurrentRow();
            result = super.next();
            if (!result) {
                break;
            }
        }
        setAggregationValueToCurrentRow(aggregationUnitMap);
        return result;
    }
    
    private void aggregate(final Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap) throws SQLException {
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
    }
    
    private void cacheCurrentRow() throws SQLException {
        for (int i = 0; i < getCurrentResultSet().getMetaData().getColumnCount(); i++) {
            currentRow.add(getCurrentResultSet().getObject(i + 1));
        }
    }
    
    private Comparable<?> getAggregationValue(final AggregationSelectItem aggregationSelectItem) throws SQLException {
        Object result = getCurrentResultSet().getObject(aggregationSelectItem.getIndex());
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    private void setAggregationValueToCurrentRow(final Map<AggregationSelectItem, AggregationUnit> aggregationUnitMap) {
        for (Entry<AggregationSelectItem, AggregationUnit> entry : aggregationUnitMap.entrySet()) {
            currentRow.set(entry.getKey().getIndex() - 1, entry.getValue().getResult());
        }
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Preconditions.checkState(labelAndIndexMap.containsKey(columnLabel), String.format("Can't find columnLabel: %s", columnLabel));
        return currentRow.get(labelAndIndexMap.get(columnLabel) - 1);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return currentRow.get(columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        Preconditions.checkState(labelAndIndexMap.containsKey(columnLabel), String.format("Can't find columnLabel: %s", columnLabel));
        return currentRow.get(labelAndIndexMap.get(columnLabel) - 1);
    }
}
