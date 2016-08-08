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
    
package com.dangdang.ddframe.rdb.sharding.merger.fixture;

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationResultSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
    
public abstract class AbstractUnsupportedOperationMockResultSet extends AbstractUnsupportedOperationResultSet {
    
    @Override
    public final boolean wasNull() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final boolean getBoolean(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final boolean getBoolean(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final byte getByte(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final byte getByte(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final short getShort(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final short getShort(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final long getLong(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final long getLong(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final float getFloat(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final float getFloat(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final double getDouble(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final double getDouble(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final byte[] getBytes(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final byte[] getBytes(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Date getDate(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Date getDate(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Time getTime(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Time getTime(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getAsciiStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getAsciiStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getBinaryStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final InputStream getBinaryStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Reader getCharacterStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    

    
    @Override
    public final int getType() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final int getConcurrency() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    

    
    @Override
    public final Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Blob getBlob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Blob getBlob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Clob getClob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final Clob getClob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final URL getURL(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final URL getURL(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
