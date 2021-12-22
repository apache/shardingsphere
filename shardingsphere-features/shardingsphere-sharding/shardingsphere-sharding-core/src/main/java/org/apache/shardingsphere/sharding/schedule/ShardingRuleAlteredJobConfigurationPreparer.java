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

package org.apache.shardingsphere.sharding.schedule;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.HandleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding rule altered job configuration preparer.
 */
@Slf4j
public final class ShardingRuleAlteredJobConfigurationPreparer implements RuleAlteredJobConfigurationPreparer {
    
    @Override
    public HandleConfiguration createHandleConfig(final RuleConfiguration ruleConfig) {
        HandleConfiguration result = new HandleConfiguration();
        Map<String, List<DataNode>> shouldScalingActualDataNodes = getShouldScalingActualDataNodes(ruleConfig);
        Collection<DataNode> dataNodes = new ArrayList<>();
        for (Entry<String, List<DataNode>> entry : shouldScalingActualDataNodes.entrySet()) {
            dataNodes.addAll(entry.getValue());
        }
        result.setJobShardingDataNodes(groupByDataSource(dataNodes));
        result.setLogicTables(getLogicTables(shouldScalingActualDataNodes.keySet()));
        result.setTablesFirstDataNodes(getTablesFirstDataNodes(shouldScalingActualDataNodes));
        return result;
    }
    
    /**
     * Get scaling actual data nodes.
     *
     * @param ruleConfig rule configuration
     * @return map(logic table name, DataNode of each logic table)
     */
    private static Map<String, List<DataNode>> getShouldScalingActualDataNodes(final RuleConfiguration ruleConfig) {
        JDBCDataSourceConfiguration sourceConfig = ruleConfig.getSource().unwrap();
        Preconditions.checkState(sourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration,
                "Only ShardingSphereJdbc type of source TypedDataSourceConfiguration is supported.");
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        ShardingRule shardingRule = new ShardingRule(sourceRuleConfig, source.getRootConfig().getDataSources().keySet());
        Map<String, TableRule> tableRules = shardingRule.getTableRules();
        Map<String, List<DataNode>> result = new LinkedHashMap<>();
        for (Entry<String, TableRule> entry : tableRules.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getActualDataNodes());
        }
        return result;
    }
    
    private static List<String> groupByDataSource(final Collection<DataNode> dataNodes) {
        Map<String, Collection<DataNode>> dataSourceDataNodesMap = new LinkedHashMap<>();
        for (DataNode each : dataNodes) {
            dataSourceDataNodesMap.computeIfAbsent(each.getDataSourceName(), k -> new LinkedList<>()).add(each);
        }
        return dataSourceDataNodesMap.values().stream().map(each -> each.stream().map(DataNode::format)
                .collect(Collectors.joining(","))).collect(Collectors.toList());
    }
    
    private static String getLogicTables(final Set<String> logicTables) {
        return Joiner.on(',').join(logicTables);
    }
    
    private static String getTablesFirstDataNodes(final Map<String, List<DataNode>> actualDataNodes) {
        List<JobDataNodeEntry> dataNodeEntries = new ArrayList<>(actualDataNodes.size());
        for (Entry<String, List<DataNode>> entry : actualDataNodes.entrySet()) {
            dataNodeEntries.add(new JobDataNodeEntry(entry.getKey(), entry.getValue().subList(0, 1)));
        }
        return new JobDataNodeLine(dataNodeEntries).marshal();
    }
    
    @Override
    public List<TaskConfiguration> createTaskConfigs(final RuleConfiguration ruleConfig, final HandleConfiguration handleConfig) {
        List<TaskConfiguration> result = new LinkedList<>();
        ShardingSphereJDBCDataSourceConfiguration sourceConfig = getSourceConfiguration(ruleConfig);
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(sourceConfig.getRootConfig().getRules());
        Map<String, DataSourceConfiguration> sourceDataSource = new YamlDataSourceConfigurationSwapper().getDataSourceConfigurations(sourceConfig.getRootConfig());
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(new ShardingRule(sourceRuleConfig, sourceConfig.getRootConfig().getDataSources().keySet()));
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfiguration(ruleConfig);
        filterByShardingDataSourceTables(dataSourceTableNameMap, handleConfig);
        Map<String, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            OnRuleAlteredActionConfiguration ruleAlteredActionConfig = getRuleAlteredActionConfig(targetRuleConfig.orElse(sourceRuleConfig)).orElse(null);
            DumperConfiguration dumperConfig = createDumperConfig(entry.getKey(), sourceDataSource.get(entry.getKey()).getProps(), entry.getValue(), ruleAlteredActionConfig);
            ImporterConfiguration importerConfig = createImporterConfig(ruleConfig, handleConfig, shardingColumnsMap);
            TaskConfiguration taskConfig = new TaskConfiguration(handleConfig, dumperConfig, importerConfig);
            log.info("toTaskConfigs, dataSourceName={}, taskConfig={}", entry.getKey(), taskConfig);
            result.add(taskConfig);
        }
        return result;
    }
    
    private Optional<OnRuleAlteredActionConfiguration> getRuleAlteredActionConfig(final ShardingRuleConfiguration shardingRuleConfig) {
        return Optional.ofNullable(shardingRuleConfig.getScaling().get(shardingRuleConfig.getScalingName()));
    }
    
    private static ShardingSphereJDBCDataSourceConfiguration getSourceConfiguration(final RuleConfiguration ruleConfig) {
        JDBCDataSourceConfiguration result = ruleConfig.getSource().unwrap();
        Preconditions.checkArgument(result instanceof ShardingSphereJDBCDataSourceConfiguration, "Only support ShardingSphere source data source.");
        return (ShardingSphereJDBCDataSourceConfiguration) result;
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfiguration(final RuleConfiguration ruleConfig) {
        JDBCDataSourceConfiguration dataSourceConfig = ruleConfig.getTarget().unwrap();
        if (dataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            return Optional.of(
                    ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig).getRootConfig().getRules()));
        }
        return Optional.empty();
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> totalDataSourceTableNameMap, final HandleConfiguration handleConfig) {
        if (null == handleConfig.getJobShardingDataNodes()) {
            log.info("jobShardingDataNodes null");
            return;
        }
        // TODO simplify data source and table name converting, and jobShardingDataNodes format
        Map<String, Set<String>> jobDataSourceTableNameMap = toDataSourceTableNameMap(getJobShardingDataNodesEntry(handleConfig));
        totalDataSourceTableNameMap.entrySet().removeIf(entry -> !jobDataSourceTableNameMap.containsKey(entry.getKey()));
        for (Entry<String, Map<String, String>> entry : totalDataSourceTableNameMap.entrySet()) {
            filterByShardingTables(entry.getValue(), jobDataSourceTableNameMap.get(entry.getKey()));
        }
    }
    
    private static String getJobShardingDataNodesEntry(final HandleConfiguration handleConfig) {
        if (handleConfig.getJobShardingItem() >= handleConfig.getJobShardingDataNodes().size()) {
            log.warn("jobShardingItem={} ge handleConfig.jobShardingDataNodes.len={}", handleConfig.getJobShardingItem(), handleConfig.getJobShardingDataNodes().size());
            return "";
        }
        return handleConfig.getJobShardingDataNodes().get(handleConfig.getJobShardingItem());
    }
    
    private static void filterByShardingTables(final Map<String, String> fullTables, final Set<String> shardingTables) {
        fullTables.entrySet().removeIf(entry -> !shardingTables.contains(entry.getKey()));
    }
    
    private static Map<String, Set<String>> toDataSourceTableNameMap(final String jobShardingDataNodesEntry) {
        Map<String, Set<String>> result = new HashMap<>();
        for (String each : new InlineExpressionParser(jobShardingDataNodesEntry).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            result.computeIfAbsent(dataNode.getDataSourceName(), k -> new HashSet<>()).add(dataNode.getTableName());
        }
        return result;
    }
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final ShardingRule shardingRule) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (TableRule each : shardingRule.getTableRules().values()) {
            mergeDataSourceTableNameMap(result, toDataSourceTableNameMap(each));
        }
        return result;
    }
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final TableRule tableRule) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Entry<String, Collection<String>> entry : tableRule.getDatasourceToTablesMap().entrySet()) {
            Map<String, String> tableNameMap = result.get(entry.getKey());
            if (null == tableNameMap) {
                result.put(entry.getKey(), toTableNameMap(tableRule.getLogicTable(), entry.getValue()));
            } else {
                tableNameMap.putAll(toTableNameMap(tableRule.getLogicTable(), entry.getValue()));
            }
        }
        return result;
    }
    
    private static Map<String, String> toTableNameMap(final String logicalTable, final Collection<String> actualTables) {
        Map<String, String> result = new HashMap<>();
        for (String each : actualTables) {
            result.put(each, logicalTable);
        }
        return result;
    }
    
    private static void mergeDataSourceTableNameMap(final Map<String, Map<String, String>> mergedResult, final Map<String, Map<String, String>> newDataSourceTableNameMap) {
        for (Entry<String, Map<String, String>> entry : newDataSourceTableNameMap.entrySet()) {
            Map<String, String> tableNameMap = mergedResult.get(entry.getKey());
            if (null == tableNameMap) {
                mergedResult.put(entry.getKey(), entry.getValue());
            } else {
                tableNameMap.putAll(entry.getValue());
            }
        }
    }
    
    private static Map<String, Set<String>> getShardingColumnsMap(final ShardingRuleConfiguration shardingRuleConfig) {
        Set<String> defaultDatabaseShardingColumns = extractShardingColumns(shardingRuleConfig.getDefaultDatabaseShardingStrategy());
        Set<String> defaultTableShardingColumns = extractShardingColumns(shardingRuleConfig.getDefaultTableShardingStrategy());
        Map<String, Set<String>> result = Maps.newConcurrentMap();
        for (ShardingTableRuleConfiguration each : shardingRuleConfig.getTables()) {
            Set<String> shardingColumns = new HashSet<>();
            shardingColumns.addAll(null == each.getDatabaseShardingStrategy() ? defaultDatabaseShardingColumns : extractShardingColumns(each.getDatabaseShardingStrategy()));
            shardingColumns.addAll(null == each.getTableShardingStrategy() ? defaultTableShardingColumns : extractShardingColumns(each.getTableShardingStrategy()));
            result.put(each.getLogicTable(), shardingColumns);
        }
        for (ShardingAutoTableRuleConfiguration each : shardingRuleConfig.getAutoTables()) {
            ShardingStrategyConfiguration shardingStrategy = each.getShardingStrategy();
            Set<String> shardingColumns = new HashSet<>(extractShardingColumns(shardingStrategy));
            result.put(each.getLogicTable(), shardingColumns);
        }
        return result;
    }
    
    private static Set<String> extractShardingColumns(final ShardingStrategyConfiguration shardingStrategy) {
        if (shardingStrategy instanceof StandardShardingStrategyConfiguration) {
            return Sets.newHashSet(((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn());
        }
        if (shardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            return Sets.newHashSet(((ComplexShardingStrategyConfiguration) shardingStrategy).getShardingColumns().split(","));
        }
        return Collections.emptySet();
    }
    
    private static DumperConfiguration createDumperConfig(final String dataSourceName, final Map<String, Object> props, final Map<String, String> tableMap,
                                                          final OnRuleAlteredActionConfiguration ruleAlteredActionConfig) {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(new StandardJDBCDataSourceConfiguration(YamlEngine.marshal(props)));
        result.setTableNameMap(tableMap);
        if (null != ruleAlteredActionConfig) {
            result.setBlockQueueSize(ruleAlteredActionConfig.getBlockQueueSize());
        }
        return result;
    }
    
    private static ImporterConfiguration createImporterConfig(final RuleConfiguration ruleConfig, final HandleConfiguration handleConfig, final Map<String, Set<String>> shardingColumnsMap) {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfig(ruleConfig.getTarget().unwrap());
        result.setShardingColumnsMap(shardingColumnsMap);
        result.setRetryTimes(handleConfig.getRetryTimes());
        return result;
    }
}
