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

package com.dangdang.ddframe.rdb.sharding.merger.row;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * 分组行.
 * 
 * @author gaohongtao
 */
@Slf4j
public class GroupByRow extends Row {
    
    private final ResultSet resultSet;
    
    private final List<GroupByColumn> groupByColumns;
    
    private final List<AggregationColumn> aggregationColumns;
    
    private final Map<AggregationColumn, AggregationUnit> aggregationUnitMap;
    
    public GroupByRow(final ResultSet resultSet, final List<GroupByColumn> groupByColumns, final List<AggregationColumn> aggregationColumns) throws SQLException {
        super(resultSet);
        this.resultSet = resultSet;
        this.groupByColumns = groupByColumns;
        this.aggregationColumns = aggregationColumns;
        aggregationUnitMap = new HashMap<>(aggregationColumns.size());
        for (AggregationColumn each : aggregationColumns) {
            aggregationUnitMap.put(each, AggregationUnitFactory.create(each.getAggregationType()));
        }
    }
    
    public void aggregate() throws SQLException {
        for (Map.Entry<AggregationColumn, AggregationUnit> each : aggregationUnitMap.entrySet()) {
            List<AggregationColumn> mergingAggregationColumns = each.getKey().getDerivedColumns().isEmpty() ? Collections.singletonList(each.getKey()) : Lists.newArrayList(each.getKey().getDerivedColumns());
            each.getValue().merge(Lists.transform(mergingAggregationColumns, new Function<IndexColumn, Comparable<?>>() {
            
                @Override
                public Comparable<?> apply(final IndexColumn input) {
                    return (Comparable<?>) getValueSilently(input.getColumnIndex());
                }
            }));
        }
    }
    
    public void generateResult() {
        for (AggregationColumn each : aggregationUnitMap.keySet()) {
            setCell(each.getColumnIndex(), aggregationUnitMap.get(each).getResult());
        }
    }
    
    public List<Object> getGroupByKey() {
        List<Object> result = new ArrayList<>(groupByColumns.size());
        for (GroupByColumn each : groupByColumns) {
            result.add(getValueSilently(each.getColumnIndex()));
        }
        return result;
    }
    
    private Object getValueSilently(final int index) {
        try {
            return resultSet.getObject(index);
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("GroupByKey is: ");
        result.append(Lists.transform(groupByColumns, new Function<GroupByColumn, Object>() {
            @Override
            public Object apply(final GroupByColumn input) {
                return getCell(input.getColumnIndex());
            }
        }));
        if (aggregationColumns.isEmpty()) {
            return result.toString();
        }
        result.append("; Aggregation result is: ").append(Lists.transform(aggregationColumns, new Function<AggregationColumn, String>() {
            @Override
            public String apply(final AggregationColumn input) {
                Object value = getCell(input.getColumnIndex());
                value = null == value ? "null" : value;
                return String.format("{index:%d, type:%s, value:%s}", input.getColumnIndex(), input.getAggregationType(), value);
            }
        }));
        return result.toString();
    }
}
