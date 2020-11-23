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

package org.apache.shardingsphere.scaling.core.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sync configuration Util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SyncConfigurationUtil {
    
    /**
     * Split Scaling configuration to Sync configurations.
     *
     * @param scalingConfig scaling configuration
     * @return list of sync configurations
     */
    public static Collection<SyncConfiguration> toSyncConfigs(final ScalingConfiguration scalingConfig) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        ShardingSphereJDBCDataSourceConfiguration sourceConfig = getSourceConfig(scalingConfig);
        ShardingRuleConfiguration sourceRuleConfig = ConfigurationYamlConverter.loadShardingRuleConfig(sourceConfig.getRule());
        Map<String, org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration> sourceDataSource = ConfigurationYamlConverter.loadDataSourceConfigs(sourceConfig.getDataSource());
        Map<String, DataSource> dataSourceMap = sourceDataSource.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().createDataSource()));
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(new ShardingRule(sourceRuleConfig, sourceConfig.getDatabaseType(), dataSourceMap));
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfig(scalingConfig);
        filterByShardingDataSourceTables(dataSourceTableNameMap, scalingConfig.getJobConfiguration());
        Map<String, Set<String>> shardingColumnsMap = toShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            DumperConfiguration dumperConfig = createDumperConfig(entry.getKey(), sourceDataSource.get(entry.getKey()).getProps(), entry.getValue());
            ImporterConfiguration importerConfig = createImporterConfig(scalingConfig, shardingColumnsMap);
            result.add(new SyncConfiguration(scalingConfig.getJobConfiguration().getConcurrency(), dumperConfig, importerConfig));
        }
        return result;
    }
    
    private static ShardingSphereJDBCDataSourceConfiguration getSourceConfig(final ScalingConfiguration scalingConfig) {
        DataSourceConfiguration result = scalingConfig.getRuleConfiguration().getSource().unwrap();
        Preconditions.checkArgument(result instanceof ShardingSphereJDBCDataSourceConfiguration, "Only support ShardingSphere source data source.");
        return (ShardingSphereJDBCDataSourceConfiguration) result;
    }
    
    private static Optional<ShardingRuleConfiguration> getTargetRuleConfig(final ScalingConfiguration scalingConfig) {
        DataSourceConfiguration dataSourceConfig = scalingConfig.getRuleConfiguration().getTarget().unwrap();
        if (dataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            return Optional.of(ConfigurationYamlConverter.loadShardingRuleConfig(((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig).getRule()));
        }
        return Optional.empty();
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> dataSourceTableNameMap, final JobConfiguration jobConfig) {
        if (null == jobConfig.getShardingTables()) {
            return;
        }
        Map<String, Set<String>> shardingDataSourceTableMap = toDataSourceTableNameMap(getShardingDataSourceTables(jobConfig));
        dataSourceTableNameMap.entrySet().removeIf(entry -> !shardingDataSourceTableMap.containsKey(entry.getKey()));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            filterByShardingTables(entry.getValue(), shardingDataSourceTableMap.get(entry.getKey()));
        }
    }
    
    private static String getShardingDataSourceTables(final JobConfiguration jobConfig) {
        if (jobConfig.getShardingItem() >= jobConfig.getShardingTables().length) {
            return "";
        }
        return jobConfig.getShardingTables()[jobConfig.getShardingItem()];
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
    
    private static DumperConfiguration createDumperConfig(final String dataSourceName, final Map<String, Object> props, final Map<String, String> tableMap) {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName(dataSourceName);
        StandardJDBCDataSourceConfiguration dumperDataSourceConfig = new StandardJDBCDataSourceConfiguration(
                props.containsKey("jdbcUrl") ? props.get("jdbcUrl").toString() : props.get("url").toString(), props.get("username").toString(), props.get("password").toString());
        result.setDataSourceConfig(dumperDataSourceConfig);
        result.setTableNameMap(tableMap);
        return result;
    }
    
    private static ImporterConfiguration createImporterConfig(final ScalingConfiguration scalingConfig, final Map<String, Set<String>> shardingRuleConfig) {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfig(scalingConfig.getRuleConfiguration().getTarget().unwrap());
        result.setShardingColumnsMap(shardingRuleConfig);
        result.setRetryTimes(scalingConfig.getJobConfiguration().getRetryTimes());
        return result;
    }
    
    private static Map<String, Set<String>> toShardingColumnsMap(final ShardingRuleConfiguration shardingRuleConfig) {
        Map<String, Set<String>> result = Maps.newConcurrentMap();
        for (ShardingTableRuleConfiguration each : shardingRuleConfig.getTables()) {
            Set<String> shardingColumns = Sets.newHashSet();
            shardingColumns.addAll(extractShardingColumns(each.getDatabaseShardingStrategy()));
            shardingColumns.addAll(extractShardingColumns(each.getTableShardingStrategy()));
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
    
    /**
     * Fill in sharding tables.
     *
     * @param scalingConfig scaling configuration
     */
    public static void fillInShardingTables(final ScalingConfiguration scalingConfig) {
        if (null != scalingConfig.getJobConfiguration().getShardingTables()) {
            return;
        }
        scalingConfig.getJobConfiguration().setShardingTables(groupByDataSource(getShouldScalingActualDataNodes(scalingConfig)));
    }
    
    private static List<String> getShouldScalingActualDataNodes(final ScalingConfiguration scalingConfig) {
        DataSourceConfiguration sourceConfig = scalingConfig.getRuleConfiguration().getSource().unwrap();
        Preconditions.checkState(sourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration,
                "Only ShardingSphereJdbc type of source ScalingDataSourceConfiguration is supported.");
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        if (!(scalingConfig.getRuleConfiguration().getTarget().unwrap() instanceof ShardingSphereJDBCDataSourceConfiguration)) {
            return getShardingRuleConfigMap(source.getRule()).values().stream().map(ShardingTableRuleConfiguration::getActualDataNodes).collect(Collectors.toList());
        }
        ShardingSphereJDBCDataSourceConfiguration target =
                (ShardingSphereJDBCDataSourceConfiguration) scalingConfig.getRuleConfiguration().getTarget().unwrap();
        List<String> result = new ArrayList<>();
        Set<String> modifiedDataSources = getModifiedDataSources(source.getDataSource(), target.getDataSource());
        Map<String, ShardingTableRuleConfiguration> oldShardingRuleConfigMap = getShardingRuleConfigMap(source.getRule());
        Map<String, ShardingTableRuleConfiguration> newShardingRuleConfigMap = getShardingRuleConfigMap(target.getRule());
        newShardingRuleConfigMap.keySet().forEach(each -> {
            if (!oldShardingRuleConfigMap.containsKey(each)) {
                return;
            }
            List<String> oldActualDataNodes = new InlineExpressionParser(oldShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            List<String> newActualDataNodes = new InlineExpressionParser(newShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            if (!CollectionUtils.isEqualCollection(oldActualDataNodes, newActualDataNodes) || includeModifiedDataSources(newActualDataNodes, modifiedDataSources)) {
                result.add(newShardingRuleConfigMap.get(each).getActualDataNodes());
            }
        });
        return result;
    }
    
    private static Set<String> getModifiedDataSources(final String oldConfig, final String newConfig) {
        Set<String> result = new HashSet<>();
        Map<String, String> oldDataSourceUrlMap = getDataSourceUrlMap(oldConfig);
        Map<String, String> newDataSourceUrlMap = getDataSourceUrlMap(newConfig);
        newDataSourceUrlMap.forEach((key, value) -> {
            if (!value.equals(oldDataSourceUrlMap.get(key))) {
                result.add(key);
            }
        });
        return result;
    }
    
    private static Map<String, String> getDataSourceUrlMap(final String configuration) {
        Map<String, String> result = new HashMap<>();
        ConfigurationYamlConverter.loadDataSourceConfigs(configuration).forEach((key, value) -> {
            JdbcUri uri = new JdbcUri(value.getProps().getOrDefault("url", value.getProps().get("jdbcUrl")).toString());
            result.put(key, String.format("%s/%s", uri.getHost(), uri.getDatabase()));
        });
        return result;
    }
    
    private static boolean includeModifiedDataSources(final List<String> actualDataNodes, final Set<String> modifiedDataSources) {
        return actualDataNodes.stream().anyMatch(each -> modifiedDataSources.contains(each.split("\\.")[0]));
    }
    
    private static Map<String, ShardingTableRuleConfiguration> getShardingRuleConfigMap(final String configuration) {
        ShardingRuleConfiguration oldShardingRuleConfig = ConfigurationYamlConverter.loadShardingRuleConfig(configuration);
        return oldShardingRuleConfig.getTables().stream().collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, Function.identity()));
    }
    
    private static String[] groupByDataSource(final List<String> actualDataNodeList) {
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
    
    private static Multimap<String, String> getNodeMultiMap(final List<String> actualDataNodeList) {
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
}
