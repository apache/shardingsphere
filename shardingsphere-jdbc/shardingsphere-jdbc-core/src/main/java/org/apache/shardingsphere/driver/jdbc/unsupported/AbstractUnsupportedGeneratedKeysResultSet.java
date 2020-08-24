/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.jdbc.unsupported;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.sql.Array;
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

/**
 * Unsupported {@code ResultSet} methods for generated keys.
 */
public abstract class AbstractUnsupportedGeneratedKeysResultSet extends AbstractUnsupportedOperationResultSet {
    
    @Override
    public final boolean getBoolean(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBoolean");
    }
    
    @Override
    public final boolean getBoolean(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBoolean");
    }
    
    @Override
    public final Date getDate(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getDate");
    }
    
    @Override
    public final Date getDate(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getDate");
    }
    
    @Override
    public final Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getDate");
    }
    
    @Override
    public final Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getDate");
    }
    
    @Override
    public final Time getTime(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTime");
    }
    
    @Override
    public final Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTime");
    }
    
    @Override
    public final Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTime");
    }
    
    @Override
    public final Time getTime(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTime");
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTimestamp");
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTimestamp");
    }
    
    @Override
    public final Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTimestamp");
    }
    
    @Override
    public final Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("getTimestamp");
    }
    
    @Override
    public final Array getArray(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getArray");
    }
    
    @Override
    public final Array getArray(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getArray");
    }
    
    @Override
    public final InputStream getAsciiStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getAsciiStream");
    }
    
    @Override
    public final InputStream getAsciiStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getAsciiStream");
    }
    
    @Override
    public final InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getUnicodeStream");
    }
    
    @Override
    public final InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getUnicodeStream");
    }
    
    @Override
    public final InputStream getBinaryStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBinaryStream");
    }
    
    @Override
    public final InputStream getBinaryStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBinaryStream");
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException("getWarnings");
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException("clearWarnings");
    }
    
    @Override
    public final Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getCharacterStream");
    }
    
    @Override
    public final Reader getCharacterStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getCharacterStream");
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException("setFetchDirection");
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        throw new SQLFeatureNotSupportedException("getFetchDirection");
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException("setFetchSize");
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        throw new SQLFeatureNotSupportedException("getFetchSize");
    }
    
    @Override
    public final Blob getBlob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBlob");
    }
    
    @Override
    public final Blob getBlob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBlob");
    }
    
    @Override
    public final Clob getClob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getClob");
    }
    
    @Override
    public final Clob getClob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getClob");
    }
    
    @Override
    public final URL getURL(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getURL");
    }
    
    @Override
    public final URL getURL(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getURL");
    }
    
    @Override
    public final SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getSQLXML");
    }
    
    @Override
    public final SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getSQLXML");
    }
}
