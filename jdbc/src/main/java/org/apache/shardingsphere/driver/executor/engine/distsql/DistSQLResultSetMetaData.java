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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;

import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.List;

/**
 * Result set meta data for DistSQL.
 */
public final class DistSQLResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final List<String> columnNames;
    
    public DistSQLResultSetMetaData(final List<String> columnNames) {
        this.columnNames = columnNames;
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.size();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) {
        checkColumnIndex(column);
        return true;
    }
    
    @Override
    public boolean isSearchable(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public int isNullable(final int column) {
        checkColumnIndex(column);
        return columnNullable;
    }
    
    @Override
    public boolean isSigned(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) {
        checkColumnIndex(column);
        return 255;
    }
    
    @Override
    public String getColumnLabel(final int column) {
        checkColumnIndex(column);
        return columnNames.get(column - 1);
    }
    
    @Override
    public String getColumnName(final int column) {
        checkColumnIndex(column);
        return columnNames.get(column - 1);
    }
    
    @Override
    public String getSchemaName(final int column) {
        checkColumnIndex(column);
        return "";
    }
    
    @Override
    public int getPrecision(final int column) {
        checkColumnIndex(column);
        return 0;
    }
    
    @Override
    public int getScale(final int column) {
        checkColumnIndex(column);
        return 0;
    }
    
    @Override
    public String getTableName(final int column) {
        checkColumnIndex(column);
        return "";
    }
    
    @Override
    public String getCatalogName(final int column) {
        checkColumnIndex(column);
        return "";
    }
    
    @Override
    public int getColumnType(final int column) {
        checkColumnIndex(column);
        return Types.CHAR;
    }
    
    @Override
    public String getColumnTypeName(final int column) {
        checkColumnIndex(column);
        return "CHAR";
    }
    
    @Override
    public boolean isReadOnly(final int column) {
        checkColumnIndex(column);
        return true;
    }
    
    @Override
    public boolean isWritable(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) {
        checkColumnIndex(column);
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) {
        checkColumnIndex(column);
        return String.class.getName();
    }
    
    private void checkColumnIndex(final int column) {
        Preconditions.checkArgument(column >= 1 && column <= columnNames.size(), "Column index %s is out of range [1, %s]", column, columnNames.size());
    }
}
