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
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.ShardingGroupExecuteCallback;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.column.EncryptColumnMetaData;
import org.apache.shardingsphere.core.metadata.column.ShardingGeneratedKeyColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;

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
 * @author panjuan
 */
@RequiredArgsConstructor
public final class TableMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String INDEX_NAME = "INDEX_NAME";
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ShardingExecuteEngine executeEngine;
    
    private final TableMetaDataConnectionManager connectionManager;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean isCheckingMetaData;
    
    /**
     * Load table meta data.
     *
     * @param logicTableName logic table name
     * @param shardingRule sharding rule
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName, final ShardingRule shardingRule) throws SQLException {
        List<TableMetaData> actualTableMetaDataList = load(getDataNodeGroups(shardingRule.getTableRule(logicTableName)), shardingRule, logicTableName);
        checkUniformed(logicTableName, actualTableMetaDataList);
        return actualTableMetaDataList.iterator().next();
    }
    
    private List<TableMetaData> load(final Map<String, List<DataNode>> dataNodeGroups, final ShardingRule shardingRule, final String logicTableName) throws SQLException {
        final String generateKeyColumnName = shardingRule.findGenerateKeyColumnName(logicTableName).orNull();
        return executeEngine.groupExecute(getDataNodeExecuteGroups(dataNodeGroups), new ShardingGroupExecuteCallback<DataNode, TableMetaData>() {
            
            @Override
            public Collection<TableMetaData> execute(final Collection<DataNode> dataNodes, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) throws SQLException {
                String dataSourceName = dataNodes.iterator().next().getDataSourceName();
                DataSourceMetaData dataSourceMetaData = TableMetaDataLoader.this.dataSourceMetas.getDataSourceMetaData(dataSourceName);
                String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getSchemaName();
                return load(shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(dataSourceName), 
                        catalog, logicTableName, dataNodes, generateKeyColumnName, shardingRule.getEncryptRule());
            }
        });
    }
    
    private Collection<TableMetaData> load(final String dataSourceName, final String catalog, 
                                           final String logicTableName, final Collection<DataNode> dataNodes, final String generateKeyColumnName, final EncryptRule encryptRule) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        try (Connection connection = connectionManager.getConnection(dataSourceName)) {
            for (DataNode each : dataNodes) {
                result.add(createTableMetaData(connection, catalog, logicTableName, each.getTableName(), generateKeyColumnName, encryptRule));
            }
        }
        return result;
    }
    
    private Map<String, List<DataNode>> getDataNodeGroups(final TableRule tableRule) {
        return isCheckingMetaData ? tableRule.getDataNodeGroups() : getFirstDataNodeWithGroups(tableRule);
    }
    
    private Map<String, List<DataNode>> getFirstDataNodeWithGroups(final TableRule tableRule) {
        DataNode firstDataNode = tableRule.getActualDataNodes().iterator().next();
        return Collections.singletonMap(firstDataNode.getDataSourceName(), Collections.singletonList(firstDataNode));
    }
    
    private Collection<ShardingExecuteGroup<DataNode>> getDataNodeExecuteGroups(final Map<String, List<DataNode>> dataNodeGroups) {
        Collection<ShardingExecuteGroup<DataNode>> result = new LinkedList<>();
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            result.addAll(getDataNodeExecuteGroups(entry.getValue()));
        }
        return result;
    }
    
    private Collection<ShardingExecuteGroup<DataNode>> getDataNodeExecuteGroups(final List<DataNode> dataNodes) {
        Collection<ShardingExecuteGroup<DataNode>> result = new LinkedList<>();
        for (List<DataNode> each : Lists.partition(dataNodes, Math.max(dataNodes.size() / maxConnectionsSizePerQuery, 1))) {
            result.add(new ShardingExecuteGroup<>(each));
        }
        return result;
    }
    
    private TableMetaData createTableMetaData(final Connection connection, final String catalog, 
                                              final String logicTableName, final String actualTableName, final String generateKeyColumnName, final EncryptRule encryptRule) throws SQLException {
        if (isTableExist(connection, catalog, actualTableName)) {
            return new TableMetaData(
                    getColumnMetaDataList(connection, catalog, logicTableName, actualTableName, generateKeyColumnName, encryptRule), getLogicIndexes(connection, catalog, actualTableName));
        }
        return new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptySet());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String catalog, final String logicTableName, final String actualTableName, 
                                                       final String generateKeyColumnName, final EncryptRule encryptRule) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, catalog, actualTableName);
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(logicTableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, null, actualTableName, "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString(COLUMN_NAME);
                String columnType = resultSet.getString(TYPE_NAME);
                boolean isPrimaryKey = primaryKeys.contains(columnName);
                Optional<ColumnMetaData> columnMetaData = getColumnMetaData(logicTableName, columnName, columnType, isPrimaryKey, generateKeyColumnName, encryptRule, derivedColumns);
                if (columnMetaData.isPresent()) {
                    result.add(columnMetaData.get());
                }
            }
        }
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, null, actualTableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
    
    private Optional<ColumnMetaData> getColumnMetaData(final String logicTableName, final String columnName, final String columnType, final boolean isPrimaryKey,
                                                       final String generateKeyColumnName, final EncryptRule encryptRule, final Collection<String> derivedColumns) {
        if (derivedColumns.contains(columnName)) {
            return Optional.absent();
        }
        if (encryptRule.isCipherColumn(logicTableName, columnName)) {
            String logicColumnName = encryptRule.getLogicColumn(logicTableName, columnName);
            String plainColumnName = encryptRule.getPlainColumn(logicTableName, logicColumnName).orNull();
            String assistedQueryColumnName = encryptRule.getAssistedQueryColumn(logicTableName, logicColumnName).orNull();
            return Optional.<ColumnMetaData>of(new EncryptColumnMetaData(logicColumnName, columnType, isPrimaryKey, columnName, plainColumnName, assistedQueryColumnName));
        }
        if (columnName.equalsIgnoreCase(generateKeyColumnName)) {
            return Optional.<ColumnMetaData>of(new ShardingGeneratedKeyColumnMetaData(columnName, columnType, isPrimaryKey));
        }
        return Optional.of(new ColumnMetaData(columnName, columnType, isPrimaryKey));
    }
    
    private Collection<String> getLogicIndexes(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(catalog, catalog, actualTableName, false, false)) {
            while (resultSet.next()) {
                Optional<String> logicIndex = getLogicIndex(resultSet.getString(INDEX_NAME), actualTableName);
                if (logicIndex.isPresent()) {
                    result.add(logicIndex.get());
                }
            }
        }
        return result;
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.contains(indexNameSuffix) ? Optional.of(actualIndexName.replace(indexNameSuffix, "")) : Optional.<String>absent();
    }
    
    private void checkUniformed(final String logicTableName, final List<TableMetaData> actualTableMetaDataList) {
        if (!isCheckingMetaData) {
            return;
        }
        TableMetaData sample = actualTableMetaDataList.iterator().next();
        for (TableMetaData each : actualTableMetaDataList) {
            if (!sample.equals(each)) {
                throw new ShardingException("Cannot get uniformed table structure for `%s`. The different meta data of actual tables are as follows:\n%s\n%s.", logicTableName, sample, each);
            }
        }
    }
}
