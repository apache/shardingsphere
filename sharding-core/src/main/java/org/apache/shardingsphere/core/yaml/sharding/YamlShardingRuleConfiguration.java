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

package org.apache.shardingsphere.core.yaml.sharding;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding rule configuration for YAML.
 *
 * @author caohao
 * @author panjuan
 * @author maxiaoguang
 */
@NoArgsConstructor
@Getter
@Setter
public class YamlShardingRuleConfiguration {
    
    private String defaultDataSourceName;
    
    private Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    private Collection<String> bindingTables = new ArrayList<>();
    
    private Collection<String> broadcastTables = new ArrayList<>();
    
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    private YamlKeyGeneratorConfiguration defaultKeyGenerator;
    
    private Map<String, YamlMasterSlaveRuleConfiguration> masterSlaveRules = new LinkedHashMap<>();
    
    public YamlShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfiguration) {
        defaultDataSourceName = shardingRuleConfiguration.getDefaultDataSourceName();
        for (TableRuleConfiguration each : shardingRuleConfiguration.getTableRuleConfigs()) {
            tables.put(each.getLogicTable(), new YamlTableRuleConfiguration(each));
        }
        bindingTables.addAll(shardingRuleConfiguration.getBindingTableGroups());
        bindingTables.addAll(shardingRuleConfiguration.getBroadcastTables());
        defaultDatabaseStrategy = new YamlShardingStrategyConfiguration(shardingRuleConfiguration.getDefaultDatabaseShardingStrategyConfig());
        defaultTableStrategy = new YamlShardingStrategyConfiguration(shardingRuleConfiguration.getDefaultTableShardingStrategyConfig());
        defaultKeyGenerator = null == shardingRuleConfiguration.getDefaultKeyGeneratorConfig() ? null : new YamlKeyGeneratorConfiguration(shardingRuleConfiguration.getDefaultKeyGeneratorConfig());
        for (MasterSlaveRuleConfiguration each : shardingRuleConfiguration.getMasterSlaveRuleConfigs()) {
            masterSlaveRules.put(each.getName(), new YamlMasterSlaveRuleConfiguration(each));
        }
    }
    
    /**
     * Get sharding rule configuration.
     *
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDataSourceName(defaultDataSourceName);
        for (Entry<String, YamlTableRuleConfiguration> entry : tables.entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTableRuleConfigs().add(tableRuleConfig.build());
        }
        result.getBindingTableGroups().addAll(bindingTables);
        result.getBroadcastTables().addAll(broadcastTables);
        if (null != defaultDatabaseStrategy) {
            result.setDefaultDatabaseShardingStrategyConfig(defaultDatabaseStrategy.build());
        }
        if (null != defaultTableStrategy) {
            result.setDefaultTableShardingStrategyConfig(defaultTableStrategy.build());
        }
        if (null != defaultKeyGenerator) {
            result.setDefaultKeyGeneratorConfig(defaultKeyGenerator.getKeyGeneratorConfiguration());
        }
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveRuleConfiguration> entry : masterSlaveRules.entrySet()) {
            YamlMasterSlaveRuleConfiguration each = entry.getValue();
            each.setName(entry.getKey());
            masterSlaveRuleConfigs.add(entry.getValue().getMasterSlaveRuleConfiguration());
        }
        result.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return result;
    }
}
