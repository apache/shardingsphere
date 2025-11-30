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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.exception.InvalidSingleRuleConfigurationException;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
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
import java.util.stream.Collectors;

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
        Map<String, DataSource> aggregatedDataSources = PhysicalDataSourceAggregator.getAggregatedDataSources(dataSources, builtRules);
        DatabaseType databaseType = dataSources.isEmpty() ? DatabaseTypeEngine.getDefaultStorageType() : DatabaseTypeEngine.getStorageType(dataSources.values().iterator().next());
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(builtRules);
        Map<String, Collection<DataNode>> actualDataNodes = SingleTableDataNodeLoader.load(databaseName, aggregatedDataSources, excludedTables);
        boolean isSchemaSupportedDatabaseType = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema().isPresent();
        if (splitTables.contains(SingleTableConstants.ALL_TABLES) || splitTables.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return loadAllTables(isSchemaSupportedDatabaseType, actualDataNodes);
        }
        Collection<DataNode> configuredDataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, splitTables);
        return loadSpecifiedTables(isSchemaSupportedDatabaseType, actualDataNodes, builtRules, configuredDataNodes);
    }
    
    private boolean isExpandRequired(final Collection<String> splitTables) {
        return splitTables.stream().anyMatch(each -> each.contains(SingleTableConstants.ASTERISK));
    }
    
    private Collection<String> loadAllTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes) {
        return actualDataNodes.values().stream().map(each -> getTableNodeString(isSchemaSupportedDatabaseType, each.iterator().next())).collect(Collectors.toList());
    }
    
    private String getTableNodeString(final boolean isSchemaSupportedDatabaseType, final DataNode dataNode) {
        return isSchemaSupportedDatabaseType
                ? formatTableName(dataNode.getDataSourceName(), dataNode.getSchemaName(), dataNode.getTableName())
                : formatTableName(dataNode.getDataSourceName(), dataNode.getTableName());
    }
    
    private String formatTableName(final String dataSourceName, final String schemaName, final String tableName) {
        return String.format("%s.%s.%s", dataSourceName, schemaName, tableName);
    }
    
    private String formatTableName(final String dataSourceName, final String tableName) {
        return String.format("%s.%s", dataSourceName, tableName);
    }
    
    private Collection<String> loadSpecifiedTables(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes,
                                                   final Collection<ShardingSphereRule> builtRules, final Collection<DataNode> configuredDataNodes) {
        Collection<String> expandRequiredDataSources = new LinkedHashSet<>(configuredDataNodes.size(), 1F);
        Map<String, DataNode> expectedDataNodes = new LinkedHashMap<>(configuredDataNodes.size(), 1F);
        for (DataNode each : configuredDataNodes) {
            if (SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                expandRequiredDataSources.add(each.getDataSourceName());
            } else {
                expectedDataNodes.put(each.getTableName(), each);
            }
        }
        return expandRequiredDataSources.isEmpty()
                ? loadSpecifiedTablesWithoutExpand(isSchemaSupportedDatabaseType, actualDataNodes, configuredDataNodes)
                : loadSpecifiedTablesWithExpand(
                        isSchemaSupportedDatabaseType, actualDataNodes, SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules), expandRequiredDataSources, expectedDataNodes);
    }
    
    private Collection<String> loadSpecifiedTablesWithExpand(final boolean isSchemaSupportedDatabaseType, final Map<String, Collection<DataNode>> actualDataNodes,
                                                             final Collection<String> featureRequiredSingleTables, final Collection<String> expandRequiredDataSources,
                                                             final Map<String, DataNode> expectedDataNodes) {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size(), 1F);
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
    
    private Collection<String> loadSpecifiedTablesWithoutExpand(final boolean isSchemaSupportedDatabaseType,
                                                                final Map<String, Collection<DataNode>> actualDataNodes, final Collection<DataNode> configuredDataNodes) {
        Collection<String> result = new LinkedHashSet<>(configuredDataNodes.size(), 1F);
        for (DataNode each : configuredDataNodes) {
            ShardingSpherePreconditions.checkContainsKey(actualDataNodes, each.getTableName(), () -> new SingleTableNotFoundException(getTableNodeString(isSchemaSupportedDatabaseType, each)));
            DataNode actualDataNode = actualDataNodes.get(each.getTableName()).iterator().next();
            String tableNodeStr = getTableNodeString(isSchemaSupportedDatabaseType, actualDataNode);
            ShardingSpherePreconditions.checkState(actualDataNode.equals(each), () -> new InvalidSingleRuleConfigurationException(
                    String.format("Single table '%s' is found that does not match %s", tableNodeStr, getTableNodeString(isSchemaSupportedDatabaseType, each))));
            result.add(tableNodeStr);
        }
        return result;
    }
    
    @Override
    public Class<SingleRuleConfiguration> getType() {
        return SingleRuleConfiguration.class;
    }
}
