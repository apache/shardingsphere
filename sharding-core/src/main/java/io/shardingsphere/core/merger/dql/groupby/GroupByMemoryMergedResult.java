/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.groupby;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.common.MemoryMergedResult;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRow;
import io.shardingsphere.core.merger.dql.groupby.aggregation.AggregationUnit;
import io.shardingsphere.core.merger.dql.groupby.aggregation.AggregationUnitFactory;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Memory merged result for group by.
 *
 * @author zhangliang
 */
public final class GroupByMemoryMergedResult extends MemoryMergedResult {
    
    private final SelectStatement selectStatement;
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    public GroupByMemoryMergedResult(
            final Map<String, Integer> labelAndIndexMap, final List<QueryResult> queryResults, final SelectStatement selectStatement) throws SQLException {
        super(labelAndIndexMap);
        this.selectStatement = selectStatement;
        memoryResultSetRows = init(queryResults);
    }
    
    private Iterator<MemoryQueryResultRow> init(final List<QueryResult> queryResults) throws SQLException {
        Map<GroupByValue, MemoryQueryResultRow> dataMap = new HashMap<>(1024);
        Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap = new HashMap<>(1024);
        for (QueryResult each : queryResults) {
            while (each.next()) {
                GroupByValue groupByValue = new GroupByValue(each, selectStatement.getGroupByItems());
                initForFirstGroupByValue(each, groupByValue, dataMap, aggregationMap);
                aggregate(each, groupByValue, aggregationMap);
            }
        }
        setAggregationValueToMemoryRow(dataMap, aggregationMap);
        List<MemoryQueryResultRow> result = getMemoryResultSetRows(dataMap);
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    private void initForFirstGroupByValue(final QueryResult queryResult, final GroupByValue groupByValue, final Map<GroupByValue, MemoryQueryResultRow> dataMap,
                                          final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) throws SQLException {
        if (!dataMap.containsKey(groupByValue)) {
            dataMap.put(groupByValue, new MemoryQueryResultRow(queryResult));
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
    
    private void aggregate(final QueryResult queryResult, final GroupByValue groupByValue, final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) throws SQLException {
        for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
            List<Comparable<?>> values = new ArrayList<>(2);
            if (each.getDerivedAggregationSelectItems().isEmpty()) {
                values.add(getAggregationValue(queryResult, each));
            } else {
                for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                    values.add(getAggregationValue(queryResult, derived));
                }
            }
            aggregationMap.get(groupByValue).get(each).merge(values);
        }
    }
    
    private Comparable<?> getAggregationValue(final QueryResult queryResult, final AggregationSelectItem aggregationSelectItem) throws SQLException {
        Object result = queryResult.getValue(aggregationSelectItem.getIndex(), Object.class);
        Preconditions.checkState(null == result || result instanceof Comparable, "Aggregation value must implements Comparable");
        return (Comparable<?>) result;
    }
    
    private void setAggregationValueToMemoryRow(final Map<GroupByValue, MemoryQueryResultRow> dataMap, final Map<GroupByValue, Map<AggregationSelectItem, AggregationUnit>> aggregationMap) {
        for (Entry<GroupByValue, MemoryQueryResultRow> entry : dataMap.entrySet()) {
            for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
                entry.getValue().setCell(each.getIndex(), aggregationMap.get(entry.getKey()).get(each).getResult());
            }
        }
    }
    
    private List<MemoryQueryResultRow> getMemoryResultSetRows(final Map<GroupByValue, MemoryQueryResultRow> dataMap) {
        List<MemoryQueryResultRow> result = new ArrayList<>(dataMap.values());
        Collections.sort(result, new GroupByRowComparator(selectStatement));
        return result;
    }
    
    @Override
    public boolean next() {
        if (memoryResultSetRows.hasNext()) {
            setCurrentResultSetRow(memoryResultSetRows.next());
            return true;
        }
        return false;
    }
}
