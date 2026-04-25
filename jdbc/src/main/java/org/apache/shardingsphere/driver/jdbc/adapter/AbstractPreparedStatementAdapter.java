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
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.statement.DialectPreparedStatementParameterReplayer;
import org.apache.shardingsphere.database.connector.core.statement.PreparedStatementParameter;
import org.apache.shardingsphere.database.connector.core.statement.SetterMethodType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.generic.UnknownSQLException;

import java.io.IOException;
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

/**
 * Adapter for {@code PreparedStatement}.
 */
public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    @Getter
    private final List<PreparedStatementParameter> parameterRecords = new ArrayList<>();
    
    private final List<List<PreparedStatementParameter>> allBatchParameterRecords = new ArrayList<>();
    
    private DatabaseType cachedDatabaseType;
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) {
        setParameter(parameterIndex, SetterMethodType.SET_NULL, null, -1L);
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) {
        setParameter(parameterIndex, SetterMethodType.SET_NULL, null, -1L);
    }
    
    @Override
    public final void setBoolean(final int parameterIndex, final boolean x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setByte(final int parameterIndex, final byte x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setShort(final int parameterIndex, final short x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setInt(final int parameterIndex, final int x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setLong(final int parameterIndex, final long x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setFloat(final int parameterIndex, final float x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setDouble(final int parameterIndex, final double x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setString(final int parameterIndex, final String x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setBigDecimal(final int parameterIndex, final BigDecimal x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x, final Calendar cal) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x, final Calendar cal) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setBytes(final int parameterIndex, final byte[] x) {
        setParameter(parameterIndex, SetterMethodType.SET_BYTES, x, -1L);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final Blob x) {
        setParameter(parameterIndex, SetterMethodType.SET_BLOB, x, -1L);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, SetterMethodType.SET_BLOB_INPUT_STREAM, x, -1L);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, SetterMethodType.SET_BLOB_INPUT_STREAM, x, length);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) {
        setParameter(parameterIndex, SetterMethodType.SET_CLOB, x, -1L);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) {
        setParameter(parameterIndex, SetterMethodType.SET_CLOB_READER, x, -1L);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) {
        setParameter(parameterIndex, SetterMethodType.SET_CLOB_READER, x, length);
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, SetterMethodType.SET_ASCII_STREAM, x, -1L);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, SetterMethodType.SET_ASCII_STREAM, x, length);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, SetterMethodType.SET_ASCII_STREAM, x, length);
    }
    
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, SetterMethodType.SET_BINARY_STREAM, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, SetterMethodType.SET_BINARY_STREAM, x, -1L);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, SetterMethodType.SET_BINARY_STREAM, x, length);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, SetterMethodType.SET_BINARY_STREAM, x, length);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) {
        try {
            setParameter(parameterIndex, SetterMethodType.SET_OBJECT, CharStreams.toString(x), -1L);
        } catch (final IOException ex) {
            throw new UnknownSQLException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) {
        try {
            setParameter(parameterIndex, SetterMethodType.SET_OBJECT, CharStreams.toString(x), -1L);
        } catch (final IOException ex) {
            throw new UnknownSQLException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) {
        try {
            setParameter(parameterIndex, SetterMethodType.SET_OBJECT, CharStreams.toString(x), -1L);
        } catch (final IOException ex) {
            throw new UnknownSQLException(ex);
        }
    }
    
    @Override
    public final void setURL(final int parameterIndex, final URL x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setSQLXML(final int parameterIndex, final SQLXML x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) {
        setParameter(parameterIndex, SetterMethodType.SET_OBJECT, x, -1L);
    }
    
    private void setParameter(final int parameterIndex, final SetterMethodType methodType, final Object value, final long length) {
        PreparedStatementParameter param = new PreparedStatementParameter(parameterIndex, methodType, value, length);
        if (parameterRecords.size() == parameterIndex - 1) {
            parameterRecords.add(param);
            return;
        }
        for (int i = parameterRecords.size(); i <= parameterIndex - 1; i++) {
            parameterRecords.add(new PreparedStatementParameter(i + 1, SetterMethodType.SET_NULL, null, -1L));
        }
        parameterRecords.set(parameterIndex - 1, param);
    }
    
    /**
     * Get parameters.
     *
     * @return parameters
     */
    public List<Object> getParameters() {
        List<Object> result = new ArrayList<>(parameterRecords.size());
        for (PreparedStatementParameter each : parameterRecords) {
            result.add(each.getValue());
        }
        return result;
    }
    
    protected final void replaySetParameter(final PreparedStatement preparedStatement, final List<Object> params, final int batchIndex) throws SQLException {
        DatabaseType databaseType = getDatabaseType(preparedStatement);
        DialectPreparedStatementParameterReplayer replayer = DatabaseTypedSPILoader.getService(
                DialectPreparedStatementParameterReplayer.class, databaseType);
        int index = 0;
        for (Object value : params) {
            index++;
            PreparedStatementParameter param = findParameterRecord(index, value, batchIndex);
            replayer.replay(preparedStatement, param);
        }
    }
    
    private DatabaseType getDatabaseType(final PreparedStatement preparedStatement) {
        if (null != cachedDatabaseType) {
            return cachedDatabaseType;
        }
        try {
            String url = preparedStatement.getConnection().getMetaData().getURL();
            cachedDatabaseType = DatabaseTypeFactory.get(url);
            return cachedDatabaseType;
        } catch (final SQLException ex) {
            return DatabaseTypeFactory.get("SQL92");
        }
    }
    
    private PreparedStatementParameter findParameterRecord(final int index, final Object value, final int batchIndex) {
        List<PreparedStatementParameter> records;
        if (batchIndex >= 0 && batchIndex < allBatchParameterRecords.size()) {
            records = allBatchParameterRecords.get(batchIndex);
        } else if (!allBatchParameterRecords.isEmpty()) {
            records = allBatchParameterRecords.get(0);
        } else {
            records = parameterRecords;
        }
        for (PreparedStatementParameter each : records) {
            if (each.getIndex() == index) {
                return new PreparedStatementParameter(index, each.getSetterMethodType(), value, each.getLength());
            }
        }
        return new PreparedStatementParameter(index, SetterMethodType.SET_OBJECT, value, -1L);
    }
    
    /**
     * Save current parameter records to batch parameter records.
     * Should be called before clearParameters in addBatch.
     */
    protected void saveBatchParameterRecords() {
        List<PreparedStatementParameter> batchRecords = new ArrayList<>(parameterRecords.size());
        for (PreparedStatementParameter each : parameterRecords) {
            batchRecords.add(new PreparedStatementParameter(each.getIndex(), each.getSetterMethodType(), each.getValue(), each.getLength()));
        }
        allBatchParameterRecords.add(batchRecords);
    }
    
    /**
     * Clear batch parameter records.
     * Should be called in clearBatch.
     */
    protected void clearBatchParameterRecords() {
        allBatchParameterRecords.clear();
    }
    
    @Override
    public final void clearParameters() {
        parameterRecords.clear();
    }
}
