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

package org.apache.shardingsphere.encrypt.metadata;

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;

import java.util.List;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Table meta data builder for encrypt.
 */
public final class EncryptTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<EncryptRule> {
    
    @Override
    public Optional<TableMetaData> load(final String tableName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                        final EncryptRule encryptRule, final ConfigurationProperties props) throws SQLException {
        String dataSourceName = dataNodes.getDataNodes(tableName).stream().map(DataNode::getDataSourceName).findFirst().orElseGet(() -> dataSourceMap.keySet().iterator().next());
        return encryptRule.findEncryptTable(tableName).isPresent() 
                ? TableMetaDataLoader.load(dataSourceMap.get(dataSourceName), tableName, databaseType) : Optional.empty();
    }
    
    @Override
    public Optional<Map<String, TableMetaData>> load(final Collection<String> tableNames, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                                     final EncryptRule rule, final ConfigurationProperties props, final ExecutorService executorService) throws SQLException {
        Optional<DialectTableMetaDataLoader> loader = findDialectTableMetaDataLoader(databaseType);
        return loader.isPresent() ? loadByDialect(loader.get(), tableNames, dataSourceMap, dataNodes, rule, executorService)
                : loadByDefault(tableNames, dataSourceMap, dataNodes, rule, databaseType);
    }
    
    private Optional<Map<String, TableMetaData>> loadByDialect(final DialectTableMetaDataLoader dialectTableMetaDataLoader, final Collection<String> tableNames,
                                                               final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes, final EncryptRule rule,
                                                               final ExecutorService executorService) throws SQLException {
        if (!tableNames.stream().allMatch(tableName -> rule.findEncryptTable(tableName).isPresent())) {
            return Optional.empty();
        }
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Map<String, List<DataNode>> dataNodeMap = tableNames.stream().map(tableName -> dataNodes.getDataNodes(tableName).iterator().next())
                .collect(Collectors.groupingBy(DataNode :: getDataSourceName));
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (Map.Entry<String, List<DataNode>> each : dataNodeMap.entrySet()) {
            futures.add(executorService.submit(() -> dialectTableMetaDataLoader
                    .load(dataSourceMap.get(each.getKey()),
                            each.getValue().stream().map(DataNode::getTableName).collect(Collectors.toList()), false)));
        }
        try {
            for (Future<Map<String, TableMetaData>> each : futures) {
                result.putAll(each.get());
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Optional<Map<String, TableMetaData>> loadByDefault(final Collection<String> tableNames, final Map<String, DataSource> dataSourceMap,
                                                               final DataNodes dataNodes, final EncryptRule rule, final DatabaseType databaseType) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            String dataSourceName = dataNodes.getDataNodes(tableName).stream().map(DataNode::getDataSourceName).findFirst().orElseGet(() -> dataSourceMap.keySet().iterator().next());
            Optional<TableMetaData> tableMetaDataOptional = rule.findEncryptTable(tableName).isPresent()
                    ? TableMetaDataLoader.load(dataSourceMap.get(dataSourceName), tableName, databaseType) : Optional.empty();
            tableMetaDataOptional.ifPresent(tableMetaData -> result.put(tableMetaData.getName(), tableMetaData));
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final DatabaseType databaseType) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(databaseType.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final EncryptRule encryptRule) {
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        return encryptTable.map(optional ->
                new TableMetaData(tableName, getEncryptColumnMetaDataList(optional, tableMetaData.getColumns().values()), tableMetaData.getIndexes().values())).orElse(tableMetaData);
    }
    
    private Collection<ColumnMetaData> getEncryptColumnMetaDataList(final EncryptTable encryptTable, final Collection<ColumnMetaData> originalColumnMetaDataList) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> plainColumns = encryptTable.getPlainColumns();
        Collection<String> assistedQueryColumns = encryptTable.getAssistedQueryColumns();
        for (ColumnMetaData each : originalColumnMetaDataList) {
            String columnName = each.getName();
            if (encryptTable.isCipherColumn(columnName)) {
                result.add(createColumnMetaData(encryptTable.getLogicColumn(columnName), each));
                continue;
            }
            if (!plainColumns.contains(columnName) && !assistedQueryColumns.contains(columnName)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private ColumnMetaData createColumnMetaData(final String columnName, final ColumnMetaData columnMetaData) {
        return new ColumnMetaData(columnName, columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), columnMetaData.isGenerated(), columnMetaData.isCaseSensitive());
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
