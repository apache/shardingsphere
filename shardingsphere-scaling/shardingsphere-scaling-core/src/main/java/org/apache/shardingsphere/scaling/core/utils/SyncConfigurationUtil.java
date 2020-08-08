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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Sync configuration Util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SyncConfigurationUtil {
    
    /**
     * Split Scaling configuration to Sync configurations.
     *
     * @param scalingConfiguration scaling configuration
     * @return list of sync configurations
     */
    public static Collection<SyncConfiguration> toSyncConfigurations(final ScalingConfiguration scalingConfiguration) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        Map<String, DataSourceConfiguration> sourceDatasource = ConfigurationYamlConverter.loadDataSourceConfigurations(scalingConfiguration.getRuleConfiguration().getSourceDatasource());
        ShardingRuleConfiguration sourceRule = ConfigurationYamlConverter.loadShardingRuleConfiguration(scalingConfiguration.getRuleConfiguration().getSourceRule());
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(sourceRule, sourceDatasource.keySet());
        filterByShardingDataSourceTables(dataSourceTableNameMap, scalingConfiguration.getJobConfiguration());
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            RdbmsConfiguration dumperConfiguration = createDumperConfiguration(entry.getKey(), sourceDatasource.get(entry.getKey()));
            dumperConfiguration.setRetryTimes(scalingConfiguration.getJobConfiguration().getRetryTimes());
            RdbmsConfiguration importerConfiguration = createImporterConfiguration(scalingConfiguration, sourceRule);
            result.add(new SyncConfiguration(scalingConfiguration.getJobConfiguration().getConcurrency(), entry.getValue(), dumperConfiguration, importerConfiguration));
        }
        return result;
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> dataSourceTableNameMap, final JobConfiguration jobConfiguration) {
        if (null == jobConfiguration.getShardingTables()) {
            return;
        }
        Map<String, Set<String>> shardingDataSourceTableMap = toDataSourceTableNameMap(getShardingDataSourceTables(jobConfiguration));
        dataSourceTableNameMap.entrySet().removeIf(entry -> !shardingDataSourceTableMap.containsKey(entry.getKey()));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
            filterByShardingTables(entry.getValue(), shardingDataSourceTableMap.get(entry.getKey()));
        }
    }
    
    private static String getShardingDataSourceTables(final JobConfiguration jobConfiguration) {
        if (jobConfiguration.getShardingItem() >= jobConfiguration.getShardingTables().length) {
            return "";
        }
        return jobConfiguration.getShardingTables()[jobConfiguration.getShardingItem()];
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
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames) {
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        Map<String, Map<String, String>> result = new HashMap<>();
        for (TableRule each : shardingRule.getTableRules()) {
            mergeDataSourceTableNameMap(result, toDataSourceTableNameMap(each));
        }
        return result;
    }
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final TableRule tableRule) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Entry<String, Collection<String>> each : tableRule.getDatasourceToTablesMap().entrySet()) {
            Map<String, String> tableNameMap = result.get(each.getKey());
            if (null == tableNameMap) {
                result.put(each.getKey(), toTableNameMap(tableRule.getLogicTable(), each.getValue()));
            } else {
                tableNameMap.putAll(toTableNameMap(tableRule.getLogicTable(), each.getValue()));
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
        for (Entry<String, Map<String, String>> each : newDataSourceTableNameMap.entrySet()) {
            Map<String, String> tableNameMap = mergedResult.get(each.getKey());
            if (null == tableNameMap) {
                mergedResult.put(each.getKey(), each.getValue());
            } else {
                tableNameMap.putAll(each.getValue());
            }
        }
    }
    
    private static RdbmsConfiguration createDumperConfiguration(final String dataSourceName, final DataSourceConfiguration dataSourceConfiguration) {
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceName(dataSourceName);
        Map<String, Object> dataSourceProperties = dataSourceConfiguration.getProps();
        JDBCDataSourceConfiguration dumperDataSourceConfiguration = new JDBCDataSourceConfiguration(
                dataSourceProperties.containsKey("jdbcUrl") ? dataSourceProperties.get("jdbcUrl").toString() : dataSourceProperties.get("url").toString(),
                dataSourceProperties.get("username").toString(), dataSourceProperties.get("password").toString());
        result.setDataSourceConfiguration(dumperDataSourceConfiguration);
        return result;
    }
    
    private static RdbmsConfiguration createImporterConfiguration(final ScalingConfiguration scalingConfiguration, final ShardingRuleConfiguration shardingRuleConfig) {
        RdbmsConfiguration result = new RdbmsConfiguration();
        JDBCDataSourceConfiguration importerDataSourceConfiguration = new JDBCDataSourceConfiguration(
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUrl(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUsername(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getPassword());
        result.setDataSourceConfiguration(importerDataSourceConfiguration);
        result.setShardingColumnsMap(toShardingColumnsMap(shardingRuleConfig));
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
}
