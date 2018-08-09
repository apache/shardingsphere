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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Table meta data loader.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableMetaDataLoader {
    
    private final ListeningExecutorService executorService;
    
    private final TableMetaDataConnectionManager connectionManager;
    
    /**
     * Load table meta data.
     *
     * @param logicTableName logic table name
     * @param shardingRule sharding rule
     * @return table meta data
     */
    public TableMetaData load(final String logicTableName, final ShardingRule shardingRule) {
        return load(shardingRule.getTableRuleByLogicTableName(logicTableName), shardingRule.getShardingDataSourceNames());
    }
    
    private TableMetaData load(final TableRule tableRule, final ShardingDataSourceNames shardingDataSourceNames) {
        List<TableMetaData> actualTableMetaDataList = loadActualTableMetaDataList(tableRule.getActualDataNodes(), shardingDataSourceNames);
        checkUniformed(tableRule.getLogicTable(), actualTableMetaDataList);
        return actualTableMetaDataList.iterator().next();
    }
    
    private TableMetaData load(final DataNode dataNode) throws SQLException {
        if (connectionManager.isAutoClose()) {
            try (Connection connection = connectionManager.getConnection(dataNode.getDataSourceName())) {
                return load(connection, dataNode);
            }
        }
        return load(connectionManager.getConnection(dataNode.getDataSourceName()), dataNode);
    }
    
    private TableMetaData load(final Connection connection, final DataNode dataNode) throws SQLException {
        return new TableMetaData(isTableExist(connection, dataNode.getTableName()) ? getColumnMetaDataList(connection, dataNode.getTableName()) : Collections.<ColumnMetaData>emptyList());
    }
    
    private boolean isTableExist(final Connection connection, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    private List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String actualTableName) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, actualTableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, actualTableName, null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String columnType = resultSet.getString("TYPE_NAME");
                result.add(new ColumnMetaData(columnName, columnType, primaryKeys.contains(columnName)));
            }
        }
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String actualTableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null, null, actualTableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
    
    private List<TableMetaData> loadActualTableMetaDataList(final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames) {
        List<ListenableFuture<TableMetaData>> result = new LinkedList<>();
        for (final DataNode each : actualDataNodes) {
            result.add(executorService.submit(new Callable<TableMetaData>() {
                
                @Override
                public TableMetaData call() throws SQLException {
                    return load(new DataNode(shardingDataSourceNames.getRawMasterDataSourceName(each.getDataSourceName()), each.getTableName()));
                }
            }));
        }
        try {
            return Futures.allAsList(result).get();
        } catch (final InterruptedException | ExecutionException ex) {
            throw new ShardingException(ex);
        }
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
