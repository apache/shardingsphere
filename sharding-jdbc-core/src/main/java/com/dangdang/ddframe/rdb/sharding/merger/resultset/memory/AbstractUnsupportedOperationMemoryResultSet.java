/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.List;
import java.util.Map;

/**
 * 声明不支持操作的内存结果集抽象类.
 *
 * @author gaohongtao
 * @author zhagliang
 */
public abstract class AbstractUnsupportedOperationMemoryResultSet extends AbstractResultSetAdapter {
    
    public AbstractUnsupportedOperationMemoryResultSet(final List<ResultSet> resultSets) throws SQLException {
        super(resultSets);
    }
    
    @Override
    public final Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject");
    }
    
    @Override
    public final Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject");
    }
    
    @Override
    public final InputStream getAsciiStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getAsciiStream");
    }
    
    @Override
    public final InputStream getAsciiStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getAsciiStream");
    }
    
    @Override
    public final InputStream getBinaryStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBinaryStream");
    }
    
    @Override
    public final InputStream getBinaryStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBinaryStream");
    }
    
    @Override
    public final InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getUnicodeStream");
    }
    
    @Override
    public final InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getUnicodeStream");
    }
    
    @Override
    public final Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getCharacterStream");
    }
    
    @Override
    public final Reader getCharacterStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getCharacterStream");
    }
    
    @Override
    public final Blob getBlob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBlob");
    }
    
    @Override
    public final Blob getBlob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getBlob");
    }
    
    @Override
    public final Clob getClob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getClob");
    }
    
    @Override
    public final Clob getClob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getClob");
    }
    
    @Override
    public final SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getSQLXML");
    }
    
    @Override
    public final SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getSQLXML");
    }
}
