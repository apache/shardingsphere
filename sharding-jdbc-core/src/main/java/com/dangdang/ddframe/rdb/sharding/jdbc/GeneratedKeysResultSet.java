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

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedGeneratedKeysResultSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成键结果集.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class GeneratedKeysResultSet extends AbstractUnsupportedGeneratedKeysResultSet {
    
    private final Table<Integer, Integer, Object> valueTable;
    
    private final Map<String, Integer> columnNameToIndexMap;
    
    private final Statement statement;
    
    private boolean isClosed;
    
    private int rowIndex = -1;
    
    public GeneratedKeysResultSet() {
        valueTable = TreeBasedTable.create();
        columnNameToIndexMap = new HashMap<>();
        statement = null;
        isClosed = true;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (isClosed()) {
            return false;
        }
        rowIndex++;
        return rowIndex + 1 <= valueTable.rowKeySet().size();
    }
    
    @Override
    public void close() throws SQLException {
        isClosed = true;
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkState();
        return new GeneratedKeysResultSetMetaData(valueTable, columnNameToIndexMap);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        checkState();
        return false;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        checkState();
        return valueTable.get(rowIndex, columnIndex - 1).toString();
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        checkState();
        return valueTable.get(rowIndex, findColumn(columnLabel)).toString();
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).byteValue();
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).byteValue();
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).shortValue();
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).shortValue();
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).intValue();
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).intValue();
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).longValue();
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).longValue();
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).floatValue();
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).floatValue();
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        checkState();
        return getNumberValue(columnIndex - 1).doubleValue();
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        checkState();
        return getNumberValue(findColumn(columnLabel)).doubleValue();
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        checkState();
        return new BigDecimal(getNumberValue(columnIndex - 1).longValue()).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        checkState();
        return new BigDecimal(getNumberValue(findColumn(columnLabel)).longValue()).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        checkState();
        return new BigDecimal(getNumberValue(columnIndex - 1).longValue());
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        checkState();
        return new BigDecimal(getNumberValue(findColumn(columnLabel)).longValue());
    }
    
    private Number getNumberValue(final int columnIndex) {
        Object value = valueTable.get(rowIndex, columnIndex);
        Preconditions.checkState(value instanceof Number);
        return (Number) value;
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        checkState();
        return getString(columnIndex).getBytes();
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        checkState();
        return getString(columnLabel).getBytes();
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        checkState();
        return valueTable.get(rowIndex, columnIndex - 1);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        checkState();
        return valueTable.get(rowIndex, findColumn(columnLabel));
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        checkState();
        return columnNameToIndexMap.get(columnLabel);
    }
    
    @Override
    public int getType() throws SQLException {
        checkState();
        return TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        checkState();
        return CONCUR_READ_ONLY;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        checkState();
        return statement;
    }
    
    private void checkState() throws SQLException {
        Preconditions.checkState(!isClosed(), "ResultSet has closed");
    }
}
