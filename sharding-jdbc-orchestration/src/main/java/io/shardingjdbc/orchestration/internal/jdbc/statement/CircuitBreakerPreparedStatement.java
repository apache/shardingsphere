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

package io.shardingjdbc.orchestration.internal.jdbc.statement;

import io.shardingjdbc.core.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import io.shardingjdbc.orchestration.internal.jdbc.connection.CircuitBreakerConnection;
import io.shardingjdbc.orchestration.internal.jdbc.resultset.CircuitBreakerResultSet;
import lombok.Getter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Circuit breaker preparedStatement.
 * 
 * @author caohao
 */
@Getter
public final class CircuitBreakerPreparedStatement extends AbstractUnsupportedOperationPreparedStatement {
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        
    }
    
    @Override
    public void clearParameters() throws SQLException {
        
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        
    }
    
    @Override
    public boolean execute() throws SQLException {
        return false;
    }
    
    @Override
    public void clearBatch() throws SQLException {
    }
    
    @Override
    public void addBatch() throws SQLException {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        
    }
    
    @Override
    public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        
    }
    
    @Override
    public void setClob(final int parameterIndex, final Clob x) throws SQLException {
        
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        return new int[]{-1};
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return new CircuitBreakerConnection();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    protected Collection<? extends Statement> getRoutedStatements() {
        return Collections.emptyList();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        return -1;
    }
}
