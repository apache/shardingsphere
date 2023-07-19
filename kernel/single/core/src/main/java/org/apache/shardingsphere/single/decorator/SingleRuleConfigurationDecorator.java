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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
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

/**
 * Single rule configuration decorator.
 */
public final class SingleRuleConfigurationDecorator implements RuleConfigurationDecorator<SingleRuleConfiguration> {
    
    @Override
    public SingleRuleConfiguration decorate(final String databaseName, final Map<String, DataSource> dataSources,
                                            final Collection<ShardingSphereRule> builtRules, final SingleRuleConfiguration ruleConfig) {
        return new SingleRuleConfiguration(decorateTables(databaseName, dataSources, new LinkedList<>(builtRules), ruleConfig.getTables()), ruleConfig.getDefaultDataSource().orElse(null));
    }
    
    private Collection<String> decorateTables(final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules, final Collection<String> tables) {
        builtRules.removeIf(SingleRule.class::isInstance);
        if (tables.isEmpty() && builtRules.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(tables);
        if (!isExpandRequired(splitTables)) {
            return splitTables;
        }
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        Map<String, DataSource> aggregatedDataSources = SingleTableLoadUtils.getAggregatedDataSourceMap(enabledDataSources, builtRules);
        DatabaseType databaseType = DatabaseTypeEngine.getStorageType(enabledDataSources.values());
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(builtRules);
        Map<String, Collection<DataNode>> actualDataNodes = SingleTableDataNodeLoader.load(databaseName, databaseType, aggregatedDataSources, excludedTables);
        Collection<DataNode> configuredDataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, splitTables);
        checkRuleConfiguration(databaseName, aggregatedDataSources, excludedTables, configuredDataNodes);
        boolean isSchemaSupportedDatabaseType = databaseType.getDefaultSchema().isPresent();
        if (splitTables.contains(SingleTableConstants.ALL_TABLES) || splitTables.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return loadAllTables(isSchemaSupportedDatabaseType, actualDataNodes);
        }
        return loadSpecifiedTables(isSchemaSupportedDatabaseType, actualDataNodes, builtRules, configuredDataNodes);
    }
    
    private boolean isExpandRequired(final Collection<String> splitTables) {
        return splitTables.stream().anyMatch(each -> each.contains(SingleTableConstants.ASTERISK));
    }
    
    private Collection<String> loadSpecifiedTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes,
                                                   final Collection<ShardingSphereRule> builtRules, final Collection<DataNode> configuredDataNodes) {
        Collection<String> expandRequiredDataSources = new LinkedHashSet<>();
        Map<String, DataNode> expectedDataNodes = new LinkedHashMap<>();
        for (DataNode each : configuredDataNodes) {
            if (SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                expandRequiredDataSources.add(each.getDataSourceName());
            } else {
                expectedDataNodes.put(each.getTableName(), each);
            }
        }
        if (expandRequiredDataSources.isEmpty()) {
            return loadSpecifiedTablesWithoutExpand(isSchemaSupportedDatabaseType, actualDataNodes, configuredDataNodes);
        }
        Collection<String> featureRequiredSingleTables = SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules);
        return loadSpecifiedTablesWithExpand(isSchemaSupportedDatabaseType, actualDataNodes, featureRequiredSingleTables, expandRequiredDataSources, expectedDataNodes);
    }
    
    private Collection<String> loadSpecifiedTablesWithExpand(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes,
                                                             final Collection<String> featureRequiredSingleTables, final Collection<String> expandRequiredDataSources,
                                                             final Map<String, DataNode> expectedDataNodes) {
        Collection<String> result = new LinkedHashSet<>();
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            if (featureRequiredSingleTables.contains(entry.getKey())) {
                continue;
            }
            DataNode physicalDataNode = entry.getValue().iterator().next();
            if (expandRequiredDataSources.contains(physicalDataNode.getDataSourceName())) {
                result.add(getTableNodeString(isSchemaSupportedDatabaseType, physicalDataNode));
                continue;
            }
            if (expectedDataNodes.containsKey(entry.getKey())) {
                DataNode dataNode = expectedDataNodes.get(entry.getKey());
                String tableNodeStr = getTableNodeString(isSchemaSupportedDatabaseType, physicalDataNode);
                ShardingSpherePreconditions.checkState(physicalDataNode.equals(dataNode),
                        () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` is found that does not match %s", tableNodeStr,
                                getTableNodeString(isSchemaSupportedDatabaseType, dataNode))));
                result.add(tableNodeStr);
            }
        }
        return result;
    }
    
    private Collection<String> loadSpecifiedTablesWithoutExpand(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes,
                                                                final Collection<DataNode> configuredDataNodes) {
        Collection<String> result = new LinkedHashSet<>();
        for (DataNode each : configuredDataNodes) {
            ShardingSpherePreconditions.checkState(actualDataNodes.containsKey(each.getTableName()),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` does not exist", getTableNodeString(isSchemaSupportedDatabaseType, each))));
            DataNode actualDataNode = actualDataNodes.get(each.getTableName()).iterator().next();
            String tableNodeStr = getTableNodeString(isSchemaSupportedDatabaseType, actualDataNode);
            ShardingSpherePreconditions.checkState(actualDataNode.equals(each),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` is found that does not match %s", tableNodeStr,
                            getTableNodeString(isSchemaSupportedDatabaseType, each))));
            result.add(tableNodeStr);
        }
        return result;
    }
    
    private Collection<String> loadAllTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            result.add(getTableNodeString(isSchemaSupportedDatabaseType, entry.getValue().iterator().next()));
        }
        return result;
    }
    
    private String getTableNodeString(final boolean isSchemaSupportedDatabaseType, final DataNode dataNode) {
        return isSchemaSupportedDatabaseType
                ? formatTableName(dataNode.getDataSourceName(), dataNode.getSchemaName(), dataNode.getTableName())
                : formatTableName(dataNode.getDataSourceName(), dataNode.getTableName());
    }
    
    private void checkRuleConfiguration(final String databaseName, final Map<String, DataSource> dataSources, final Collection<String> excludedTables, final Collection<DataNode> dataNodes) {
        for (DataNode each : dataNodes) {
            if (!SingleTableConstants.ASTERISK.equals(each.getDataSourceName())) {
                ShardingSpherePreconditions.checkState(dataSources.containsKey(each.getDataSourceName()),
                        () -> new InvalidSingleRuleConfigurationException(String.format("Data source `%s` does not exist in database `%s`", each.getDataSourceName(), databaseName)));
            }
            ShardingSpherePreconditions.checkState(!excludedTables.contains(each.getTableName()),
                    () -> new InvalidSingleRuleConfigurationException(String.format("Table `%s` existed and is not a single table in database `%s`",
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
