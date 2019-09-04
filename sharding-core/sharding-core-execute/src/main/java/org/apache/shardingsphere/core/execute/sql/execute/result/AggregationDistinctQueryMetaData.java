/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.execute.sql.execute.result;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Aggregation distinct query metadata.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryMetaData {
    
    private final Collection<AggregationDistinctColumnMetaData> aggregationDistinctColumnMetaDataList = new LinkedList<>();
    
    private final Map<Integer, String> aggregationDistinctColumnIndexAndLabels = new HashMap<>();
    
    private final Map<Integer, AggregationType> aggregationDistinctColumnIndexAndAggregationTypes = new HashMap<>();
    
    private final Map<Integer, Integer> aggregationDistinctColumnIndexAndCountColumnIndexes = new HashMap<>();
    
    private final Map<Integer, Integer> aggregationDistinctColumnIndexAndSumColumnIndexes = new HashMap<>();
    
    public AggregationDistinctQueryMetaData(final Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems, final QueryResultMetaData queryResultMetaData) {
        aggregationDistinctColumnMetaDataList.addAll(getColumnMetaDataList(aggregationDistinctSelectItems, queryResultMetaData));
        aggregationDistinctColumnIndexAndLabels.putAll(getAggregationDistinctColumnIndexAndLabels());
        aggregationDistinctColumnIndexAndAggregationTypes.putAll(getAggregationDistinctColumnIndexAndAggregationTypes());
        aggregationDistinctColumnIndexAndCountColumnIndexes.putAll(getAggregationDistinctColumnIndexAndCountColumnIndexes());
        aggregationDistinctColumnIndexAndSumColumnIndexes.putAll(getAggregationDistinctColumnIndexAndSumColumnIndexes());
    }
    
    private Collection<AggregationDistinctColumnMetaData> getColumnMetaDataList(final Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems, 
                                                                                final QueryResultMetaData queryResultMetaData) {
        Collection<AggregationDistinctColumnMetaData> result = new LinkedList<>();
        for (AggregationDistinctSelectItem each : aggregationDistinctSelectItems) {
            result.add(getAggregationDistinctColumnMetaData(each, queryResultMetaData.getColumnIndex(each.getColumnLabel()), queryResultMetaData));
        }
        return result;
    }
    
    private AggregationDistinctColumnMetaData getAggregationDistinctColumnMetaData(final AggregationDistinctSelectItem selectItem, 
                                                                                   final int aggregationDistinctColumnIndex, final QueryResultMetaData queryResultMetaData) {
        List<AggregationSelectItem> derivedSelectItems = selectItem.getDerivedAggregationItems();
        if (derivedSelectItems.isEmpty()) {
            return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType());
        }
        int countDerivedIndex = queryResultMetaData.getColumnIndex(derivedSelectItems.get(0).getColumnLabel());
        int sumDerivedIndex = queryResultMetaData.getColumnIndex(derivedSelectItems.get(1).getColumnLabel());
        return new AggregationDistinctColumnMetaData(aggregationDistinctColumnIndex, selectItem.getColumnLabel(), selectItem.getType(), countDerivedIndex, sumDerivedIndex);
    }
    
    private Map<Integer, String> getAggregationDistinctColumnIndexAndLabels() {
        Map<Integer, String> result = new HashMap<>();
        for (AggregationDistinctColumnMetaData each : aggregationDistinctColumnMetaDataList) {
            result.put(each.columnIndex, each.columnLabel);
        }
        return result;
    }
    
    private Map<Integer, AggregationType> getAggregationDistinctColumnIndexAndAggregationTypes() {
        Map<Integer, AggregationType> result = new LinkedHashMap<>();
        for (AggregationDistinctColumnMetaData each : aggregationDistinctColumnMetaDataList) {
            result.put(each.columnIndex, each.aggregationType);
        }
        return result;
    }
    
    private Map<Integer, Integer> getAggregationDistinctColumnIndexAndCountColumnIndexes() {
        Map<Integer, Integer> result = new HashMap<>();
        for (AggregationDistinctColumnMetaData each : aggregationDistinctColumnMetaDataList) {
            result.put(each.columnIndex, each.derivedCountIndex);
        }
        return result;
    }
    
    private Map<Integer, Integer> getAggregationDistinctColumnIndexAndSumColumnIndexes() {
        Map<Integer, Integer> result = new HashMap<>();
        for (AggregationDistinctColumnMetaData each : aggregationDistinctColumnMetaDataList) {
            result.put(each.columnIndex, each.derivedSumIndex);
        }
        return result;
    }
    
    /**
     * Is aggregation distinct column index.
     * 
     * @param columnIndex column index
     * @return is aggregation distinct column index or not
     */
    public boolean isAggregationDistinctColumnIndex(final int columnIndex) {
        return aggregationDistinctColumnIndexAndLabels.keySet().contains(columnIndex);
    }
    
    /**
     * Is aggregation distinct column label.
     *
     * @param columnLabel column label
     * @return is aggregation distinct column label or not
     */
    public boolean isAggregationDistinctColumnLabel(final String columnLabel) {
        return aggregationDistinctColumnIndexAndLabels.values().contains(columnLabel);
    }
    
    /**
     * Get aggregation type.
     * 
     * @param distinctColumnIndex distinct column index
     * @return aggregation type
     */
    public AggregationType getAggregationType(final int distinctColumnIndex) {
        return aggregationDistinctColumnIndexAndAggregationTypes.get(distinctColumnIndex);
    }
    
    /**
     * Is derived count column index.
     * 
     * @param columnIndex column index
     * @return is derived count column index or not
     */
    public boolean isDerivedCountColumnIndex(final int columnIndex) {
        return aggregationDistinctColumnIndexAndCountColumnIndexes.values().contains(columnIndex);
    }
    
    /**
     * Is derived sum column index.
     *
     * @param columnIndex column index
     * @return is derived sum column index or not
     */
    public boolean isDerivedSumColumnIndex(final int columnIndex) {
        return aggregationDistinctColumnIndexAndSumColumnIndexes.values().contains(columnIndex);
    }
    
    /**
     * Get aggregation distinct column index.
     *
     * @param derivedSumIndex derived sum index
     * @return aggregation distinct column index
     */
    public int getAggregationDistinctColumnIndex(final int derivedSumIndex) {
        for (Entry<Integer, Integer> entry : aggregationDistinctColumnIndexAndSumColumnIndexes.entrySet()) {
            if (entry.getValue().equals(derivedSumIndex)) {
                return entry.getKey();
            }
        }
        throw new ShardingException("Can not get aggregation distinct column index.");
    }
    
    /**
     * Get aggregation distinct column index.
     *
     * @param distinctColumnLabel aggregation distinct column label
     * @return aggregation distinct column index
     */
    public int getAggregationDistinctColumnIndex(final String distinctColumnLabel) {
        for (Entry<Integer, String> entry : aggregationDistinctColumnIndexAndLabels.entrySet()) {
            if (entry.getValue().equals(distinctColumnLabel)) {
                return entry.getKey();
            }
        }
        throw new ShardingException("Can not get aggregation distinct column index.");
    }
    
    /**
     * Get aggregation distinct column label.
     *
     * @param distinctColumnIndex aggregation distinct column index
     * @return aggregation distinct column label
     */
    public String getAggregationDistinctColumnLabel(final int distinctColumnIndex) {
        return aggregationDistinctColumnIndexAndLabels.get(distinctColumnIndex);
    }
    
    @RequiredArgsConstructor 
    private final class AggregationDistinctColumnMetaData {
        
        private final int columnIndex;
        
        private final String columnLabel;
        
        private final AggregationType aggregationType;
        
        private final int derivedCountIndex;
        
        private final int derivedSumIndex;
        
        private AggregationDistinctColumnMetaData(final int columnIndex, final String columnLabel, final AggregationType aggregationType) {
            this(columnIndex, columnLabel, aggregationType, -1, -1);
        }
    }
}
