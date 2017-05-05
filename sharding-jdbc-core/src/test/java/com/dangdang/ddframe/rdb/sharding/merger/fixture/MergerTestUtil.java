/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergerTestUtil {
    
    
    public static ResultSet mockResult(final List<String> columnNames) throws SQLException {
        ResultSet result = getResultSet(columnNames);
        when(result.next()).thenReturn(true, false);
        return result;
    }
    
    public static ResultSet mockResult(final List<String> columnNames, final List<ResultSetRow> resultSetRows) throws SQLException {
        ResultSet result = getResultSet(columnNames);
        expectNext(result, resultSetRows);
        expectGetData(result, columnNames, resultSetRows);
        return result;
    }
    
    private static void expectNext(final ResultSet result, final List<ResultSetRow> resultSetRows) throws SQLException {
        Boolean[] hasNext = new Boolean[resultSetRows.size()];
        for (int i = 0; i < resultSetRows.size(); i++) {
            hasNext[i] = i != resultSetRows.size() - 1;
        }
        when(result.next()).thenReturn(true, hasNext);
    }
    
    private static void expectGetData(final ResultSet result, final List<String> columnNames, final List<ResultSetRow> resultSetRows) throws SQLException {
        for (int i = 0; i < columnNames.size(); i++) {
            Object[] resultData = new Object[resultSetRows.size() - 1];
            for (int j = 1; j < resultSetRows.size(); j++) {
                resultData[j - 1] = resultSetRows.get(j).getCell(i + 1);
            }
            when(result.getObject(i + 1)).thenReturn(resultSetRows.get(0).getCell(i + 1), resultData);
        }
    }
    
    private static ResultSet getResultSet(final List<String> columnNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(result.next()).thenReturn(true);
        when(result.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(columnNames.size());
        int count = 1;
        for (String each : columnNames) {
            when(resultSetMetaData.getColumnLabel(count)).thenReturn(each);
            count++;
        }
        return result;
    }
    
    public static AggregationSelectItemContext createAggregationColumn(final AggregationType aggregationType, final String name, final String alias, final int index) {
        AggregationSelectItemContext result = new AggregationSelectItemContext(name, Optional.fromNullable(alias), index, aggregationType);
        if (AggregationType.AVG.equals(aggregationType)) {
            result.getDerivedAggregationSelectItemContexts().add(
                    new AggregationSelectItemContext(AggregationType.COUNT.name(), Optional.of("sharding_gen_1"), -1, AggregationType.COUNT));
            result.getDerivedAggregationSelectItemContexts().add(
                    new AggregationSelectItemContext(AggregationType.SUM.name(), Optional.of("sharding_gen_2"), -1, AggregationType.SUM));
        }
        return result;
    }
}
