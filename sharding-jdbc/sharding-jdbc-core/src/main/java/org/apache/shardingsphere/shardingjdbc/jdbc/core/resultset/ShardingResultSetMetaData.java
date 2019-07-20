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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.WrapperAdapter;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Sharding result set meta data.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final ShardingRule shardingRule;
    
    @Override
    public int getColumnCount() throws SQLException {
        int result = 0;
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            if (!DerivedColumn.isDerivedColumn(resultSetMetaData.getColumnLabel(columnIndex))) {
                result++;
            }
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
        return resultSetMetaData.getColumnLabel(column);
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        return resultSetMetaData.getColumnName(column);
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
