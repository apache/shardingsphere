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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class SSResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final QueryResultMetaData metaData;
    
    public SSResultSetMetaData(final QueryResultMetaData metaData) {
        this.metaData = metaData;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return metaData.getColumnCount();
    }
    
    @Override
    public boolean isAutoIncrement(final int i) throws SQLException {
        return metaData.isAutoIncrement(i);
    }
    
    @Override
    public boolean isCaseSensitive(final int i) throws SQLException {
        // TODO 
        return false;
    }
    
    @Override
    public boolean isSearchable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCurrency(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public int isNullable(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public boolean isSigned(final int i) throws SQLException {
        return metaData.isSigned(i);
    }
    
    @Override
    public int getColumnDisplaySize(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int i) throws SQLException {
        return metaData.getColumnLabel(i);
    }
    
    @Override
    public String getColumnName(final int i) throws SQLException {
        return metaData.getColumnName(i);
    }
    
    @Override
    public String getSchemaName(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public int getPrecision(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public int getScale(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public String getTableName(final int i) throws SQLException {
        return metaData.getTableName(i);
    }
    
    @Override
    public String getCatalogName(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public int getColumnType(final int i) throws SQLException {
        return metaData.getColumnType(i);
    }
    
    @Override
    public String getColumnTypeName(final int i) throws SQLException {
        return metaData.getColumnTypeName(i);
    }
    
    @Override
    public boolean isReadOnly(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isWritable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public String getColumnClassName(final int i) throws SQLException {
        // TODO 
        return metaData.getColumnTypeName(i);
    }
}
