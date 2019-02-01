/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.base.Preconditions;
import io.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Master-slave adapter for {@code PreparedStatement}.
 *
 * @author zhangliang
 */
public abstract class AbstractMasterSlavePreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        getTargetPreparedStatement().setNull(parameterIndex, sqlType);
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        getTargetPreparedStatement().setNull(parameterIndex, sqlType, typeName);
    }
    
    @Override
    public final void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        getTargetPreparedStatement().setBoolean(parameterIndex, x);
    }
    
    @Override
    public final void setByte(final int parameterIndex, final byte x) throws SQLException {
        getTargetPreparedStatement().setByte(parameterIndex, x);
    }
    
    @Override
    public final void setShort(final int parameterIndex, final short x) throws SQLException {
        getTargetPreparedStatement().setShort(parameterIndex, x);
    }
    
    @Override
    public final void setInt(final int parameterIndex, final int x) throws SQLException {
        getTargetPreparedStatement().setInt(parameterIndex, x);
    }
    
    @Override
    public final void setLong(final int parameterIndex, final long x) throws SQLException {
        getTargetPreparedStatement().setLong(parameterIndex, x);
    }
    
    @Override
    public final void setFloat(final int parameterIndex, final float x) throws SQLException {
        getTargetPreparedStatement().setFloat(parameterIndex, x);
    }
    
    @Override
    public final void setDouble(final int parameterIndex, final double x) throws SQLException {
        getTargetPreparedStatement().setDouble(parameterIndex, x);
    }
    
    @Override
    public final void setString(final int parameterIndex, final String x) throws SQLException {
        getTargetPreparedStatement().setString(parameterIndex, x);
    }
    
    @Override
    public final void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        getTargetPreparedStatement().setBigDecimal(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x) throws SQLException {
        getTargetPreparedStatement().setDate(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        getTargetPreparedStatement().setDate(parameterIndex, x, cal);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x) throws SQLException {
        getTargetPreparedStatement().setTime(parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        getTargetPreparedStatement().setTime(parameterIndex, x, cal);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        getTargetPreparedStatement().setTimestamp(parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        getTargetPreparedStatement().setTimestamp(parameterIndex, x, cal);
    }
    
    @Override
    public final void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        getTargetPreparedStatement().setBytes(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        getTargetPreparedStatement().setBlob(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) throws SQLException {
        getTargetPreparedStatement().setBlob(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        getTargetPreparedStatement().setBlob(parameterIndex, x, length);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) throws SQLException {
        getTargetPreparedStatement().setClob(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) throws SQLException {
        getTargetPreparedStatement().setClob(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) throws SQLException {
        getTargetPreparedStatement().setClob(parameterIndex, x, length);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        getTargetPreparedStatement().setAsciiStream(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        getTargetPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        getTargetPreparedStatement().setAsciiStream(parameterIndex, x, length);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        getTargetPreparedStatement().setUnicodeStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        getTargetPreparedStatement().setBinaryStream(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        getTargetPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        getTargetPreparedStatement().setBinaryStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) throws SQLException {
        getTargetPreparedStatement().setCharacterStream(parameterIndex, x);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) throws SQLException {
        getTargetPreparedStatement().setCharacterStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) throws SQLException {
        getTargetPreparedStatement().setCharacterStream(parameterIndex, x, length);
    }
    
    @Override
    public final void setSQLXML(final int parameterIndex, final SQLXML x) throws SQLException {
        getTargetPreparedStatement().setSQLXML(parameterIndex, x);
    }
    
    @Override
    public final void setURL(final int parameterIndex, final URL x) throws SQLException {
        getTargetPreparedStatement().setURL(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x) throws SQLException {
        getTargetPreparedStatement().setObject(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        getTargetPreparedStatement().setObject(parameterIndex, x, targetSqlType);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        getTargetPreparedStatement().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }
    
    @Override
    public final void clearParameters() throws SQLException {
        getTargetPreparedStatement().clearParameters();
    }
    
    private PreparedStatement getTargetPreparedStatement() {
        Preconditions.checkArgument(1 == getRoutedStatements().size(), "Cannot support setParameter for DDL");
        return (PreparedStatement) getRoutedStatements().iterator().next();
    }
}
