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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * Raw result set meta data.
 */
@RequiredArgsConstructor
public final class RawResultSetMetaData implements ResultSetMetaData {
    
    private final List<RawQueryResultColumnMetaData> columns;
    
    @Override
    public int getColumnCount() {
        return columns.size();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return getColumnMetaData(column).isAutoIncrement();
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        getColumnMetaData(column);
        return true;
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        getColumnMetaData(column);
        return true;
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        getColumnMetaData(column);
        return false;
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        return getColumnMetaData(column).isNotNull() ? ResultSetMetaData.columnNoNulls : ResultSetMetaData.columnNullable;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return getColumnMetaData(column).isSigned();
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return getColumnMetaData(column).getLength();
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        return getColumnMetaData(column).getLabel();
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        return getColumnMetaData(column).getName();
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        getColumnMetaData(column);
        return "";
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return getColumnMetaData(column).getLength();
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return getColumnMetaData(column).getDecimals();
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        return getColumnMetaData(column).getTableName();
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        getColumnMetaData(column);
        return "";
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return getColumnMetaData(column).getType();
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return getColumnMetaData(column).getTypeName();
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        getColumnMetaData(column);
        return false;
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        getColumnMetaData(column);
        return true;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        getColumnMetaData(column);
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        return getColumnClassNameByType(getColumnMetaData(column).getType());
    }
    
    private String getColumnClassNameByType(final int columnType) {
        switch (columnType) {
            case Types.BOOLEAN:
            case Types.BIT:
                return Boolean.class.getName();
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return Integer.class.getName();
            case Types.BIGINT:
                return Long.class.getName();
            case Types.FLOAT:
            case Types.REAL:
                return Float.class.getName();
            case Types.DOUBLE:
                return Double.class.getName();
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.class.getName();
            case Types.DATE:
                return Date.class.getName();
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return Time.class.getName();
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return Timestamp.class.getName();
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return byte[].class.getName();
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return String.class.getName();
            default:
                return Object.class.getName();
        }
    }
    
    private RawQueryResultColumnMetaData getColumnMetaData(final int column) throws SQLException {
        if (column < 1 || column > columns.size()) {
            throw new SQLException(String.format("Column index out of range: %s", column));
        }
        return columns.get(column - 1);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return iface.cast(this);
        }
        throw new SQLFeatureNotSupportedException(String.format("`%s` cannot be unwrapped as `%s`", getClass().getName(), iface.getName()));
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return iface.isInstance(this);
    }
}
