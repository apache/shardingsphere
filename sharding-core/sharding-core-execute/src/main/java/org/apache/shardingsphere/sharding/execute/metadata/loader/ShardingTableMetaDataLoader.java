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

package org.apache.shardingsphere.sharding.execute.metadata.loader;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.column.ShardingGeneratedKeyColumnMetaData;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.executor.engine.ExecutorEngine;
import org.apache.shardingsphere.underlying.executor.engine.GroupedCallback;
import org.apache.shardingsphere.underlying.executor.engine.InputGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table meta data loader for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ShardingTableMetaDataLoader implements TableMetaDataLoader<ShardingRule> {
    
    private static final String INDEX_NAME = "INDEX_NAME";
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ExecutorEngine executorEngine;
    
    private final ConnectionManager connectionManager;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean isCheckingMetaData;
    
    private final ColumnMetaDataLoader columnMetaDataLoader = new ColumnMetaDataLoader();
    
    @Override
    public TableMetaData load(final String logicTableName, final ShardingRule shardingRule) throws SQLException {
        final String generateKeyColumnName = shardingRule.findGenerateKeyColumnName(logicTableName).orNull();
        List<TableMetaData> actualTableMetaDataList = load(getDataNodeGroups(shardingRule.getTableRule(logicTableName)), shardingRule, generateKeyColumnName);
        checkUniformed(logicTableName, actualTableMetaDataList);
        return actualTableMetaDataList.iterator().next();
    }
    
    private List<TableMetaData> load(final Map<String, List<DataNode>> dataNodeGroups, final ShardingRule shardingRule, final String generateKeyColumnName) throws SQLException {
        return executorEngine.execute(getDataNodeInputGroups(dataNodeGroups), new GroupedCallback<DataNode, TableMetaData>() {
            
            @Override
            public Collection<TableMetaData> execute(final Collection<DataNode> dataNodes, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
                String masterDataSourceName = shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(dataNodes.iterator().next().getDataSourceName());
                DataSourceMetaData dataSourceMetaData = ShardingTableMetaDataLoader.this.dataSourceMetas.getDataSourceMetaData(masterDataSourceName);
                return load(masterDataSourceName, dataSourceMetaData, dataNodes, generateKeyColumnName);
            }
        });
    }
    
    private Collection<TableMetaData> load(final String dataSourceName, 
                                           final DataSourceMetaData dataSourceMetaData, final Collection<DataNode> dataNodes, final String generateKeyColumnName) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        try (Connection connection = connectionManager.getConnection(dataSourceName)) {
            for (DataNode each : dataNodes) {
                result.add(createTableMetaData(connection, dataSourceMetaData, each.getTableName(), generateKeyColumnName));
            }
        }
        return result;
    }
    
    private Map<String, List<DataNode>> getDataNodeGroups(final TableRule tableRule) {
        return isCheckingMetaData ? tableRule.getDataNodeGroups() : getFirstDataNodeGroups(tableRule);
    }
    
    private Map<String, List<DataNode>> getFirstDataNodeGroups(final TableRule tableRule) {
        DataNode firstDataNode = tableRule.getActualDataNodes().get(0);
        return Collections.singletonMap(firstDataNode.getDataSourceName(), Collections.singletonList(firstDataNode));
    }
    
    private Collection<InputGroup<DataNode>> getDataNodeInputGroups(final Map<String, List<DataNode>> dataNodeGroups) {
        Collection<InputGroup<DataNode>> result = new LinkedList<>();
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            result.addAll(getDataNodeInputGroups(entry.getValue()));
        }
        return result;
    }
    
    private Collection<InputGroup<DataNode>> getDataNodeInputGroups(final List<DataNode> dataNodes) {
        Collection<InputGroup<DataNode>> result = new LinkedList<>();
        for (List<DataNode> each : Lists.partition(dataNodes, Math.max(dataNodes.size() / maxConnectionsSizePerQuery, 1))) {
            result.add(new InputGroup<>(each));
        }
        return result;
    }
    
    private TableMetaData createTableMetaData(final Connection connection, 
                                              final DataSourceMetaData dataSourceMetaData, final String actualTableName, final String generateKeyColumnName) throws SQLException {
        String catalog = dataSourceMetaData.getCatalog();
        String schema = dataSourceMetaData.getSchema();
        return isTableExist(connection, catalog, actualTableName)
                ? new TableMetaData(getColumnMetaDataList(connection, catalog, actualTableName, generateKeyColumnName), getLogicIndexes(connection, catalog, schema, actualTableName))
                : new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptySet());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String actualTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, actualTableName, null)) {
            return resultSet.next();
        }
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String catalog, final String actualTableName, final String generateKeyColumnName) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : columnMetaDataLoader.load(connection, catalog, actualTableName)) {
            result.add(filterColumnMetaData(each, generateKeyColumnName));
        }
        return result;
    }
    
    private ColumnMetaData filterColumnMetaData(final ColumnMetaData columnMetaData, final String generateKeyColumnName) {
        return columnMetaData.getName().equalsIgnoreCase(generateKeyColumnName) 
                ? new ShardingGeneratedKeyColumnMetaData(columnMetaData.getName(), columnMetaData.getDataType(), columnMetaData.isPrimaryKey()) : columnMetaData;
    }
    
    private Collection<String> getLogicIndexes(final Connection connection, final String catalog, final String schema, final String actualTableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(catalog, schema, actualTableName, false, false)) {
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
        if (null == actualIndexName) {
            return Optional.absent();
        }
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
                throw new ShardingSphereException("Cannot get uniformed table structure for `%s`. The different meta data of actual tables are as follows:\n%s\n%s.", logicTableName, sample, each);
            }
        }
    }
    
    @Override
    public TableMetas loadAll(final ShardingRule shardingRule) throws SQLException {
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
        List<TableMetaData> tableMetaDataList = getTableMetaDataList(shardingRule, tableNames);
        return loadDefaultTables(tableNames, tableMetaDataList);
    }
    
    private Map<String, TableMetaData> loadDefaultTables(final Collection<String> tableNames, final List<TableMetaData> tableMetaDataList) {
        Map<String, TableMetaData> result = new HashMap<>(tableNames.size(), 1);
        Iterator<String> tabNameIterator = tableNames.iterator();
        for (TableMetaData each : tableMetaDataList) {
            result.put(tabNameIterator.next(), each);
        }
        return result;
    }
    
    private Collection<String> loadAllTableNames(final String dataSourceName) throws SQLException {
        DataSourceMetaData dataSourceMetaData = dataSourceMetas.getDataSourceMetaData(dataSourceName);
        String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getCatalog();
        String schemaName = null == dataSourceMetaData ? null : dataSourceMetaData.getSchema();
        return loadAllTableNames(dataSourceName, catalog, schemaName);
    }
    
    private Collection<String> loadAllTableNames(final String dataSourceName, final String catalog, final String schemaName) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        try (Connection connection = connectionManager.getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(catalog, schemaName, null, new String[]{"TABLE"})) {
            result.addAll(loadTableName(resultSet));
        }
        return result;
    }
    
    private Collection<String> loadTableName(final ResultSet resultSet) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            if (!tableName.contains("$") && !tableName.contains("/")) {
                result.add(tableName);
            }
        }
        return result;
    }
    
    private List<TableMetaData> getTableMetaDataList(final ShardingRule shardingRule, final Collection<String> tableNames) throws SQLException {
        Map<String, List<DataNode>> result = new LinkedHashMap<>();
        String defaultDataSource = shardingRule.getShardingDataSourceNames().getDefaultDataSourceName();
        result.put(defaultDataSource, new LinkedList<DataNode>());
        for (String each : tableNames) {
            result.get(defaultDataSource).addAll(getDataNodeGroups(shardingRule.getTableRule(each)).get(defaultDataSource));
        }
        return load(result, shardingRule, null);
    }
}
