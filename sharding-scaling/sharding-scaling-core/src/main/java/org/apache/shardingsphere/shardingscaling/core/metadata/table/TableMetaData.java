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

package org.apache.shardingsphere.shardingscaling.core.metadata.table;

import lombok.Getter;
import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Table meta data.
 */
public final class TableMetaData {
    
    private final List<ColumnMetaData> columnMetaData = new ArrayList<>();
    
    @Getter
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    private final Set<String> primaryKeyColumnsSet = new HashSet<>();
    
    /**
     * Get size of columns.
     *
     * @return size of columns
     */
    public int getColumnsSize() {
        return columnMetaData.size();
    }
    
    /**
     * Add column meta data.
     *
     * @param columnMetaData column meta data
     */
    public void addAllColumnMetaData(final Collection<ColumnMetaData> columnMetaData) {
        this.columnMetaData.addAll(columnMetaData);
    }
    
    /**
     * Get column meta data.
     *
     * @param columnIndex column index
     * @return column meta data
     */
    public ColumnMetaData getColumnMetaData(final int columnIndex) {
        return columnMetaData.get(columnIndex);
    }
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public Collection<String> getColumnNames() {
        Collection<String> result = new LinkedList<>();
        for (ColumnMetaData each : columnMetaData) {
            result.add(each.getColumnName());
        }
        return result;
    }
    
    /**
     * Find index of column.
     *
     * @param columnName column name
     * @return index of column
     */
    public int findColumnIndex(final String columnName) {
        for (int i = 0; i < columnMetaData.size(); i++) {
            if (columnMetaData.get(i).getColumnName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Add primary key.
     *
     * @param primaryKeyColumnName primary key column name
     */
    public void addAllPrimaryKey(final Collection<String> primaryKeyColumnName) {
        primaryKeyColumns.addAll(primaryKeyColumnName);
        primaryKeyColumnsSet.addAll(primaryKeyColumnName);
    }
    
    /**
     * Judge column whether primary key.
     *
     * @param columnIndex column index
     * @return true if the column is primary key, otherwise false
     */
    public boolean isPrimaryKey(final int columnIndex) {
        if (columnIndex >= columnMetaData.size()) {
            return false;
        }
        return primaryKeyColumnsSet.contains(columnMetaData.get(columnIndex).getColumnName());
    }
}
