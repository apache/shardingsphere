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

package org.apache.shardingsphere.sharding.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table meta data builder for sharding.
 */
public final class ShardingTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<ShardingRule> {
    
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    private static final int FUTURE_GET_TIME_OUT_SECOND = 5;
    
    @Override
    public Optional<TableMetaData> load(final String tableName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                        final ShardingRule rule, final ConfigurationProperties props) throws SQLException {
        if (!rule.findTableRule(tableName).isPresent()) {
            return Optional.empty();
        }
        boolean isCheckingMetaData = props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        int maxConnectionsSizePerQuery = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        TableRule tableRule = rule.getTableRule(tableName);
        if (!isCheckingMetaData) {
            DataNode dataNode = dataNodes.getDataNodes(tableName).iterator().next();
            return TableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        }
        Map<String, TableMetaData> actualTableMetaDataMap = parallelLoadTables(databaseType, dataSourceMap, dataNodes, tableName, maxConnectionsSizePerQuery);
        if (actualTableMetaDataMap.isEmpty()) {
            return Optional.empty();
        }
        checkUniformed(tableRule.getLogicTable(), actualTableMetaDataMap, rule);
        return Optional.of(actualTableMetaDataMap.values().iterator().next());
    }
    
    @Override
    public Optional<Map<String, TableMetaData>> load(final Collection<String> tableNames, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                        final ShardingRule rule, final ConfigurationProperties props, final ExecutorService executorService) throws SQLException {
        if (!tableNames.stream().allMatch(tableName -> rule.findTableRule(tableName).isPresent())) {
            return Optional.empty();
        }
        boolean isCheckingMetaData = props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        return isCheckingMetaData ? loadWithCheck(tableNames, databaseType, dataSourceMap, dataNodes, rule, props)
                : loadWithOutCheck(tableNames, databaseType, dataSourceMap, dataNodes, rule, executorService);
    }
    
    private Optional<Map<String, TableMetaData>> loadWithCheck(final Collection<String> tableNames, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                               final DataNodes dataNodes, final ShardingRule rule, final ConfigurationProperties props) {
        int maxConnectionsSizePerQuery = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Map<String, TableMetaData> result = new HashMap<>();
        for (String tableName : tableNames) {
            TableRule tableRule = rule.getTableRule(tableName);
            Map<String, TableMetaData> actualTableMetaDataMap = parallelLoadTables(databaseType, dataSourceMap, dataNodes, tableName, maxConnectionsSizePerQuery);
            if (actualTableMetaDataMap.isEmpty()) {
                return Optional.empty();
            }
            checkUniformed(tableRule.getLogicTable(), actualTableMetaDataMap, rule);
            result.put(tableRule.getLogicTable(), actualTableMetaDataMap.values().iterator().next());
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Optional<Map<String, TableMetaData>> loadWithOutCheck(final Collection<String> tableNames, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                                  final DataNodes dataNodes, final ShardingRule rule, final ExecutorService executorService) throws SQLException {
        Optional<DialectTableMetaDataLoader> loader = findDialectTableMetaDataLoader(databaseType);
        return loader.isPresent() ? loadByDialect(loader.get(), tableNames, dataSourceMap, dataNodes, rule, executorService)
                : loadByDefault(tableNames, dataNodes, dataSourceMap, databaseType);
    }
    
    private Optional<Map<String, TableMetaData>> loadByDialect(final DialectTableMetaDataLoader loader, final Collection<String> tableNames, final Map<String, DataSource> dataSourceMap,
                                                               final DataNodes dataNodes, final ShardingRule rule, final ExecutorService executorService) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Map<String, List<DataNode>> dataNodeMap = tableNames.stream()
                .map(tableName -> dataNodes.getDataNodes(tableName).iterator().next())
                .collect(Collectors.groupingBy(DataNode :: getDataSourceName));
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (Entry<String, List<DataNode>> each : dataNodeMap.entrySet()) {
            futures.add(executorService.submit(() -> loader.load(dataSourceMap.get(each.getKey()),
                            each.getValue().stream().map(DataNode::getTableName).collect(Collectors.toList()), false)));
        }
        try {
            for (Future<Map<String, TableMetaData>> each : futures) {
                Map<String, TableMetaData> map = each.get();
                result.putAll(getLogic2ActualMap(map.values(), rule));
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Optional<Map<String, TableMetaData>> loadByDefault(final Collection<String> tableNames, final DataNodes dataNodes,
                                                               final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            DataNode dataNode = dataNodes.getDataNodes(tableName).iterator().next();
            TableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType).ifPresent(tableMetaData -> result.put(tableName, tableMetaData));
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Map<String, TableMetaData> getLogic2ActualMap(final Collection<TableMetaData> tableMetaDatas, final ShardingRule rule) {
        return tableMetaDatas.stream().collect(
                Collectors.toMap(tableMetaData -> rule.findLogicTableByActualTable(tableMetaData.getName()).get(), Function.identity(), (oldValue, newValue) -> oldValue));
    }
    
    private static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final DatabaseType databaseType) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(databaseType.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Map<String, TableMetaData> parallelLoadTables(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                                          final String tableName, final int maxConnectionsSizePerQuery) {
        Map<String, List<DataNode>> dataNodeGroups = dataNodes.getDataNodeGroups(tableName);
        Map<String, TableMetaData> result = new HashMap<>(dataNodeGroups.size(), 1);
        Map<String, Future<Optional<TableMetaData>>> tableFutureMap = new HashMap<>(dataNodeGroups.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataNodeGroups.size() * maxConnectionsSizePerQuery));
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Future<Optional<TableMetaData>> futures = executorService.submit(() -> loadTableByDataNode(each, databaseType, dataSourceMap));
                tableFutureMap.put(each.getTableName(), futures);
            }
        }
        tableFutureMap.forEach((key, value) -> {
            try {
                getTableMetaData(value).ifPresent(tableMetaData -> result.put(key, tableMetaData));
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                executorService.shutdownNow();
                throw new IllegalStateException(String.format("Error while fetching tableMetaData with key= %s and Value=%s", key, value), ex);
            }
        });
        executorService.shutdownNow();
        return result;
    }
    
    private Optional<TableMetaData> getTableMetaData(final Future<Optional<TableMetaData>> value) throws InterruptedException, ExecutionException, TimeoutException {
        return value.get(FUTURE_GET_TIME_OUT_SECOND, TimeUnit.SECONDS);
    }
    
    private Optional<TableMetaData> loadTableByDataNode(final DataNode dataNode, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        try {
            return TableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        } catch (final SQLException ex) {
            throw new IllegalStateException(String.format("SQLException for DataNode=%s and databaseType=%s", dataNode, databaseType.getName()), ex);
        }
    }
    
    private void checkUniformed(final String logicTableName, final Map<String, TableMetaData> actualTableMetaDataMap, final ShardingRule shardingRule) {
        TableMetaData sample = decorate(logicTableName, actualTableMetaDataMap.values().iterator().next(), shardingRule);
        Collection<TableMetaDataViolation> violations = actualTableMetaDataMap.entrySet().stream()
                .filter(entry -> !sample.equals(decorate(logicTableName, entry.getValue(), shardingRule)))
                .map(entry -> new TableMetaDataViolation(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        throwExceptionIfNecessary(violations, logicTableName);
    }
    
    private void throwExceptionIfNecessary(final Collection<TableMetaDataViolation> violations, final String logicTableName) {
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(
                    "Cannot get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows:").append(System.lineSeparator());
            for (TableMetaDataViolation each : violations) {
                errorMessage.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
            }
            throw new ShardingSphereException(errorMessage.toString(), logicTableName);
        }
    }

    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final ShardingRule shardingRule) {
        return shardingRule.findTableRule(tableName).map(
            tableRule -> new TableMetaData(tableName, getColumnMetaDataList(tableMetaData, tableRule), getIndexMetaDataList(tableMetaData, tableRule))).orElse(tableMetaData);
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Optional<String> generateKeyColumn = tableRule.getGenerateKeyColumn();
        if (!generateKeyColumn.isPresent()) {
            return tableMetaData.getColumns().values();
        }
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (Entry<String, ColumnMetaData> entry : tableMetaData.getColumns().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(generateKeyColumn.get())) {
                result.add(new ColumnMetaData(entry.getValue().getName(), entry.getValue().getDataType(), entry.getValue().isPrimaryKey(), true, entry.getValue().isCaseSensitive()));
            } else {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    
    private Collection<IndexMetaData> getIndexMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Collection<IndexMetaData> result = new HashSet<>();
        for (Entry<String, IndexMetaData> entry : tableMetaData.getIndexes().entrySet()) {
            for (DataNode each : tableRule.getActualDataNodes()) {
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(logicIndex -> result.add(new IndexMetaData(logicIndex)));
            }
        }
        return result;
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? Optional.of(actualIndexName.replace(indexNameSuffix, "")) : Optional.empty();
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class TableMetaDataViolation {
        
        private final String actualTableName;
        
        private final TableMetaData tableMetaData;
    }
}
