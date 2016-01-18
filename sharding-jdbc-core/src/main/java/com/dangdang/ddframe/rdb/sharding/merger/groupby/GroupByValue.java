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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationValue;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;

import lombok.ToString;

/**
 * 分组结果集数据存储对象.
 * 
 * @author gaohongtao, zhangliang
 */
@ToString
public class GroupByValue implements AggregationValue, Comparable<GroupByValue> {
    
    private final Map<Integer, Comparable<?>> indexMap = new LinkedHashMap<>();
    
    private final CaseInsensitiveMap<String, Comparable<?>> columnLabelMap = new CaseInsensitiveMap<>();
    
    private final List<OrderByColumn> orderColumns = new ArrayList<>();
    
    private final List<GroupByColumn> groupByColumns = new ArrayList<>();
    
    public void put(final int index, final String columnName, final Comparable<?> value) {
        if (!indexMap.containsKey(index)) {
            indexMap.put(index, value);
        }
        if (!columnLabelMap.containsKey(columnName)) {
            columnLabelMap.put(columnName, value);
        }
    }
    
    @Override
    public Comparable<?> getValue(final ResultSetQueryIndex resultSetQueryIndex) {
        return resultSetQueryIndex.isQueryBySequence() ? indexMap.get(resultSetQueryIndex.getQueryIndex()) : columnLabelMap.get(resultSetQueryIndex.getQueryName());
    }
    
    public void addGroupByColumns(final List<GroupByColumn> columns) {
        groupByColumns.addAll(columns);
    }
    
    public void addOrderColumns(final List<OrderByColumn> columns) {
        orderColumns.addAll(columns);
    }
    
    @Override
    public int compareTo(final GroupByValue other) {
        if (null == other) {
            return -1;
        }
        if (orderColumns.isEmpty()) {
            return compareFromGroupByColumns(other);
        }
        return compareFromOrderByColumns(other);
    }
    
    private int compareFromGroupByColumns(final GroupByValue other) {
        for (GroupByColumn each : groupByColumns) {
            int result = ResultSetUtil.compareTo(columnLabelMap.get(each.getAlias()), other.columnLabelMap.get(each.getAlias()), each.getOrderByType());
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
    
    private int compareFromOrderByColumns(final GroupByValue other) {
        for (OrderByColumn each : orderColumns) {
            OrderByType orderByType = null == each.getOrderByType() ? OrderByType.ASC : each.getOrderByType();
            Comparable<?> thisValue;
            Comparable<?> otherValue;
            if (each.getName().isPresent()) {
                thisValue = columnLabelMap.get(each.getName().get());
                otherValue = other.columnLabelMap.get(each.getName().get());
            } else {
                thisValue = indexMap.get(each.getIndex().get());
                otherValue = other.indexMap.get(each.getIndex().get());
            }
            int result = ResultSetUtil.compareTo(thisValue, otherValue, orderByType);
            if (0 != result) {
                return result;
            }
        }
        return 0;
    }
}
