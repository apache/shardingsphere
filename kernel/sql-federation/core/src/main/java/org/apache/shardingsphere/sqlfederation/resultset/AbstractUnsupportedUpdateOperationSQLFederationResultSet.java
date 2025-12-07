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

package org.apache.shardingsphere.sqlfederation.resultset;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Unsupported {@code ResultSet} update methods for SQL federation.
 */
public abstract class AbstractUnsupportedUpdateOperationSQLFederationResultSet extends SQLFederationWrapperAdapter implements ResultSet {
    
    @Override
    public final void updateNull(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNull");
    }
    
    @Override
    public final void updateNull(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNull");
    }
    
    @Override
    public final void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBoolean");
    }
    
    @Override
    public final void updateBoolean(final String columnLabel, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBoolean");
    }
    
    @Override
    public final void updateByte(final int columnIndex, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateByte");
    }
    
    @Override
    public final void updateByte(final String columnLabel, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateByte");
    }
    
    @Override
    public final void updateShort(final int columnIndex, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateShort");
    }
    
    @Override
    public final void updateShort(final String columnLabel, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateShort");
    }
    
    @Override
    public final void updateInt(final int columnIndex, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateInt");
    }
    
    @Override
    public final void updateInt(final String columnLabel, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateInt");
    }
    
    @Override
    public final void updateLong(final int columnIndex, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateLong");
    }
    
    @Override
    public final void updateLong(final String columnLabel, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateLong");
    }
    
    @Override
    public final void updateFloat(final int columnIndex, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateFloat");
    }
    
    @Override
    public final void updateFloat(final String columnLabel, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateFloat");
    }
    
    @Override
    public final void updateDouble(final int columnIndex, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateDouble");
    }
    
    @Override
    public final void updateDouble(final String columnLabel, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateDouble");
    }
    
    @Override
    public final void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBigDecimal");
    }
    
    @Override
    public final void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBigDecimal");
    }
    
    @Override
    public final void updateString(final int columnIndex, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateString");
    }
    
    @Override
    public final void updateString(final String columnLabel, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateString");
    }
    
    @Override
    public final void updateNString(final int columnIndex, final String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNString");
    }
    
    @Override
    public final void updateNString(final String columnLabel, final String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNString");
    }
    
    @Override
    public final void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBytes");
    }
    
    @Override
    public final void updateBytes(final String columnLabel, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBytes");
    }
    
    @Override
    public final void updateDate(final int columnIndex, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateDate");
    }
    
    @Override
    public final void updateDate(final String columnLabel, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateDate");
    }
    
    @Override
    public final void updateTime(final int columnIndex, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateTime");
    }
    
    @Override
    public final void updateTime(final String columnLabel, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateTime");
    }
    
    @Override
    public final void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateTimestamp");
    }
    
    @Override
    public final void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateTimestamp");
    }
    
    @Override
    public final void updateAsciiStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateAsciiStream(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateAsciiStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateAsciiStream(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateAsciiStream");
    }
    
    @Override
    public final void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBinaryStream");
    }
    
    @Override
    public final void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateCharacterStream(final String columnLabel, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateCharacterStream");
    }
    
    @Override
    public final void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNCharacterStream");
    }
    
    @Override
    public final void updateNCharacterStream(final String columnLabel, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNCharacterStream");
    }
    
    @Override
    public final void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNCharacterStream");
    }
    
    @Override
    public final void updateNCharacterStream(final String columnLabel, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNCharacterStream");
    }
    
    @Override
    public final void updateObject(final int columnIndex, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject");
    }
    
    @Override
    public final void updateObject(final String columnLabel, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject");
    }
    
    @Override
    public final void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject");
    }
    
    @Override
    public final void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateObject");
    }
    
    @Override
    public final void updateRef(final int columnIndex, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateRef");
    }
    
    @Override
    public final void updateRef(final String columnLabel, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateRef");
    }
    
    @Override
    public final void updateBlob(final int columnIndex, final Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateBlob(final String columnLabel, final Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateBlob");
    }
    
    @Override
    public final void updateClob(final int columnIndex, final Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateClob(final String columnLabel, final Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateClob");
    }
    
    @Override
    public final void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateNClob");
    }
    
    @Override
    public final void updateArray(final int columnIndex, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateArray");
    }
    
    @Override
    public final void updateArray(final String columnLabel, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateArray");
    }
    
    @Override
    public final void updateRowId(final int columnIndex, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateRowId");
    }
    
    @Override
    public final void updateRowId(final String columnLabel, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateRowId");
    }
    
    @Override
    public final void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateSQLXML");
    }
    
    @Override
    public final void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("updateSQLXML");
    }
}
