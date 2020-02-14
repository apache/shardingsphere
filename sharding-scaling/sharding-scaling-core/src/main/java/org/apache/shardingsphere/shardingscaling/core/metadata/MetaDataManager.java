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

package org.apache.shardingsphere.shardingscaling.core.metadata;

import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.shardingscaling.core.metadata.table.TableMetaDataLoader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Meta data manager.
 *
 * @author avalon566
 * @author yangyi
 */
public final class MetaDataManager {
    
    private Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
    
    private TableMetaDataLoader tableMetaDataLoader;

    public MetaDataManager(final DataSource dataSource) {
        this.tableMetaDataLoader = new TableMetaDataLoader(dataSource);
    }

    /**
     * Get primary key column name by table name.
     *
     * @param tableName table name
     * @return list of table name
     */
    public List<String> getPrimaryKeys(final String tableName) {
        if (!tableMetaDataMap.containsKey(tableName)) {
            tableMetaDataMap.put(tableName, tableMetaDataLoader.load(tableName));
        }
        return tableMetaDataMap.get(tableName).getPrimaryKeyColumns();
    }

    /**
     * Get all column meta data by table name.
     *
     * @param tableName table name
     * @return list of column meta data
     */
    public List<ColumnMetaData> getColumnNames(final String tableName) {
        if (!tableMetaDataMap.containsKey(tableName)) {
            tableMetaDataMap.put(tableName, tableMetaDataLoader.load(tableName));
        }
        return tableMetaDataMap.get(tableName).getColumnMetaDatas();
    }

    /**
     * Find column index by column name.
     *
     * @param metaData   meta data list
     * @param columnName table name
     * @return index
     */
    public int findColumnIndex(final List<ColumnMetaData> metaData, final String columnName) {
        for (int i = 0; i < metaData.size(); i++) {
            if (metaData.get(i).getColumnName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
