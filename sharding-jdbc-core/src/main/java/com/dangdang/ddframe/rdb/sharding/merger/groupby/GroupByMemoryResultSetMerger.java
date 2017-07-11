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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    private final Map<GroupByValue, MemoryResultSetRow> dataMap;
    
    private final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationUnitMap;
    
    private Iterator<MemoryResultSetRow> data;
    
    public GroupByMemoryResultSetMerger(final Map<String, Integer> labelAndIndexMap, final List<ResultSet> resultSets, final SelectStatement selectStatement) throws SQLException {
        super(labelAndIndexMap);
        this.selectStatement = selectStatement;
        dataMap = new HashMap<>(1024);
        aggregationUnitMap = new HashMap<>(1024);
        init(resultSets);
    }
    
    private void init(final List<ResultSet> resultSets) throws SQLException {
        for (ResultSet each : resultSets) {
            while (each.next()) {
                GroupByValue groupByValue = new GroupByValue(each, selectStatement.getGroupByItems());
                MemoryResultSetRow memoryResultSetRow = new MemoryResultSetRow(each);
                if (!dataMap.containsKey(groupByValue)) {
                    dataMap.put(groupByValue, memoryResultSetRow);
                }
                if (!aggregationUnitMap.containsKey(groupByValue)) {
                    Map<AggregationSelectItem, AggregationUnit> map = Maps.toMap(selectStatement.getAggregationSelectItems(), new Function<AggregationSelectItem, AggregationUnit>() {
                        
                        @Override
                        public AggregationUnit apply(final AggregationSelectItem input) {
                            return AggregationUnitFactory.create(input.getType());
                        }
                    });
                    aggregationUnitMap.put(groupByValue, map);
                }
                for (AggregationSelectItem aggregationSelectItem : selectStatement.getAggregationSelectItems()) {
                    List<Comparable<?>> values = new ArrayList<>(2);
                    if (aggregationSelectItem.getDerivedAggregationSelectItems().isEmpty()) {
                        values.add(getAggregationValue(each, aggregationSelectItem));
                    } else {
                        for (AggregationSelectItem derivedAggregationSelectItem : aggregationSelectItem.getDerivedAggregationSelectItems()) {
                            values.add(getAggregationValue(each, derivedAggregationSelectItem));
                        }
                    }
                    aggregationUnitMap.get(groupByValue).get(aggregationSelectItem).merge(values);
                }
            }
        }
        for (Entry<GroupByValue, MemoryResultSetRow> entry : dataMap.entrySet()) {
            for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
                entry.getValue().setCell(each.getIndex(), aggregationUnitMap.get(entry.getKey()).get(each).getResult());
            }
            
        }
        List<MemoryResultSetRow> data = new ArrayList<>(dataMap.values());
        Collections.sort(data, new Comparator<MemoryResultSetRow>() {
            
            @Override
            public int compare(final MemoryResultSetRow o1, final MemoryResultSetRow o2) {
                if (!selectStatement.getOrderByItems().isEmpty()) {
                    return compare(o1, o2, selectStatement.getOrderByItems());
                }
                return compare(o1, o2, selectStatement.getGroupByItems());
            }
            
            private int compare(final MemoryResultSetRow o1, final MemoryResultSetRow o2, final List<OrderItem> orderItems) {
                for (OrderItem each : orderItems) {
                    Object orderValue1 = o1.getCell(each.getIndex());
                    Preconditions.checkState(orderValue1 instanceof Comparable, "Order by value must implements Comparable");
                    Object orderValue2 = o2.getCell(each.getIndex());
                    Preconditions.checkState(orderValue2 instanceof Comparable, "Order by value must implements Comparable");
                    int result = compareTo((Comparable) orderValue1, (Comparable) orderValue2, each.getType());
                    if (0 != result) {
                        return result;
                    }
                }
                return 0;
            }
            
            @SuppressWarnings({ "rawtypes", "unchecked" })
            private int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderType type) {
                return OrderType.ASC == type ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
            }
        });
        this.data = data.iterator();
        if (!data.isEmpty()) {
            setCurrentResultSetRow(data.get(0));
        }
    }
    
    private Comparable<?> getAggregationValue(final ResultSet resultSet, final AggregationSelectItem aggregationSelectItem) throws SQLException {
        Object result = resultSet.getObject(aggregationSelectItem.getIndex());
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (data.hasNext()) {
            setCurrentResultSetRow(data.next());
            return true;
        }
        return false;
    }
}
