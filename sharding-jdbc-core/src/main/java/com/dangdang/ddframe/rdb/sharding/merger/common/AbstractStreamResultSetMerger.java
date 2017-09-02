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
    
    private boolean wasNull;
    
    protected ResultSet getCurrentResultSet() throws SQLException {
        if (null == currentResultSet) {
            throw new SQLException("Current ResultSet is null, ResultSet perhaps end of next.");
        }
        return currentResultSet;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object result;
        if (Object.class == type) {
            result = getCurrentResultSet().getObject(columnIndex);
        } else if (boolean.class == type) {
            result = getCurrentResultSet().getBoolean(columnIndex);
        } else if (byte.class == type) {
            result = getCurrentResultSet().getByte(columnIndex);
        } else if (short.class == type) {
            result = getCurrentResultSet().getShort(columnIndex);
        } else if (int.class == type) {
            result = getCurrentResultSet().getInt(columnIndex);
        } else if (long.class == type) {
            result = getCurrentResultSet().getLong(columnIndex);
        } else if (float.class == type) {
            result = getCurrentResultSet().getFloat(columnIndex);
        } else if (double.class == type) {
            result = getCurrentResultSet().getDouble(columnIndex);
        } else if (String.class == type) {
            result = getCurrentResultSet().getString(columnIndex);
        } else if (BigDecimal.class == type) {
            result = getCurrentResultSet().getBigDecimal(columnIndex);
        } else if (byte[].class == type) {
            result = getCurrentResultSet().getBytes(columnIndex);
        } else if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnIndex);
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnIndex);
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnIndex);
        } else if (URL.class == type) {
            result = getCurrentResultSet().getURL(columnIndex);
        } else if (Blob.class == type) {
            result = getCurrentResultSet().getBlob(columnIndex);
        } else if (Clob.class == type) {
            result = getCurrentResultSet().getClob(columnIndex);
        } else if (SQLXML.class == type) {
            result = getCurrentResultSet().getSQLXML(columnIndex);
        } else if (Reader.class == type) {
            result = getCurrentResultSet().getCharacterStream(columnIndex);
        } else {
            result = getCurrentResultSet().getObject(columnIndex);
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Object result;
        if (Object.class == type) {
            result = getCurrentResultSet().getObject(columnLabel);
        } else if (boolean.class == type) {
            result = getCurrentResultSet().getBoolean(columnLabel);
        } else if (byte.class == type) {
            result = getCurrentResultSet().getByte(columnLabel);
        } else if (short.class == type) {
            result = getCurrentResultSet().getShort(columnLabel);
        } else if (int.class == type) {
            result = getCurrentResultSet().getInt(columnLabel);
        } else if (long.class == type) {
            result = getCurrentResultSet().getLong(columnLabel);
        } else if (float.class == type) {
            result = getCurrentResultSet().getFloat(columnLabel);
        } else if (double.class == type) {
            result = getCurrentResultSet().getDouble(columnLabel);
        } else if (String.class == type) {
            result = getCurrentResultSet().getString(columnLabel);
        } else if (BigDecimal.class == type) {
            result = getCurrentResultSet().getBigDecimal(columnLabel);
        } else if (byte[].class == type) {
            result = getCurrentResultSet().getBytes(columnLabel);
        } else if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnLabel);
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnLabel);
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnLabel);
        } else if (URL.class == type) {
            result = getCurrentResultSet().getURL(columnLabel);
        } else if (Blob.class == type) {
            result = getCurrentResultSet().getBlob(columnLabel);
        } else if (Clob.class == type) {
            result = getCurrentResultSet().getClob(columnLabel);
        } else if (SQLXML.class == type) {
            result = getCurrentResultSet().getSQLXML(columnLabel);
        } else if (Reader.class == type) {
            result = getCurrentResultSet().getCharacterStream(columnLabel);
        } else {
            result = getCurrentResultSet().getObject(columnLabel);
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result;
        if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnIndex, calendar);
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnIndex, calendar);
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnIndex, calendar);
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result;
        if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnLabel, calendar);
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnLabel, calendar);
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnLabel, calendar);
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        InputStream result;
        if ("Ascii".equals(type)) {
            result = getCurrentResultSet().getAsciiStream(columnIndex);
        } else if ("Unicode".equals(type)) {
            result = getCurrentResultSet().getUnicodeStream(columnIndex);
        } else if ("Binary".equals(type)) {
            result = getCurrentResultSet().getBinaryStream(columnIndex);
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        InputStream result;
        if ("Ascii".equals(type)) {
            result = getCurrentResultSet().getAsciiStream(columnLabel);
        } else if ("Unicode".equals(type)) {
            result = getCurrentResultSet().getUnicodeStream(columnLabel);
        } else if ("Binary".equals(type)) {
            result = getCurrentResultSet().getBinaryStream(columnLabel);
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        wasNull = getCurrentResultSet().wasNull();
        return result;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }
}
