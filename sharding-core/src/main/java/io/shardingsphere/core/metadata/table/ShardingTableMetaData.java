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

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sharding table meta data.
 *
 * @author panjuan
 * @author zhaojun
 * @author zhangliang
 */
public class ShardingTableMetaData {
    
    private final TableMetaDataLoader tableMetaDataLoader;
    
    private final Map<String, TableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    @Setter
    private TableMetaDataExecutorAdapter executorAdapter;
    
    public ShardingTableMetaData(final ListeningExecutorService executorService, final TableMetaDataExecutorAdapter executorAdapter) {
        this.executorAdapter = executorAdapter;
        tableMetaDataLoader = new TableMetaDataLoader(executorService, executorAdapter);
    }
    
    /**
     * Initialize sharding meta data.
     *
     * @param shardingRule sharding rule
     */
    public void init(final ShardingRule shardingRule) {
        try {
            initLogicTables(shardingRule);
            initDefaultTables(shardingRule);
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private void initLogicTables(final ShardingRule shardingRule) {
        for (TableRule each : shardingRule.getTableRules()) {
            tableMetaDataMap.put(each.getLogicTable(), tableMetaDataLoader.loadTableMetaData(each.getLogicTable(), shardingRule));
        }
    }
    
    private void initDefaultTables(final ShardingRule shardingRule) throws SQLException {
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        if (actualDefaultDataSourceName.isPresent()) {
            for (String each : getAllTableNames(actualDefaultDataSourceName.get())) {
                tableMetaDataMap.put(each, tableMetaDataLoader.loadTableMetaData(each, shardingRule));
            }
        }
    }
    
    private Collection<String> getAllTableNames(final String dataSourceName) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (Connection connection = executorAdapter.getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return result;
    }
    
    /**
     * Put table meta data.
     * 
     * @param logicTableName logic table name
     * @param tableMetaData table meta data
     */
    public void put(final String logicTableName, final TableMetaData tableMetaData) {
        tableMetaDataMap.put(logicTableName, tableMetaData);
    }
    
    /**
     * Judge contains table from table meta data or not.
     *
     * @param tableName table name
     * @return contains table from table meta data or not
     */
    public boolean containsTable(final String tableName) {
        return tableMetaDataMap.containsKey(tableName);
    }
    
    /**
     * Judge contains column from table meta data or not.
     * 
     * @param tableName table name
     * @param column column
     * @return contains column from table meta data or not
     */
    public boolean containsColumn(final String tableName, final String column) {
        return containsTable(tableName) && tableMetaDataMap.get(tableName).getAllColumnNames().contains(column.toLowerCase());
    }
    
    /**
     * Get all column names via table.
     *
     * @param tableName table name
     * @return column names.
     */
    public Collection<String> getAllColumnNames(final String tableName) {
        return tableMetaDataMap.get(tableName).getAllColumnNames();
    }
}
