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

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.invocation.SetParameterMethodInvocation;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import lombok.Getter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Adapter for {@code PreparedStatement}.
 *
 * @author zhangliang
 */
public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    private boolean closed;
    
    private boolean poolable;
    
    private int fetchSize;
    
    private final List<SetParameterMethodInvocation> setParameterMethodInvocations = new LinkedList<>();
    
    @Getter
    private final List<Object> parameters = new ArrayList<>();
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        getRoutedPreparedStatements().clear();
        Collection<SQLException> exceptions = new LinkedList<>();
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            try {
                each.close();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public final boolean isPoolable() throws SQLException {
        return poolable;
    }
    
    @Override
    public final void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setPoolable", new Class[] {boolean.class}, new Object[] {poolable});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setPoolable(poolable);
        }
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return fetchSize;
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        this.fetchSize = rows;
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setFetchSize", new Class[] {int.class}, new Object[] {rows});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setFetchSize(rows);
        }
    }
    
    @Override
    public final void setEscapeProcessing(final boolean enable) throws SQLException {
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setEscapeProcessing", new Class[] {boolean.class}, new Object[] {enable});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setEscapeProcessing(enable);
        }
    }
    
    @Override
    public final void cancel() throws SQLException {
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.cancel();
        }
    }
    
    @Override
    public final int getUpdateCount() throws SQLException {
        long result = 0;
        boolean hasResult = false;
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            if (each.getUpdateCount() > -1) {
                hasResult = true;
            }
            result += each.getUpdateCount();
        }
        if (result > Integer.MAX_VALUE) {
            result = Integer.MAX_VALUE;
        }
        return hasResult ? Long.valueOf(result).intValue() : -1;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
    
    @Override
    public final boolean getMoreResults() throws SQLException {
        return false;
    }
    
    @Override
    public final boolean getMoreResults(final int current) throws SQLException {
        return false;
    }
    
    @Override
    public final int getMaxFieldSize() throws SQLException {
        return getRoutedPreparedStatements().isEmpty() ? 0 : getRoutedPreparedStatements().iterator().next().getMaxFieldSize();
    }
    
    @Override
    public final void setMaxFieldSize(final int max) throws SQLException {
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setMaxFieldSize", new Class[] {int.class}, new Object[] {max});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setMaxFieldSize(max);
        }
    }
    
    // TODO Confirm MaxRows for multiple databases is need special handle. eg: 10 statements maybe MaxRows / 10
    @Override
    public final int getMaxRows() throws SQLException {
        return getRoutedPreparedStatements().isEmpty() ? -1 : getRoutedPreparedStatements().iterator().next().getMaxRows();
    }
    
    @Override
    public final void setMaxRows(final int max) throws SQLException {
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setMaxRows", new Class[] {int.class}, new Object[] {max});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setMaxRows(max);
        }
    }
    
    @Override
    public final int getQueryTimeout() throws SQLException {
        return getRoutedPreparedStatements().isEmpty() ? 0 : getRoutedPreparedStatements().iterator().next().getQueryTimeout();
    }
    
    @Override
    public final void setQueryTimeout(final int seconds) throws SQLException {
        if (getRoutedPreparedStatements().isEmpty()) {
            recordMethodInvocation(PreparedStatement.class, "setQueryTimeout", new Class[] {int.class}, new Object[] {seconds});
            return;
        }
        for (PreparedStatement each : getRoutedPreparedStatements()) {
            each.setQueryTimeout(seconds);
        }
    }
    
    protected abstract Collection<PreparedStatement> getRoutedPreparedStatements();
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        setParameter(parameterIndex, null);
        recordSetParameterForNull(new Class[]{int.class, int.class}, parameterIndex, sqlType);
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        setParameter(parameterIndex, null);
        recordSetParameterForNull(new Class[]{int.class, int.class, String.class}, parameterIndex, sqlType, typeName);
    }
    
    @Override
    public final void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBoolean", new Class[]{int.class, boolean.class}, parameterIndex, x);
    }
    
    @Override
    public final void setByte(final int parameterIndex, final byte x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setByte", new Class[]{int.class, byte.class}, parameterIndex, x);
    }
    
    @Override
    public final void setShort(final int parameterIndex, final short x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setShort", new Class[]{int.class, short.class}, parameterIndex, x);
    }
    
    @Override
    public final void setInt(final int parameterIndex, final int x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setInt", new Class[]{int.class, int.class}, parameterIndex, x);
    }
    
    @Override
    public final void setLong(final int parameterIndex, final long x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setLong", new Class[]{int.class, long.class}, parameterIndex, x);
    }
    
    @Override
    public final void setFloat(final int parameterIndex, final float x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setFloat", new Class[]{int.class, float.class}, parameterIndex, x);
    }
    
    @Override
    public final void setDouble(final int parameterIndex, final double x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setDouble", new Class[]{int.class, double.class}, parameterIndex, x);
    }
    
    @Override
    public final void setString(final int parameterIndex, final String x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setString", new Class[]{int.class, String.class}, parameterIndex, x);
    }
    
    @Override
    public final void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBigDecimal", new Class[]{int.class, BigDecimal.class}, parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setDate", new Class[]{int.class, Date.class}, parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setDate", new Class[]{int.class, Date.class, Calendar.class}, parameterIndex, x, cal);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setTime", new Class[]{int.class, Time.class}, parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setTime", new Class[]{int.class, Time.class, Calendar.class}, parameterIndex, x, cal);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setTimestamp", new Class[]{int.class, Timestamp.class}, parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setTimestamp", new Class[]{int.class, Timestamp.class, Calendar.class}, parameterIndex, x, cal);
    }
    
    @Override
    public final void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBytes", new Class[]{int.class, byte[].class}, parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBlob", new Class[]{int.class, Blob.class}, parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBlob", new Class[]{int.class, InputStream.class}, parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBlob", new Class[]{int.class, InputStream.class, long.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setClob", new Class[]{int.class, Clob.class}, parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setClob", new Class[]{int.class, Reader.class}, parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setClob", new Class[]{int.class, Reader.class, long.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setAsciiStream", new Class[]{int.class, InputStream.class}, parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setAsciiStream", new Class[]{int.class, InputStream.class, int.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setAsciiStream", new Class[]{int.class, InputStream.class, long.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setUnicodeStream", new Class[]{int.class, InputStream.class, int.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBinaryStream", new Class[]{int.class, InputStream.class}, parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBinaryStream", new Class[]{int.class, InputStream.class, int.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setBinaryStream", new Class[]{int.class, InputStream.class, long.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setCharacterStream", new Class[]{int.class, Reader.class}, parameterIndex, x);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setCharacterStream", new Class[]{int.class, Reader.class, int.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setCharacterStream", new Class[]{int.class, Reader.class, long.class}, parameterIndex, x, length);
    }
    
    @Override
    public final void setURL(final int parameterIndex, final URL x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setURL", new Class[]{int.class, URL.class}, parameterIndex, x);
    }
    
    @Override
    public final void setSQLXML(final int parameterIndex, final SQLXML x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setSQLXML", new Class[]{int.class, SQLXML.class}, parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setObject", new Class[]{int.class, Object.class}, parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setObject", new Class[]{int.class, Object.class, int.class}, parameterIndex,  x, targetSqlType);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        setParameter(parameterIndex, x);
        recordSetParameter("setObject", new Class[]{int.class, Object.class, int.class, int.class}, parameterIndex, x, targetSqlType, scaleOrLength);
    }
    
    private void setParameter(final int parameterIndex, final Object value) {
        if (parameters.size() == parameterIndex - 1) {
            parameters.add(value);
            return;
        }
        for (int i = parameters.size(); i <= parameterIndex - 1; i++) {
            parameters.add(null);
        }
        parameters.set(parameterIndex - 1, value);
    }
    
    private void recordSetParameter(final String methodName, final Class[] argumentTypes, final Object... arguments) {
        try {
            setParameterMethodInvocations.add(new SetParameterMethodInvocation(PreparedStatement.class.getMethod(methodName, argumentTypes), arguments, arguments[1]));
        } catch (final NoSuchMethodException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    private void recordSetParameterForNull(final Class[] argumentTypes, final Object... arguments) {
        try {
            setParameterMethodInvocations.add(new SetParameterMethodInvocation(PreparedStatement.class.getMethod("setNull", argumentTypes), arguments, null));
        } catch (final NoSuchMethodException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    protected void replaySetParameter(final PreparedStatement preparedStatement) {
        addParameters();
        for (SetParameterMethodInvocation each : setParameterMethodInvocations) {
            updateParameterValues(each, parameters.get(each.getIndex() - 1));
            each.invoke(preparedStatement);
        }
    }
    
    private void addParameters() {
        for (int i = setParameterMethodInvocations.size(); i < parameters.size(); i++) {
            recordSetParameter("setObject", new Class[]{int.class, Object.class}, i + 1, parameters.get(i));
        }
    }
    
    private void updateParameterValues(final SetParameterMethodInvocation setParameterMethodInvocation, final Object value) {
        if (!Objects.equals(setParameterMethodInvocation.getValue(), value)) {
            setParameterMethodInvocation.changeValueArgument(value);
        }
    }
    
    @Override
    public final void clearParameters() throws SQLException {
        parameters.clear();
        setParameterMethodInvocations.clear();
    }
}
