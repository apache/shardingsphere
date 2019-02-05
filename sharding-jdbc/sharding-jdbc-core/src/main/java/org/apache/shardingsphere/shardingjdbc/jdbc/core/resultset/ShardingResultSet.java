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

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractResultSetAdapter;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result that support sharding.
 *
 * @author zhangliang
 */
public final class ShardingResultSet extends AbstractResultSetAdapter {
    
    private final MergedResult mergeResultSet;
    
    private final Map<String, Integer> columnLabelIndexMap = new LinkedHashMap<>();
    
    public ShardingResultSet(final List<ResultSet> resultSets, final MergedResult mergeResultSet, final Statement statement) {
        super(resultSets, statement);
        this.mergeResultSet = mergeResultSet;
    }
    
    public ShardingResultSet(final List<ResultSet> resultSets, final MergedResult mergeResultSet, final Map<String, Integer> columnLabelIndexMap, final Statement statement) {
        super(resultSets, statement);
        this.mergeResultSet = mergeResultSet;
        this.columnLabelIndexMap.putAll(columnLabelIndexMap);
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
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, boolean.class), boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return (byte) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, byte.class), byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return (short) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, short.class), short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return (int) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, int.class), int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return (long) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, long.class), long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return (float) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, float.class), float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return (double) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, double.class), double.class);
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return (String) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, String.class), String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, byte[].class), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, byte[].class), byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Date.class, cal), Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Time.class, cal), Time.class);
    }
            
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, "Ascii");
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return mergeResultSet.getInputStream(columnLabel, "Ascii");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnIndex, "Unicode")), columnIndex);
        return convert(String.valueOf(result));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnLabel, "Unicode")), columnLabel);
        return convert(String.valueOf(result));
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnIndex, "Binary")), columnIndex);
        return convert(String.valueOf(result));
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnLabel, "Binary")), columnLabel);
        return convert(String.valueOf(result));
    }
    
    @SneakyThrows
    @SuppressWarnings("all")
    private String convert(final InputStream value) {
        byte[] bytes = new byte[value.available()];
        value.read(bytes);
        return new String(bytes);
    }
    
    private InputStream convert(final String value) {
        return new ByteArrayInputStream(value.getBytes());
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader) decode(mergeResultSet.getValue(columnIndex, Reader.class), columnIndex);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return (Reader) decode(mergeResultSet.getValue(columnLabel, Reader.class), columnLabel);
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) decode(mergeResultSet.getValue(columnIndex, Blob.class), columnIndex);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return (Blob) decode(mergeResultSet.getValue(columnLabel, Blob.class), columnLabel);
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) decode(mergeResultSet.getValue(columnIndex, Clob.class), columnIndex);
    }
        
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return (Clob) decode(mergeResultSet.getValue(columnLabel, Clob.class), columnLabel);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) decode(mergeResultSet.getValue(columnIndex, URL.class), columnIndex);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return (URL) decode(mergeResultSet.getValue(columnLabel, URL.class), columnLabel);
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) decode(mergeResultSet.getValue(columnIndex, SQLXML.class), columnIndex);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return (SQLXML) decode(mergeResultSet.getValue(columnLabel, SQLXML.class), columnLabel);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return decode(mergeResultSet.getValue(columnIndex, Object.class), columnIndex);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return decode(mergeResultSet.getValue(columnLabel, Object.class), columnLabel);
    }
    
    @SneakyThrows
    private Object decode(final Object value, final String columnLabel) {
        Optional<ShardingEncryptor> shardingEncryptor = ShardingEncryptorEngine.getShardingEncryptor(getMetaData().getTableName(columnLabelIndexMap.get(columnLabel)), 
                getMetaData().getColumnName(columnLabelIndexMap.get(columnLabel)));
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decode(value) : value;
        
    }
    
    @SneakyThrows
    private Object decode(final Object value, final int columnIndex) {
        Optional<ShardingEncryptor> shardingEncryptor = ShardingEncryptorEngine.getShardingEncryptor(getMetaData().getTableName(columnIndex), getMetaData().getColumnName(columnIndex));
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decode(value) : value;
        
    }
}
