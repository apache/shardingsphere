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

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO javadoc
/**
 * @author gaohongtao
 */
@Slf4j
public class GroupByRow extends Row {
    
    private final ResultSet resultSet;
    
    private final List<GroupByColumn> groupByColumns;
    
    private final List<AggregationColumn> aggregationColumns;
    
    public GroupByRow(final ResultSet resultSet, final List<GroupByColumn> groupByColumns, final List<AggregationColumn> aggregationColumns) throws SQLException {
        super(resultSet);
        this.resultSet = resultSet;
        this.groupByColumns = groupByColumns;
        this.aggregationColumns = aggregationColumns;
    }
    
    // TODO javadoc
    public boolean aggregate() throws SQLException {
        Map<AggregationColumn, AggregationUnit> aggregationUnitMap = null;
        if (!aggregationColumns.isEmpty()) {
            aggregationUnitMap = new HashMap<>(aggregationColumns.size());
        }
        List<Object> groupByKey = getGroupByKey(groupByColumns);
        log.trace("Group {} start", groupByKey);
        boolean hasNext = false;
        do {
            if (!groupByColumns.isEmpty() && !groupByKey.equals(getGroupByKey(groupByColumns))) {
                log.trace("Group {} finish", groupByKey);
                break;
            }
            mergeAggregationColumn(aggregationUnitMap);
        } while (hasNext = resultSet.next());
        if (null == aggregationUnitMap) {
            return hasNext;
        }
        for (AggregationColumn each : aggregationUnitMap.keySet()) {
            setCell(each.getColumnIndex(), aggregationUnitMap.get(each).getResult());
        }
        return hasNext;
    }
    
    private List<Object> getGroupByKey(final List<GroupByColumn> groupByColumns) {
        List<Object> result = new ArrayList<>(groupByColumns.size());
        for (GroupByColumn each : groupByColumns) {
            result.add(getValueSafely(each.getColumnIndex()));
        }
        return result;
    }
    
    private void mergeAggregationColumn(final Map<AggregationColumn, AggregationUnit> aggregationUnitMap) {
        if (null == aggregationUnitMap) {
            return;
        }
        for (AggregationColumn each : aggregationColumns) {
            if (!aggregationUnitMap.containsKey(each)) {
                aggregationUnitMap.put(each, AggregationUnitFactory.create(each.getAggregationType()));
            }
            AggregationUnit unit = aggregationUnitMap.get(each);
            List<AggregationColumn> mergingAggregationColumns;
            if (each.getDerivedColumns().isEmpty()) {
                mergingAggregationColumns = Collections.singletonList(each);
            } else {
                mergingAggregationColumns = Lists.newArrayList(each.getDerivedColumns());
            }
            unit.merge(Lists.transform(mergingAggregationColumns, new Function<IndexColumn, Comparable<?>>() {
                
                @Override
                public Comparable<?> apply(final IndexColumn input) {
                    log.trace("Column Index {} will be merged", input.getColumnIndex());
                    return (Comparable<?>) getValueSafely(input.getColumnIndex());
                }
            }).toArray(new Comparable[mergingAggregationColumns.size()]));
        }
    }
    
    private Object getValueSafely(final int index) {
        try {
            return resultSet.getObject(index);
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    @Override
    // TODO toString问题
    public String toString() {
        return String.format("Group by columns is %s, aggregation column is %s, %s", groupByColumns, aggregationColumns, super.toString());
    }
}
