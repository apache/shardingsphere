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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.execute.sql.execute.row.QueryRow;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationDistinctProjection;

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

/**
 * Aggregation distinct query result.
 *
 * @author panjuan
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AggregationDistinctQueryResult implements QueryResult {
    
    @Getter
    private final QueryResultMetaData queryResultMetaData;
    
    private final Iterator<QueryRow> resultData;
    
    private final AggregationDistinctQueryMetaData metaData;
    
    private QueryRow currentRow;
    
    public AggregationDistinctQueryResult(final Collection<QueryResult> queryResults, final List<AggregationDistinctProjection> aggregationDistinctProjections) throws SQLException {
        List<String> distinctColumnLabels = Lists.transform(aggregationDistinctProjections, new Function<AggregationDistinctProjection, String>() {
            
            @Override
            public String apply(final AggregationDistinctProjection input) {
                return input.getDistinctColumnLabel();
            }
        });
        this.queryResultMetaData = queryResults.iterator().next().getQueryResultMetaData();
        this.resultData = getResultData(queryResults, distinctColumnLabels);
        metaData = new AggregationDistinctQueryMetaData(aggregationDistinctProjections, queryResultMetaData);
    }
    
    private Iterator<QueryRow> getResultData(final Collection<QueryResult> queryResults, final List<String> distinctColumnLabels) throws SQLException {
        Collection<QueryRow> result = new LinkedHashSet<>();
        List<Integer> distinctColumnIndexes = Lists.transform(distinctColumnLabels, new Function<String, Integer>() {
            
            @Override
            public Integer apply(final String input) {
                return getColumnIndex(input);
            }
        });
        for (QueryResult each : queryResults) {
            result.addAll(getQueryRows(each, distinctColumnIndexes));
        }
        return result.iterator();
    }
    
    private Collection<QueryRow> getQueryRows(final QueryResult queryResult, final List<Integer> distinctColumnIndexes) throws SQLException {
        Collection<QueryRow> result = new LinkedHashSet<>();
        while (queryResult.next()) {
            List<Object> rowData = new ArrayList<>(queryResult.getColumnCount());
            for (int columnIndex = 1; columnIndex <= queryResult.getColumnCount(); columnIndex++) {
                rowData.add(queryResult.getValue(columnIndex, Object.class));
            }
            result.add(new QueryRow(rowData, distinctColumnIndexes));
        }
        return result;
    }
    
    /**
     * Divide one distinct query result to multiple child ones.
     *
     * @return multiple child distinct query results
     */
    public List<AggregationDistinctQueryResult> divide() {
        return Lists.newArrayList(Iterators.transform(resultData, new Function<QueryRow, AggregationDistinctQueryResult>() {
            
            @Override
            public AggregationDistinctQueryResult apply(final QueryRow input) {
                Collection<QueryRow> resultData = new LinkedHashSet<>();
                resultData.add(input);
                return new AggregationDistinctQueryResult(queryResultMetaData, resultData.iterator(), metaData);
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
        return getValue(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return getValue(columnLabel);
    }
    
    private Object getValue(final int columnIndex) {
        if (metaData.isAggregationDistinctColumnIndex(columnIndex)) {
            return AggregationType.COUNT == metaData.getAggregationType(columnIndex) ? 1 : currentRow.getValue(columnIndex);
        }
        if (metaData.isDerivedCountColumnIndex(columnIndex)) {
            return 1;
        }
        if (metaData.isDerivedSumColumnIndex(columnIndex)) {
            return currentRow.getValue(metaData.getAggregationDistinctColumnIndex(columnIndex));
        }
        return currentRow.getValue(columnIndex);
    }
    
    private Object getValue(final String columnLabel) {
        return getValue(getColumnIndex(columnLabel));
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
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return queryResultMetaData.isCaseSensitive(columnIndex);
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return queryResultMetaData.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        if (metaData.isAggregationDistinctColumnIndex(columnIndex)) {
            return metaData.getAggregationDistinctColumnLabel(columnIndex);
        }
        String columnLabel = queryResultMetaData.getColumnLabel(columnIndex);
        if (null != columnLabel) {
            return columnLabel;
        }
        throw new SQLException("Column index out of range", "9999");
    }
    
    private Integer getColumnIndex(final String columnLabel) {
        return null != metaData && metaData.isAggregationDistinctColumnLabel(columnLabel) ? metaData.getAggregationDistinctColumnIndex(columnLabel) : queryResultMetaData.getColumnIndex(columnLabel);
    }
}
