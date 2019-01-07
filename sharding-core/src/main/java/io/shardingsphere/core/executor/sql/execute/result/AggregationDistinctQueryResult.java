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
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.executor.sql.execute.row.QueryRow;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Aggregation distinct query result.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryResult extends DistinctQueryResult {
    
    private final AggregationDistinctQueryMetaData metaData;
        
    private AggregationDistinctQueryResult(final Multimap<String, Integer> columnLabelAndIndexMap, final Iterator<QueryRow> resultData, final AggregationDistinctQueryMetaData distinctQueryMetaData) {
        super(columnLabelAndIndexMap, resultData);
        metaData = distinctQueryMetaData;
    }
    
    @SneakyThrows
    public AggregationDistinctQueryResult(final Collection<QueryResult> queryResults, final List<AggregationDistinctSelectItem> aggregationDistinctSelectItems) {
        super(queryResults, Lists.transform(aggregationDistinctSelectItems, new Function<AggregationDistinctSelectItem, String>() {
    
            @Override
            public String apply(final AggregationDistinctSelectItem input) {
                return input.getDistinctColumnLabel();
            }
        }));
        metaData = new AggregationDistinctQueryMetaData(aggregationDistinctSelectItems, getColumnLabelAndIndexMap());
    }
    
    /**
     * Divide one distinct query result to multiple child ones.
     *
     * @return multiple child distinct query results
     */
    @Override
    public List<DistinctQueryResult> divide() {
        return Lists.newArrayList(Iterators.transform(getResultData(), new Function<QueryRow, DistinctQueryResult>() {
    
            @Override
            public DistinctQueryResult apply(final QueryRow input) {
                Set<QueryRow> resultData = new LinkedHashSet<>();
                resultData.add(input);
                return new AggregationDistinctQueryResult(getColumnLabelAndIndexMap(), resultData.iterator(), metaData);
            }
        }));
    }
    
    private Object getValue(final int columnIndex) {
        if (metaData.getAggregationDistinctColumnIndexes().contains(columnIndex)) {
            return AggregationType.COUNT == metaData.getAggregationType(columnIndex) ? 1 : super.getValue(columnIndex, Object.class);
        }
        if (metaData.getDerivedCountColumnIndexes().contains(columnIndex)) {
            return 1;
        }
        if (metaData.getDerivedSumColumnIndexes().contains(columnIndex)) {
            return super.getValue(metaData.getAggregationDistinctColumnIndex(columnIndex), Object.class);
        }
        return super.getValue(columnIndex, Object.class);
    }
    
    private Object getValue(final String columnLabel) {
        return getValue(getColumnIndex(columnLabel));
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
        return getInputStream(getValue(columnIndex));
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return getInputStream(getValue(columnLabel));
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
        if (metaData.getAggregationDistinctColumnIndexes().contains(columnIndex)) {
            return metaData.getAggregationDistinctColumnLabel(columnIndex);
        }
        for (Entry<String, Integer> entry : getColumnLabelAndIndexMap().entries()) {
            if (columnIndex == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new SQLException("Column index out of range", "9999");
    }
    
    @Override
    protected Integer getColumnIndex(final String columnLabel) {
        return isContainColumnLabel(columnLabel) ? metaData.getAggregationDistinctColumnIndex(columnLabel) : super.getColumnIndex(columnLabel);
    }
    
    private boolean isContainColumnLabel(final String columnLabel) {
        return null != metaData && metaData.getAggregationDistinctColumnLabels().contains(columnLabel);
    }
}
