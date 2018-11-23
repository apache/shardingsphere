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

package io.shardingsphere.core.executor.sql.execute.result;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Aggregation distinct query result.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryResult extends DistinctQueryResult {
    
    private final Map<Integer, Integer> derivedCountIndexAndDistinctIndexes = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> derivedSumIndexAndDistinctIndexes = new LinkedHashMap<>();
    
    private AggregationDistinctQueryResult(final Multimap<String, Integer> columnLabelAndIndexMap, final Iterator<List<Object>> resultData, final Map<Integer, Integer> derivedCountIndexAndDistinctIndexes, final Map<Integer, Integer> derivedSumIndexAndDistinctIndexes) {
        super(columnLabelAndIndexMap, resultData);
        this.derivedCountIndexAndDistinctIndexes.putAll(derivedCountIndexAndDistinctIndexes);
        this.derivedSumIndexAndDistinctIndexes.putAll(derivedSumIndexAndDistinctIndexes);
    }
    
    @SneakyThrows
    public AggregationDistinctQueryResult(final Collection<QueryResult> queryResults, final SelectStatement selectStatement) {
        super(queryResults);
        init(selectStatement);
    }
    
    private void init(final SelectStatement selectStatement) {
        for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
            List<AggregationSelectItem> derivedAggregationSelectItems = each.getDerivedAggregationSelectItems();
            if (!derivedAggregationSelectItems.isEmpty()) {
                derivedCountIndexAndDistinctIndexes.put(derivedAggregationSelectItems.get(0).getIndex(), each.getIndex());
                derivedSumIndexAndDistinctIndexes.put(derivedAggregationSelectItems.get(1).getIndex(), each.getIndex());
            }
        }
    }
    
    /**
     * Divide one distinct query result to multiple child ones.
     *
     * @return multiple child distinct query results
     */
    @Override
    public List<DistinctQueryResult> divide() {
        return Lists.newArrayList(Iterators.transform(getResultData(), new Function<List<Object>, DistinctQueryResult>() {
    
            @Override
            public DistinctQueryResult apply(final List<Object> row) {
                Set<List<Object>> resultData = new LinkedHashSet<>();
                resultData.add(row);
                return new AggregationDistinctQueryResult(getColumnLabelAndIndexMap(), resultData.iterator(), derivedCountIndexAndDistinctIndexes, derivedSumIndexAndDistinctIndexes);
            }
        }));
    }
    
    private Object getValue(final int columnIndex) {
        if (derivedCountIndexAndDistinctIndexes.keySet().contains(columnIndex)) {
            return 1;
        }
        if (derivedSumIndexAndDistinctIndexes.keySet().contains(columnIndex)) {
            return getCurrentRow().get(derivedSumIndexAndDistinctIndexes.get(columnIndex) - 1);
        }
        return getCurrentRow().get(columnIndex - 1);
    }
    
    
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return getValue(columnIndex);
    }

    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return currentRow.get(getIndexByColumnLabel(columnLabel));
    }

    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.get(columnIndex - 1);
    }

    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return currentRow.get(getIndexByColumnLabel(columnLabel));
    }

    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return (InputStream) currentRow.get(columnIndex - 1);
    }

    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return (InputStream) currentRow.get(getIndexByColumnLabel(columnLabel));
    }

    @Override
    public boolean wasNull() {
        return null == currentRow;
    }

    @Override
    public int getColumnCount() {
        return columnLabelAndIndexMap.size();
    }

    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        for (Entry<String, Integer> entry : columnLabelAndIndexMap.entries()) {
            if (columnIndex == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new SQLException("Column index out of range", "9999");
    }
    
    private Integer getIndexByColumnLabel(final String columnLabel) {
        return new ArrayList<>(columnLabelAndIndexMap.get(columnLabel)).get(0) - 1;
    }
}
