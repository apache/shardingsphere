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
import com.dangdang.ddframe.rdb.sharding.merger.common.AbstractMemoryResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.common.MemoryResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 内存分组归并结果集接口.
 *
 * @author zhangliang
 */
public final class GroupByMemoryResultSetMerger extends AbstractMemoryResultSetMerger {
    
    private final SelectStatement selectStatement;
    
    private final OrderType nullOrderType;
    
    private final Iterator<MemoryResultSetRow> memoryResultSetRows;
    
    public GroupByMemoryResultSetMerger(
            final Map<String, Integer> labelAndIndexMap, final List<ResultSet> resultSets, final SelectStatement selectStatement, final OrderType nullOrderType) throws SQLException {
        super(labelAndIndexMap);
        this.selectStatement = selectStatement;
        this.nullOrderType = nullOrderType;
        memoryResultSetRows = init(resultSets);
    }
    
    private Iterator<MemoryResultSetRow> init(final List<ResultSet> resultSets) throws SQLException {
        Map<GroupByValue, MemoryResultSetRow> dataMap = new HashMap<>(1024);
        Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap = new HashMap<>(1024);
        for (ResultSet each : resultSets) {
            while (each.next()) {
                GroupByValue groupByValue = new GroupByValue(each, selectStatement.getGroupByItems());
                initForFirstGroupByValue(each, groupByValue, dataMap, aggregationMap);
                aggregate(each, groupByValue, aggregationMap);
            }
        }
        setAggregationValueToMemoryRow(dataMap, aggregationMap);
        List<MemoryResultSetRow> result = getMemoryResultSetRows(dataMap);
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    private void initForFirstGroupByValue(final ResultSet resultSet, final GroupByValue groupByValue, final Map<GroupByValue, MemoryResultSetRow> dataMap, 
                                          final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) throws SQLException {
        if (!dataMap.containsKey(groupByValue)) {
            dataMap.put(groupByValue, new MemoryResultSetRow(resultSet));
        }
        if (!aggregationMap.containsKey(groupByValue)) {
            Map<AggregationSelectItem, AggregationUnit> map = Maps.toMap(selectStatement.getAggregationSelectItems(), new Function<AggregationSelectItem, AggregationUnit>() {
                
                @Override
                public AggregationUnit apply(final AggregationSelectItem input) {
                    return AggregationUnitFactory.create(input.getType());
                }
            });
            aggregationMap.put(groupByValue, map);
        }
    }
    
    private void aggregate(final ResultSet resultSet, final GroupByValue groupByValue, final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) throws SQLException {
        for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
            List<Comparable<?>> values = new ArrayList<>(2);
            if (each.getDerivedAggregationSelectItems().isEmpty()) {
                values.add(getAggregationValue(resultSet, each));
            } else {
                for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                    values.add(getAggregationValue(resultSet, derived));
                }
            }
            aggregationMap.get(groupByValue).get(each).merge(values);
        }
    }
    
    private Comparable<?> getAggregationValue(final ResultSet resultSet, final AggregationSelectItem aggregationSelectItem) throws SQLException {
        Object result = resultSet.getObject(aggregationSelectItem.getIndex());
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    private void setAggregationValueToMemoryRow(final Map<GroupByValue, MemoryResultSetRow> dataMap, final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) {
        for (Entry<GroupByValue, MemoryResultSetRow> entry : dataMap.entrySet()) {
            for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
                entry.getValue().setCell(each.getIndex(), aggregationMap.get(entry.getKey()).get(each).getResult());
            }
        }
    }
    
    private List<MemoryResultSetRow> getMemoryResultSetRows(final Map<GroupByValue, MemoryResultSetRow> dataMap) {
        List<MemoryResultSetRow> result = new ArrayList<>(dataMap.values());
        Collections.sort(result, new GroupByRowComparator(selectStatement, nullOrderType));
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (memoryResultSetRows.hasNext()) {
            setCurrentResultSetRow(memoryResultSetRows.next());
            return true;
        }
        return false;
    }
}
