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

package com.dangdang.ddframe.rdb.sharding.merger.common;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMerger;
import lombok.Setter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * 流式归并结果集.
 *
 * @author thor zhangliang
 */
@Setter
public abstract class AbstractStreamResultSetMerger implements ResultSetMerger {
    
    private ResultSet currentResultSet;
    
    protected ResultSet getCurrentResultSet() throws SQLException {
        if (null == currentResultSet) {
            throw new SQLException("Current ResultSet is null, ResultSet perhaps end of next.");
        }
        return currentResultSet;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Object.class == type) {
            return getCurrentResultSet().getObject(columnIndex);
        }
        if (boolean.class == type) {
            return getCurrentResultSet().getBoolean(columnIndex);
        }
        if (byte.class == type) {
            return getCurrentResultSet().getByte(columnIndex);
        }
        if (short.class == type) {
            return getCurrentResultSet().getShort(columnIndex);
        }
        if (int.class == type) {
            return getCurrentResultSet().getInt(columnIndex);
        }
        if (long.class == type) {
            return getCurrentResultSet().getLong(columnIndex);
        }
        if (float.class == type) {
            return getCurrentResultSet().getFloat(columnIndex);
        }
        if (double.class == type) {
            return getCurrentResultSet().getDouble(columnIndex);
        }
        if (String.class == type) {
            return getCurrentResultSet().getString(columnIndex);
        }
        if (BigDecimal.class == type) {
            return getCurrentResultSet().getBigDecimal(columnIndex);
        }
        if (byte[].class == type) {
            return getCurrentResultSet().getBytes(columnIndex);
        }
        if (Date.class == type) {
            return getCurrentResultSet().getDate(columnIndex);
        }
        if (Time.class == type) {
            return getCurrentResultSet().getTime(columnIndex);
        }
        if (Timestamp.class == type) {
            return getCurrentResultSet().getTimestamp(columnIndex);
        }
        if (URL.class == type) {
            return getCurrentResultSet().getURL(columnIndex);
        }
        if (Blob.class == type) {
            return getCurrentResultSet().getBlob(columnIndex);
        }
        if (Clob.class == type) {
            return getCurrentResultSet().getClob(columnIndex);
        }
        if (SQLXML.class == type) {
            return getCurrentResultSet().getSQLXML(columnIndex);
        }
        if (Reader.class == type) {
            return getCurrentResultSet().getCharacterStream(columnIndex);
        }
        return getCurrentResultSet().getObject(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        if (Object.class == type) {
            return getCurrentResultSet().getObject(columnLabel);
        }
        if (boolean.class == type) {
            return getCurrentResultSet().getBoolean(columnLabel);
        }
        if (byte.class == type) {
            return getCurrentResultSet().getByte(columnLabel);
        }
        if (short.class == type) {
            return getCurrentResultSet().getShort(columnLabel);
        }
        if (int.class == type) {
            return getCurrentResultSet().getInt(columnLabel);
        }
        if (long.class == type) {
            return getCurrentResultSet().getLong(columnLabel);
        }
        if (float.class == type) {
            return getCurrentResultSet().getFloat(columnLabel);
        }
        if (double.class == type) {
            return getCurrentResultSet().getDouble(columnLabel);
        }
        if (String.class == type) {
            return getCurrentResultSet().getString(columnLabel);
        }
        if (BigDecimal.class == type) {
            return getCurrentResultSet().getBigDecimal(columnLabel);
        }
        if (byte[].class == type) {
            return getCurrentResultSet().getBytes(columnLabel);
        }
        if (Date.class == type) {
            return getCurrentResultSet().getDate(columnLabel);
        }
        if (Time.class == type) {
            return getCurrentResultSet().getTime(columnLabel);
        }
        if (Timestamp.class == type) {
            return getCurrentResultSet().getTimestamp(columnLabel);
        }
        if (URL.class == type) {
            return getCurrentResultSet().getURL(columnLabel);
        }
        if (Blob.class == type) {
            return getCurrentResultSet().getBlob(columnLabel);
        }
        if (Clob.class == type) {
            return getCurrentResultSet().getClob(columnLabel);
        }
        if (SQLXML.class == type) {
            return getCurrentResultSet().getSQLXML(columnLabel);
        }
        if (Reader.class == type) {
            return getCurrentResultSet().getCharacterStream(columnLabel);
        }
        return getCurrentResultSet().getObject(columnLabel);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return getCurrentResultSet().getDate(columnIndex, calendar);
        }
        if (Time.class == type) {
            return getCurrentResultSet().getTime(columnIndex, calendar);
        }
        if (Timestamp.class == type) {
            return getCurrentResultSet().getTimestamp(columnIndex, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return getCurrentResultSet().getDate(columnLabel, calendar);
        }
        if (Time.class == type) {
            return getCurrentResultSet().getTime(columnLabel, calendar);
        }
        if (Timestamp.class == type) {
            return getCurrentResultSet().getTimestamp(columnLabel, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        if ("Ascii".equals(type)) {
            return getCurrentResultSet().getAsciiStream(columnIndex);
        }
        if ("Unicode".equals(type)) {
            return getCurrentResultSet().getUnicodeStream(columnIndex);
        }
        if ("Binary".equals(type)) {
            return getCurrentResultSet().getBinaryStream(columnIndex);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        if ("Ascii".equals(type)) {
            return getCurrentResultSet().getAsciiStream(columnLabel);
        }
        if ("Unicode".equals(type)) {
            return getCurrentResultSet().getUnicodeStream(columnLabel);
        }
        if ("Binary".equals(type)) {
            return getCurrentResultSet().getBinaryStream(columnLabel);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
}
