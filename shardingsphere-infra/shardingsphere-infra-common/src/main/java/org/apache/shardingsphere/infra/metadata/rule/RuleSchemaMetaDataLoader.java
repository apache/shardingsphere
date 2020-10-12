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

package org.apache.shardingsphere.infra.metadata.rule;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.rule.spi.RuleMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.rule.spi.RuleMetaDataLoader;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.metadata.database.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.model.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.model.table.TableMetaData;

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
     * @param props configuration properties
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RuleSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                   final ConfigurationProperties props) throws SQLException {
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
        decorate(configuredSchemaMetaData);
        Map<String, Collection<String>> unConfiguredSchemaMetaDataMap = loadUnConfiguredSchemaMetaData(databaseType, dataSourceMap, excludedTableNames);
        return new RuleSchemaMetaData(configuredSchemaMetaData, unConfiguredSchemaMetaDataMap);
    }
    
    /**
     * Load rule schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param props configuration properties
     * @return rule schema meta data
     * @throws SQLException SQL exception
     */
    public RuleSchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource, 
                                   final ConfigurationProperties props) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return load(databaseType, dataSourceMap, props);
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
    public Optional<TableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                        final String tableName, final ConfigurationProperties props) throws SQLException {
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
    public Optional<TableMetaData> load(final DatabaseType databaseType, final DataSource dataSource, 
                                        final String tableName, final ConfigurationProperties props) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return load(databaseType, dataSourceMap, tableName, props);
    }
    
    private Map<String, Collection<String>> loadUnConfiguredSchemaMetaData(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                                       final Collection<String> excludedTableNames) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Collection<String> tableNames = SchemaMetaDataLoader.loadUnconfiguredTableNames(entry.getValue(), databaseType, excludedTableNames);
            if (!tableNames.isEmpty()) {
                result.put(entry.getKey(), tableNames);
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void decorate(final SchemaMetaData schemaMetaData) {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(schemaMetaData.getAllTableNames().size(), 1);
        Map<ShardingSphereRule, RuleMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataDecorator.class);
        for (String each : schemaMetaData.getAllTableNames()) {
            for (Entry<ShardingSphereRule, RuleMetaDataDecorator> entry : decorators.entrySet()) {
                tableMetaDataMap.put(each, entry.getValue().decorate(each, tableMetaDataMap.getOrDefault(each, schemaMetaData.get(each)), entry.getKey()));
            }
        }
        schemaMetaData.merge(new SchemaMetaData(tableMetaDataMap));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData) {
        Map<ShardingSphereRule, RuleMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, RuleMetaDataDecorator.class);
        TableMetaData result = null;
        for (Entry<ShardingSphereRule, RuleMetaDataDecorator> entry : decorators.entrySet()) {
            result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, entry.getKey());
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
