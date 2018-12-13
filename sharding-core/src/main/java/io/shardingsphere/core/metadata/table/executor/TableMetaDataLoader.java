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

import com.google.common.collect.Lists;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.ShardingGroupExecuteCallback;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table meta data loader.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableMetaDataLoader {
    
    private final ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    private final ShardingExecuteEngine executeEngine;
    
    private final TableMetaDataConnectionManager connectionManager;
    
    private final int maxConnectionsSizePerQuery;
    
    /**
     * Load table meta data.
     *
     * @param logicTableName logic table name
     * @param shardingRule sharding rule
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName, final ShardingRule shardingRule) throws SQLException {
        List<TableMetaData> actualTableMetaDataList = load(shardingRule.getTableRuleByLogicTableName(logicTableName).getDataNodeGroups(), shardingRule.getShardingDataSourceNames());
        checkUniformed(logicTableName, actualTableMetaDataList);
        return actualTableMetaDataList.iterator().next();
    }
    
    private List<TableMetaData> load(final Map<String, List<DataNode>> dataNodeGroups, final ShardingDataSourceNames shardingDataSourceNames) throws SQLException {
        return executeEngine.groupExecute(getDataNodeGroups(dataNodeGroups), new ShardingGroupExecuteCallback<DataNode, TableMetaData>() {
            
            @Override
            public Collection<TableMetaData> execute(final Collection<DataNode> dataNodes, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) throws SQLException {
                String dataSourceName = dataNodes.iterator().next().getDataSourceName();
                DataSourceMetaData dataSourceMetaData = shardingDataSourceMetaData.getActualDataSourceMetaData(dataSourceName);
                String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getSchemeName();
                return load(shardingDataSourceNames.getRawMasterDataSourceName(dataSourceName), catalog, dataNodes);
            }
        });
    }
    
    private Collection<TableMetaData> load(final String dataSourceName, final String catalog, final Collection<DataNode> dataNodes) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        try (Connection connection = connectionManager.getConnection(dataSourceName)) {
            for (DataNode each : dataNodes) {
                result.add(new TableMetaData(
                        isTableExist(connection, catalog, each.getTableName()) ? getColumnMetaDataList(connection, catalog, each.getTableName()) : Collections.<ColumnMetaData>emptyList()));
            }
        }
        return result;
    }
    
    private Collection<ShardingExecuteGroup<DataNode>> getDataNodeGroups(final Map<String, List<DataNode>> dataNodeGroups) {
        Collection<ShardingExecuteGroup<DataNode>> result = new LinkedList<>();
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            result.addAll(getDataNodeGroups(entry.getValue()));
        }
        return result;
    }
    
    private Collection<ShardingExecuteGroup<DataNode>> getDataNodeGroups(final List<DataNode> dataNodes) {
        Collection<ShardingExecuteGroup<DataNode>> result = new LinkedList<>();
        for (List<DataNode> each : Lists.partition(dataNodes, Math.max(dataNodes.size() / maxConnectionsSizePerQuery, 1))) {
            result.add(new ShardingExecuteGroup<>(each));
        }
        return result;
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    private List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, catalog, actualTableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, null, actualTableName, null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String columnType = resultSet.getString("TYPE_NAME");
                result.add(new ColumnMetaData(columnName, columnType, primaryKeys.contains(columnName)));
            }
        }
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, null, actualTableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final List<TableMetaData> actualTableMetaDataList) {
        final TableMetaData sample = actualTableMetaDataList.iterator().next();
        for (TableMetaData each : actualTableMetaDataList) {
            if (!sample.equals(each)) {
                throw new ShardingException("Cannot get uniformed table structure for `%s`. The different meta data of actual tables are as follows:\n%s\n%s.", logicTableName, sample, each);
            }
        }
    }
}
