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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultSet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Abstract JDBC query result set.
 */
@RequiredArgsConstructor
public abstract class AbstractJDBCQueryResultSet implements QueryResultSet {
    
    private final ResultSetMetaData resultSetMetaData;
    
    @Override
    public final int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }
    
    @Override
    public final String getTableName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getTableName(columnIndex);
    }
    
    @Override
    public final String getColumnName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnName(columnIndex);
    }
    
    @Override
    public final String getColumnLabel(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnLabel(columnIndex);
    }
    
    @Override
    public final int getColumnType(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnType(columnIndex);
    }
    
    @Override
    public final String getColumnTypeName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnTypeName(columnIndex);
    }
    
    @Override
    public final int getColumnLength(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(columnIndex);
    }
    
    @Override
    public final int getDecimals(final int columnIndex) throws SQLException {
        return resultSetMetaData.getScale(columnIndex);
    }
    
    @Override
    public final boolean isSigned(final int columnIndex) throws SQLException {
        return resultSetMetaData.isSigned(columnIndex);
    }
    
    @Override
    public final boolean isNotNull(final int columnIndex) throws SQLException {
        return resultSetMetaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls;
    }
    
    @Override
    public final boolean isAutoIncrement(final int columnIndex) throws SQLException {
        return resultSetMetaData.isAutoIncrement(columnIndex);
    }
}
