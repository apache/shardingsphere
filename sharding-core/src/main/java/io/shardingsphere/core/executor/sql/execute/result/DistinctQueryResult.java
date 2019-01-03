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
import io.shardingsphere.core.executor.sql.execute.row.QueryRow;
import io.shardingsphere.core.merger.QueryResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Distinct query result.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class DistinctQueryResult implements QueryResult {
    
    private final Multimap<String, Integer> columnLabelAndIndexMap;
    
    private final Iterator<QueryRow> resultData;
    
    private QueryRow currentRow;
    
    @SneakyThrows
    public DistinctQueryResult(final Collection<QueryResult> queryResults, final List<String> distinctColumnLabels) {
        this.columnLabelAndIndexMap = getColumnLabelAndIndexMap(queryResults.iterator().next());
        resultData = getResultData(queryResults, distinctColumnLabels);
    }
    
    @SneakyThrows
    private Multimap<String, Integer> getColumnLabelAndIndexMap(final QueryResult queryResult) {
        Multimap<String, Integer> result = HashMultimap.create();
        for (int columnIndex = 1; columnIndex <= queryResult.getColumnCount(); columnIndex++) {
            result.put(queryResult.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    @SneakyThrows
    private Iterator<QueryRow> getResultData(final Collection<QueryResult> queryResults, final List<String> distinctColumnLabels) {
        Set<QueryRow> result = new LinkedHashSet<>();
        List<Integer> distinctColumnIndexes = Lists.transform(distinctColumnLabels, new Function<String, Integer>() {
            
            @Override
            public Integer apply(final String input) {
                return getColumnIndex(input);
            }
        });
        for (QueryResult each : queryResults) {
            fill(result, each, distinctColumnIndexes);
        }
        return result.iterator();
    }
    
    @SneakyThrows
    private void fill(final Set<QueryRow> resultData, final QueryResult queryResult, final List<Integer> distinctColumnIndexes) {
        while (queryResult.next()) {
            List<Object> rowData = new ArrayList<>(queryResult.getColumnCount());
            for (int columnIndex = 1; columnIndex <= queryResult.getColumnCount(); columnIndex++) {
                rowData.add(queryResult.getValue(columnIndex, Object.class));
            }
            resultData.add(new QueryRow(rowData, distinctColumnIndexes));
        }
    }
    
    /**
     * Divide one distinct query result to multiple child ones.
     *
     * @return multiple child distinct query results
     */
    public List<DistinctQueryResult> divide() {
        return Lists.newArrayList(Iterators.transform(resultData, new Function<QueryRow, DistinctQueryResult>() {
            
            @Override
            public DistinctQueryResult apply(final QueryRow row) {
                Set<QueryRow> resultData = new LinkedHashSet<>();
                resultData.add(row);
                return new DistinctQueryResult(columnLabelAndIndexMap, resultData.iterator());
            }
        }));
    }
    
    @Override
    public final boolean next() {
        if (resultData.hasNext()) {
            currentRow = resultData.next();
            return true;
        }
        currentRow = null;
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.getColumnValue(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return currentRow.getColumnValue(getColumnIndex(columnLabel));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(columnIndex);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return currentRow.getColumnValue(getColumnIndex(columnLabel));
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return getInputStream(currentRow.getColumnValue(columnIndex));
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return getInputStream(currentRow.getColumnValue(getColumnIndex(columnLabel)));
    }
    
    @SneakyThrows
    protected InputStream getInputStream(final Object value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
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
    
    protected Integer getColumnIndex(final String columnLabel) {
        return new ArrayList<>(columnLabelAndIndexMap.get(columnLabel)).get(0);
    }
}
