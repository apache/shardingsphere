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

package org.apache.shardingsphere.underlying.common.metadata.schema;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.metadata.schema.spi.RuleMetaDataDecorator;
import org.apache.shardingsphere.underlying.common.metadata.schema.spi.RuleMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.common.rule.DataNodes;
import org.apache.shardingsphere.underlying.common.rule.DataNodeRoutedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;

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
     * @param properties configuration properties
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public RuleSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final ConfigurationProperties properties) throws SQLException {
        Collection<String> excludedTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        SchemaMetaData configuredSchemaMetaData = new SchemaMetaData(new HashMap<>());
        for (Entry<ShardingSphereRule, RuleMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataLoader.class).entrySet()) {
            SchemaMetaData schemaMetaData = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), entry.getKey(), properties, excludedTableNames);
            excludedTableNames.addAll(schemaMetaData.getAllTableNames());
            if (entry.getKey() instanceof DataNodeRoutedRule) {
                excludedTableNames.addAll(((DataNodeRoutedRule) entry.getKey()).getAllActualTables());
            }
            configuredSchemaMetaData.merge(schemaMetaData);
        }
        configuredSchemaMetaData = decorate(configuredSchemaMetaData);
        Map<String, SchemaMetaData> unconfiguredSchemaMetaDataMap = new HashMap<>(dataSourceMap.size(), 1);
        int maxConnectionCount = properties.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        // TODO use multiple threads for different data sources
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            SchemaMetaData schemaMetaData = SchemaMetaDataLoader.load(entry.getValue(), maxConnectionCount, databaseType.getName(), excludedTableNames);
            if (!schemaMetaData.getAllTableNames().isEmpty()) {
                unconfiguredSchemaMetaDataMap.put(entry.getKey(), schemaMetaData);
            }
        }
        return new RuleSchemaMetaData(configuredSchemaMetaData, unconfiguredSchemaMetaDataMap);
    }
    
    /**
     * Load rule schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param properties configuration properties
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    public RuleSchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, properties);
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param tableName table name
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public Optional<TableMetaData> load(final DatabaseType databaseType, 
                                        final Map<String, DataSource> dataSourceMap, final String tableName, final ConfigurationProperties properties) throws SQLException {
        for (Entry<ShardingSphereRule, RuleMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataLoader.class).entrySet()) {
            Optional<TableMetaData> result = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), tableName, entry.getKey(), properties);
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
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public Optional<TableMetaData> load(final DatabaseType databaseType,
                                        final DataSource dataSource, final String tableName, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, tableName, properties);
    }
    
    @SuppressWarnings("unchecked")
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
        TableMetaData result = tableMetaData;
        for (Entry<ShardingSphereRule, RuleMetaDataDecorator> entry : OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataDecorator.class).entrySet()) {
            result = entry.getValue().decorate(tableName, tableMetaData, entry.getKey());
        }
        return result;
    }
}
