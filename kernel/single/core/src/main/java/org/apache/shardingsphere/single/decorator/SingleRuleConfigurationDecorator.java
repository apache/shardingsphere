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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;
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
        boolean isSchemaAvailable = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable();
        if (splitTables.contains(SingleTableConstants.ALL_TABLES) || splitTables.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return loadAllTables(isSchemaAvailable, actualDataNodes);
        }
        Collection<DataNode> configuredDataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, splitTables);
        return loadSpecifiedTables(isSchemaAvailable, actualDataNodes, builtRules, configuredDataNodes);
    }
    
    private boolean isExpandRequired(final Collection<String> splitTables) {
        return splitTables.stream().anyMatch(each -> each.contains(SingleTableConstants.ASTERISK));
    }
    
    private Collection<String> loadAllTables(final boolean isSchemaAvailable, final Map<String, Collection<DataNode>> actualDataNodes) {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            result.addAll(entry.getValue().stream().map(each -> getTableNodeString(isSchemaAvailable, each)).collect(Collectors.toList()));
        }
        return result;
    }
    
    private String getTableNodeString(final boolean isSchemaAvailable, final DataNode dataNode) {
        return isSchemaAvailable
                ? formatTableName(dataNode.getDataSourceName(), dataNode.getSchemaName(), dataNode.getTableName())
                : formatTableName(dataNode.getDataSourceName(), dataNode.getTableName());
    }
    
    private String formatTableName(final String dataSourceName, final String schemaName, final String tableName) {
        return String.format("%s.%s.%s", dataSourceName, schemaName, tableName);
    }
    
    private String formatTableName(final String dataSourceName, final String tableName) {
        return String.format("%s.%s", dataSourceName, tableName);
    }
    
    private Collection<String> loadSpecifiedTables(final boolean isSchemaAvailable, final Map<String, Collection<DataNode>> actualDataNodes,
                                                   final Collection<ShardingSphereRule> builtRules, final Collection<DataNode> configuredDataNodes) {
        DataNodeClassification dataNodeClassification = classifyDataNodes(configuredDataNodes);
        if (dataNodeClassification.expandDataSources.isEmpty() && dataNodeClassification.expandDataSourceSchemas.isEmpty()) {
            return loadSpecifiedTablesWithoutExpand(isSchemaAvailable, actualDataNodes, configuredDataNodes);
        }
        return loadSpecifiedTablesWithExpand(isSchemaAvailable, actualDataNodes, SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules),
                dataNodeClassification.getExpandDataSources(), dataNodeClassification.getExpandDataSourceSchemas(), dataNodeClassification.getExpectedDataNodes());
    }
    
    private DataNodeClassification classifyDataNodes(final Collection<DataNode> configuredDataNodes) {
        Collection<String> expandDataSources = new LinkedHashSet<>();
        Map<String, Set<String>> expandDataSourceSchemas = new LinkedHashMap<>();
        Map<String, DataNode> expectedDataNodes = new LinkedHashMap<>();
        for (DataNode each : configuredDataNodes) {
            categorizeDataNode(each, expandDataSources, expandDataSourceSchemas, expectedDataNodes);
        }
        return new DataNodeClassification(expandDataSources, expandDataSourceSchemas, expectedDataNodes);
    }
    
    private void categorizeDataNode(final DataNode dataNode, final Collection<String> expandDataSources,
                                    final Map<String, Set<String>> expandDataSourceSchemas, final Map<String, DataNode> expectedDataNodes) {
        if (SingleTableConstants.ASTERISK.equals(dataNode.getTableName())) {
            if (SingleTableConstants.ASTERISK.equals(dataNode.getSchemaName())) {
                expandDataSources.add(dataNode.getDataSourceName());
            } else {
                expandDataSourceSchemas.computeIfAbsent(dataNode.getDataSourceName(), key -> new LinkedHashSet<>()).add(dataNode.getSchemaName());
            }
        } else {
            expectedDataNodes.put(dataNode.getTableName(), dataNode);
        }
    }
    
    private Collection<String> loadSpecifiedTablesWithExpand(final boolean isSchemaAvailable, final Map<String, Collection<DataNode>> actualDataNodes,
                                                             final Collection<String> featureRequiredSingleTables, final Collection<String> expandDataSources,
                                                             final Map<String, Set<String>> expandDataSourceSchemas, final Map<String, DataNode> expectedDataNodes) {
        Collection<String> result = new LinkedHashSet<>(actualDataNodes.size(), 1F);
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            if (featureRequiredSingleTables.contains(entry.getKey())) {
                continue;
            }
            DataNode physicalDataNode = entry.getValue().iterator().next();
            if (expandDataSources.contains(physicalDataNode.getDataSourceName())) {
                result.add(getTableNodeString(isSchemaAvailable, physicalDataNode));
                continue;
            }
            Set<String> requiredSchemas = expandDataSourceSchemas.get(physicalDataNode.getDataSourceName());
            if (null != requiredSchemas && requiredSchemas.contains(physicalDataNode.getSchemaName())) {
                result.add(getTableNodeString(isSchemaAvailable, physicalDataNode));
                continue;
            }
            if (expectedDataNodes.containsKey(entry.getKey())) {
                DataNode dataNode = expectedDataNodes.get(entry.getKey());
                String tableNodeStr = getTableNodeString(isSchemaAvailable, physicalDataNode);
                ShardingSpherePreconditions.checkState(physicalDataNode.equals(dataNode),
                        () -> new InvalidSingleRuleConfigurationException(String.format("Single table `%s` is found that does not match %s", tableNodeStr,
                                getTableNodeString(isSchemaAvailable, dataNode))));
                result.add(tableNodeStr);
            }
        }
        return result;
    }
    
    private Collection<String> loadSpecifiedTablesWithoutExpand(final boolean isSchemaAvailable, final Map<String, Collection<DataNode>> actualDataNodes,
                                                                final Collection<DataNode> configuredDataNodes) {
        Collection<String> result = new LinkedHashSet<>(configuredDataNodes.size(), 1F);
        for (DataNode each : configuredDataNodes) {
            ShardingSpherePreconditions.checkContainsKey(actualDataNodes, each.getTableName(), () -> new SingleTableNotFoundException(getTableNodeString(isSchemaAvailable, each)));
            DataNode actualDataNode = actualDataNodes.get(each.getTableName()).iterator().next();
            String tableNodeStr = getTableNodeString(isSchemaAvailable, actualDataNode);
            ShardingSpherePreconditions.checkState(actualDataNode.equals(each), () -> new InvalidSingleRuleConfigurationException(
                    String.format("Single table '%s' is found that does not match %s", tableNodeStr, getTableNodeString(isSchemaAvailable, each))));
            result.add(tableNodeStr);
        }
        return result;
    }
    
    @Override
    public Class<SingleRuleConfiguration> getType() {
        return SingleRuleConfiguration.class;
    }
    
    @Getter
    @RequiredArgsConstructor
    private static class DataNodeClassification {
        
        private final Collection<String> expandDataSources;
        
        private final Map<String, Set<String>> expandDataSourceSchemas;
        
        private final Map<String, DataNode> expectedDataNodes;
    }
}
