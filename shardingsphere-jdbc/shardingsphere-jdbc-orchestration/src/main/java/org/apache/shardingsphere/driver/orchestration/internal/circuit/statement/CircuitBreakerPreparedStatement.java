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

package org.apache.shardingsphere.driver.orchestration.internal.circuit.statement;

import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import org.apache.shardingsphere.driver.orchestration.internal.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.driver.orchestration.internal.circuit.resultset.CircuitBreakerResultSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Circuit breaker preparedStatement.
 */
@Getter
public final class CircuitBreakerPreparedStatement extends AbstractUnsupportedOperationPreparedStatement {
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) {
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) {
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) {
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) {
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) {
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) {
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) {
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) {
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) {
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) {
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) {
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) {
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) {
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) {
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) {
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) {
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) {
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) {
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) {
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) {
    }
    
    @Override
    public void clearParameters() {
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) {
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) {
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) {
    }
    
    @Override
    public boolean execute() {
        return false;
    }
    
    @Override
    public void clearBatch() {
    }
    
    @Override
    public void addBatch() {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) {
    }
    
    @Override
    public void setBlob(final int parameterIndex, final Blob x) {
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) {
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) {
    }
    
    @Override
    public void setClob(final int parameterIndex, final Clob x) {
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) {
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) {
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) {
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() {
        return null;
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) {
    }
    
    @Override
    public int[] executeBatch() {
        return new int[]{-1};
    }
    
    @Override
    public Connection getConnection() {
        return new CircuitBreakerConnection();
    }
    
    @Override
    public ResultSet getGeneratedKeys() {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int getResultSetHoldability() {
        return 0;
    }
    
    @Override
    public ResultSet getResultSet() {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int getResultSetConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public int getResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    protected boolean isAccumulate() {
        return false;
    }
    
    @Override
    protected Collection<? extends Statement> getRoutedStatements() {
        return Collections.emptyList();
    }
    
    @Override
    public ResultSet executeQuery() {
        return new CircuitBreakerResultSet();
    }
    
    @Override
    public int executeUpdate() {
        return -1;
    }
}
