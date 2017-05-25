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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.WrapperAdapter;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 生成键结果集元数据.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
class GeneratedKeysResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final String generatedKeyColumn;
    
    @Override
    public int getColumnCount() throws SQLException {
        return 1;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return true;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return true;
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return false;
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return columnNoNulls;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return true;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return generatedKeyColumn;
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return generatedKeyColumn;
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return "";
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return 0;
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return 0;
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return "";
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return "";
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return Types.BIGINT;
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return "";
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return true;
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        Preconditions.checkArgument(column == 1);
        return Number.class.getName();
    }
}
