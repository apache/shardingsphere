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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.queryresult.StreamQueryResult;
import org.apache.shardingsphere.underlying.merge.MergeEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Encrypt result set.
 */
public final class EncryptResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final RuntimeContext runtimeContext;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final Statement encryptStatement;
    
    private final ResultSet originalResultSet;
    
    private final MergedResult mergedResult;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    public EncryptResultSet(final RuntimeContext runtimeContext,
                            final SQLStatementContext sqlStatementContext, final Statement encryptStatement, final ResultSet resultSet) throws SQLException {
        this.runtimeContext = runtimeContext;
        this.sqlStatementContext = sqlStatementContext;
        this.encryptStatement = encryptStatement;
        originalResultSet = resultSet;
        mergedResult = createMergedResult(runtimeContext, resultSet);
        columnLabelAndIndexMap = createColumnLabelAndIndexMap(originalResultSet.getMetaData());
    }
    
    private MergedResult createMergedResult(final RuntimeContext runtimeContext, final ResultSet resultSet) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(runtimeContext.getDatabaseType(), 
                runtimeContext.getMetaData().getSchema().getConfiguredSchemaMetaData(), runtimeContext.getProperties(), runtimeContext.getRules());
        return mergeEngine.merge(Collections.singletonList(new StreamQueryResult(resultSet)), sqlStatementContext);
    }
    
    private Map<String, Integer> createColumnLabelAndIndexMap(final ResultSetMetaData resultSetMetaData) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    @Override
    public boolean previous() throws SQLException {
        return originalResultSet.previous();
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        return originalResultSet.isBeforeFirst();
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        return originalResultSet.isAfterLast();
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        return originalResultSet.isFirst();
    }
    
    @Override
    public boolean isLast() throws SQLException {
        return originalResultSet.isLast();
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        originalResultSet.beforeFirst();
    }
    
    @Override
    public void afterLast() throws SQLException {
        originalResultSet.afterLast();
    }
    
    @Override
    public boolean first() throws SQLException {
        return originalResultSet.first();
    }
    
    @Override
    public boolean last() throws SQLException {
        return originalResultSet.last();
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        return originalResultSet.absolute(row);
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        return originalResultSet.relative(rows);
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (boolean) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (byte) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (short) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (int) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (long) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (float) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (double) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, double.class), double.class);
    }

    @Override
    public int getRow() throws SQLException {
        return originalResultSet.getRow();
    }

    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (String) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (BigDecimal) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (BigDecimal) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (byte[]) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Date) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Date) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Time) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Time) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Timestamp) ResultSetUtil.convertValue(mergedResult.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Timestamp) ResultSetUtil.convertValue(mergedResult.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return mergedResult.getInputStream(columnIndex, "Ascii");
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return mergedResult.getInputStream(columnIndex, "Ascii");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return mergedResult.getInputStream(columnIndex, "Unicode");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return mergedResult.getInputStream(columnIndex, "Unicode");
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return mergedResult.getInputStream(columnIndex, "Binary");
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return mergedResult.getInputStream(columnIndex, "Binary");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader) mergedResult.getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Reader) mergedResult.getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) mergedResult.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Blob) mergedResult.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) mergedResult.getValue(columnIndex, Clob.class);
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (Clob) mergedResult.getValue(columnIndex, Clob.class);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) mergedResult.getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (URL) mergedResult.getValue(columnIndex, URL.class);
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) mergedResult.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return (SQLXML) mergedResult.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return mergedResult.getValue(columnIndex, Object.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        int columnIndex = columnLabelAndIndexMap.get(columnLabel);
        return mergedResult.getValue(columnIndex, Object.class);
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new ShardingSphereResultSetMetaData(originalResultSet.getMetaData(), runtimeContext.getRules(), sqlStatementContext);
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return originalResultSet.findColumn(columnLabel);
    }
    
    @Override
    public void close() throws SQLException {
        originalResultSet.close();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return originalResultSet.isClosed();
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        originalResultSet.setFetchDirection(direction);
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return originalResultSet.getFetchDirection();
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        originalResultSet.setFetchSize(rows);
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return originalResultSet.getFetchSize();
    }
    
    @Override
    public int getType() throws SQLException {
        return originalResultSet.getType();
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return originalResultSet.getConcurrency();
    }
    
    @Override
    public Statement getStatement() {
        return encryptStatement;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return originalResultSet.getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        originalResultSet.clearWarnings();
    }
}
