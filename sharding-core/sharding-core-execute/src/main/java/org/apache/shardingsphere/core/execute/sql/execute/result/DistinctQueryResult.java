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
import java.util.Set;

/**
 * Distinct query result.
 *
 * @author panjuan
 * @author yangyi
 * @author sunbufu
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class DistinctQueryResult implements QueryResult {
    
    @Getter
    private final QueryResultMetaData queryResultMetaData;
    
    private final Iterator<QueryRow> resultData;
    
    private QueryRow currentRow;
    
    public DistinctQueryResult(final Collection<QueryResult> queryResults, final List<String> distinctColumnLabels) throws SQLException {
        QueryResult firstQueryResult = queryResults.iterator().next();
        this.queryResultMetaData = firstQueryResult.getQueryResultMetaData();
        resultData = getResultData(queryResults, distinctColumnLabels);
    }
    
    private Iterator<QueryRow> getResultData(final Collection<QueryResult> queryResults, final List<String> distinctColumnLabels) throws SQLException {
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
    
    private void fill(final Set<QueryRow> resultData, final QueryResult queryResult, final List<Integer> distinctColumnIndexes) throws SQLException {
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

                return new DistinctQueryResult(queryResultMetaData, resultData.iterator());
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
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return queryResultMetaData.isCaseSensitive(columnIndex);
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return queryResultMetaData.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        String columnLabel = queryResultMetaData.getColumnLabel(columnIndex);
        if (null != columnLabel) {
            return columnLabel;
        }
        throw new SQLException("Column index out of range", "9999");
    }
    
    protected Integer getColumnIndex(final String columnLabel) {
        return queryResultMetaData.getColumnIndex(columnLabel);
    }
}
