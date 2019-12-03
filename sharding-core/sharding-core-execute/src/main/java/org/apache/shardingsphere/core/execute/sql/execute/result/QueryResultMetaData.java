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
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
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
    
    private final EncryptRule encryptRule;

    private final Map<String, Integer> columnLabelAndIndexes;

    private final SQLStatementContext sqlStatementContext;
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData, final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.encryptRule = shardingRule.getEncryptRule();
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
        this.sqlStatementContext = sqlStatementContext;
    }
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData, final EncryptRule encryptRule, final SQLStatementContext sqlStatementContext) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.encryptRule = encryptRule;
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
        this.sqlStatementContext = sqlStatementContext;
    }
    
    public QueryResultMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.encryptRule = new EncryptRule();
        columnLabelAndIndexes = getColumnLabelAndIndexMap();
        this.sqlStatementContext = null;
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
    
    /**
     * Get sharding encryptor.
     * 
     * @param columnIndex column index
     * @return sharding encryptor
     * @throws SQLException SQL exception
     */
    public Optional<ShardingEncryptor> getShardingEncryptor(final int columnIndex) throws SQLException {
        final String actualColumn = resultSetMetaData.getColumnName(columnIndex);
        if (null == sqlStatementContext) {
            return Optional.absent();
        }
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        for (String each : tableNames) {
            Optional<ShardingEncryptor> result = findShardingEncryptorWithTable(actualColumn, each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private Optional<ShardingEncryptor> findShardingEncryptorWithTable(final String actualColumn, final String logicTableName) {
        if (null == encryptRule) {
            return Optional.absent();
        }
        if (encryptRule.isCipherColumn(logicTableName, actualColumn)) {
            return encryptRule.findShardingEncryptor(logicTableName, encryptRule.getLogicColumn(logicTableName, actualColumn));
        }
        return Optional.absent();
    }
}
