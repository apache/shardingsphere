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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.governance.core.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.common.datasource.JdbcUri;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Job configuration util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            Map<String, String> shouldScalingActualDataNodes = getShouldScalingActualDataNodes(jobConfig);
            handleConfig.setShardingTables(groupByDataSource(shouldScalingActualDataNodes.values()));
            handleConfig.setLogicTables(getLogicTables(shouldScalingActualDataNodes.keySet()));
        }
    }
    
    private static Map<String, String> getShouldScalingActualDataNodes(final JobConfiguration jobConfig) {
        ScalingDataSourceConfiguration sourceConfig = jobConfig.getRuleConfig().getSource().unwrap();
        Preconditions.checkState(sourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration,
                "Only ShardingSphereJdbc type of source ScalingDataSourceConfiguration is supported.");
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        if (!(jobConfig.getRuleConfig().getTarget().unwrap() instanceof ShardingSphereJDBCDataSourceConfiguration)) {
            return getShardingRuleConfigMap(source.getRootRuleConfigs()).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, each -> each.getValue().getActualDataNodes()));
        }
        ShardingSphereJDBCDataSourceConfiguration target = (ShardingSphereJDBCDataSourceConfiguration) jobConfig.getRuleConfig().getTarget().unwrap();
        return getShouldScalingActualDataNodes(getModifiedDataSources(source.getRootRuleConfigs(), target.getRootRuleConfigs()),
                getShardingRuleConfigMap(source.getRootRuleConfigs()), getShardingRuleConfigMap(target.getRootRuleConfigs()));
    }
    
    private static Map<String, String> getShouldScalingActualDataNodes(final Set<String> modifiedDataSources,
                                                                       final Map<String, ShardingTableRuleConfiguration> oldShardingRuleConfigMap,
                                                                       final Map<String, ShardingTableRuleConfiguration> newShardingRuleConfigMap) {
        Map<String, String> result = Maps.newHashMap();
        newShardingRuleConfigMap.keySet().forEach(each -> {
            if (!oldShardingRuleConfigMap.containsKey(each)) {
                return;
            }
            List<String> oldActualDataNodes = new InlineExpressionParser(oldShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            List<String> newActualDataNodes = new InlineExpressionParser(newShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            if (!CollectionUtils.isEqualCollection(oldActualDataNodes, newActualDataNodes) || includeModifiedDataSources(newActualDataNodes, modifiedDataSources)) {
                result.put(each, oldShardingRuleConfigMap.get(each).getActualDataNodes());
            }
        });
        return result;
    }
    
    private static Set<String> getModifiedDataSources(final YamlRootRuleConfigurations sourceRootRuleConfigs, final YamlRootRuleConfigurations targetRootRuleConfigs) {
        Set<String> result = new HashSet<>();
        Map<String, String> oldDataSourceUrlMap = getDataSourceUrlMap(sourceRootRuleConfigs.getDataSources());
        Map<String, String> newDataSourceUrlMap = getDataSourceUrlMap(targetRootRuleConfigs.getDataSources());
        newDataSourceUrlMap.forEach((key, value) -> {
            if (!value.equals(oldDataSourceUrlMap.get(key))) {
                result.add(key);
            }
        });
        return result;
    }
    
    private static Map<String, String> getDataSourceUrlMap(final Map<String, Map<String, Object>> dataSources) {
        return dataSources.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    JdbcUri uri = new JdbcUri(JDBCUtil.getJdbcUrl(entry.getValue()));
                    return String.format("%s/%s", uri.getHost(), uri.getDatabase());
                }));
    }
    
    private static boolean includeModifiedDataSources(final List<String> actualDataNodes, final Set<String> modifiedDataSources) {
        return actualDataNodes.stream().anyMatch(each -> modifiedDataSources.contains(each.split("\\.")[0]));
    }
    
    private static Map<String, ShardingTableRuleConfiguration> getShardingRuleConfigMap(final YamlRootRuleConfigurations rootRuleConfigurations) {
        ShardingRuleConfiguration ruleConfig = YamlConfigurationConverter.convertShardingRuleConfig(rootRuleConfigurations.getRules());
        return ruleConfig.getTables().stream()
                .collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, Function.identity()));
    }
    
    private static String[] groupByDataSource(final Collection<String> actualDataNodeList) {
        List<String> result = new ArrayList<>();
        Multimap<String, String> multiMap = getNodeMultiMap(actualDataNodeList);
        for (String key : multiMap.keySet()) {
            List<String> list = new ArrayList<>();
            for (String value : multiMap.get(key)) {
                list.add(String.format("%s.%s", key, value));
            }
            result.add(String.join(",", list));
        }
        return result.toArray(new String[0]);
    }
    
    private static Multimap<String, String> getNodeMultiMap(final Collection<String> actualDataNodeList) {
        Multimap<String, String> result = HashMultimap.create();
        for (String actualDataNodes : actualDataNodeList) {
            for (String actualDataNode : actualDataNodes.split(",")) {
                String[] nodeArray = split(actualDataNode);
                for (String dataSource : new InlineExpressionParser(nodeArray[0]).splitAndEvaluate()) {
                    result.put(dataSource, nodeArray[1]);
                }
            }
        }
        return result;
    }
    
    private static String[] split(final String actualDataNode) {
        boolean flag = true;
        int i = 0;
        for (; i < actualDataNode.length(); i++) {
            char each = actualDataNode.charAt(i);
            if (each == '{') {
                flag = false;
            } else if (each == '}') {
                flag = true;
            } else if (flag && each == '.') {
                break;
            }
        }
        return new String[]{actualDataNode.substring(0, i), actualDataNode.substring(i + 1)};
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
        ShardingSphereJDBCDataSourceConfiguration sourceConfig = getSourceConfig(jobConfig);
        ShardingRuleConfiguration sourceRuleConfig = YamlConfigurationConverter.convertShardingRuleConfig(sourceConfig.getRootRuleConfigs().getRules());
        Map<String, DataSourceConfiguration> sourceDataSource = YamlConfigurationConverter.convertDataSourceConfigurations(sourceConfig.getRootRuleConfigs().getDataSources());
        Map<String, DataSource> dataSourceMap = sourceDataSource.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().createDataSource()));
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(new ShardingRule(sourceRuleConfig, sourceConfig.getDatabaseType(), dataSourceMap));
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfig(jobConfig);
        filterByShardingDataSourceTables(dataSourceTableNameMap, jobConfig.getHandleConfig());
        Map<String, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        for (Map.Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            DumperConfiguration dumperConfig = createDumperConfig(entry.getKey(), sourceDataSource.get(entry.getKey()).getProps(), entry.getValue());
            ImporterConfiguration importerConfig = createImporterConfig(jobConfig, shardingColumnsMap);
            result.add(new TaskConfiguration(jobConfig.getHandleConfig(), dumperConfig, importerConfig));
        }
        return result;
    }
    
    private static ShardingSphereJDBCDataSourceConfiguration getSourceConfig(final JobConfiguration jobConfig) {
        ScalingDataSourceConfiguration result = jobConfig.getRuleConfig().getSource().unwrap();
        Preconditions.checkArgument(result instanceof ShardingSphereJDBCDataSourceConfiguration, "Only support ShardingSphere source data source.");
        return (ShardingSphereJDBCDataSourceConfiguration) result;
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfig(final JobConfiguration jobConfig) {
        ScalingDataSourceConfiguration dataSourceConfig = jobConfig.getRuleConfig().getTarget().unwrap();
        if (dataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            return Optional.of(YamlConfigurationConverter.convertShardingRuleConfig(((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig).getRootRuleConfigs().getRules()));
        }
        return Optional.empty();
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> dataSourceTableNameMap, final HandleConfiguration handleConfig) {
        if (null == handleConfig.getShardingTables()) {
            return;
        }
        Map<String, Set<String>> shardingDataSourceTableMap = toDataSourceTableNameMap(getShardingDataSourceTables(handleConfig));
        dataSourceTableNameMap.entrySet().removeIf(entry -> !shardingDataSourceTableMap.containsKey(entry.getKey()));
        for (Map.Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            filterByShardingTables(entry.getValue(), shardingDataSourceTableMap.get(entry.getKey()));
        }
    }
    
    private static String getShardingDataSourceTables(final HandleConfiguration handleConfig) {
        if (handleConfig.getShardingItem() >= handleConfig.getShardingTables().length) {
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
        for (TableRule each : shardingRule.getTableRules()) {
            mergeDataSourceTableNameMap(result, toDataSourceTableNameMap(each));
        }
        return result;
    }
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final TableRule tableRule) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, Collection<String>> entry : tableRule.getDatasourceToTablesMap().entrySet()) {
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
        for (Map.Entry<String, Map<String, String>> entry : newDataSourceTableNameMap.entrySet()) {
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
            Set<String> shardingColumns = Sets.newHashSet();
            shardingColumns.addAll(null == each.getDatabaseShardingStrategy() ? defaultDatabaseShardingColumns : extractShardingColumns(each.getDatabaseShardingStrategy()));
            shardingColumns.addAll(null == each.getTableShardingStrategy() ? defaultTableShardingColumns : extractShardingColumns(each.getTableShardingStrategy()));
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
