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

import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedDatabaseMetaDataResultSet;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Database meta data result set.
 *
 * @author yangyi
 */
public final class DatabaseMetaDataResultSet extends AbstractUnsupportedDatabaseMetaDataResultSet {
    
    private final int type;
    
    private final int concurrency;
    
    private volatile boolean closed;
    
    public DatabaseMetaDataResultSet(final ResultSet resultSet) throws SQLException {
        this.type = resultSet.getType();
        this.concurrency = resultSet.getConcurrency();
    }
    
    @Override
    public boolean next() throws SQLException {
        checkClosed();
        return false;
    }
    
    @Override
    public void close() throws SQLException {
        checkClosed();
        closed = true;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        checkClosed();
        return false;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        checkClosed();
        return false;
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        checkClosed();
        return false;
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        checkClosed();
        return new byte[0];
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        checkClosed();
        return new byte[0];
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        checkClosed();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        checkClosed();
        return null;
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        checkClosed();
        return 0;
    }
    
    @Override
    public int getType() throws SQLException {
        checkClosed();
        return this.type;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        checkClosed();
        return this.concurrency;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.closed;
    }
    
    private void checkClosed() throws SQLException {
        if (closed) {
            throw new SQLException("ResultSet has closed.");
        }
    }
}
