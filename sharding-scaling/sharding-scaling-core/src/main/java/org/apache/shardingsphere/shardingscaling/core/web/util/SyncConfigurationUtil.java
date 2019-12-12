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

package org.apache.shardingsphere.shardingscaling.core.web.util;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingscaling.core.config.yaml.YamlProxyRuleConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SyncConfiguration Util.
 *
 * @author ssxlulu
 */
public class SyncConfigurationUtil {
    
    /**
     * Split ScalingConfiguration to SyncConfigurations.
     *
     * @param scalingConfiguration ScalingConfiguration
     * @return List of SyncConfigurations
     */
    public static Collection<SyncConfiguration> toSyncConfigurations(final ScalingConfiguration scalingConfiguration) {
        Collection<SyncConfiguration> result = new LinkedList<>();
        YamlProxyRuleConfiguration ruleConfiguration = loadTestYamlProxyRuleConfiguration();
        Map<String, YamlDataSourceParameter> dataSources = getDataSources(ruleConfiguration);
        Map<String, Map<String, String>> dataSourceTableNameMap = toDataSourceTableNameMap(ruleConfiguration);
        for (String each : dataSourceTableNameMap.keySet()) {
            RdbmsConfiguration readerConfiguration = createReaderConfiguration(dataSources.get(each));
            RdbmsConfiguration writerConfiguration = createWriterConfiguration(scalingConfiguration);
            Map<String, String> tableNameMap = dataSourceTableNameMap.get(each);
            if (null == tableNameMap) {
                tableNameMap = new HashMap<>();
            }
            result.add(new SyncConfiguration(scalingConfiguration.getJobConfiguration().getConcurrency(), tableNameMap,
                    readerConfiguration, writerConfiguration));
        }
        return result;
    }
    
    private static RdbmsConfiguration createReaderConfiguration(final YamlDataSourceParameter dataSource) {
        RdbmsConfiguration result = new RdbmsConfiguration();
        DataSourceConfiguration readerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                dataSource.getUrl(),
                dataSource.getUsername(),
                dataSource.getPassword());
        result.setDataSourceConfiguration(readerDataSourceConfiguration);
        return result;
    }
    
    private static RdbmsConfiguration createWriterConfiguration(final ScalingConfiguration scalingConfiguration) {
        RdbmsConfiguration writerConfiguration = new RdbmsConfiguration();
        DataSourceConfiguration writerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUrl(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getUsername(),
                scalingConfiguration.getRuleConfiguration().getDestinationDataSources().getPassword());
        writerConfiguration.setDataSourceConfiguration(writerDataSourceConfiguration);
        return writerConfiguration;
    }
    
    private static YamlProxyRuleConfiguration loadTestYamlProxyRuleConfiguration() {
        try {
            return YamlEngine.unmarshal(new File(SyncConfigurationUtil.class.getResource("/conf/config-sharding.yaml").getFile()),
                    YamlProxyRuleConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Map<String, YamlDataSourceParameter> getDataSources(final YamlProxyRuleConfiguration ruleConfiguration) {
        return ruleConfiguration.getDataSources();
    }
    
    private static ShardingRule getShardingRule(final YamlProxyRuleConfiguration ruleConfiguration) {
        return new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(ruleConfiguration.getShardingRule()), getDataSources(ruleConfiguration).keySet());
    }
    
    private static Map<String, Map<String, String>> toDataSourceTableNameMap(final YamlProxyRuleConfiguration ruleConfiguration) {
        ShardingRule shardingRule = getShardingRule(ruleConfiguration);
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
    
    private static void mergeDataSourceTableNameMap(final Map<String, Map<String, String>> o, final Map<String, Map<String, String>> n) {
        for (Map.Entry<String, Map<String, String>> each : n.entrySet()) {
            Map<String, String> tableNameMap = o.get(each.getKey());
            if (null == tableNameMap) {
                o.put(each.getKey(), each.getValue());
            } else {
                tableNameMap.putAll(each.getValue());
            }
        }
    }
    
    private static Map<String, String> toTableNameMap(final String logicalTable, final Collection<String> actualTables) {
        Map<String, String> result = new HashMap<>();
        for (String each : actualTables) {
            result.put(each, logicalTable);
        }
        return result;
    }
}
