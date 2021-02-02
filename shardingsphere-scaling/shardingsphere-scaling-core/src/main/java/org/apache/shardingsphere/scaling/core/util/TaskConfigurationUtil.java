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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ConfigurationYamlConverter;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import javax.sql.DataSource;
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
import java.util.stream.Collectors;

/**
 * Task configuration Util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskConfigurationUtil {
    
    /**
     * Split job configuration to task configurations.
     *
     * @param jobConfig job configuration
     * @return list of task configurations
     */
    public static List<TaskConfiguration> toTaskConfigs(final JobConfiguration jobConfig) {
        List<TaskConfiguration> result = new LinkedList<>();
        ShardingSphereJDBCDataSourceConfiguration sourceConfig = getSourceConfig(jobConfig);
        ShardingRuleConfiguration sourceRuleConfig = ConfigurationYamlConverter.loadShardingRuleConfig(sourceConfig.getRule());
        Map<String, DataSourceConfiguration> sourceDataSource = ConfigurationYamlConverter.loadDataSourceConfigs(sourceConfig.getDataSource());
        Map<String, DataSource> dataSourceMap = sourceDataSource.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().createDataSource()));
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(new ShardingRule(sourceRuleConfig, sourceConfig.getDatabaseType(), dataSourceMap));
        Optional<ShardingRuleConfiguration> targetRuleConfig = getTargetRuleConfig(jobConfig);
        filterByShardingDataSourceTables(dataSourceTableNameMap, jobConfig.getHandleConfig());
        Map<String, Set<String>> shardingColumnsMap = getShardingColumnsMap(targetRuleConfig.orElse(sourceRuleConfig));
        for (Entry<String, Map<String, String>> entry : dataSourceTableNameMap.entrySet()) {
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
            return Optional.of(ConfigurationYamlConverter.loadShardingRuleConfig(((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig).getRule()));
        }
        return Optional.empty();
    }
    
    private static void filterByShardingDataSourceTables(final Map<String, Map<String, String>> dataSourceTableNameMap, final HandleConfiguration handleConfig) {
        if (null == handleConfig.getShardingTables() || null == handleConfig.getShardingItem()) {
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
        StandardJDBCDataSourceConfiguration dumperDataSourceConfig = new StandardJDBCDataSourceConfiguration(
                props.containsKey("jdbcUrl") ? props.get("jdbcUrl").toString() : props.get("url").toString(), props.get("username").toString(), props.get("password").toString());
        result.setDataSourceConfig(dumperDataSourceConfig);
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
