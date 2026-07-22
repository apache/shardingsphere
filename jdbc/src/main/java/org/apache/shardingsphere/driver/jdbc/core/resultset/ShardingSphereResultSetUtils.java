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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for {@link ShardingSphereResultSet}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereResultSetUtils {
    
    /**
     * Create column label and index map.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return column label and index map
     * @throws SQLException SQL exception
     */
    public static Map<String, Integer> createColumnLabelAndIndexMap(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections()) {
            return createColumnLabelAndIndexMapWithExpandProjections(
                    ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getColumnLabelAndIndexMap(), resultSetMetaData);
        }
        return createColumnLabelAndIndexMapFromMetaData(resultSetMetaData);
    }
    
    private static Map<String, Integer> createColumnLabelAndIndexMapWithExpandProjections(final Map<String, Integer> expandedMap,
                                                                                          final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (null == resultSetMetaData || expandedMap.size() == resultSetMetaData.getColumnCount()) {
            return expandedMap;
        }
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        for (Entry<String, Integer> entry : expandedMap.entrySet()) {
            result.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private static Map<String, Integer> createColumnLabelAndIndexMapFromMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
}
