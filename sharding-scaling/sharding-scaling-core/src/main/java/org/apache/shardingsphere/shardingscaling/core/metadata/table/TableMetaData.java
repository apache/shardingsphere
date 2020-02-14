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

import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

/**
 * Table meta data.
 *
 * @author yangyi
 */
public final class TableMetaData {

    private final List<ColumnMetaData> columnMetaDatas = new ArrayList<>();
    
    @Getter
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    /**
     * Get size of columns.
     *
     * @return size of columns
     */
    public int getColumnsSize() {
        return columnMetaDatas.size();
    }
    
    /**
     * Add column meta data.
     *
     * @param columnMetaData column meta data
     */
    public void addAllColumnMetaData(final Collection<ColumnMetaData> columnMetaData) {
        columnMetaDatas.addAll(columnMetaData);
    }
    
    /**
     * Get column meta data.
     *
     * @param columnIndex column index
     * @return column meta data
     */
    public ColumnMetaData getColumnMetaData(final int columnIndex) {
        return columnMetaDatas.get(columnIndex);
    }
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public Collection<String> getColumnNames() {
        Collection<String> result = new LinkedList<>();
        for (ColumnMetaData each : columnMetaDatas) {
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
        for (int i = 0; i < columnMetaDatas.size(); i++) {
            if (columnMetaDatas.get(i).getColumnName().equals(columnName)) {
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
    }
}
