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

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.io.CharStreams;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.adapter.invocation.SetParameterMethodInvocation;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapter for {@code PreparedStatement}.
 */
public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    private final List<SetParameterMethodInvocation> setParameterMethodInvocations = new LinkedList<>();
    
    @Getter
    private final List<Object> parameters = new ArrayList<>();
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) {
        setParameter(parameterIndex, null);
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) {
        setParameter(parameterIndex, null);
    }
    
    @Override
    public final void setBoolean(final int parameterIndex, final boolean x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setByte(final int parameterIndex, final byte x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setShort(final int parameterIndex, final short x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setInt(final int parameterIndex, final int x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setLong(final int parameterIndex, final long x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setFloat(final int parameterIndex, final float x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDouble(final int parameterIndex, final double x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setString(final int parameterIndex, final String x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBigDecimal(final int parameterIndex, final BigDecimal x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBytes(final int parameterIndex, final byte[] x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final Blob x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setURL(final int parameterIndex, final URL x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setSQLXML(final int parameterIndex, final SQLXML x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) {
        setParameter(parameterIndex, x);
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
    
    protected final void replaySetParameter(final PreparedStatement preparedStatement, final List<Object> parameters) {
        setParameterMethodInvocations.clear();
        addParameters(parameters);
        setParameterMethodInvocations.forEach(each -> each.invoke(preparedStatement));
    }
    
    private void addParameters(final List<Object> parameters) {
        int i = 0;
        for (Object each : parameters) {
            setParameters(new Class[]{int.class, Object.class}, i++ + 1, each);
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setParameters(final Class<?>[] argumentTypes, final Object... arguments) {
        setParameterMethodInvocations.add(new SetParameterMethodInvocation(PreparedStatement.class.getMethod("setObject", argumentTypes), arguments, arguments[1]));
    }
    
    @Override
    public final void clearParameters() {
        parameters.clear();
        setParameterMethodInvocations.clear();
    }
}
