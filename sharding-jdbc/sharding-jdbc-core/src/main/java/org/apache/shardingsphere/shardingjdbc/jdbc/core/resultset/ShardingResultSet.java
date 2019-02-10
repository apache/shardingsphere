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
 * @author panjuan
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
        return (boolean) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, boolean.class), columnIndex), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, boolean.class), columnLabel), boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, byte.class), columnIndex), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return (byte) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, byte.class), columnLabel), byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, short.class), columnIndex), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return (short) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, short.class), columnLabel), short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, int.class), columnIndex), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return (int) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, int.class), columnLabel), int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, long.class), columnIndex), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return (long) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, long.class), columnLabel), long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, float.class), columnIndex), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return (float) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, float.class), columnLabel), float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, double.class), columnIndex), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return (double) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, double.class), columnLabel), double.class);
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, String.class), columnIndex), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return (String) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, String.class), columnLabel), String.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, BigDecimal.class), columnIndex), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, BigDecimal.class), columnLabel), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, BigDecimal.class), columnIndex), BigDecimal.class);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, BigDecimal.class), columnLabel), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, byte[].class), columnIndex), byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return (byte[]) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, byte[].class), columnLabel), byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, Date.class), columnIndex), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return (Date) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, Date.class), columnLabel), Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnIndex, Date.class, cal), columnIndex), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnLabel, Date.class, cal), columnLabel), Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, Time.class), columnIndex), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return (Time) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, Time.class), columnLabel), Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnIndex, Time.class, cal), columnIndex), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnLabel, Time.class, cal), columnLabel), Time.class);
    }
            
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnIndex, Timestamp.class), columnIndex), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(decode(mergeResultSet.getValue(columnLabel, Timestamp.class), columnLabel), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnIndex, Timestamp.class, cal), columnIndex), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(decode(mergeResultSet.getCalendarValue(columnLabel, Timestamp.class, cal), columnLabel), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnIndex, "Ascii")), columnIndex);
        return convert(String.valueOf(result));
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        Object result = decode(convert(mergeResultSet.getInputStream(columnLabel, "Ascii")), columnLabel);
        return convert(String.valueOf(result));
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
        Integer index = columnLabelIndexMap.get(columnLabel);
        if (null == index) {
            return value;
        }
        Optional<ShardingEncryptor> shardingEncryptor = getShardingEncryptorEngine().getShardingEncryptor(getMetaData().getTableName(index), getMetaData().getColumnName(index));
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(value) : value;
    }
    
    @SneakyThrows
    private Object decode(final Object value, final int columnIndex) {
        Optional<ShardingEncryptor> shardingEncryptor = getShardingEncryptorEngine().getShardingEncryptor(getMetaData().getTableName(columnIndex), getMetaData().getColumnName(columnIndex));
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(value) : value;
    }
}
