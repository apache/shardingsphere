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

package org.apache.shardingsphere.sharding.execute.metadata;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.sharding.execute.metadata.loader.ShardingTableMetaDataLoader;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.executor.engine.ExecutorEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Table meta data initializer for sharding.
 *
 * @author zhangliang
 */
public final class ShardingTableMetaDataInitializer {
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ConnectionManager connectionManager;
    
    private final ShardingTableMetaDataLoader tableMetaDataLoader;
    
    public ShardingTableMetaDataInitializer(final DataSourceMetas dataSourceMetas, final ExecutorEngine executorEngine,
                                            final ConnectionManager connectionManager, final int maxConnectionsSizePerQuery, final boolean isCheckingMetaData) {
        this.dataSourceMetas = dataSourceMetas;
        this.connectionManager = connectionManager;
        tableMetaDataLoader = new ShardingTableMetaDataLoader(dataSourceMetas, executorEngine, connectionManager, maxConnectionsSizePerQuery, isCheckingMetaData);
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
     * Load table metas.
     *
     * @param shardingRule sharding rule
     * @return Table metas
     * @throws SQLException SQL exception
     */
    public TableMetas load(final ShardingRule shardingRule) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>();
        result.putAll(loadShardingTables(shardingRule));
        result.putAll(loadDefaultTables(shardingRule));
        return new TableMetas(result);
    }
    
    private Map<String, TableMetaData> loadShardingTables(final ShardingRule shardingRule) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            result.put(each.getLogicTable(), load(each.getLogicTable(), shardingRule));
        }
        return result;
    }
    
    private Map<String, TableMetaData> loadDefaultTables(final ShardingRule shardingRule) throws SQLException {
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        if (!actualDefaultDataSourceName.isPresent()) {
            return Collections.emptyMap();
        }
        Collection<String> tableNames = loadAllTableNames(actualDefaultDataSourceName.get());
        Map<String, TableMetaData> result = new HashMap<>(tableNames.size(), 1);
        for (String each : tableNames) {
            result.put(each, load(each, shardingRule));
        }
        return result;
    }
    
    private Collection<String> loadAllTableNames(final String dataSourceName) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        DataSourceMetaData dataSourceMetaData = dataSourceMetas.getDataSourceMetaData(dataSourceName);
        String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getCatalog();
        String schemaName = null == dataSourceMetaData ? null : dataSourceMetaData.getSchema();
        try (
                Connection connection = connectionManager.getConnection(dataSourceName);
                ResultSet resultSet = connection.getMetaData().getTables(catalog, schemaName, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (!tableName.contains("$") && !tableName.contains("/")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
}
