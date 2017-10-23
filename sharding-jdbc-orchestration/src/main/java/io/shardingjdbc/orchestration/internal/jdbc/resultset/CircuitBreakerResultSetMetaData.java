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

package io.shardingjdbc.orchestration.internal.jdbc.resultset;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Circuit breaker result set metadata.
 * 
 * @author caohao
 */
public final class CircuitBreakerResultSetMetaData implements ResultSetMetaData {
    
    @Override
    public int getColumnCount() throws SQLException {
        return 0;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        return ResultSetMetaData.columnNullable;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return 0;
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return 0;
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return 0;
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        return null;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
