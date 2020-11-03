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

package org.apache.shardingsphere.infra.metadata.model.logic;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.model.logic.spi.LogicMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.model.logic.spi.LogicMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

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
public final class LogicSchemaMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(LogicMetaDataLoader.class);
        ShardingSphereServiceLoader.register(LogicMetaDataDecorator.class);
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
    public LogicSchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                    final ConfigurationProperties props) throws SQLException {
        Collection<String> excludedTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        PhysicalSchemaMetaData configuredSchemaMetaData = new PhysicalSchemaMetaData();
        for (Entry<ShardingSphereRule, LogicMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, LogicMetaDataLoader.class).entrySet()) {
            PhysicalSchemaMetaData schemaMetaData = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), entry.getKey(), props, excludedTableNames);
            excludedTableNames.addAll(schemaMetaData.getAllTableNames());
            if (entry.getKey() instanceof DataNodeRoutedRule) {
                excludedTableNames.addAll(((DataNodeRoutedRule) entry.getKey()).getAllActualTables());
            }
            configuredSchemaMetaData.merge(schemaMetaData);
        }
        decorate(configuredSchemaMetaData);
        return new LogicSchemaMetaData(configuredSchemaMetaData);
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
    public LogicSchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource,
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
    public Optional<PhysicalTableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                final String tableName, final ConfigurationProperties props) throws SQLException {
        for (Entry<ShardingSphereRule, LogicMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, LogicMetaDataLoader.class).entrySet()) {
            Optional<PhysicalTableMetaData> result = entry.getValue().load(databaseType, dataSourceMap, new DataNodes(rules), tableName, entry.getKey(), props);
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
    public Optional<PhysicalTableMetaData> load(final DatabaseType databaseType, final DataSource dataSource,
                                                final String tableName, final ConfigurationProperties props) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DefaultSchema.LOGIC_NAME, dataSource);
        return load(databaseType, dataSourceMap, tableName, props);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void decorate(final PhysicalSchemaMetaData schemaMetaData) {
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(schemaMetaData.getAllTableNames().size(), 1);
        Map<ShardingSphereRule, LogicMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, LogicMetaDataDecorator.class);
        for (String each : schemaMetaData.getAllTableNames()) {
            for (Entry<ShardingSphereRule, LogicMetaDataDecorator> entry : decorators.entrySet()) {
                tableMetaDataMap.put(each, entry.getValue().decorate(each, tableMetaDataMap.getOrDefault(each, schemaMetaData.get(each)), entry.getKey()));
            }
        }
        schemaMetaData.merge(new PhysicalSchemaMetaData(tableMetaDataMap));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private PhysicalTableMetaData decorate(final String tableName, final PhysicalTableMetaData tableMetaData) {
        Map<ShardingSphereRule, LogicMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, LogicMetaDataDecorator.class);
        PhysicalTableMetaData result = null;
        for (Entry<ShardingSphereRule, LogicMetaDataDecorator> entry : decorators.entrySet()) {
            result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, entry.getKey());
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
