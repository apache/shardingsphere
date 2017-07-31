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

package com.dangdang.ddframe.rdb.sharding.jdbc.core.resultset;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMerger;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;

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
import java.util.List;

/**
 * 支持分片的结果集.
 *
 * @author zhangliang
 */
public final class ShardingResultSet extends AbstractResultSetAdapter {
    
    private final ResultSetMerger mergeResultSet;
    
    private boolean wasNull;
    
    public ShardingResultSet(final List<ResultSet> resultSets, final ResultSetMerger mergeResultSet) {
        super(resultSets);
        this.mergeResultSet = mergeResultSet;
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergeResultSet.next();
    }
    
    @Override
    // TODO
    public boolean wasNull() throws SQLException {
        return wasNull;
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, boolean.class);
        wasNull = false;
        return (boolean) ResultSetUtil.convertValue(result, boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, boolean.class);
        wasNull = false;
        return (boolean) ResultSetUtil.convertValue(result, boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, byte.class);
        wasNull = null == result;
        return (byte) ResultSetUtil.convertValue(result, byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, byte.class);
        wasNull = null == result;
        return (byte) ResultSetUtil.convertValue(result, byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, short.class);
        wasNull = null == result;
        return (short) ResultSetUtil.convertValue(result, short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, short.class);
        wasNull = null == result;
        return (short) ResultSetUtil.convertValue(result, short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, int.class);
        wasNull = null == result;
        return (int) ResultSetUtil.convertValue(result, int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, int.class);
        wasNull = null == result;
        return (int) ResultSetUtil.convertValue(result, int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, long.class);
        wasNull = null == result;
        return (long) ResultSetUtil.convertValue(result, long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, long.class);
        wasNull = null == result;
        return (long) ResultSetUtil.convertValue(result, long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, float.class);
        wasNull = null == result;
        return (float) ResultSetUtil.convertValue(result, float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, float.class);
        wasNull = null == result;
        return (float) ResultSetUtil.convertValue(result, float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, double.class);
        wasNull = null == result;
        return (double) ResultSetUtil.convertValue(result, double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, double.class);
        wasNull = null == result;
        return (double) ResultSetUtil.convertValue(result, double.class);
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, String.class);
        wasNull = null == result;
        return (String) ResultSetUtil.convertValue(result, String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, String.class);
        wasNull = null == result;
        return (String) ResultSetUtil.convertValue(result, String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, BigDecimal.class);
        wasNull = null == result;
        return (BigDecimal) ResultSetUtil.convertValue(result, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, BigDecimal.class);
        wasNull = null == result;
        return (BigDecimal) ResultSetUtil.convertValue(result, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, BigDecimal.class);
        wasNull = null == result;
        return (BigDecimal) ResultSetUtil.convertValue(result, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, BigDecimal.class);
        wasNull = null == result;
        return (BigDecimal) ResultSetUtil.convertValue(result, BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, byte[].class);
        wasNull = null == result;
        return (byte[]) ResultSetUtil.convertValue(result, byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, byte[].class);
        wasNull = null == result;
        return (byte[]) ResultSetUtil.convertValue(result, byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Date.class);
        wasNull = null == result;
        return (Date) ResultSetUtil.convertValue(result, Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Date.class);
        wasNull = null == result;
        return (Date) ResultSetUtil.convertValue(result, Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnIndex, Date.class, cal);
        wasNull = null == result;
        return (Date) ResultSetUtil.convertValue(result, Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnLabel, Date.class, cal);
        wasNull = null == result;
        return (Date) ResultSetUtil.convertValue(result, Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Time.class);
        wasNull = null == result;
        return (Time) ResultSetUtil.convertValue(result, Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Time.class);
        wasNull = null == result;
        return (Time) ResultSetUtil.convertValue(result, Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnIndex, Time.class, cal);
        wasNull = null == result;
        return (Time) ResultSetUtil.convertValue(result, Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnLabel, Time.class, cal);
        wasNull = null == result;
        return (Time) ResultSetUtil.convertValue(result, Time.class);
    }
            
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Timestamp.class);
        wasNull = null == result;
        return (Timestamp) ResultSetUtil.convertValue(result, Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Timestamp.class);
        wasNull = null == result;
        return (Timestamp) ResultSetUtil.convertValue(result, Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnIndex, Timestamp.class, cal);
        wasNull = null == result;
        return (Timestamp) ResultSetUtil.convertValue(result, Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        Object result = mergeResultSet.getCalendarValue(columnLabel, Timestamp.class, cal);
        wasNull = null == result;
        return (Timestamp) ResultSetUtil.convertValue(result, Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnIndex, "Ascii");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnLabel, "Ascii");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnIndex, "Unicode");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnLabel, "Unicode");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnIndex, "Binary");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        InputStream result = mergeResultSet.getInputStream(columnLabel, "Binary");
        wasNull = null == result;
        return result;
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Reader.class);
        wasNull = null == result;
        return (Reader) result;
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Reader.class);
        wasNull = null == result;
        return (Reader) result;
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Blob.class);
        wasNull = null == result;
        return (Blob) result;
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Blob.class);
        wasNull = null == result;
        return (Blob) result;
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Clob.class);
        wasNull = null == result;
        return (Clob) result;
    }
        
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Clob.class);
        wasNull = null == result;
        return (Clob) result;
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, URL.class);
        wasNull = null == result;
        return (URL) result;
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, URL.class);
        wasNull = null == result;
        return (URL) result;
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, SQLXML.class);
        wasNull = null == result;
        return (SQLXML) result;
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, SQLXML.class);
        wasNull = null == result;
        return (SQLXML) result;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        Object result = mergeResultSet.getValue(columnIndex, Object.class);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        Object result = mergeResultSet.getValue(columnLabel, Object.class);
        wasNull = null == result;
        return result;
    }
}
