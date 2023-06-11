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

package org.apache.shardingsphere.single.decorator;

import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.exception.InvalidSingleRuleConfigurationException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single rule configuration decorator.
 */
public final class SingleRuleConfigurationDecorator implements RuleConfigurationDecorator<SingleRuleConfiguration> {
    
    @Override
    public SingleRuleConfiguration decorate(final String databaseName, final Map<String, DataSource> dataSources,
                                            final Collection<ShardingSphereRule> builtRules, final SingleRuleConfiguration ruleConfig) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.getTables().addAll(decorateTables(databaseName, dataSources, new LinkedList<>(builtRules), ruleConfig.getTables()));
        ruleConfig.getDefaultDataSource().ifPresent(result::setDefaultDataSource);
        return result;
    }
    
    private Collection<String> decorateTables(final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules, final Collection<String> tables) {
        builtRules.removeIf(SingleRule.class::isInstance);
        if (tables.isEmpty() && builtRules.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        Map<String, DataSource> aggregatedDataSources = SingleTableLoadUtils.getAggregatedDataSourceMap(enabledDataSources, builtRules);
        DatabaseType databaseType = DatabaseTypeEngine.getStorageType(enabledDataSources.values());
        Collection<String> excludedTables = SingleTableLoadUtils.getLoadedTables(builtRules);
        Map<String, Collection<DataNode>> physicalTables = loadPhysicalTables(databaseName, databaseType, aggregatedDataSources, excludedTables);
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(tables);
        Collection<DataNode> configuredDataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, splitTables);
        checkRuleConfiguration(databaseName, aggregatedDataSources, excludedTables, configuredDataNodes);
        boolean isSchemaSupportedDatabaseType = databaseType instanceof SchemaSupportedDatabaseType;
        if (splitTables.contains(SingleTableConstants.ALL_TABLES)) {
            return loadAllTables(isSchemaSupportedDatabaseType, physicalTables);
        }
        Collection<String> featureRequiredSingleTables = SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules, excludedTables);
        return loadSpecifiedTables(isSchemaSupportedDatabaseType, physicalTables, featureRequiredSingleTables, configuredDataNodes);
    }
    
    private Collection<String> loadSpecifiedTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> physicalTables,
                                                   final Collection<String> featureRequiredSingleTables, final Collection<DataNode> configuredDataNodes) {
        Collection<String> expandRequiredDataSources = new LinkedHashSet<>();
        Map<String, DataNode> unExpandDataNodes = new LinkedHashMap<>();
        for (DataNode each : configuredDataNodes) {
            if (SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                expandRequiredDataSources.add(each.getDataSourceName());
            } else {
                unExpandDataNodes.put(each.getTableName(), each);
            }
        }
        if (expandRequiredDataSources.isEmpty()) {
            return loadSpecifiedTablesWithoutExpand(isSchemaSupportedDatabaseType, physicalTables, featureRequiredSingleTables, configuredDataNodes);
        }
        return loadSpecifiedTablesWithExpand(isSchemaSupportedDatabaseType, physicalTables, featureRequiredSingleTables, expandRequiredDataSources, unExpandDataNodes);
    }
    
    private Collection<String> loadSpecifiedTablesWithExpand(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> physicalTables,
                                                             final Collection<String> featureRequiredSingleTables, final Collection<String> expandRequiredDataSources,
                                                             final Map<String, DataNode> unExpandDataNodes) {
        Collection<String> result = new LinkedHashSet<>();
        Collection<String> loadedTableNames = new LinkedHashSet<>();
        for (Entry<String, Collection<DataNode>> entry : physicalTables.entrySet()) {
            DataNode physicalDataNode = entry.getValue().iterator().next();
            if (expandRequiredDataSources.contains(physicalDataNode.getDataSourceName())) {
                result.add(getTableNodeString(isSchemaSupportedDatabaseType, physicalDataNode));
                loadedTableNames.add(physicalDataNode.getTableName());
                continue;
            }
            if (unExpandDataNodes.containsKey(entry.getKey())) {
                DataNode dataNode = unExpandDataNodes.get(entry.getKey());
                String tableNodeStr = getTableNodeString(isSchemaSupportedDatabaseType, physicalDataNode);
                ShardingSpherePreconditions.checkState(physicalDataNode.equals(dataNode),
                        () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` is found that does not match %s", tableNodeStr,
                                getTableNodeString(isSchemaSupportedDatabaseType, dataNode))));
                result.add(tableNodeStr);
                loadedTableNames.add(physicalDataNode.getTableName());
                continue;
            }
            if (featureRequiredSingleTables.contains(entry.getKey()) && !loadedTableNames.contains(entry.getKey())) {
                result.add(getTableNodeString(isSchemaSupportedDatabaseType, entry.getValue().iterator().next()));
                loadedTableNames.add(entry.getKey());
            }
        }
        return result;
    }
    
    private Collection<String> loadSpecifiedTablesWithoutExpand(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> physicalTables,
                                                                final Collection<String> featureRequiredSingleTables, final Collection<DataNode> configuredDataNodes) {
        Collection<String> result = new LinkedHashSet<>();
        Collection<String> loadedTableNames = new LinkedHashSet<>();
        for (DataNode each : configuredDataNodes) {
            ShardingSpherePreconditions.checkState(physicalTables.containsKey(each.getTableName()),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` does not exist", getTableNodeString(isSchemaSupportedDatabaseType, each))));
            DataNode physicalDataNode = physicalTables.get(each.getTableName()).iterator().next();
            String tableNodeStr = getTableNodeString(isSchemaSupportedDatabaseType, physicalDataNode);
            ShardingSpherePreconditions.checkState(physicalDataNode.equals(each),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` is found that does not match %s", tableNodeStr,
                            getTableNodeString(isSchemaSupportedDatabaseType, each))));
            result.add(tableNodeStr);
            loadedTableNames.add(each.getTableName());
        }
        for (final String each : featureRequiredSingleTables) {
            if (physicalTables.containsKey(each) && !loadedTableNames.contains(each)) {
                result.add(getTableNodeString(isSchemaSupportedDatabaseType, physicalTables.get(each).iterator().next()));
                loadedTableNames.add(each);
            }
        }
        return result;
    }
    
    private Collection<String> loadAllTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> physicalTables) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, Collection<DataNode>> entry : physicalTables.entrySet()) {
            result.add(getTableNodeString(isSchemaSupportedDatabaseType, entry.getValue().iterator().next()));
        }
        return result;
    }
    
    private String getTableNodeString(final boolean isSchemaSupportedDatabaseType, final DataNode dataNode) {
        return isSchemaSupportedDatabaseType
                ? formatTableName(dataNode.getDataSourceName(), dataNode.getSchemaName(), dataNode.getTableName())
                : formatTableName(dataNode.getDataSourceName(), dataNode.getTableName());
    }
    
    private Map<String, Collection<DataNode>> loadPhysicalTables(final String databaseName, final DatabaseType databaseType, final Map<String, DataSource> dataSources,
                                                                 final Collection<String> excludedTables) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            Map<String, Collection<DataNode>> dataNodeMap = loadPhysicalTables(databaseName, databaseType, entry.getKey(), entry.getValue(), excludedTables);
            for (Entry<String, Collection<DataNode>> each : dataNodeMap.entrySet()) {
                Collection<DataNode> addedDataNodes = each.getValue();
                Collection<DataNode> existDataNodes = result.getOrDefault(each.getKey().toLowerCase(), new LinkedHashSet<>(addedDataNodes.size(), 1F));
                existDataNodes.addAll(addedDataNodes);
                result.putIfAbsent(each.getKey().toLowerCase(), existDataNodes);
            }
        }
        return result;
    }
    
    private Map<String, Collection<DataNode>> loadPhysicalTables(final String databaseName, final DatabaseType databaseType, final String dataSourceName,
                                                                 final DataSource dataSource, final Collection<String> excludedTables) {
        Map<String, Collection<String>> schemaTableNames = SingleTableDataNodeLoader.loadSchemaTableNames(databaseName, databaseType, dataSource, dataSourceName);
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                if (excludedTables.contains(each)) {
                    continue;
                }
                Collection<DataNode> dataNodes = result.getOrDefault(each, new LinkedList<>());
                DataNode dataNode = new DataNode(dataSourceName, each);
                dataNode.setSchemaName(entry.getKey());
                dataNodes.add(dataNode);
                result.putIfAbsent(each, dataNodes);
            }
        }
        return result;
    }
    
    private void checkRuleConfiguration(final String databaseName, final Map<String, DataSource> dataSources, final Collection<String> excludedTables, final Collection<DataNode> dataNodes) {
        for (DataNode each : dataNodes) {
            if (!SingleTableConstants.ASTERISK.equals(each.getDataSourceName())) {
                ShardingSpherePreconditions.checkState(dataSources.containsKey(each.getDataSourceName()),
                        () -> new InvalidSingleRuleConfigurationException(String.format("Data source `%s` does not exist in database `%s`", each.getDataSourceName(), databaseName)));
            }
            ShardingSpherePreconditions.checkState(!excludedTables.contains(each.getTableName()),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Table `%s` already exists or cannot be loaded as a single table in database `%s`",
                            each.getTableName(), databaseName)));
        }
    }
    
    private String formatTableName(final String dataSourceName, final String tableName) {
        return String.format("%s.%s", dataSourceName, tableName);
    }
    
    private String formatTableName(final String dataSourceName, final String schemaName, final String tableName) {
        return String.format("%s.%s.%s", dataSourceName, schemaName, tableName);
    }
    
    @Override
    public String getType() {
        return SingleRuleConfiguration.class.getName();
    }
}
