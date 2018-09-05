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

package io.shardingsphere.core.metadata.table.executor;

import com.google.common.base.Optional;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Table meta data initializer.
 *
 * @author zhangliang
 */
public final class TableMetaDataInitializer {
    
    private final TableMetaDataConnectionManager connectionManager;
    
    private final TableMetaDataLoader tableMetaDataLoader;
    
    public TableMetaDataInitializer(final ShardingDataSourceMetaData shardingDataSourceMetaData, 
                                    final ShardingExecuteEngine executeEngine, final TableMetaDataConnectionManager connectionManager, final int maxConnectionsSizePerQuery) {
        this.connectionManager = connectionManager;
        tableMetaDataLoader = new TableMetaDataLoader(shardingDataSourceMetaData, executeEngine, connectionManager, maxConnectionsSizePerQuery);
    }
    
    /**
     * Load all table meta data.
     * 
     * @param shardingRule sharding rule
     * @return all table meta data
     */
    public Map<String, TableMetaData> load(final ShardingRule shardingRule) {
        Map<String, TableMetaData> result = new HashMap<>();
        try {
            result.putAll(loadShardingTables(shardingRule));
            result.putAll(loadDefaultTables(shardingRule));
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
        return result;
    }
    
    private Map<String, TableMetaData> loadShardingTables(final ShardingRule shardingRule) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            result.put(each.getLogicTable(), tableMetaDataLoader.load(each.getLogicTable(), shardingRule));
        }
        return result;
    }
    
    private Map<String, TableMetaData> loadDefaultTables(final ShardingRule shardingRule) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(shardingRule.getTableRules().size(), 1);
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        if (actualDefaultDataSourceName.isPresent()) {
            for (String each : getAllTableNames(actualDefaultDataSourceName.get())) {
                result.put(each, tableMetaDataLoader.load(each, shardingRule));
            }
        }
        return result;
    }
    
    private Collection<String> getAllTableNames(final String dataSourceName) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        try (Connection connection = connectionManager.getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(dataSourceName, null, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (!tableName.contains("$")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
}
