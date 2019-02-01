/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Sharding table meta data.
 *
 * @author panjuan
 * @author zhaojun
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingTableMetaData {
    
    @Getter
    private final Map<String, TableMetaData> tables;
    
    /**
     * Get table meta data by table name.
     * @param logicTableName logicTableName logic table name
     * @return table mata data
     */
    public TableMetaData get(final String logicTableName) {
        return tables.get(logicTableName);
    }
    
    /**
     * Add table meta data.
     * 
     * @param logicTableName logic table name
     * @param tableMetaData table meta data
     */
    public void put(final String logicTableName, final TableMetaData tableMetaData) {
        tables.put(logicTableName, tableMetaData);
    }
    
    /**
     * Remove table meta data.
     *
     * @param logicTableName logic table name
     */
    public void remove(final String logicTableName) {
        tables.remove(logicTableName);
    }
    
    /**
     * Judge contains table from table meta data or not.
     *
     * @param tableName table name
     * @return contains table from table meta data or not
     */
    public boolean containsTable(final String tableName) {
        return tables.containsKey(tableName);
    }
    
    /**
     * Judge contains column from table meta data or not.
     * 
     * @param tableName table name
     * @param column column
     * @return contains column from table meta data or not
     */
    public boolean containsColumn(final String tableName, final String column) {
        return containsTable(tableName) && tables.get(tableName).getColumns().keySet().contains(column.toLowerCase());
    }
    
    /**
     * Get all column names via table.
     *
     * @param tableName table name
     * @return column names.
     */
    public Collection<String> getAllColumnNames(final String tableName) {
        return tables.containsKey(tableName) ? tables.get(tableName).getColumns().keySet() : Collections.<String>emptyList();
    }
}
