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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.HandleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
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
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Sharding rule altered job configuration preparer.
 */
@Slf4j
public final class ShardingRuleAlteredJobConfigurationPreparer implements RuleAlteredJobConfigurationPreparer {
    
    @Override
    public HandleConfiguration createHandleConfiguration(final PipelineConfiguration pipelineConfig) {
        HandleConfiguration result = new HandleConfiguration();
        Map<String, List<DataNode>> shouldScalingActualDataNodes = getShouldScalingActualDataNodes(pipelineConfig);
        result.setJobShardingDataNodes(getJobShardingDataNodes(shouldScalingActualDataNodes));
        result.setLogicTables(getLogicTables(shouldScalingActualDataNodes.keySet()));
        result.setTablesFirstDataNodes(getTablesFirstDataNodes(shouldScalingActualDataNodes));
        return result;
    }
    
    private static Map<String, List<DataNode>> getShouldScalingActualDataNodes(final PipelineConfiguration pipelineConfig) {
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(pipelineConfig.getSource().getType(), pipelineConfig.getSource().getParameter());
        ShardingSpherePipelineDataSourceConfiguration source = (ShardingSpherePipelineDataSourceConfiguration) sourceDataSourceConfig;
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(source.getRootConfig().getRules());
        ShardingRule shardingRule = new ShardingRule(sourceRuleConfig, source.getRootConfig().getDataSources().keySet());
        Map<String, TableRule> tableRules = shardingRule.getTableRules();
        Map<String, List<DataNode>> result = new LinkedHashMap<>();
        for (Entry<String, TableRule> entry : tableRules.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getActualDataNodes());
        }
        return result;
    }
    
    private List<String> getJobShardingDataNodes(final Map<String, List<DataNode>> actualDataNodes) {
        List<String> result = new LinkedList<>();
        Map<String, Map<String, List<DataNode>>> groupedDataSourceDataNodesMap = groupDataSourceDataNodesMapByDataSourceName(actualDataNodes);
        for (Map<String, List<DataNode>> each : groupedDataSourceDataNodesMap.values()) {
            List<JobDataNodeEntry> dataNodeEntries = new ArrayList<>(each.size());
            for (Entry<String, List<DataNode>> entry : each.entrySet()) {
                dataNodeEntries.add(new JobDataNodeEntry(entry.getKey(), entry.getValue()));
            }
            result.add(new JobDataNodeLine(dataNodeEntries).marshal());
        }
        return result;
    }
    
    private Map<String, Map<String, List<DataNode>>> groupDataSourceDataNodesMapByDataSourceName(final Map<String, List<DataNode>> actualDataNodes) {
        Map<String, Map<String, List<DataNode>>> result = new LinkedHashMap<>();
        for (Entry<String, List<DataNode>> entry : actualDataNodes.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Map<String, List<DataNode>> groupedDataNodesMap = result.computeIfAbsent(each.getDataSourceName(), k -> new LinkedHashMap<>());
                groupedDataNodesMap.computeIfAbsent(entry.getKey(), k -> new LinkedList<>()).add(each);
            }
        }
        return result;
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
    public Collection<TaskConfiguration> createTaskConfigurations(final PipelineConfiguration pipelineConfig, final HandleConfiguration handleConfig) {
        ShardingSpherePipelineDataSourceConfiguration sourceConfig = getSourceConfiguration(pipelineConfig);
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(sourceConfig.getRootConfig().getRules());
        Map<String, DataSourceConfiguration> sourceDataSource = new YamlDataSourceConfigurationSwapper().getDataSourceConfigurations(sourceConfig.getRootConfig());
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfiguration(pipelineConfig);
        Map<String, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        JobDataNodeLine dataNodeLine = JobDataNodeLine.unmarshal(handleConfig.getJobShardingDataNodes().get(handleConfig.getJobShardingItem()));
        String dataSourceName = dataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        Map<String, String> tableMap = new LinkedHashMap<>();
        for (JobDataNodeEntry each : dataNodeLine.getEntries()) {
            for (DataNode dataNode : each.getDataNodes()) {
                tableMap.put(dataNode.getTableName(), each.getLogicTableName());
            }
        }
        OnRuleAlteredActionConfiguration ruleAlteredActionConfig = getRuleAlteredActionConfig(targetRuleConfig.orElse(sourceRuleConfig)).orElse(null);
        DumperConfiguration dumperConfig = createDumperConfig(dataSourceName, sourceDataSource.get(dataSourceName).getProps(), tableMap, ruleAlteredActionConfig);
        ImporterConfiguration importerConfig = createImporterConfig(pipelineConfig, handleConfig, shardingColumnsMap);
        TaskConfiguration taskConfig = new TaskConfiguration(handleConfig, dumperConfig, importerConfig);
        log.info("toTaskConfigs, dataSourceName={}, taskConfig={}", dataSourceName, taskConfig);
        return Collections.singletonList(taskConfig);
    }
    
    private Optional<OnRuleAlteredActionConfiguration> getRuleAlteredActionConfig(final ShardingRuleConfiguration shardingRuleConfig) {
        return Optional.ofNullable(shardingRuleConfig.getScaling().get(shardingRuleConfig.getScalingName()));
    }
    
    private static ShardingSpherePipelineDataSourceConfiguration getSourceConfiguration(final PipelineConfiguration pipelineConfig) {
        PipelineDataSourceConfiguration result = PipelineDataSourceConfigurationFactory.newInstance(pipelineConfig.getSource().getType(), pipelineConfig.getSource().getParameter());
        return (ShardingSpherePipelineDataSourceConfiguration) result;
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfiguration(final PipelineConfiguration pipelineConfig) {
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(pipelineConfig.getTarget().getType(), pipelineConfig.getTarget().getParameter());
        if (!(targetDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration)) {
            return Optional.empty();
        }
        ShardingSpherePipelineDataSourceConfiguration target = (ShardingSpherePipelineDataSourceConfiguration) targetDataSourceConfig;
        return Optional.of(ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(target.getRootConfig().getRules()));
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
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration(YamlEngine.marshal(props)));
        result.setTableNameMap(tableMap);
        if (null != ruleAlteredActionConfig) {
            result.setBlockQueueSize(ruleAlteredActionConfig.getBlockQueueSize());
        }
        return result;
    }
    
    private static ImporterConfiguration createImporterConfig(final PipelineConfiguration pipelineConfig, final HandleConfiguration handleConfig, final Map<String, Set<String>> shardingColumnsMap) {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfig(PipelineDataSourceConfigurationFactory.newInstance(pipelineConfig.getTarget().getType(), pipelineConfig.getTarget().getParameter()));
        result.setShardingColumnsMap(shardingColumnsMap);
        result.setRetryTimes(handleConfig.getRetryTimes());
        return result;
    }
}
