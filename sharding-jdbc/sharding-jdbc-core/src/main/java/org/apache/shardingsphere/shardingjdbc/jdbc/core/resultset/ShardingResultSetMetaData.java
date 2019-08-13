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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset;

import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding result set meta data.
 * 
 * @author zhangliang
 * @author panjuan
 */
public final class ShardingResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final ShardingRule shardingRule;
    
    private final OptimizedStatement optimizedStatement;
    
    private final Map<String, String> logicAndActualColumns;
    
    public ShardingResultSetMetaData(final ResultSetMetaData resultSetMetaData, 
                                     final ShardingRule shardingRule, final OptimizedStatement optimizedStatement, final Map<String, String> logicAndActualColumns) {
        this.resultSetMetaData = resultSetMetaData;
        this.shardingRule = shardingRule;
        this.optimizedStatement = optimizedStatement;
        this.logicAndActualColumns = logicAndActualColumns;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount() - getDerivedColumnCount();
    }
    
    private int getDerivedColumnCount() throws SQLException {
        int result = 0;
        Collection<String> assistedQueryColumns = getAssistedQueryColumns();
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
            if (DerivedColumn.isDerivedColumn(columnLabel) || assistedQueryColumns.contains(columnLabel)) {
                result++;
            }
        }
        return result;
    }
    
    private Collection<String> getAssistedQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (String each : optimizedStatement.getTables().getTableNames()) {
            result.addAll(shardingRule.getEncryptRule().getAssistedQueryColumns(each));
        }
        return result;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return resultSetMetaData.isAutoIncrement(column);
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        return resultSetMetaData.isCaseSensitive(column);
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return resultSetMetaData.isSearchable(column);
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        return resultSetMetaData.isCurrency(column);
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        return resultSetMetaData.isNullable(column);
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return resultSetMetaData.isSigned(column);
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(column);
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        String result = resultSetMetaData.getColumnLabel(column);
        return logicAndActualColumns.values().contains(result) ? getLogicColumn(result) : result;
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        String result = resultSetMetaData.getColumnName(column);
        return logicAndActualColumns.values().contains(result) ? getLogicColumn(result) : result;
    }
    
    private String getLogicColumn(final String actualColumn) throws SQLException {
        for (Entry<String, String> entry : logicAndActualColumns.entrySet()) {
            if (entry.getValue().contains(actualColumn)) {
                return entry.getKey();
            }
        }
        throw new SQLException(String.format("Can not get logic column by %s.", actualColumn));
    }
    
    @Override
    public String getSchemaName(final int column) {
        return ShardingConstant.LOGIC_SCHEMA_NAME;
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return resultSetMetaData.getPrecision(column);
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return resultSetMetaData.getScale(column);
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        String actualTableName = resultSetMetaData.getTableName(column);
        return shardingRule.getLogicTableNames(actualTableName).isEmpty() ? actualTableName : shardingRule.getLogicTableNames(actualTableName).iterator().next();
    }
    
    @Override
    public String getCatalogName(final int column) {
        return ShardingConstant.LOGIC_SCHEMA_NAME;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return resultSetMetaData.getColumnType(column);
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return resultSetMetaData.getColumnTypeName(column);
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return resultSetMetaData.isReadOnly(column);
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return resultSetMetaData.isWritable(column);
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return resultSetMetaData.isDefinitelyWritable(column);
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        return resultSetMetaData.getColumnClassName(column);
    }
}
