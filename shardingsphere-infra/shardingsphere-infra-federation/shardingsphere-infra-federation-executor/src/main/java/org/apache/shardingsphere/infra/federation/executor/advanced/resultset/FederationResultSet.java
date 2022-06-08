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

package org.apache.shardingsphere.infra.federation.executor.advanced.resultset;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
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
 * Federation result set.
 */
@RequiredArgsConstructor
public final class FederationResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final MergedResult mergedResult;
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public void close() throws SQLException {
        
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return false;
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return false;
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return 0;
    }
    
    @Override
    public float getFloat(final int columnIndex) {
        return 0;
    }
    
    @Override
    public float getFloat(final String columnLabel) {
        return 0;
    }
    
    @Override
    public double getDouble(final int columnIndex) {
        return 0;
    }
    
    @Override
    public double getDouble(final String columnLabel) {
        return 0;
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
    public BigDecimal getBigDecimal(final int columnIndex) {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) {
        return null;
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
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
    public Time getTime(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
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
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
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
    public Reader getCharacterStream(final int columnIndex) {
        return null;
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) {
        return null;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        
    }
    
    @Override
    public int getFetchDirection() {
        return ResultSet.FETCH_FORWARD;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
    }
    
    @Override
    public int getFetchSize() {
        return 0;
    }
    
    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
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
    public Array getArray(final int columnIndex) {
        return null;
    }
    
    @Override
    public Array getArray(final String columnLabel) {
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
    public SQLXML getSQLXML(final int columnIndex) {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) {
        return null;
    }
    
    @Override
    public String getNString(final int columnIndex) {
        return null;
    }
    
    @Override
    public String getNString(final String columnLabel) {
        return null;
    }
}
