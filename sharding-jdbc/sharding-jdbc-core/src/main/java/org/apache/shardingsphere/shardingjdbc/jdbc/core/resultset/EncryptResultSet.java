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

import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.execute.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.core.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.EncryptRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationResultSet;

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt result set.
 *
 * @author panjuan
 */
public final class EncryptResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final EncryptRule encryptRule;
    
    private final OptimizedStatement optimizedStatement;
    
    private final Statement encryptStatement;
    
    private ResultSet originalResultSet;
    
    private IteratorStreamMergedResult resultSet;
    
    private final Map<String, String> logicAndActualColumns;
    
    public EncryptResultSet(final EncryptRuntimeContext encryptRuntimeContext, 
                            final OptimizedStatement optimizedStatement, final Statement encryptStatement, final ResultSet resultSet) throws SQLException {
        this.encryptRule = encryptRuntimeContext.getRule();
        this.optimizedStatement = optimizedStatement;
        this.encryptStatement = encryptStatement;
        originalResultSet = resultSet;
        QueryResult queryResult = new StreamQueryResult(resultSet, encryptRule);
        this.resultSet = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
        logicAndActualColumns = createLogicAndActualColumns(encryptRuntimeContext.getProps().<Boolean>getValue(ShardingPropertiesConstant.QUERY_WITH_CIPHER_COLUMN));
    }
    
    private Map<String, String> createLogicAndActualColumns(final boolean isQueryWithCipherColumn) {
        return isQueryWithCipherColumn ? createLogicAndCipherColumns() : createLogicAndPlainColumns();
    }
    
    private Map<String, String> createLogicAndCipherColumns() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : optimizedStatement.getTables().getTableNames()) {
            result.putAll(encryptRule.getLogicAndCipherColumns(each));
        }
        return result;
    }
    
    private Map<String, String> createLogicAndPlainColumns() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : optimizedStatement.getTables().getTableNames()) {
            result.putAll(encryptRule.getLogicAndPlainColumns(each));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }
    
    @Override
    public boolean wasNull() {
        return resultSet.wasNull();
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), boolean.class), boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return (byte) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), byte.class), byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return (short) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), short.class), short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return (int) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), int.class), int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return (long) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), long.class), long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return (float) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), float.class), float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return (double) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), double.class), double.class);
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return (String) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), String.class), String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), byte[].class), byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Date.class, cal), Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Time.class, cal), Time.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getValue(getActualColumnLabel(columnLabel), Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Ascii");
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Ascii");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Unicode");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Unicode");
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Binary");
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Binary");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader) resultSet.getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return (Reader) resultSet.getValue(getActualColumnLabel(columnLabel), Reader.class);
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) resultSet.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return (Blob) resultSet.getValue(getActualColumnLabel(columnLabel), Blob.class);
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) resultSet.getValue(columnIndex, Clob.class);
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return (Clob) resultSet.getValue(getActualColumnLabel(columnLabel), Clob.class);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) resultSet.getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return (URL) resultSet.getValue(getActualColumnLabel(columnLabel), URL.class);
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) resultSet.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return (SQLXML) resultSet.getValue(getActualColumnLabel(columnLabel), SQLXML.class);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return resultSet.getValue(columnIndex, Object.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return resultSet.getValue(getActualColumnLabel(columnLabel), Object.class);
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new EncryptResultSetMetaData(originalResultSet.getMetaData(), encryptRule, optimizedStatement, logicAndActualColumns);
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return originalResultSet.findColumn(getActualColumnLabel(columnLabel));
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
    
    private String getActualColumnLabel(final String columnLabel) {
        return logicAndActualColumns.keySet().contains(columnLabel) ? logicAndActualColumns.get(columnLabel) : columnLabel;
    }
}
