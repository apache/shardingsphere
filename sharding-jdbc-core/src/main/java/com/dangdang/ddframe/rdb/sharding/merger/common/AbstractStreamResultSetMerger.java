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
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (boolean.class == type) {
            result = getCurrentResultSet().getBoolean(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
        } else if (byte.class == type) {
            result = getCurrentResultSet().getByte(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (short.class == type) {
            result = getCurrentResultSet().getShort(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (int.class == type) {
            result = getCurrentResultSet().getInt(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (long.class == type) {
            result = getCurrentResultSet().getLong(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (float.class == type) {
            result = getCurrentResultSet().getFloat(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (double.class == type) {
            result = getCurrentResultSet().getDouble(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (String.class == type) {
            result = getCurrentResultSet().getString(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (BigDecimal.class == type) {
            result = getCurrentResultSet().getBigDecimal(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (byte[].class == type) {
            result = getCurrentResultSet().getBytes(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (URL.class == type) {
            result = getCurrentResultSet().getURL(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Blob.class == type) {
            result = getCurrentResultSet().getBlob(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Clob.class == type) {
            result = getCurrentResultSet().getClob(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (SQLXML.class == type) {
            result = getCurrentResultSet().getSQLXML(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else if (Reader.class == type) {
            result = getCurrentResultSet().getCharacterStream(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
            return result;
        } else {
            result = getCurrentResultSet().getObject(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
        }
        return result;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Object result;
        if (Object.class == type) {
            result = getCurrentResultSet().getObject(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (boolean.class == type) {
            result = getCurrentResultSet().getBoolean(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (byte.class == type) {
            result = getCurrentResultSet().getByte(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (short.class == type) {
            result = getCurrentResultSet().getShort(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (int.class == type) {
            result = getCurrentResultSet().getInt(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (long.class == type) {
            result = getCurrentResultSet().getLong(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (float.class == type) {
            result = getCurrentResultSet().getFloat(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (double.class == type) {
            result = getCurrentResultSet().getDouble(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (String.class == type) {
            result = getCurrentResultSet().getString(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (BigDecimal.class == type) {
            result = getCurrentResultSet().getBigDecimal(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (byte[].class == type) {
            result = getCurrentResultSet().getBytes(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (URL.class == type) {
            result = getCurrentResultSet().getURL(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Blob.class == type) {
            result = getCurrentResultSet().getBlob(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Clob.class == type) {
            result = getCurrentResultSet().getClob(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (SQLXML.class == type) {
            result = getCurrentResultSet().getSQLXML(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Reader.class == type) {
            result = getCurrentResultSet().getCharacterStream(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else {
            result = getCurrentResultSet().getObject(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        }
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result;
        if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnIndex, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnIndex, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnIndex, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        return result;
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        Object result;
        if (Date.class == type) {
            result = getCurrentResultSet().getDate(columnLabel, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Time.class == type) {
            result = getCurrentResultSet().getTime(columnLabel, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else if (Timestamp.class == type) {
            result = getCurrentResultSet().getTimestamp(columnLabel, calendar);
            wasNull = getCurrentResultSet().wasNull();
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        InputStream result;
        if ("Ascii".equals(type)) {
            result = getCurrentResultSet().getAsciiStream(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
        } else if ("Unicode".equals(type)) {
            result = getCurrentResultSet().getUnicodeStream(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
        } else if ("Binary".equals(type)) {
            result = getCurrentResultSet().getBinaryStream(columnIndex);
            wasNull = getCurrentResultSet().wasNull();
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        InputStream result;
        if ("Ascii".equals(type)) {
            result = getCurrentResultSet().getAsciiStream(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if ("Unicode".equals(type)) {
            result = getCurrentResultSet().getUnicodeStream(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else if ("Binary".equals(type)) {
            result = getCurrentResultSet().getBinaryStream(columnLabel);
            wasNull = getCurrentResultSet().wasNull();
        } else {
            throw new SQLException(String.format("Unsupported type: %s", type));
        }
        return result;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }
}
