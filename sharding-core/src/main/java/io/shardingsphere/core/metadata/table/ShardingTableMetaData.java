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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.exception.ShardingException;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Sharding table meta data.
 *
 * @author panjuan
 * @author zhaojun
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class ShardingTableMetaData {
    
    private final ListeningExecutorService executorService;
    
    private final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
    
    /**
     * Initialize sharding meta data.
     *
     * @param shardingRule sharding rule
     */
    public void init(final ShardingRule shardingRule) {
        try {
            for (TableRule each : getTableRules(shardingRule)) {
                refresh(each, shardingRule);
            }
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private Collection<TableRule> getTableRules(final ShardingRule shardingRule) throws SQLException {
        Collection<TableRule> result = new LinkedList<>(shardingRule.getTableRules());
        result.addAll(getDefaultTableRules(shardingRule));
        return result;
    }
    
    private Collection<TableRule> getDefaultTableRules(final ShardingRule shardingRule) throws SQLException {
        Optional<String> defaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        if (!defaultDataSourceName.isPresent()) {
            return Collections.emptyList();
        }
        Collection<TableRule> result = new LinkedList<>();
        for (String each : getAllTableNames(defaultDataSourceName.get())) {
            result.add(shardingRule.getTableRuleByLogicTableName(each));
        }
        return result;
    }
    
    private Collection<String> getAllTableNames(final String dataSourceName) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (Connection connection = getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return result;
    }
    
    protected abstract Connection getConnection(String dataSourceName) throws SQLException;
    
    /**
     * Refresh table meta data.
     *
     * @param tableRule table rule
     * @param shardingRule sharding rule
     */
    public void refresh(final TableRule tableRule, final ShardingRule shardingRule) {
        refresh(tableRule, shardingRule, Collections.<String, Connection>emptyMap());
    }
    
    /**
     * Refresh table meta data.
     *
     * @param tableRule table rule
     * @param shardingRule sharding rule
     * @param connectionMap connection map passing from sharding connection
     */
    public void refresh(final TableRule tableRule, final ShardingRule shardingRule, final Map<String, Connection> connectionMap) {
        tableMetaDataMap.put(tableRule.getLogicTable(), loadTableMetaData(tableRule, shardingRule.getShardingDataSourceNames(), connectionMap));
    }
    
    private TableMetaData loadTableMetaData(final TableRule tableRule, final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) {
        List<TableMetaData> actualTableMetaDataList = loadActualTableMetaDataList(tableRule.getActualDataNodes(), shardingDataSourceNames, connectionMap);
        checkUniformed(tableRule.getLogicTable(), actualTableMetaDataList);
        return actualTableMetaDataList.iterator().next();
    }
    
    protected abstract TableMetaData loadTableMetaData(DataNode dataNode, Map<String, Connection> connectionMap) throws SQLException;
    
    private List<TableMetaData> loadActualTableMetaDataList(final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) {
        List<ListenableFuture<TableMetaData>> result = new LinkedList<>();
        for (final DataNode each : actualDataNodes) {
            result.add(executorService.submit(new Callable<TableMetaData>() {
                
                @Override
                public TableMetaData call() throws SQLException {
                    return loadTableMetaData(new DataNode(shardingDataSourceNames.getRawMasterDataSourceName(each.getDataSourceName()), each.getTableName()), connectionMap);
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
    
    protected boolean isTableExist(final Connection connection, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    protected List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String actualTableName) throws SQLException {
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
