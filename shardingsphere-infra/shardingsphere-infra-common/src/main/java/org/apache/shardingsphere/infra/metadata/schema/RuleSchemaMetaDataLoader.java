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

package org.apache.shardingsphere.infra.metadata.schema;

import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataLoader;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Rule schema meta data loader.
 */
@RequiredArgsConstructor
public final class RuleSchemaMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(RuleMetaDataLoader.class);
        ShardingSphereServiceLoader.register(RuleMetaDataDecorator.class);
    }
    
    private final Collection<ShardingSphereRule> rules;
    
    /**
     * Load rule schema meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param props configuration properties
     * @param executorService executor service
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RuleSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                   final ConfigurationProperties props, final ListeningExecutorService executorService) throws SQLException {
        Collection<String> excludedTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        SchemaMetaData configuredSchemaMetaData = new SchemaMetaData();
        for (Entry<ShardingSphereRule, RuleMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataLoader.class).entrySet()) {
            SchemaMetaData schemaMetaData = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), entry.getKey(), props, excludedTableNames);
            excludedTableNames.addAll(schemaMetaData.getAllTableNames());
            if (entry.getKey() instanceof DataNodeRoutedRule) {
                excludedTableNames.addAll(((DataNodeRoutedRule) entry.getKey()).getAllActualTables());
            }
            configuredSchemaMetaData.merge(schemaMetaData);
        }
        configuredSchemaMetaData = decorate(configuredSchemaMetaData);
        int maxConnectionCount = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Map<String, SchemaMetaData> unconfiguredSchemaMetaDataMap = executorService == null ? syncLoad(databaseType, dataSourceMap, maxConnectionCount, excludedTableNames)
                : asyncLoad(databaseType, dataSourceMap, executorService, maxConnectionCount, excludedTableNames);
        return new RuleSchemaMetaData(configuredSchemaMetaData, unconfiguredSchemaMetaDataMap);
    }
    
    /**
     * Load rule schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param props configuration properties
     * @param executorService executor service
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    public RuleSchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource, final ConfigurationProperties props,
                                   final ListeningExecutorService executorService) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return load(databaseType, dataSourceMap, props, executorService);
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param tableName table name
     * @param props configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<TableMetaData> load(final DatabaseType databaseType, 
                                        final Map<String, DataSource> dataSourceMap, final String tableName, final ConfigurationProperties props) throws SQLException {
        for (Entry<ShardingSphereRule, RuleMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataLoader.class).entrySet()) {
            Optional<TableMetaData> result = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), tableName, entry.getKey(), props);
            if (result.isPresent()) {
                return Optional.of(decorate(tableName, result.get()));
            }
        }
        return Optional.empty();
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param tableName table name
     * @param props configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public Optional<TableMetaData> load(final DatabaseType databaseType,
                                        final DataSource dataSource, final String tableName, final ConfigurationProperties props) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return load(databaseType, dataSourceMap, tableName, props);
    }
    
    private Map<String, SchemaMetaData> asyncLoad(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final ListeningExecutorService executorService,
                                                  final int maxConnectionCount, final Collection<String> excludedTableNames) {
        Map<String, SchemaMetaData> result = new ConcurrentHashMap<>(dataSourceMap.size(), 1);
        dataSourceMap.entrySet().stream().map(each -> executorService.submit(() -> {
            try {
                SchemaMetaData schemaMetaData = SchemaMetaDataLoader.load(each.getValue(), maxConnectionCount, databaseType.getName(), excludedTableNames);
                if (!schemaMetaData.getAllTableNames().isEmpty()) {
                    result.put(each.getKey(), schemaMetaData);
                }
            } catch (final SQLException ex) {
                throw new ShardingSphereException("RuleSchemaMetaData load failed", ex);
            }
        })).forEach(listenableFuture -> {
            try {
                listenableFuture.get();
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingSphereException("RuleSchemaMetaData load failed", ex);
            }
        });
        return result;
    }
    
    private Map<String, SchemaMetaData> syncLoad(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                 final int maxConnectionCount, final Collection<String> excludedTableNames) throws SQLException {
        Map<String, SchemaMetaData> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            SchemaMetaData schemaMetaData = SchemaMetaDataLoader.load(entry.getValue(), maxConnectionCount, databaseType.getName(), excludedTableNames);
            if (!schemaMetaData.getAllTableNames().isEmpty()) {
                result.put(entry.getKey(), schemaMetaData);
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private SchemaMetaData decorate(final SchemaMetaData schemaMetaData) {
        Map<String, TableMetaData> result = new HashMap<>(schemaMetaData.getAllTableNames().size(), 1);
        Map<ShardingSphereRule, RuleMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataDecorator.class);
        for (String each : schemaMetaData.getAllTableNames()) {
            for (Entry<ShardingSphereRule, RuleMetaDataDecorator> entry : decorators.entrySet()) {
                result.put(each, entry.getValue().decorate(each, result.getOrDefault(each, schemaMetaData.get(each)), entry.getKey()));
            }
        }
        return new SchemaMetaData(result);
    }
    
    @SuppressWarnings("unchecked")
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData) {
        return OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataDecorator.class).entrySet().stream()
                .map(entry -> entry.getValue().decorate(tableName, tableMetaData, entry.getKey())).reduce((first, second) -> second).orElse(tableMetaData);
    }
}
