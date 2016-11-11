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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Table;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成键结果集元数据.
 * 
 * @author gaohongtao
 */
class GeneratedKeysResultSetMetaData implements ResultSetMetaData {
    
    private final Map<Integer, String> indexToColumnNameMap = new HashMap<>();
    
    private final Table<Integer, Integer, Object> autoIncrementValueTable;
    
    GeneratedKeysResultSetMetaData(final Table<Integer, Integer, Object> autoIncrementValueTable, final Map<String, Integer> autoIncrementColumnNameToIndexMap) {
        this.autoIncrementValueTable = autoIncrementValueTable;
        for (Map.Entry<String, Integer> each : autoIncrementColumnNameToIndexMap.entrySet()) {
            indexToColumnNameMap.put(each.getValue(), each.getKey());
        }
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return indexToColumnNameMap.size();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        checkIndex(column);
        return true;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        checkIndex(column);
        return true;
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        checkIndex(column);
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        checkIndex(column);
        return false;
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        checkIndex(column);
        return columnNoNulls;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        checkIndex(column);
        return true;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        checkIndex(column);
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        checkIndex(column);
        return indexToColumnNameMap.get(column - 1);
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        checkIndex(column);
        return indexToColumnNameMap.get(column - 1);
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        checkIndex(column);
        return "";
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        checkIndex(column);
        return 0;
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        checkIndex(column);
        return 0;
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        checkIndex(column);
        return "";
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        checkIndex(column);
        return "";
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        checkIndex(column);
        Object value = autoIncrementValueTable.get(0, column - 1);
        return value instanceof Number ? Types.BIGINT : Types.VARCHAR;
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        checkIndex(column);
        return "";
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        checkIndex(column);
        return true;
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        checkIndex(column);
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        checkIndex(column);
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        checkIndex(column);
        Object value = autoIncrementValueTable.get(0, column - 1);
        return value.getClass().getName();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return (T) this;
        }
        throw new SQLException(String.format("[%s] cannot be unwrapped as [%s]", getClass().getName(), iface.getName()));
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
    
    private void checkIndex(final int column) {
        Preconditions.checkArgument(column >= 1 && column <= indexToColumnNameMap.size());
    }
}
