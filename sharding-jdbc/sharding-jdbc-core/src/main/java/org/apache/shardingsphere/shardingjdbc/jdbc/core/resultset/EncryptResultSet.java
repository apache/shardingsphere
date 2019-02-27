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

import org.apache.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
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

/**
 * Encrypt result set.
 *
 * @author panjuan
 */
public final class EncryptResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final StreamQueryResult queryResult;
    
    public EncryptResultSet(final ResultSet resultSet) {
        queryResult = new StreamQueryResult(resultSet);
    }
    
    @Override
    public boolean next() throws SQLException {
        return queryResult.next();
    }
    
    @Override
    public void close() throws SQLException {
        
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return false;
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return null;
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return false;
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return null;
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }
    
    @Override
    public int getType() throws SQLException {
        return 0;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return null;
    }
}
