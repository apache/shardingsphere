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

package org.apache.shardingsphere.shardingscaling.core.config.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingscaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        for (String each : dataSourceTableNameMap.keySet()) {
            RdbmsConfiguration readerConfiguration = createReaderConfiguration(sourceDatasource.get(each));
            RdbmsConfiguration writerConfiguration = createWriterConfiguration(scalingConfiguration);
            Map<String, String> tableNameMap = dataSourceTableNameMap.get(each);
            result.add(new SyncConfiguration(scalingConfiguration.getJobConfiguration().getConcurrency(), tableNameMap, readerConfiguration, writerConfiguration));
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
        for (Map.Entry<String, Collection<String>> each : tableRule.getDatasourceToTablesMap().entrySet()) {
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
        for (Map.Entry<String, Map<String, String>> each : newDataSourceTableNameMap.entrySet()) {
            Map<String, String> tableNameMap = mergedResult.get(each.getKey());
            if (null == tableNameMap) {
                mergedResult.put(each.getKey(), each.getValue());
            } else {
                tableNameMap.putAll(each.getValue());
            }
        }
    }
    
    private static RdbmsConfiguration createReaderConfiguration(final DataSourceConfiguration dataSourceConfiguration) {
        RdbmsConfiguration result = new RdbmsConfiguration();
        Map<String, Object> dataSourceProperties = dataSourceConfiguration.getProperties();
        JDBCDataSourceConfiguration readerDataSourceConfiguration = new JDBCDataSourceConfiguration(
                dataSourceProperties.containsKey("jdbcUrl") ? dataSourceProperties.get("jdbcUrl").toString() : dataSourceProperties.get("url").toString(),
                dataSourceProperties.get("username").toString(), dataSourceProperties.get("password").toString());
        result.setDataSourceConfiguration(readerDataSourceConfiguration);
        return result;
    }
    
    private static RdbmsConfiguration createWriterConfiguration(final ScalingConfiguration scalingConfiguration) {
        RdbmsConfiguration result = new RdbmsConfiguration();
        JDBCDataSourceConfiguration writerDataSourceConfiguration = new JDBCDataSourceConfiguration(
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUrl(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUsername(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getPassword());
        result.setDataSourceConfiguration(writerDataSourceConfiguration);
        return result;
    }
}
