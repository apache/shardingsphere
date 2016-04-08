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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dangdang.ddframe.rdb.sharding.executor.ExecuteUnit;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.executor.MergeUnit;
import com.dangdang.ddframe.rdb.sharding.jdbc.AbstractShardingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnit;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationUnitFactory;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 分组结果集.
 * 
 * <p>
 * 采用map-reduce的方式.
 * map-reduce过程发生在nextForSharding()方法第一次被调用的时候, 将相同group-key的结果集放在一起,并同时做order by的排序处理(相当于shuffle过程).
 * </p>
 * 
 * @author gaohongtao, zhangliang
 */
@Slf4j
public final class GroupByResultSet extends AbstractShardingResultSet {
    
    private final List<GroupByColumn> groupByColumns;
    
    private final List<OrderByColumn> orderByColumns;
    
    private final List<AggregationColumn> aggregationColumns;
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final List<String> columnLabels;
    
    private final ExecutorEngine executorEngine;
    
    private Iterator<GroupByValue> groupByResultIterator;
    
    @Getter(AccessLevel.PROTECTED)
    private GroupByValue currentGroupByResultSet;
    
    public GroupByResultSet(final List<ResultSet> resultSets, final MergeContext mergeContext) throws SQLException {
        super(resultSets, mergeContext.getLimit());
        groupByColumns = mergeContext.getGroupByColumns();
        orderByColumns = mergeContext.getOrderByColumns();
        aggregationColumns = mergeContext.getAggregationColumns();
        resultSetMetaData = getResultSets().iterator().next().getMetaData();
        columnLabels = new ArrayList<>(resultSetMetaData.getColumnCount());
        fillRelatedColumnNames();
        executorEngine = mergeContext.getExecutorEngine();
    }
    
    private void fillRelatedColumnNames() throws SQLException {
        for (int i = 1; i < resultSetMetaData.getColumnCount() + 1; i++) {
            columnLabels.add(resultSetMetaData.getColumnLabel(i));
        }
    }
    
    @Override
    protected boolean nextForSharding() throws SQLException {
        if (null == groupByResultIterator) {
            ResultSetUtil.fillIndexesForDerivedAggregationColumns(getResultSets().iterator().next(), aggregationColumns);
            groupByResultIterator = reduce(map()).iterator();
        }
        if (groupByResultIterator.hasNext()) {
            currentGroupByResultSet = groupByResultIterator.next();
            return true;
        } else {
            return false;
        }
    }
    
    private Multimap<GroupByKey, GroupByValue> map() {
        ExecuteUnit<ResultSet, Map<GroupByKey, GroupByValue>> executeUnit = new ExecuteUnit<ResultSet, Map<GroupByKey, GroupByValue>>() {
            
            @Override
            public Map<GroupByKey, GroupByValue> execute(final ResultSet resultSet) throws SQLException {
                // TODO 应该可以根据limit判断result的初始size，避免size满了重分配
                Map<GroupByKey, GroupByValue> result = new HashMap<>();
                while (resultSet.next()) {
                    GroupByValue groupByValue = new GroupByValue();
                    for (int count = 1; count <= columnLabels.size(); count++) {
                        groupByValue.put(count, resultSetMetaData.getColumnLabel(count), (Comparable<?>) resultSet.getObject(count));
                    }
                    GroupByKey groupByKey = new GroupByKey();
                    for (GroupByColumn each : groupByColumns) {
                        groupByKey.append(ResultSetUtil.getValue(each, resultSet).toString());
                    }
                    result.put(groupByKey, groupByValue);
                }
                log.trace("Result set mapping: {}", result);
                return result;
            }
        };
        MergeUnit<Map<GroupByKey, GroupByValue>, Multimap<GroupByKey, GroupByValue>> mergeUnit = new MergeUnit<Map<GroupByKey, GroupByValue>, Multimap<GroupByKey, GroupByValue>>() {
            
            @Override
            public Multimap<GroupByKey, GroupByValue> merge(final List<Map<GroupByKey, GroupByValue>> values) {
                Multimap<GroupByKey, GroupByValue> result = HashMultimap.create();
                for (Map<GroupByKey, GroupByValue> each : values) {
                    for (Entry<GroupByKey, GroupByValue> entry : each.entrySet()) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
                return result;
            }
        };
        Multimap<GroupByKey, GroupByValue> result = executorEngine.execute(getResultSets(), executeUnit, mergeUnit);
        log.trace("Mapped result: {}", result);
        return result;
    }
    
    private Collection<GroupByValue> reduce(final Multimap<GroupByKey, GroupByValue> mappedResult) throws SQLException {
        List<GroupByValue> result = new ArrayList<>(mappedResult.values().size() * columnLabels.size());
        for (GroupByKey key : mappedResult.keySet()) {
            Collection<GroupByValue> each = mappedResult.get(key);
            GroupByValue reduceResult = new GroupByValue();
            for (int i = 0; i < columnLabels.size(); i++) {
                int index = i + 1;
                Optional<AggregationColumn> aggregationColumn = findAggregationColumn(index);
                Comparable<?> value = null;
                if (aggregationColumn.isPresent()) {
                    value = aggregate(aggregationColumn.get(), index, each);
                }
                value = null == value ? each.iterator().next().getValue(new ResultSetQueryIndex(index)) : value;
                reduceResult.put(index, columnLabels.get(i), value);
            }
            if (orderByColumns.isEmpty()) {
                reduceResult.addGroupByColumns(groupByColumns);
            } else {
                reduceResult.addOrderColumns(orderByColumns);
            }
            result.add(reduceResult);
        }
        Collections.sort(result);
        log.trace("Reduced result: {}", result);
        return result;
    }
    
    private Optional<AggregationColumn> findAggregationColumn(final int index) {
        for (AggregationColumn each : aggregationColumns) {
            if (each.getIndex() == index) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Comparable<?> aggregate(final AggregationColumn column, final int index, final Collection<GroupByValue> groupByValues) throws SQLException {
        AggregationUnit unit = AggregationUnitFactory.create(column.getAggregationType(), BigDecimal.class);
        for (GroupByValue each : groupByValues) {
            unit.merge(column, each, new ResultSetQueryIndex(index));
        }
        return unit.getResult();
    }
}
