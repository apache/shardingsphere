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

import org.apache.shardingsphere.core.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.merger.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.EncryptStatement;
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

/**
 * Encrypt result set.
 *
 * @author panjuan
 */
public class EncryptResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final EncryptStatement encryptStatement;
    
    private ResultSet originalResultSet;
    
    private IteratorStreamMergedResult resultSet;
    
    public EncryptResultSet(final EncryptStatement encryptStatement, final ResultSet resultSet, final EncryptRule encryptRule, final ShardingEncryptorEngine encryptorEngine) {
        this.encryptStatement = encryptStatement;
        originalResultSet = resultSet;
        QueryResult queryResult = new StreamQueryResult(resultSet, encryptRule.getAllEncryptTableNames(), encryptorEngine);
        this.resultSet = new IteratorStreamMergedResult(Collections.singletonList(queryResult));
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
    public boolean getBoolean(int columnIndex) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, boolean.class), boolean.class);
    }
    
    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return (byte) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, byte.class), byte.class);
    }
    
    @Override
    public short getShort(int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(String columnLabel) throws SQLException {
        return (short) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, short.class), short.class);
    }
    
    @Override
    public int getInt(int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(String columnLabel) throws SQLException {
        return (int) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, int.class), int.class);
    }
    
    @Override
    public long getLong(int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(String columnLabel) throws SQLException {
        return (long) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, long.class), long.class);
    }
    
    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return (float) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, float.class), float.class);
    }
    
    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return (double) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, double.class), double.class);
    }
    
    @Override
    public String getString(int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(String columnLabel) throws SQLException {
        return (String) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, String.class), String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, byte[].class), byte[].class);
    }
    
    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Date.class, cal), Date.class);
    }
    
    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Time.class, cal), Time.class);
    }
    
    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getValue(columnLabel, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(resultSet.getCalendarValue(columnLabel, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Ascii");
    }
    
    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Ascii");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Unicode");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Unicode");
    }
    
    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return resultSet.getInputStream(columnIndex, "Binary");
    }
    
    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return resultSet.getInputStream(columnLabel, "Binary");
    }
    
    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return (Reader) resultSet.getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return (Reader) resultSet.getValue(columnLabel, Reader.class);
    }
    
    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return (Blob) resultSet.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return (Blob) resultSet.getValue(columnLabel, Blob.class);
    }
    
    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return (Clob) resultSet.getValue(columnIndex, Clob.class);
    }
    
    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return (Clob) resultSet.getValue(columnLabel, Clob.class);
    }
    
    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return (URL) resultSet.getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return (URL) resultSet.getValue(columnLabel, URL.class);
    }
    
    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return (SQLXML) resultSet.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return (SQLXML) resultSet.getValue(columnLabel, SQLXML.class);
    }
    
    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return resultSet.getValue(columnIndex, Object.class);
    }
    
    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return resultSet.getValue(columnLabel, Object.class);
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return originalResultSet.getMetaData();
    }
    
    @Override
    public int findColumn(String columnLabel) throws SQLException {
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
    public void setFetchDirection(int direction) throws SQLException {
        originalResultSet.setFetchDirection(direction);
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return originalResultSet.getFetchDirection();
    }
    
    @Override
    public void setFetchSize(int rows) throws SQLException {
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
