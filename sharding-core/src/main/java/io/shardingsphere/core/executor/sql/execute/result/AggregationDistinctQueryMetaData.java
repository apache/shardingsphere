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
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregation distinct query metadata.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryMetaData {
    
    private final Collection<AggregationDistinctColumnMetaData> columnMetaDataList;
    
    public AggregationDistinctQueryMetaData(final Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems, final Multimap<String, Integer> columnLabelAndIndexMap) {
        columnMetaDataList = new LinkedList<>();
        for (AggregationDistinctSelectItem each : aggregationDistinctSelectItems) {
            columnMetaDataList.add(getAggregationDistinctColumnMetaData(each, new ArrayList<>(columnLabelAndIndexMap.get(each.getColumnLabel())).get(0), columnLabelAndIndexMap));
        }
    }
    
    private AggregationDistinctColumnMetaData getAggregationDistinctColumnMetaData(final AggregationDistinctSelectItem selectItem, 
                                                                                   final int aggregationDistinctColumnIndex, final Multimap<String, Integer> columnLabelAndIndexMap) {
        List<AggregationSelectItem> derivedSelectItems = selectItem.getDerivedAggregationSelectItems();
        if (derivedSelectItems.isEmpty()) {
            return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType(), Optional.<Integer>absent(), Optional.<Integer>absent());
        }
        int countDerivedIndex = columnLabelAndIndexMap.size() + 1;
        int sumDerivedIndex = countDerivedIndex + 1;
        reviseColumnLabelAndIndexMap(columnLabelAndIndexMap, selectItem, countDerivedIndex, sumDerivedIndex);
        return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType(), Optional.of(countDerivedIndex), Optional.of(sumDerivedIndex));
    }
    
    private void reviseColumnLabelAndIndexMap(final Multimap<String, Integer> columnLabelAndIndexMap, 
                                              final AggregationDistinctSelectItem selectItem, final int countDerivedIndex, final int sumDerivedIndex) {
        columnLabelAndIndexMap.put(selectItem.getDerivedAggregationSelectItems().get(0).getColumnLabel(), countDerivedIndex);
        columnLabelAndIndexMap.put(selectItem.getDerivedAggregationSelectItems().get(1).getColumnLabel(), sumDerivedIndex);
    }
    
    /**
     * Get aggregation distinct column indexes.
     * 
     * @return aggregation distinct column indexes
     */
    public Collection<Integer> getAggregationDistinctColumnIndexes() {
        
        return Collections2.transform(columnMetaDataList, new Function<AggregationDistinctColumnMetaData, Integer>() {
            @Override
            public Integer apply(final AggregationDistinctColumnMetaData input) {
                return input.getColumnIndex();
            }
        });
    }
    
    /**
     * Get aggregation type.
     * 
     * @param distinctColumnIndex distinct column index
     * @return aggregation type
     */
    public AggregationType getAggregationType(final int distinctColumnIndex) {
        return Collections2.filter(columnMetaDataList, new Predicate<AggregationDistinctColumnMetaData>() {
            @Override
            public boolean apply(final AggregationDistinctColumnMetaData input) {
                return distinctColumnIndex == input.columnIndex;
            }
        }).iterator().next().getAggregationType();
    }
    
    /**
     * Get derived count column indexes.
     *
     * @return derived count column indexes
     */
    public Collection<Integer> getDerivedCountColumnIndexes() {
        Collection<Integer> result = new LinkedList<>();
        for (AggregationDistinctColumnMetaData each : columnMetaDataList) {
            if (each.getDerivedCountIndex().isPresent()) {
                result.add(each.getDerivedCountIndex().get());
            }
        }
        return result;
    }
    
    /**
     * Get derived sum column indexes.
     *
     * @return derived sum column indexes
     */
    public Collection<Integer> getDerivedSumColumnIndexes() {
        Collection<Integer> result = new LinkedList<>();
        for (AggregationDistinctColumnMetaData each : columnMetaDataList) {
            if (each.getDerivedSumIndex().isPresent()) {
                result.add(each.getDerivedSumIndex().get());
            }
        }
        return result;
    }
    
    @RequiredArgsConstructor 
    @Getter(AccessLevel.PRIVATE)
    private final class AggregationDistinctColumnMetaData {
        
        private final int columnIndex;
        
        private final String columnLabel;
        
        private final AggregationType aggregationType;
        
        private final Optional<Integer> derivedCountIndex;
        
        private final Optional<Integer> derivedSumIndex;
    }
}
