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

package com.dangdang.ddframe.rdb.sharding.jdbc.unsupported;

import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingStatement;

import java.io.Reader;
import java.sql.Array;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * 声明不支持操作的预编译语句对象.
 * 
 * @author zhangliang
 */
public abstract class AbstractUnsupportedOperationPreparedStatement extends ShardingStatement implements PreparedStatement {
    
    protected AbstractUnsupportedOperationPreparedStatement(final ShardingConnection shardingConnection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        super(shardingConnection, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("getMetaData");
    }
    
    @Override
    public final ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException("ParameterMetaData");
    }
    
    @Override
    public final void setNString(final int parameterIndex, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNString");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final NClob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNClob(final int parameterIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }
    
    @Override
    public final void setNCharacterStream(final int parameterIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream");
    }
    
    @Override
    public final void setNCharacterStream(final int parameterIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNCharacterStream");
    }
    
    @Override
    public final void setArray(final int parameterIndex, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setArray");
    }
    
    @Override
    public final void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRowId");
    }
    
    @Override
    public final void setRef(final int parameterIndex, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRef");
    }
}
