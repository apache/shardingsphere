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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * JDBC query result meta data.
 */
@RequiredArgsConstructor
public final class JDBCQueryResultMetaData implements QueryResultMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }
    
    @Override
    public String getTableName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getTableName(columnIndex);
    }
    
    @Override
    public String getColumnName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnName(columnIndex);
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnLabel(columnIndex);
    }
    
    @Override
    public int getColumnType(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnType(columnIndex);
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnTypeName(columnIndex);
    }
    
    @Override
    public int getColumnLength(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(columnIndex);
    }
    
    @Override
    public int getDecimals(final int columnIndex) throws SQLException {
        return resultSetMetaData.getScale(columnIndex);
    }
    
    @Override
    public boolean isSigned(final int columnIndex) throws SQLException {
        return resultSetMetaData.isSigned(columnIndex);
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) throws SQLException {
        return resultSetMetaData.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls;
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) throws SQLException {
        return resultSetMetaData.isAutoIncrement(columnIndex);
    }
}
