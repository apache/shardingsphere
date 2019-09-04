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

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;

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
    
    private final ShardingRule shardingRule;
    
    private final EncryptRule encryptRule;

    private final Map<String, Integer> columnLabelAndIndexes;
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData, final ShardingRule shardingRule) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.shardingRule = shardingRule;
        this.encryptRule = shardingRule.getEncryptRule();
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
    }
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData, final EncryptRule encryptRule) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.shardingRule = null;
        this.encryptRule = encryptRule;
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
    }
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.shardingRule = null;
        this.encryptRule = new EncryptRule();
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
     * Whether the column value is case sensitive.
     *
     * @param columnIndex column index
     * @return true if column is case sensitive, otherwise false
     * @throws SQLException SQL exception
     */
    public boolean isCaseSensitive(final int columnIndex) throws SQLException {
        return resultSetMetaData.isCaseSensitive(columnIndex);
    }
    
    /**
     * Get sharding encryptor.
     * 
     * @param columnIndex column index
     * @return sharding encryptor
     * @throws SQLException SQL exception
     */
    public Optional<ShardingEncryptor> getShardingEncryptor(final int columnIndex) throws SQLException {
        String logicTable = getTableName(columnIndex);
        return encryptRule.getShardingEncryptor(logicTable, getLogicColumn(logicTable, columnIndex));
    }
    
    private String getTableName(final int columnIndex) throws SQLException {
        String actualTableName = resultSetMetaData.getTableName(columnIndex);
        if (null == shardingRule) {
            return actualTableName;
        }
        Optional<TableRule> tableRule = shardingRule.findTableRuleByActualTable(actualTableName);
        return tableRule.isPresent() ? tableRule.get().getLogicTable() : actualTableName;
    }
    
    private String getLogicColumn(final String tableName, final int columnIndex) throws SQLException {
        String columnLabel = resultSetMetaData.getColumnName(columnIndex);
        return encryptRule.isCipherColumn(tableName, columnLabel) ? encryptRule.getLogicColumn(tableName, columnLabel) : columnLabel;
    }
}
