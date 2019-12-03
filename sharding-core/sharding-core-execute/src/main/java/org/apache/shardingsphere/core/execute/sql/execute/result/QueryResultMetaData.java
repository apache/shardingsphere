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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Query result meta data.
 *
 * @author panjuan
 * @author yangyi
 */
public final class QueryResultMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final Map<String, Integer> columnLabelAndIndexes;
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
    }
    
    private Map<String, Integer> getColumnLabelAndIndexMap() throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    /**
     * Get column count.
     * 
     * @return column count
     * @throws SQLException SQL exception
     */
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }
    
    /**
     * Get column label.
     * 
     * @param columnIndex column index
     * @return column label
     * @throws SQLException SQL exception
     */
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnLabel(columnIndex);
    }
    
    /**
     * Get column name.
     * 
     * @param columnIndex column index
     * @return column name
     * @throws SQLException SQL exception
     */
    public String getColumnName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnName(columnIndex);
    }
    
    /**
     * Get column index.
     * 
     * @param columnLabel column label
     * @return column name
     */
    public Integer getColumnIndex(final String columnLabel) {
        return columnLabelAndIndexes.get(columnLabel);
    }
    
    /**
     * Whether value is case sensitive or not.
     *
     * @param columnIndex column index
     * @return true if column is case sensitive, otherwise false
     * @throws SQLException SQL exception
     */
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return resultSetMetaData.isCaseSensitive(columnIndex);
    }
}
