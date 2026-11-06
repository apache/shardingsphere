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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractResultSetAdapter;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere result set.
 */
@HighFrequencyInvocation
public final class ShardingSphereResultSet extends AbstractResultSetAdapter {
    
    private static final String ASCII = "Ascii";
    
    private static final String UNICODE = "Unicode";
    
    private static final String BINARY = "Binary";
    
    private final MergedResult mergeResultSet;
    
    private final DialectResultSetValueConverter dialectResultSetValueConverter;
    
    @Getter
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    private final ResultSetMetaData resultSetMetaData;
    
    public ShardingSphereResultSet(final List<ResultSet> resultSets, final MergedResult mergeResultSet, final Statement statement, final SQLStatementContext sqlStatementContext) throws SQLException {
        this(resultSets, mergeResultSet, statement, sqlStatementContext, null);
    }
    
    /**
     * Read JDBC metadata during construction and build the column-label mapping when none is supplied.
     *
     * @param resultSets result sets
     * @param mergeResultSet merged result set
     * @param statement statement
     * @param sqlStatementContext SQL statement context
     * @param columnLabelAndIndexMap column label and index map, or {@code null} to build it from JDBC metadata
     * @throws SQLException if JDBC metadata cannot be read
     */
    public ShardingSphereResultSet(final List<ResultSet> resultSets, final MergedResult mergeResultSet, final Statement statement,
                                   final SQLStatementContext sqlStatementContext, final Map<String, Integer> columnLabelAndIndexMap) throws SQLException {
        super(resultSets, statement, sqlStatementContext);
        this.mergeResultSet = mergeResultSet;
        DatabaseType protocolType = getProtocolType(statement);
        dialectResultSetValueConverter = getDialectResultSetValueConverter(protocolType);
        resultSetMetaData = resultSets.get(0).getMetaData();
        this.columnLabelAndIndexMap = null == columnLabelAndIndexMap ? ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(sqlStatementContext, resultSetMetaData) : columnLabelAndIndexMap;
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergeResultSet.next();
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergeResultSet.wasNull();
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return getBoolean(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) getValue(columnIndex, boolean.class);
    }
    
    /**
     * Read and convert a column value; SQL {@code NULL} yields {@code null} for reference types or the primitive default.
     *
     * @param columnIndex column index
     * @param type expected value type
     * @return converted value, or {@code null} for SQL {@code NULL} with a reference type
     * @throws SQLException SQL exception
     */
    private Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return convertJdbcValue(columnIndex, mergeResultSet.getValue(columnIndex, type), type);
    }
    
    /**
     * Convert a JDBC value; {@code null} represents SQL {@code NULL}, yielding null for reference types or the primitive default.
     *
     * @param columnIndex column index
     * @param value original value, or {@code null} for SQL {@code NULL}
     * @param type expected value type
     * @return converted value, or {@code null} for SQL {@code NULL} with a reference type
     * @throws SQLException SQL exception
     */
    private Object convertJdbcValue(final int columnIndex, final Object value, final Class<?> type) throws SQLException {
        return ResultSetUtils.convertValue(normalizeValue(columnIndex, value, type), type);
    }
    
    /**
     * Normalize a JDBC value, retaining {@code null} for SQL {@code NULL}.
     *
     * @param columnIndex column index
     * @param value original value, or {@code null} for SQL {@code NULL}
     * @param type expected value type
     * @return normalized value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    private Object normalizeValue(final int columnIndex, final Object value, final Class<?> type) throws SQLException {
        return value instanceof LocalTime && isTimeColumn(columnIndex) ? ResultSetUtils.convertTimeValue((LocalTime) value, type) : value;
    }
    
    private boolean isTimeColumn(final int columnIndex) throws SQLException {
        return Types.TIME == resultSetMetaData.getColumnType(columnIndex);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) getValue(columnIndex, byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return getByte(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) getValue(columnIndex, short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return getShort(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) getValue(columnIndex, int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return getInt(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) getValue(columnIndex, long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return getLong(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) getValue(columnIndex, float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return getFloat(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) getValue(columnIndex, double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return getDouble(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return getString(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) getValue(columnIndex, String.class);
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return getString(columnIndex);
    }
    
    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return getString(columnLabel);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) getValue(columnIndex, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return getBigDecimal(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) getValue(columnIndex, BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return getBigDecimal(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtils.convertValue(mergeResultSet.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return getBytes(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) getValue(columnIndex, Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return getDate(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) convertJdbcValue(columnIndex, mergeResultSet.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return getDate(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) getValue(columnIndex, Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return getTime(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) convertJdbcValue(columnIndex, mergeResultSet.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return getTime(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) getValue(columnIndex, Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return getTimestamp(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) convertJdbcValue(columnIndex, mergeResultSet.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return getTimestamp(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, ASCII);
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return getAsciiStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, UNICODE);
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return getUnicodeStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, BINARY);
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return getBinaryStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        // TODO To be supported: encrypt, mask, and so on
        return mergeResultSet.getCharacterStream(columnIndex);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return getCharacterStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) mergeResultSet.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return getBlob(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) mergeResultSet.getValue(columnIndex, Clob.class);
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return getClob(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return (Array) mergeResultSet.getValue(columnIndex, Array.class);
    }
    
    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return getArray(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) mergeResultSet.getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return getURL(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) mergeResultSet.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return getSQLXML(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return normalizeValue(columnIndex, mergeResultSet.getValue(columnIndex, Object.class), Time.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return getObject(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        if (BigInteger.class.equals(type)) {
            return (T) BigInteger.valueOf(getLong(columnIndex));
        }
        if (Blob.class.equals(type)) {
            return (T) getBlob(columnIndex);
        }
        if (Clob.class.equals(type)) {
            return (T) getClob(columnIndex);
        }
        if (LocalDateTime.class.equals(type) || LocalDate.class.equals(type) || LocalTime.class.equals(type) || OffsetDateTime.class.equals(type)) {
            return (T) convertValue(normalizeValue(columnIndex, mergeResultSet.getValue(columnIndex, Timestamp.class), Timestamp.class), type);
        }
        return (T) convertValue(normalizeValue(columnIndex, mergeResultSet.getValue(columnIndex, type), type), type);
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return getObject(getIndexFromColumnLabelAndIndexMap(columnLabel), type);
    }
    
    private Integer getIndexFromColumnLabelAndIndexMap(final String columnLabel) throws SQLException {
        Integer result = columnLabelAndIndexMap.get(columnLabel);
        ShardingSpherePreconditions.checkNotNull(result, () -> new SQLFeatureNotSupportedException(String.format("Can not get index from column label `%s`.", columnLabel)));
        return result;
    }
    
    private DatabaseType getProtocolType(final Statement statement) {
        if (statement instanceof ShardingSpherePreparedStatement) {
            return ((ShardingSpherePreparedStatement) statement).getUsedDatabase().getProtocolType();
        }
        if (statement instanceof ShardingSphereStatement) {
            ShardingSphereStatement shardingSphereStatement = (ShardingSphereStatement) statement;
            return shardingSphereStatement.getConnection().getContextManager().getMetaDataContexts().getMetaData().getDatabase(shardingSphereStatement.getUsedDatabaseName()).getProtocolType();
        }
        return null;
    }
    
    private DialectResultSetValueConverter getDialectResultSetValueConverter(final DatabaseType protocolType) {
        return null == protocolType ? null : TypedSPILoader.findService(DialectResultSetValueConverter.class, protocolType.getType()).orElse(null);
    }
    
    private Object convertValue(final Object value, final Class<?> type) throws SQLFeatureNotSupportedException {
        return null == dialectResultSetValueConverter ? ResultSetUtils.convertValue(value, type) : dialectResultSetValueConverter.convertValue(value, type);
    }
}
