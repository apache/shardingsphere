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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
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
    
    private final Multimap<String, Integer> aggregationColumnLabelAndIndexes = HashMultimap.create();
    
    private final Map<Integer, AggregationType> distinctIndexAndAggregationTypes = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> derivedCountIndexAndDistinctIndexes = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> derivedSumIndexAndDistinctIndexes = new LinkedHashMap<>();
    
    private AggregationDistinctQueryResult(final Multimap<String, Integer> columnLabelAndIndexMap, final Iterator<List<Object>> resultData,
                                           final Multimap<String, Integer> aggregationColumnLabelAndIndexes, final Map<Integer, AggregationType> distinctIndexAndAggregationTypes,
                                           final Map<Integer, Integer> derivedCountIndexAndDistinctIndexes, final Map<Integer, Integer> derivedSumIndexAndDistinctIndexes) {
        super(columnLabelAndIndexMap, resultData);
        this.aggregationColumnLabelAndIndexes.putAll(aggregationColumnLabelAndIndexes);
        this.distinctIndexAndAggregationTypes.putAll(distinctIndexAndAggregationTypes);
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
            initAggregationColumnLabelAndIndexes(each);
            initDerivedIndexAndDistinctIndexes(each);
            distinctIndexAndAggregationTypes.put(getIndexByColumnLabel(each.getColumnLabel()), each.getType());
        }
    }
    
    private void initDerivedIndexAndDistinctIndexes(final AggregationSelectItem selectItem) {
        List<AggregationSelectItem> derivedAggregationSelectItems = selectItem.getDerivedAggregationSelectItems();
        if (!derivedAggregationSelectItems.isEmpty()) {
            derivedCountIndexAndDistinctIndexes.put(derivedAggregationSelectItems.get(0).getIndex(), selectItem.getIndex());
            derivedSumIndexAndDistinctIndexes.put(derivedAggregationSelectItems.get(1).getIndex(), selectItem.getIndex());
        }
    }
    
    private void initAggregationColumnLabelAndIndexes(final AggregationSelectItem selectItem) {
        if (selectItem instanceof AggregationDistinctSelectItem) {
            AggregationDistinctSelectItem distinctSelectItem = (AggregationDistinctSelectItem) selectItem;
            aggregationColumnLabelAndIndexes.put(distinctSelectItem.getColumnLabel(), getIndexByColumnLabel(distinctSelectItem.getColumnLabel()));
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
            public DistinctQueryResult apply(final List<Object> input) {
                Set<List<Object>> resultData = new LinkedHashSet<>();
                resultData.add(input);
                return new AggregationDistinctQueryResult(getColumnLabelAndIndexMap(),
                        resultData.iterator(), aggregationColumnLabelAndIndexes, distinctIndexAndAggregationTypes, derivedCountIndexAndDistinctIndexes, derivedSumIndexAndDistinctIndexes);
            }
        }));
    }
    
    private Object getValue(final int columnIndex) {
        if (distinctIndexAndAggregationTypes.keySet().contains(columnIndex)) {
            return AggregationType.COUNT.equals(distinctIndexAndAggregationTypes.get(columnIndex)) ? 1 : getCurrentRow().get(columnIndex - 1);
        }
        if (derivedCountIndexAndDistinctIndexes.keySet().contains(columnIndex)) {
            return 1;
        }
        if (derivedSumIndexAndDistinctIndexes.keySet().contains(columnIndex)) {
            return getCurrentRow().get(derivedSumIndexAndDistinctIndexes.get(columnIndex) - 1);
        }
        return getCurrentRow().get(columnIndex - 1);
    }
    
    private Object getValue(final String columnLabel) {
        return getValue(getIndexByColumnLabel(columnLabel));
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return getValue(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return getValue(columnLabel);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return getValue(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return getValue(columnLabel);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return (InputStream) getValue(columnIndex);
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return (InputStream) getValue(columnLabel);
    }
    
    @Override
    public boolean wasNull() {
        return null == getCurrentRow();
    }
    
    @Override
    public int getColumnCount() {
        return getColumnLabelAndIndexMap().size();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        for (Entry<String, Integer> entry : aggregationColumnLabelAndIndexes.entries()) {
            if (columnIndex == entry.getValue()) {
                return entry.getKey();
            }
        }
        for (Entry<String, Integer> entry : getColumnLabelAndIndexMap().entries()) {
            if (columnIndex == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new SQLException("Column index out of range", "9999");
    }
    
    private Integer getIndexByColumnLabel(final String columnLabel) {
        return new ArrayList<>(aggregationColumnLabelAndIndexes.containsKey(columnLabel) ? aggregationColumnLabelAndIndexes.get(columnLabel)
                : getColumnLabelAndIndexMap().get(columnLabel)).get(0) - 1;
    }
}
