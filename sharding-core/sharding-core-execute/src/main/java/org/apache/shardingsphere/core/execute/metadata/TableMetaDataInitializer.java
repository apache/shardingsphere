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
import org.apache.shardingsphere.underlying.common.metadata.table.ConnectionManager;
import org.apache.shardingsphere.underlying.executor.engine.ExecutorEngine;
import org.apache.shardingsphere.core.execute.metadata.loader.ShardingTableMetaDataLoader;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Table meta data initializer.
 *
 * @author zhangliang
 */
public final class TableMetaDataInitializer {
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ConnectionManager connectionManager;
    
    private final ShardingTableMetaDataLoader tableMetaDataLoader;
    
    public TableMetaDataInitializer(final DataSourceMetas dataSourceMetas, final ExecutorEngine executorEngine,
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
        return decorate(tableMetaDataLoader.load(logicTableName, shardingRule), logicTableName, shardingRule.getEncryptRule());
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
    
    private TableMetaData decorate(final TableMetaData tableMetaData, final String logicTableName, final EncryptRule encryptRule) {
        return new TableMetaData(getEncryptColumnMetaDataList(logicTableName, tableMetaData.getColumns().values(), encryptRule), tableMetaData.getIndexes());
    }
    
    private Collection<ColumnMetaData> getEncryptColumnMetaDataList(final String tableName, final Collection<ColumnMetaData> originalColumnMetaDataList, final EncryptRule encryptRule) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        for (ColumnMetaData each : originalColumnMetaDataList) {
            if (!derivedColumns.contains(each.getName())) {
                result.add(getEncryptColumnMetaData(tableName, each, encryptRule));
            }
        }
        return result;
    }
    
    private ColumnMetaData getEncryptColumnMetaData(final String tableName, final ColumnMetaData originalColumnMetaData, final EncryptRule encryptRule) {
        if (!encryptRule.isCipherColumn(tableName, originalColumnMetaData.getName())) {
            return originalColumnMetaData;
        }
        String logicColumnName = encryptRule.getLogicColumnOfCipher(tableName, originalColumnMetaData.getName());
        String plainColumnName = encryptRule.findPlainColumn(tableName, logicColumnName).orNull();
        String assistedQueryColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName).orNull();
        return new EncryptColumnMetaData(
                logicColumnName, originalColumnMetaData.getDataType(), originalColumnMetaData.isPrimaryKey(), originalColumnMetaData.getName(), plainColumnName, assistedQueryColumnName);
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
