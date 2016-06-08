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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory;

import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.ResultSetRow;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * 内存结果集抽象类.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractMemoryResultSet extends AbstractUnsupportedOperationMemoryResultSet {
    
    private boolean beforeFirst = true;
    
    @Getter(AccessLevel.PROTECTED)
    private ResultSetRow currentRow;
    
    private boolean wasNull;
    
    public AbstractMemoryResultSet(final List<ResultSet> resultSets) throws SQLException {
        super(resultSets);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (beforeFirst) {
            initRows(getResultSets());
            beforeFirst = false;
        }
        Optional<? extends ResultSetRow> row = nextRow();
        if (row.isPresent()) {
            currentRow = row.get();
            log.trace("Current row is {}", currentRow);
            return true;
        }
        return false;
    }
    
    protected abstract void initRows(final List<ResultSet> resultSets) throws SQLException;
    
    protected abstract Optional<? extends ResultSetRow> nextRow() throws SQLException;
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        String formattedColumnLabel = getColumnLabelIndexMap().containsKey(columnLabel) ? columnLabel : SQLUtil.getExactlyValue(columnLabel);
        if (!getColumnLabelIndexMap().containsKey(formattedColumnLabel)) {
            throw new SQLException(String.format("Column label %s does not exist", formattedColumnLabel));
        }
        return getColumnLabelIndexMap().get(formattedColumnLabel);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        Preconditions.checkState(!isClosed(), "Result set is closed");
        Preconditions.checkState(!beforeFirst, "Before start of result set");
        Preconditions.checkState(null != currentRow, "After end of result set");
        Preconditions.checkArgument(currentRow.inRange(columnIndex), String.format("Column Index %d out of range", columnIndex));
        Object result = currentRow.getCell(columnIndex);
        if (null == result) {
            wasNull = true;
        }
        return result;
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return getObject(findColumn(columnLabel));
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(getObject(columnIndex), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return getString(findColumn(columnLabel));
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        Object cell = getObject(columnIndex);
        if (null == cell) {
            wasNull = true;
            return false;
        }
        return (cell instanceof Boolean) ? (Boolean) cell : Boolean.valueOf(cell.toString());
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return getBoolean(findColumn(columnLabel));
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(getObject(columnIndex), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return getByte(findColumn(columnLabel));
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(getObject(columnIndex), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return getShort(findColumn(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(getObject(columnIndex), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return getInt(findColumn(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(getObject(columnIndex), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return getLong(findColumn(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(getObject(columnIndex), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return getFloat(findColumn(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(getObject(columnIndex), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return getDouble(findColumn(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        BigDecimal result = (BigDecimal) ResultSetUtil.convertValue(getObject(columnIndex), BigDecimal.class);
        return result.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return getBigDecimal(findColumn(columnLabel), scale);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(getObject(columnIndex), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return getBigDecimal(findColumn(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (null == value) {
            wasNull = true;
            return null;
        }
        return value.getBytes();
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return getBytes(findColumn(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return getDate(columnIndex, null);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return getDate(findColumn(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        // TODO 时间相关取值未实现calendar模式
        return (Date) ResultSetUtil.convertValue(getObject(columnIndex), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return getDate(findColumn(columnLabel), cal);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return getTime(columnIndex, null);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return getTime(findColumn(columnLabel));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(getObject(columnIndex), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return getTime(findColumn(columnLabel), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return getTimestamp(columnIndex, null);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return getTimestamp(findColumn(columnLabel));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(getObject(columnIndex), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnLabel), cal);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (null == value) {
            wasNull = true;
            return null;
        }
        try {
            return new URL(value);
        } catch (final MalformedURLException ex) {
            throw new SQLException("URL Malformed URL exception");
        }
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return getURL(findColumn(columnLabel));
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return getResultSets().get(0).getStatement();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getResultSets().get(0).getMetaData();
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        int result = 0;
        for (ResultSet each : getResultSets()) {
            result += each.getFetchSize();
        }
        return result;
    }
    
    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
}
