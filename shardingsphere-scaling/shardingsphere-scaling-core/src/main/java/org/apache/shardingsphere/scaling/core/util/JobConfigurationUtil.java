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

package org.apache.shardingsphere.scaling.core.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.config.datasource.typed.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.driver.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;

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
 * Job configuration util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JobConfigurationUtil {
    
    private static final SnowflakeKeyGenerateAlgorithm ID_AUTO_INCREASE_GENERATOR = initIdAutoIncreaseGenerator();
    
    private static SnowflakeKeyGenerateAlgorithm initIdAutoIncreaseGenerator() {
        SnowflakeKeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.init();
        return result;
    }
    
    private static Long generateKey() {
        return (Long) ID_AUTO_INCREASE_GENERATOR.generateKey();
    }
    
    /**
     * Fill in properties for job configuration.
     *
     * @param jobConfig job configuration
     */
    public static void fillInProperties(final JobConfiguration jobConfig) {
        HandleConfiguration handleConfig = jobConfig.getHandleConfig();
        if (null == handleConfig.getJobId()) {
            handleConfig.setJobId(generateKey());
        }
        if (Strings.isNullOrEmpty(handleConfig.getDatabaseType())) {
            handleConfig.setDatabaseType(jobConfig.getRuleConfig().getSource().unwrap().getDatabaseType().getName());
        }
        if (null == jobConfig.getHandleConfig().getShardingTables()) {
            Map<String, List<DataNode>> shouldScalingActualDataNodes = getShouldScalingActualDataNodes(jobConfig);
            Collection<DataNode> dataNodes = new ArrayList<>();
            for (Entry<String, List<DataNode>> entry : shouldScalingActualDataNodes.entrySet()) {
                dataNodes.addAll(entry.getValue());
            }
            handleConfig.setShardingTables(groupByDataSource(dataNodes));
            handleConfig.setLogicTables(getLogicTables(shouldScalingActualDataNodes.keySet()));
        }
    }
    
    private static Map<String, List<DataNode>> getShouldScalingActualDataNodes(final JobConfiguration jobConfig) {
        TypedDataSourceConfiguration sourceConfig = jobConfig.getRuleConfig().getSource().unwrap();
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
    
    private static String[] groupByDataSource(final Collection<DataNode> dataNodes) {
        Map<String, Collection<DataNode>> dataSourceDataNodesMap = new LinkedHashMap<>();
        for (DataNode each : dataNodes) {
            dataSourceDataNodesMap.computeIfAbsent(each.getDataSourceName(), k -> new LinkedList<>()).add(each);
        }
        return dataSourceDataNodesMap.values().stream().map(each -> each.stream().map(dataNode -> String.format("%s.%s", dataNode.getDataSourceName(), dataNode.getTableName()))
                .collect(Collectors.joining(","))).toArray(String[]::new);
    }
    
    private static String getLogicTables(final Set<String> logicTables) {
        return logicTables.stream()
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("");
    }
    
    /**
     * Split job configuration to task configurations.
     *
     * @param jobConfig job configuration
     * @return list of task configurations
     */
    public static List<TaskConfiguration> toTaskConfigs(final JobConfiguration jobConfig) {
        List<TaskConfiguration> result = new LinkedList<>();
        ShardingSphereJDBCDataSourceConfiguration sourceConfig = getSourceConfiguration(jobConfig);
        ShardingRuleConfiguration sourceRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(sourceConfig.getRootConfig().getRules());
        Map<String, DataSourceConfiguration> sourceDataSource = getDataSourceConfigurations(sourceConfig.getRootConfig());
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(new ShardingRule(sourceRuleConfig, sourceConfig.getRootConfig().getDataSources().keySet()));
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfiguration(jobConfig);
        filterByShardingDataSourceTables(dataSourceTableNameMap, jobConfig.getHandleConfig());
        Map<String, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            DumperConfiguration dumperConfig = createDumperConfig(entry.getKey(), sourceDataSource.get(entry.getKey()).getProps(), entry.getValue());
            ImporterConfiguration importerConfig = createImporterConfig(jobConfig, shardingColumnsMap);
            TaskConfiguration taskConfig = new TaskConfiguration(jobConfig.getHandleConfig(), dumperConfig, importerConfig);
            log.info("toTaskConfigs, dataSourceName={}, taskConfig={}", entry.getKey(), taskConfig);
            result.add(taskConfig);
        }
        return result;
    }
    
    private static ShardingSphereJDBCDataSourceConfiguration getSourceConfiguration(final JobConfiguration jobConfig) {
        TypedDataSourceConfiguration result = jobConfig.getRuleConfig().getSource().unwrap();
        Preconditions.checkArgument(result instanceof ShardingSphereJDBCDataSourceConfiguration, "Only support ShardingSphere source data source.");
        return (ShardingSphereJDBCDataSourceConfiguration) result;
    }
    
    /**
     * Get data source configurations.
     *
     * @param yamlRootConfiguration yaml root configuration
     * @return data source name to data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurations(final YamlRootConfiguration yamlRootConfiguration) {
        Map<String, Map<String, Object>> yamlDataSourceConfigs = yamlRootConfiguration.getDataSources();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfiguration(final JobConfiguration jobConfig) {
        TypedDataSourceConfiguration dataSourceConfig = jobConfig.getRuleConfig().getTarget().unwrap();
        if (dataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            return Optional.of(
                    ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig).getRootConfig().getRules()));
        }
        return Optional.empty();
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> dataSourceTableNameMap, final HandleConfiguration handleConfig) {
        if (null == handleConfig.getShardingTables()) {
            log.info("shardingTables null");
            return;
        }
        Map<String, Set<String>> shardingDataSourceTableMap = toDataSourceTableNameMap(getShardingDataSourceTables(handleConfig));
        dataSourceTableNameMap.entrySet().removeIf(entry -> !shardingDataSourceTableMap.containsKey(entry.getKey()));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            filterByShardingTables(entry.getValue(), shardingDataSourceTableMap.get(entry.getKey()));
        }
    }
    
    private static String getShardingDataSourceTables(final HandleConfiguration handleConfig) {
        if (handleConfig.getShardingItem() >= handleConfig.getShardingTables().length) {
            log.warn("shardingItem={} ge handleConfig.shardingTables.len={}", handleConfig.getShardingItem(), handleConfig.getShardingTables().length);
            return "";
        }
        return handleConfig.getShardingTables()[handleConfig.getShardingItem()];
    }
    
    private static void filterByShardingTables(final Map<String, String> fullTables, final Set<String> shardingTables) {
        fullTables.entrySet().removeIf(entry -> !shardingTables.contains(entry.getKey()));
    }
    
    private static Map<String, Set<String>> toDataSourceTableNameMap(final String shardingDataSourceTables) {
        Map<String, Set<String>> result = new HashMap<>();
        for (String each : new InlineExpressionParser(shardingDataSourceTables).splitAndEvaluate()) {
            String[] table = each.split("\\.");
            if (!result.containsKey(table[0])) {
                result.put(table[0], new HashSet<>());
            }
            result.get(table[0]).add(table[1]);
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
    
    private static DumperConfiguration createDumperConfig(final String dataSourceName, final Map<String, Object> props, final Map<String, String> tableMap) {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(new StandardJDBCDataSourceConfiguration(YamlEngine.marshal(props)));
        result.setTableNameMap(tableMap);
        return result;
    }
    
    private static ImporterConfiguration createImporterConfig(final JobConfiguration jobConfig, final Map<String, Set<String>> shardingColumnsMap) {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfig(jobConfig.getRuleConfig().getTarget().unwrap());
        result.setShardingColumnsMap(shardingColumnsMap);
        result.setRetryTimes(jobConfig.getHandleConfig().getRetryTimes());
        return result;
    }
}
