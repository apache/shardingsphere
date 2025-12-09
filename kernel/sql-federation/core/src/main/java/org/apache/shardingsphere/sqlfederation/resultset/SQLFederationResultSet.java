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

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.Schema;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * SQL federation result set.
 */
public final class SQLFederationResultSet extends AbstractUnsupportedOperationSQLFederationResultSet {
    
    private static final String ASCII = "Ascii";
    
    private static final String UNICODE = "Unicode";
    
    private static final String BINARY = "Binary";
    
    private static final Collection<Class<?>> INVALID_FEDERATION_TYPES = new HashSet<>(Arrays.asList(Blob.class, Clob.class, Reader.class, InputStream.class, SQLXML.class));
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    private final Enumerator<?> enumerator;
    
    private final Map<String, Integer> columnLabelAndIndexes;
    
    private final SQLFederationResultSetMetaData resultSetMetaData;
    
    private final SQLFederationColumnTypeConverter columnTypeConverter;
    
    private final String processId;
    
    private Object[] currentRows;
    
    private boolean wasNull;
    
    private boolean closed;
    
    public SQLFederationResultSet(final Enumerator<?> enumerator, final Schema sqlFederationSchema, final List<Projection> expandProjections, final DatabaseType databaseType,
                                  final RelDataType resultColumnType, final String processId) {
        this.enumerator = enumerator;
        this.processId = processId;
        columnTypeConverter = DatabaseTypedSPILoader.findService(SQLFederationColumnTypeConverter.class, databaseType).orElse(null);
        columnLabelAndIndexes = new CaseInsensitiveMap<>(expandProjections.size(), 1F);
        Map<Integer, String> indexAndColumnLabels = new CaseInsensitiveMap<>(expandProjections.size(), 1F);
        handleColumnLabelAndIndex(columnLabelAndIndexes, indexAndColumnLabels, expandProjections);
        resultSetMetaData = new SQLFederationResultSetMetaData(sqlFederationSchema, expandProjections, databaseType, resultColumnType, indexAndColumnLabels, columnTypeConverter);
    }
    
    private void handleColumnLabelAndIndex(final Map<String, Integer> columnLabelAndIndexes, final Map<Integer, String> indexAndColumnLabels, final List<Projection> expandProjections) {
        for (int columnIndex = 1; columnIndex <= expandProjections.size(); columnIndex++) {
            Projection projection = expandProjections.get(columnIndex - 1);
            String columnLabel = projection.getColumnLabel();
            columnLabelAndIndexes.put(columnLabel, columnIndex);
            indexAndColumnLabels.put(columnIndex, columnLabel);
        }
    }
    
    @Override
    public boolean next() {
        try {
            return next0();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            close();
            throw ex;
        }
    }
    
    private boolean next0() {
        boolean result = enumerator.moveNext();
        if (result) {
            Object current = enumerator.current();
            currentRows = null == current ? new Object[]{null} : getCurrentRows(current);
        } else {
            currentRows = new Object[]{null};
            processEngine.completeSQLExecution(processId);
        }
        return result;
    }
    
    private Object[] getCurrentRows(final Object current) {
        return current.getClass().isArray() && !(current instanceof byte[]) ? (Object[]) current : new Object[]{current};
    }
    
    @Override
    public void close() {
        closed = true;
        currentRows = null;
        try {
            enumerator.close();
        } finally {
            processEngine.completeSQLExecution(processId);
        }
    }
    
    @Override
    public boolean wasNull() {
        return wasNull;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtils.convertValue(getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return getString(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) ResultSetUtils.convertValue(getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return getBoolean(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtils.convertValue(getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return getByte(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtils.convertValue(getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return getShort(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtils.convertValue(getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return getInt(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtils.convertValue(getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return getLong(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtils.convertValue(getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return getFloat(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtils.convertValue(getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return getDouble(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtils.convertValue(getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return getBigDecimal(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtils.convertValue(getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return getBigDecimal(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtils.convertValue(getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return getBytes(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtils.convertValue(getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return getDate(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtils.convertValue(getCalendarValue(columnIndex), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return getDate(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtils.convertValue(getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return getTime(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtils.convertValue(getCalendarValue(columnIndex), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return getTime(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtils.convertValue(getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return getTimestamp(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtils.convertValue(getCalendarValue(columnIndex), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return getTimestamp(getIndexFromColumnLabelAndIndexMap(columnLabel), cal);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return getInputStream(ASCII);
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return getAsciiStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return getInputStream(UNICODE);
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return getUnicodeStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return getInputStream(BINARY);
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return getBinaryStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public ResultSetMetaData getMetaData() {
        return resultSetMetaData;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return getValue(columnIndex, Object.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return getObject(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return getIndexFromColumnLabelAndIndexMap(columnLabel);
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader) getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return getCharacterStream(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public void setFetchDirection(final int direction) {
    }
    
    @Override
    public int getFetchDirection() {
        return FETCH_FORWARD;
    }
    
    @Override
    public void setFetchSize(final int rows) {
    }
    
    @Override
    public int getFetchSize() {
        return 0;
    }
    
    @Override
    public int getType() {
        return TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() {
        return CONCUR_READ_ONLY;
    }
    
    @Override
    public Statement getStatement() {
        return null;
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return getBlob(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) getValue(columnIndex, Clob.class);
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return getClob(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return (Array) getValue(columnIndex, Array.class);
    }
    
    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return getArray(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return getURL(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return getSQLXML(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return getString(columnIndex);
    }
    
    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return getString(columnLabel);
    }
    
    private Integer getIndexFromColumnLabelAndIndexMap(final String columnLabel) throws SQLException {
        Integer result = columnLabelAndIndexes.get(columnLabel);
        ShardingSpherePreconditions.checkNotNull(result, () -> new SQLFeatureNotSupportedException(String.format("can not get index from column label `%s`", columnLabel)));
        return result;
    }
    
    private Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        ShardingSpherePreconditions.checkNotContains(INVALID_FEDERATION_TYPES, type, () -> new SQLFeatureNotSupportedException(String.format("Get value from `%s`", type.getName())));
        Object result = currentRows[columnIndex - 1];
        wasNull = null == result;
        return null == columnTypeConverter ? result : columnTypeConverter.convertColumnValue(result);
    }
    
    private Object getCalendarValue(final int columnIndex) {
        // TODO implement with calendar
        Object result = currentRows[columnIndex - 1];
        wasNull = null == result;
        return result;
    }
    
    private InputStream getInputStream(final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException(String.format("Get input stream from `%s`", type));
    }
}
