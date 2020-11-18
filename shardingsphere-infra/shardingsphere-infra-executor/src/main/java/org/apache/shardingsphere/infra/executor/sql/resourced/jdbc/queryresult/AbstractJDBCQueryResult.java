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

package org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Abstract JDBC query result.
 */
@RequiredArgsConstructor
public abstract class AbstractJDBCQueryResult implements QueryResult {
    
    private final ResultSetMetaData resultSetMetaData;
    
    @Override
    public final int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
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
    public final String getColumnTypeName(final int columnIndex) throws SQLException {
        return resultSetMetaData.getColumnTypeName(columnIndex);
    }
}
