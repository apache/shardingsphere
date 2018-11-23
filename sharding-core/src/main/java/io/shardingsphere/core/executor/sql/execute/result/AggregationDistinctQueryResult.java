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
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Aggregation distinct query result.
 *
 * @author panjuan
 */
public final class AggregationDistinctQueryResult extends DistinctQueryResult {
    
    @Getter
    private final Iterator<List<Object>> resultData;
    
    private final List<Integer> aggregationDistinctColumnIndexes = new LinkedList<>();
    
    private final List<Integer> derivedCountIndexes = new LinkedList<>();
    
    private final List<Integer> derivedSumIndexes = new LinkedList<>();
    
    private AggregationDistinctQueryResult(final Multimap<String, Integer> columnLabelAndIndexMap, final Iterator<List<Object>> resultData,
                                           final List<Integer> aggregationDistinctColumnIndexes, final List<Integer> derivedCountIndexes, final List<Integer> derivedSumIndexes) {
        super(columnLabelAndIndexMap, resultData);
        
    }
    
    private Iterator<List<Object>> getResultData(final SelectStatement selectStatement) {
        List<Integer> aggregationDistinctColumnIndexes = getAggregationDistinctColumnIndexes(selectStatement);
        List<Integer> derivedCountIndexes = new LinkedList<>();
        List<Integer> derivedSumIndexes = new LinkedList<>();
    }
    
    @SneakyThrows
    public AggregationDistinctQueryResult(final Collection<QueryResult> queryResults, final SelectStatement selectStatement) {
        super(queryResults);
    }
    
    private List<Integer> getAggregationDistinctColumnIndexes(final SelectStatement selectStatement) {
        List<Integer> result = new LinkedList<>();
        for (AggregationDistinctSelectItem each :selectStatement.getAggregationDistinctSelectItems()) {
            result.add(each.getIndex());
        }
        return result;
    }
    
    private void getDerivedCountIndexes(final SelectStatement selectStatement) {
        for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
            List<AggregationSelectItem> derivedAggregationSelectItems = each.getDerivedAggregationSelectItems();
            if (!derivedAggregationSelectItems.isEmpty()) {
                derivedCountIndexes.add(derivedAggregationSelectItems.get(0).getIndex());
            }
        }
    }
    
    private void getDerivedSumIndexes(final SelectStatement selectStatement) {
        for (AggregationSelectItem each : selectStatement.getAggregationSelectItems()) {
            List<AggregationSelectItem> derivedAggregationSelectItems = each.getDerivedAggregationSelectItems();
            if (!derivedAggregationSelectItems.isEmpty()) {
                derivedSumIndexes.add(derivedAggregationSelectItems.get(1).getIndex());
            }
        }
    }
    
    @Override
    protected void fill(final Set<List<Object>> resultData, final QueryResult queryResult) throws SQLException {
        while (queryResult.next()) {
            List<Object> row = new ArrayList<>(queryResult.getColumnCount());
            for (int columnIndex = 1; columnIndex <= queryResult.getColumnCount(); columnIndex++) {
                row.add(queryResult.getValue(columnIndex, Object.class));
            }
            resultData.add(row);
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
                return new AggregationDistinctQueryResult(getColumnLabelAndIndexMap(), resultData.iterator(), aggregationDistinctColumnIndexes, derivedCountIndexes, derivedSumIndexes);
            }
        }));
    }
    
    @Override
    public boolean next() {
        if (resultData.hasNext()) {
            currentRow = resultData.next();
            return true;
        }
        currentRow = null;
        return false;
    }

    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.get(columnIndex - 1);
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
