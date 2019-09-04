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

package org.apache.shardingsphere.core.execute.metadata;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
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
    
    private final DataSourceMetas dataSourceMetas;
    
    private final TableMetaDataConnectionManager connectionManager;
    
    private final TableMetaDataLoader tableMetaDataLoader;
    
    public TableMetaDataInitializer(final DataSourceMetas dataSourceMetas, final ShardingExecuteEngine executeEngine,
                                    final TableMetaDataConnectionManager connectionManager, final int maxConnectionsSizePerQuery, final boolean isCheckingMetaData) {
        this.dataSourceMetas = dataSourceMetas;
        this.connectionManager = connectionManager;
        tableMetaDataLoader = new TableMetaDataLoader(dataSourceMetas, executeEngine, connectionManager, maxConnectionsSizePerQuery, isCheckingMetaData);
    }
    
    /**
     * Load table meta data.
     *
     * @param logicTableName logic table name
     * @param shardingRule sharding rule
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName, final ShardingRule shardingRule) throws SQLException {
        return tableMetaDataLoader.load(logicTableName, shardingRule);
    }
    
    /**
     * Load all table meta data.
     * 
     * @param shardingRule sharding rule
     * @return all table meta data
     * @throws SQLException SQL exception
     */
    public Map<String, TableMetaData> load(final ShardingRule shardingRule) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>();
        result.putAll(loadShardingTables(shardingRule));
        result.putAll(loadDefaultTables(shardingRule));
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
        DataSourceMetaData dataSourceMetaData = this.dataSourceMetas.getDataSourceMetaData(dataSourceName);
        String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getSchemaName();
        try (Connection connection = connectionManager.getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(catalog, getCurrentSchemaName(connection), null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (!tableName.contains("$") && !tableName.contains("/")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
    
    private String getCurrentSchemaName(final Connection connection) throws SQLException {
        try {
            return connection.getSchema();
        } catch (final AbstractMethodError | SQLFeatureNotSupportedException ignore) {
            return null;
        }
    }
}
