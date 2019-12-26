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

package org.apache.shardingsphere.core.yaml.swapper;

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Sharding rule configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class ShardingRuleConfigurationYamlSwapper implements YamlSwapper<YamlShardingRuleConfiguration, ShardingRuleConfiguration> {
    
    private final TableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper = new TableRuleConfigurationYamlSwapper();
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGeneratorConfigurationYamlSwapper keyGeneratorConfigurationYamlSwapper = new KeyGeneratorConfigurationYamlSwapper();
    
    private final MasterSlaveRuleConfigurationYamlSwapper masterSlaveRuleConfigurationYamlSwapper = new MasterSlaveRuleConfigurationYamlSwapper();
    
    private final EncryptRuleConfigurationYamlSwapper encryptRuleConfigurationYamlSwapper = new EncryptRuleConfigurationYamlSwapper();
    
    @Override
    public YamlShardingRuleConfiguration swap(final ShardingRuleConfiguration data) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (TableRuleConfiguration each : data.getTableRuleConfigs()) {
            result.getTables().put(each.getLogicTable(), tableRuleConfigurationYamlSwapper.swap(each));
        }
        result.getBindingTables().addAll(data.getBindingTableGroups());
        result.getBroadcastTables().addAll(data.getBroadcastTables());
        result.setDefaultDataSourceName(data.getDefaultDataSourceName());
        if (null != data.getDefaultDatabaseShardingStrategyConfig()) {
            result.setDefaultDatabaseStrategy(shardingStrategyConfigurationYamlSwapper.swap(data.getDefaultDatabaseShardingStrategyConfig()));
        }
        if (null != data.getDefaultTableShardingStrategyConfig()) {
            result.setDefaultTableStrategy(shardingStrategyConfigurationYamlSwapper.swap(data.getDefaultTableShardingStrategyConfig()));
        }
        if (null != data.getDefaultKeyGeneratorConfig()) {
            result.setDefaultKeyGenerator(keyGeneratorConfigurationYamlSwapper.swap(data.getDefaultKeyGeneratorConfig()));
        }
        for (MasterSlaveRuleConfiguration each : data.getMasterSlaveRuleConfigs()) {
            result.getMasterSlaveRules().put(each.getName(), masterSlaveRuleConfigurationYamlSwapper.swap(each));
        }
        if (null != data.getEncryptRuleConfig()) {
            result.setEncryptRule(encryptRuleConfigurationYamlSwapper.swap(data.getEncryptRuleConfig()));
        }
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration swap(final YamlShardingRuleConfiguration yamlConfiguration) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (Entry<String, YamlTableRuleConfiguration> entry : yamlConfiguration.getTables().entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTableRuleConfigs().add(tableRuleConfigurationYamlSwapper.swap(tableRuleConfig));
        }
        result.setDefaultDataSourceName(yamlConfiguration.getDefaultDataSourceName());
        result.getBindingTableGroups().addAll(yamlConfiguration.getBindingTables());
        result.getBroadcastTables().addAll(yamlConfiguration.getBroadcastTables());
        if (null != yamlConfiguration.getDefaultDatabaseStrategy()) {
            result.setDefaultDatabaseShardingStrategyConfig(shardingStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultDatabaseStrategy()));
        }
        if (null != yamlConfiguration.getDefaultTableStrategy()) {
            result.setDefaultTableShardingStrategyConfig(shardingStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultTableStrategy()));
        }
        if (null != yamlConfiguration.getDefaultKeyGenerator()) {
            result.setDefaultKeyGeneratorConfig(keyGeneratorConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultKeyGenerator()));
        }
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveRuleConfiguration> entry : yamlConfiguration.getMasterSlaveRules().entrySet()) {
            YamlMasterSlaveRuleConfiguration each = entry.getValue();
            each.setName(entry.getKey());
            masterSlaveRuleConfigs.add(masterSlaveRuleConfigurationYamlSwapper.swap(entry.getValue()));
        }
        result.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        if (null != yamlConfiguration.getEncryptRule()) {
            result.setEncryptRuleConfig(encryptRuleConfigurationYamlSwapper.swap(yamlConfiguration.getEncryptRule()));
        }
        return result;
    }
}
