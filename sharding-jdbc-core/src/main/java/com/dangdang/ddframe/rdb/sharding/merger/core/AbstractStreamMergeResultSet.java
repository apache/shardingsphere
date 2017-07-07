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

package com.dangdang.ddframe.rdb.sharding.merger.core;

import lombok.Getter;
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
@Getter
@Setter
public abstract class AbstractStreamMergeResultSet implements MergeResultSet {
    
    private ResultSet currentResultSet;
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Object.class == type) {
            return currentResultSet.getObject(columnIndex);
        }
        if (boolean.class == type) {
            return currentResultSet.getBoolean(columnIndex);
        }
        if (byte.class == type) {
            return currentResultSet.getByte(columnIndex);
        }
        if (short.class == type) {
            return currentResultSet.getShort(columnIndex);
        }
        if (int.class == type) {
            return currentResultSet.getInt(columnIndex);
        }
        if (long.class == type) {
            return currentResultSet.getLong(columnIndex);
        }
        if (float.class == type) {
            return currentResultSet.getFloat(columnIndex);
        }
        if (double.class == type) {
            return currentResultSet.getDouble(columnIndex);
        }
        if (String.class == type) {
            return currentResultSet.getString(columnIndex);
        }
        if (BigDecimal.class == type) {
            return currentResultSet.getBigDecimal(columnIndex);
        }
        if (BigDecimal.class == type) {
            return currentResultSet.getBigDecimal(columnIndex);
        }
        if (byte[].class == type) {
            return currentResultSet.getBytes(columnIndex);
        }
        if (Date.class == type) {
            return currentResultSet.getDate(columnIndex);
        }
        if (Time.class == type) {
            return currentResultSet.getTime(columnIndex);
        }
        if (Timestamp.class == type) {
            return currentResultSet.getTimestamp(columnIndex);
        }
        if (URL.class == type) {
            return currentResultSet.getURL(columnIndex);
        }
        if (Blob.class == type) {
            return currentResultSet.getBlob(columnIndex);
        }
        if (Clob.class == type) {
            return currentResultSet.getClob(columnIndex);
        }
        if (SQLXML.class == type) {
            return currentResultSet.getSQLXML(columnIndex);
        }
        if (Reader.class == type) {
            return currentResultSet.getCharacterStream(columnIndex);
        }
        return currentResultSet.getObject(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        if (Object.class == type) {
            return currentResultSet.getObject(columnLabel);
        }
        if (boolean.class == type) {
            return currentResultSet.getBoolean(columnLabel);
        }
        if (byte.class == type) {
            return currentResultSet.getByte(columnLabel);
        }
        if (short.class == type) {
            return currentResultSet.getShort(columnLabel);
        }
        if (int.class == type) {
            return currentResultSet.getInt(columnLabel);
        }
        if (long.class == type) {
            return currentResultSet.getLong(columnLabel);
        }
        if (float.class == type) {
            return currentResultSet.getFloat(columnLabel);
        }
        if (double.class == type) {
            return currentResultSet.getDouble(columnLabel);
        }
        if (String.class == type) {
            return currentResultSet.getString(columnLabel);
        }
        if (BigDecimal.class == type) {
            return currentResultSet.getBigDecimal(columnLabel);
        }
        if (BigDecimal.class == type) {
            return currentResultSet.getBigDecimal(columnLabel);
        }
        if (byte[].class == type) {
            return currentResultSet.getBytes(columnLabel);
        }
        if (Date.class == type) {
            return currentResultSet.getDate(columnLabel);
        }
        if (Time.class == type) {
            return currentResultSet.getTime(columnLabel);
        }
        if (Timestamp.class == type) {
            return currentResultSet.getTimestamp(columnLabel);
        }
        if (URL.class == type) {
            return currentResultSet.getURL(columnLabel);
        }
        if (Blob.class == type) {
            return currentResultSet.getBlob(columnLabel);
        }
        if (Clob.class == type) {
            return currentResultSet.getClob(columnLabel);
        }
        if (SQLXML.class == type) {
            return currentResultSet.getSQLXML(columnLabel);
        }
        if (Reader.class == type) {
            return currentResultSet.getCharacterStream(columnLabel);
        }
        return currentResultSet.getObject(columnLabel);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return currentResultSet.getDate(columnIndex, calendar);
        }
        if (Time.class == type) {
            return currentResultSet.getTime(columnIndex, calendar);
        }
        if (Timestamp.class == type) {
            return currentResultSet.getTimestamp(columnIndex, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return currentResultSet.getDate(columnLabel, calendar);
        }
        if (Time.class == type) {
            return currentResultSet.getTime(columnLabel, calendar);
        }
        if (Timestamp.class == type) {
            return currentResultSet.getTimestamp(columnLabel, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        if ("Ascii".equals(type)) {
            return currentResultSet.getAsciiStream(columnIndex);
        }
        if ("Unicode".equals(type)) {
            return currentResultSet.getUnicodeStream(columnIndex);
        }
        if ("Binary".equals(type)) {
            return currentResultSet.getBinaryStream(columnIndex);
        }
        if ("Binary".equals(type)) {
            return currentResultSet.getBinaryStream(columnIndex);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        if ("Ascii".equals(type)) {
            return currentResultSet.getAsciiStream(columnLabel);
        }
        if ("Unicode".equals(type)) {
            return currentResultSet.getUnicodeStream(columnLabel);
        }
        if ("Binary".equals(type)) {
            return currentResultSet.getBinaryStream(columnLabel);
        }
        if ("Binary".equals(type)) {
            return currentResultSet.getBinaryStream(columnLabel);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
}
