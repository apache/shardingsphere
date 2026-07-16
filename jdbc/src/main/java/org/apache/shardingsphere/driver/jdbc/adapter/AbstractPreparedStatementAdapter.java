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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Adapter for {@code PreparedStatement}.
 */
public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    @Getter
    private final List<Object> parameters = new ArrayList<>();
    
    private final List<ReplaySetParameter> setParameterContexts = new ArrayList<>();
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) {
        setParameter(parameterIndex, null, new ReplaySetParameter(ReplayMethod.SET_NULL, new Object[]{sqlType}));
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) {
        setParameter(parameterIndex, null, new ReplaySetParameter(ReplayMethod.SET_NULL_WITH_NAME, new Object[]{sqlType, typeName}));
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
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BLOB, new Object[0]));
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BLOB_STREAM, new Object[0]));
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BLOB_STREAM_WITH_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CLOB, new Object[0]));
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CLOB_READER, new Object[0]));
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CLOB_READER_WITH_LENGTH, new Object[]{length}));
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_ASCII_STREAM, new Object[0]));
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_ASCII_STREAM_WITH_INT_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_ASCII_STREAM_WITH_LONG_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_UNICODE_STREAM, new Object[]{length}));
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BINARY_STREAM, new Object[0]));
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BINARY_STREAM_WITH_INT_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_BINARY_STREAM_WITH_LONG_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CHARACTER_STREAM, new Object[0]));
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CHARACTER_STREAM_WITH_INT_LENGTH, new Object[]{length}));
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_CHARACTER_STREAM_WITH_LONG_LENGTH, new Object[]{length}));
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
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_OBJECT_WITH_TYPE, new Object[]{targetSqlType}));
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) {
        setParameter(parameterIndex, x, new ReplaySetParameter(ReplayMethod.SET_OBJECT_WITH_TYPE_AND_SCALE, new Object[]{targetSqlType, scaleOrLength}));
    }
    
    private void setParameter(final int parameterIndex, final Object value) {
        setParameter(parameterIndex, value, new ReplaySetParameter(ReplayMethod.SET_OBJECT, new Object[0]));
    }
    
    private void setParameter(final int parameterIndex, final Object value, final ReplaySetParameter replaySetParameter) {
        if (parameters.size() == parameterIndex - 1) {
            parameters.add(value);
            setParameterContexts.add(replaySetParameter);
            return;
        }
        for (int i = parameters.size(); i <= parameterIndex - 1; i++) {
            parameters.add(null);
            setParameterContexts.add(null);
        }
        parameters.set(parameterIndex - 1, value);
        setParameterContexts.set(parameterIndex - 1, replaySetParameter);
    }
    
    @SuppressWarnings("deprecation")
    protected final void replaySetParameter(final PreparedStatement preparedStatement, final List<Object> params) throws SQLException {
        int index = 0;
        for (Object each : params) {
            index++;
            ReplaySetParameter context = matchReplaySetParameter(index - 1, each);
            
            if (context == null) {
                preparedStatement.setObject(index, each);
                continue;
            }
            
            Object[] args = context.getArguments();
            switch (context.getMethod()) {
                case SET_NULL:
                    preparedStatement.setNull(index, (int) args[0]);
                    break;
                case SET_NULL_WITH_NAME:
                    preparedStatement.setNull(index, (int) args[0], (String) args[1]);
                    break;
                case SET_BLOB:
                    if (each instanceof Blob) {
                        preparedStatement.setBlob(index, (Blob) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_BLOB_STREAM:
                    if (each instanceof InputStream) {
                        preparedStatement.setBlob(index, (InputStream) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_BLOB_STREAM_WITH_LENGTH:
                    if (each instanceof InputStream) {
                        preparedStatement.setBlob(index, (InputStream) each, (long) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CLOB:
                    if (each instanceof Clob) {
                        preparedStatement.setClob(index, (Clob) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CLOB_READER:
                    if (each instanceof Reader) {
                        preparedStatement.setClob(index, (Reader) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CLOB_READER_WITH_LENGTH:
                    if (each instanceof Reader) {
                        preparedStatement.setClob(index, (Reader) each, (long) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_ASCII_STREAM:
                    if (each instanceof InputStream) {
                        preparedStatement.setAsciiStream(index, (InputStream) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_ASCII_STREAM_WITH_INT_LENGTH:
                    if (each instanceof InputStream) {
                        preparedStatement.setAsciiStream(index, (InputStream) each, (int) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_ASCII_STREAM_WITH_LONG_LENGTH:
                    if (each instanceof InputStream) {
                        preparedStatement.setAsciiStream(index, (InputStream) each, (long) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_UNICODE_STREAM:
                    if (each instanceof InputStream) {
                        preparedStatement.setUnicodeStream(index, (InputStream) each, (int) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_BINARY_STREAM:
                    if (each instanceof InputStream) {
                        preparedStatement.setBinaryStream(index, (InputStream) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_BINARY_STREAM_WITH_INT_LENGTH:
                    if (each instanceof InputStream) {
                        preparedStatement.setBinaryStream(index, (InputStream) each, (int) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_BINARY_STREAM_WITH_LONG_LENGTH:
                    if (each instanceof InputStream) {
                        preparedStatement.setBinaryStream(index, (InputStream) each, (long) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CHARACTER_STREAM:
                    if (each instanceof Reader) {
                        preparedStatement.setCharacterStream(index, (Reader) each);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CHARACTER_STREAM_WITH_INT_LENGTH:
                    if (each instanceof Reader) {
                        preparedStatement.setCharacterStream(index, (Reader) each, (int) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_CHARACTER_STREAM_WITH_LONG_LENGTH:
                    if (each instanceof Reader) {
                        preparedStatement.setCharacterStream(index, (Reader) each, (long) args[0]);
                    } else {
                        preparedStatement.setObject(index, each);
                    }
                    break;
                case SET_OBJECT_WITH_TYPE:
                    preparedStatement.setObject(index, each, (int) args[0]);
                    break;
                case SET_OBJECT_WITH_TYPE_AND_SCALE:
                    preparedStatement.setObject(index, each, (int) args[0], (int) args[1]);
                    break;
                case SET_OBJECT:
                default:
                    preparedStatement.setObject(index, each);
                    break;
            }
        }
    }
    
    private ReplaySetParameter matchReplaySetParameter(final int paramIndex, final Object each) {
        if (paramIndex < parameters.size()) {
            Object original = parameters.get(paramIndex);
            if (Objects.equals(each, original)) {
                return setParameterContexts.get(paramIndex);
            }
        }
        
        if (each != null) {
            for (int i = 0; i < parameters.size(); i++) {
                if (each == parameters.get(i)) {
                    return setParameterContexts.get(i);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public final void clearParameters() {
        parameters.clear();
        setParameterContexts.clear();
    }
    
    @RequiredArgsConstructor
    @Getter
    protected static class ReplaySetParameter {
        
        private final ReplayMethod method;
        
        private final Object[] arguments;
    }
    
    protected enum ReplayMethod {
        
        SET_NULL, SET_NULL_WITH_NAME,
        SET_BLOB, SET_BLOB_STREAM, SET_BLOB_STREAM_WITH_LENGTH,
        SET_CLOB, SET_CLOB_READER, SET_CLOB_READER_WITH_LENGTH,
        SET_ASCII_STREAM, SET_ASCII_STREAM_WITH_INT_LENGTH, SET_ASCII_STREAM_WITH_LONG_LENGTH,
        SET_UNICODE_STREAM,
        SET_BINARY_STREAM, SET_BINARY_STREAM_WITH_INT_LENGTH, SET_BINARY_STREAM_WITH_LONG_LENGTH,
        SET_CHARACTER_STREAM, SET_CHARACTER_STREAM_WITH_INT_LENGTH, SET_CHARACTER_STREAM_WITH_LONG_LENGTH,
        SET_OBJECT, SET_OBJECT_WITH_TYPE, SET_OBJECT_WITH_TYPE_AND_SCALE
    }
}
