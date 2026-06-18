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

package org.apache.shardingsphere.driver.state.circuit.resultset;

import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationResultSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Circuit breaker result set.
 */
public final class CircuitBreakerResultSet extends AbstractUnsupportedOperationResultSet {
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public boolean wasNull() {
        return true;
    }
    
    @Override
    public String getString(final int columnIndex) {
        return "";
    }
    
    @Override
    public String getString(final String columnLabel) {
        return "";
    }
    
    @Override
    public String getNString(final int columnIndex) {
        return "";
    }
    
    @Override
    public String getNString(final String columnLabel) {
        return "";
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) {
        return false;
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) {
        return false;
    }
    
    @Override
    public byte getByte(final int columnIndex) {
        return 0;
    }
    
    @Override
    public byte getByte(final String columnLabel) {
        return 0;
    }
    
    @Override
    public short getShort(final int columnIndex) {
        return 0;
    }
    
    @Override
    public short getShort(final String columnLabel) {
        return 0;
    }
    
    @Override
    public int getInt(final int columnIndex) {
        return 0;
    }
    
    @Override
    public int getInt(final String columnLabel) {
        return 0;
    }
    
    @Override
    public long getLong(final int columnIndex) {
        return 0L;
    }
    
    @Override
    public long getLong(final String columnLabel) {
        return 0L;
    }
    
    @Override
    public float getFloat(final int columnIndex) {
        return 0F;
    }
    
    @Override
    public float getFloat(final String columnLabel) {
        return 0F;
    }
    
    @Override
    public double getDouble(final int columnIndex) {
        return 0D;
    }
    
    @Override
    public double getDouble(final String columnLabel) {
        return 0D;
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) {
        return new byte[0];
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final int columnIndex) {
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel) {
        return null;
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) {
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) {
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex) {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel) {
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) {
        return null;
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
        return new CircuitBreakerResultSetMetaData();
    }
    
    @Override
    public Object getObject(final int columnIndex) {
        return null;
    }
    
    @Override
    public Object getObject(final String columnLabel) {
        return null;
    }
    
    @Override
    public int findColumn(final String columnLabel) {
        return 0;
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) {
        return null;
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) {
        return null;
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
    public Array getArray(final int columnIndex) {
        return null;
    }
    
    @Override
    public Array getArray(final String columnLabel) {
        return null;
    }
    
    @Override
    public Blob getBlob(final int columnIndex) {
        return null;
    }
    
    @Override
    public Blob getBlob(final String columnLabel) {
        return null;
    }
    
    @Override
    public Clob getClob(final int columnIndex) {
        return null;
    }
    
    @Override
    public Clob getClob(final String columnLabel) {
        return null;
    }
    
    @Override
    public URL getURL(final int columnIndex) {
        return null;
    }
    
    @Override
    public URL getURL(final String columnLabel) {
        return null;
    }
    
    @Override
    public boolean isClosed() {
        return false;
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) {
        return null;
    }
}
