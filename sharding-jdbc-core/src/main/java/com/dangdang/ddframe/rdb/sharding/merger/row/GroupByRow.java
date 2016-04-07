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

/**
 * @author gaohongtao
 */
public class GroupByRow extends Row {
    
    private final ResultSet resultSet;
    
    private final Function<IndexColumn, Object> getValueByColumnIndexFunction = new Function<IndexColumn, Object>() {
        @Override
        public Object apply(final IndexColumn input) {
            return getValueSafely(input.getColumnIndex());
        }
    };
    
    public GroupByRow(final ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.resultSet = resultSet;
    }
    
    public boolean aggregate(final List<GroupByColumn> groupByColumns, final List<AggregationColumn> aggregationColumns) throws SQLException {
        Map<AggregationColumn, AggregationUnit> aggregationUnitMap = null;
        if (!aggregationColumns.isEmpty()) {
            aggregationUnitMap = new HashMap<>(aggregationColumns.size());
        }
        List<Object> groupByKey = getGroupByKey(groupByColumns);
        boolean hasNext;
        while (hasNext = resultSet.next()) {
            List<Object> nextRowGroupByKey = getGroupByKey(groupByColumns);
            if (!groupByColumns.isEmpty() && !groupByKey.equals(nextRowGroupByKey)) {
                break;
            }
            mergeAggregationColumn(aggregationColumns, aggregationUnitMap);
            groupByKey = nextRowGroupByKey;
        }
        if (null == aggregationUnitMap) {
            return hasNext;
        }
        for (AggregationColumn each : aggregationUnitMap.keySet()) {
            setCell(each.getColumnIndex(), aggregationUnitMap.get(each).getResult());
        }
        return hasNext;
    }
    
    private List<Object> getGroupByKey(final List<GroupByColumn> groupByColumns) {
        return Lists.transform(groupByColumns, getValueByColumnIndexFunction);
    }
        
    @SuppressWarnings("SuspiciousToArrayCall")
    private void mergeAggregationColumn(final List<AggregationColumn> aggregationColumns, final Map<AggregationColumn, AggregationUnit> aggregationUnitMap) {
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
            unit.merge(Lists.transform(mergingAggregationColumns, getValueByColumnIndexFunction).toArray(new Comparable[each.getDerivedColumns().size()]));
        }
    }
    
    private Object getValueSafely(final int index) {
        try {
            return resultSet.getObject(index);
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
}
