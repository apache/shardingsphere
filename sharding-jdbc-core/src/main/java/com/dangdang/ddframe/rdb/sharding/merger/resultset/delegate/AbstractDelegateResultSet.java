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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.delegate;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 代理结果集抽象类.
 * 
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractDelegateResultSet extends AbstractResultSetAdapter {
    
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private ResultSet delegate;
    
    private boolean beforeFirst = true;
    
    private int offset;
    
    public AbstractDelegateResultSet(final List<ResultSet> resultSets) throws SQLException {
        super(resultSets);
        delegate = resultSets.get(0);
        LoggerFactory.getLogger(this.getClass().getName()).debug("{} join pipeline", this.hashCode());
    }
    
    @Override
    public final boolean next() throws SQLException {
        boolean result = beforeFirst ? firstNext() : afterFirstNext();
        beforeFirst = false;
        if (result) {
            LoggerFactory.getLogger(this.getClass().getName()).trace("Access result set, total size is: {}, result set hashcode is: {}, offset is: {}", getResultSets().size(), delegate.hashCode(), ++offset);
        }
        return result;
    }
    
    protected abstract boolean firstNext() throws SQLException;
    
    protected abstract boolean afterFirstNext() throws SQLException;
    
    @Override
    public final boolean getBoolean(final int columnIndex) throws SQLException {
        return delegate.getBoolean(columnIndex);
    }
    
    @Override
    public final boolean getBoolean(final String columnLabel) throws SQLException {
        return delegate.getBoolean(columnLabel);
    }
    
    @Override
    public final byte getByte(final int columnIndex) throws SQLException {
        return delegate.getByte(columnIndex);
    }
    
    @Override
    public final byte getByte(final String columnLabel) throws SQLException {
        return delegate.getByte(columnLabel);
    }
    
    @Override
    public final short getShort(final int columnIndex) throws SQLException {
        return delegate.getShort(columnIndex);
    }
    
    @Override
    public final short getShort(final String columnLabel) throws SQLException {
        return delegate.getShort(columnLabel);
    }
    
    @Override
    public final int getInt(final int columnIndex) throws SQLException {
        return delegate.getInt(columnIndex);
    }
    
    @Override
    public final int getInt(final String columnLabel) throws SQLException {
        return delegate.getInt(columnLabel);
    }
    
    @Override
    public final long getLong(final int columnIndex) throws SQLException {
        return delegate.getLong(columnIndex);
    }
    
    @Override
    public final long getLong(final String columnLabel) throws SQLException {
        return delegate.getLong(columnLabel);
    }
    
    @Override
    public final float getFloat(final int columnIndex) throws SQLException {
        return delegate.getFloat(columnIndex);
    }
    
    @Override
    public final float getFloat(final String columnLabel) throws SQLException {
        return delegate.getFloat(columnLabel);
    }
    
    @Override
    public final double getDouble(final int columnIndex) throws SQLException {
        return delegate.getDouble(columnIndex);
    }
    
    @Override
    public final double getDouble(final String columnLabel) throws SQLException {
        return delegate.getDouble(columnLabel);
    }
    
    @Override
    public final String getString(final int columnIndex) throws SQLException {
        return delegate.getString(columnIndex);
    }
    
    @Override
    public final String getString(final String columnLabel) throws SQLException {
        return delegate.getString(columnLabel);
    }
    
    @Override
    public final BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return delegate.getBigDecimal(columnIndex);
    }
    
    @Override
    public final BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return delegate.getBigDecimal(columnLabel);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return delegate.getBigDecimal(columnIndex, scale);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return delegate.getBigDecimal(columnLabel, scale);
    }
    
    @Override
    public final byte[] getBytes(final int columnIndex) throws SQLException {
        return delegate.getBytes(columnIndex);
    }
    
    @Override
    public final byte[] getBytes(final String columnLabel) throws SQLException {
        return delegate.getBytes(columnLabel);
    }
    
    @Override
    public final Date getDate(final int columnIndex) throws SQLException {
        return delegate.getDate(columnIndex);
    }
    
    @Override
    public final Date getDate(final String columnLabel) throws SQLException {
        return delegate.getDate(columnLabel);
    }
    
    @Override
    public final Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getDate(columnIndex, cal);
    }
    
    @Override
    public final Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return delegate.getDate(columnLabel, cal);
    }
    
    @Override
    public final Time getTime(final int columnIndex) throws SQLException {
        return delegate.getTime(columnIndex);
    }
    
    @Override
    public final Time getTime(final String columnLabel) throws SQLException {
        return delegate.getTime(columnLabel);
    }
    
    @Override
    public final Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getTime(columnIndex, cal);
    }
    
    @Override
    public final Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return delegate.getTime(columnLabel, cal);
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return delegate.getTimestamp(columnIndex);
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return delegate.getTimestamp(columnLabel);
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnIndex, cal);
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnLabel, cal);
    }
    
    @Override
    public final InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return delegate.getAsciiStream(columnIndex);
    }
    
    @Override
    public final InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return delegate.getAsciiStream(columnLabel);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return delegate.getUnicodeStream(columnIndex);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return delegate.getUnicodeStream(columnLabel);
    }
    
    @Override
    public final InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return delegate.getBinaryStream(columnIndex);
    }
    
    @Override
    public final InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return delegate.getBinaryStream(columnLabel);
    }
    
    @Override
    public final Reader getCharacterStream(final int columnIndex) throws SQLException {
        return delegate.getCharacterStream(columnIndex);
    }
    
    @Override
    public final Reader getCharacterStream(final String columnLabel) throws SQLException {
        return delegate.getCharacterStream(columnLabel);
    }
    
    @Override
    public final Blob getBlob(final int columnIndex) throws SQLException {
        return delegate.getBlob(columnIndex);
    }
    
    @Override
    public final Blob getBlob(final String columnLabel) throws SQLException {
        return delegate.getBlob(columnLabel);
    }
    
    @Override
    public final Clob getClob(final int columnIndex) throws SQLException {
        return delegate.getClob(columnIndex);
    }
    
    @Override
    public final Clob getClob(final String columnLabel) throws SQLException {
        return delegate.getClob(columnLabel);
    }
    
    @Override
    public final URL getURL(final int columnIndex) throws SQLException {
        return delegate.getURL(columnIndex);
    }
    
    @Override
    public final URL getURL(final String columnLabel) throws SQLException {
        return delegate.getURL(columnLabel);
    }
    
    @Override
    public final SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return delegate.getSQLXML(columnIndex);
    }
    
    @Override
    public final SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return delegate.getSQLXML(columnLabel);
    }
    
    @Override
    public final Object getObject(final int columnIndex) throws SQLException {
        return delegate.getObject(columnIndex);
    }
    
    @Override
    public final Object getObject(final String columnLabel) throws SQLException {
        return delegate.getObject(columnLabel);
    }
    
    @Override
    public final Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(columnIndex, map);
    }
    
    @Override
    public final Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(columnLabel, map);
    }
    
    @Override
    public final boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }
    
    @Override
    public final int getType() throws SQLException {
        return delegate.getType();
    }
    
    @Override
    public final int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }
    
    @Override
    public final Statement getStatement() throws SQLException {
        return delegate.getStatement();
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }
    
    @Override
    public final int findColumn(final String columnLabel) throws SQLException {
        return delegate.findColumn(columnLabel);
    }
}
