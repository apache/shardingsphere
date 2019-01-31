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

package org.apache.shardingsphere.orchestration.yaml.dumper.impl;

import org.apache.shardingsphere.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlEncryptorConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlTableRuleConfiguration;
import org.apache.shardingsphere.orchestration.yaml.dumper.DefaultYamlRepresenter;
import org.apache.shardingsphere.orchestration.yaml.dumper.YamlDumper;
import org.yaml.snakeyaml.Yaml;

/**
 * Sharding configuration YAML dumper.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class ShardingRuleConfigurationYamlDumper implements YamlDumper<ShardingRuleConfiguration> {
    
    @Override
    public String dump(final ShardingRuleConfiguration shardingRuleConfiguration) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(createYamlShardingRuleConfiguration(shardingRuleConfiguration));
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfiguration) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (TableRuleConfiguration each : shardingRuleConfiguration.getTableRuleConfigs()) {
            result.getTables().put(each.getLogicTable(), createYamlTableRuleConfiguration(each));
        }
        result.getBindingTables().addAll(shardingRuleConfiguration.getBindingTableGroups());
        result.getBroadcastTables().addAll(shardingRuleConfiguration.getBroadcastTables());
        result.setDefaultDataSourceName(shardingRuleConfiguration.getDefaultDataSourceName());
        result.setDefaultDatabaseStrategy(new YamlShardingStrategyConfiguration(shardingRuleConfiguration.getDefaultDatabaseShardingStrategyConfig()));
        result.setDefaultTableStrategy(new YamlShardingStrategyConfiguration(shardingRuleConfiguration.getDefaultTableShardingStrategyConfig()));
        if (null != shardingRuleConfiguration.getDefaultKeyGeneratorConfig()) {
            result.setDefaultKeyGenerator(createYamlKeyGeneratorConfiguration(shardingRuleConfiguration.getDefaultKeyGeneratorConfig()));
        }
        for (MasterSlaveRuleConfiguration each : shardingRuleConfiguration.getMasterSlaveRuleConfigs()) {
            result.getMasterSlaveRules().put(each.getName(), new YamlMasterSlaveRuleConfiguration(each));
        }
        return result;
    }
    
    private YamlTableRuleConfiguration createYamlTableRuleConfiguration(final TableRuleConfiguration tableRuleConfiguration) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(tableRuleConfiguration.getLogicTable());
        result.setActualDataNodes(tableRuleConfiguration.getActualDataNodes());
        result.setDatabaseStrategy(new YamlShardingStrategyConfiguration(tableRuleConfiguration.getDatabaseShardingStrategyConfig()));
        result.setTableStrategy(new YamlShardingStrategyConfiguration(tableRuleConfiguration.getTableShardingStrategyConfig()));
        if (null != tableRuleConfiguration.getKeyGeneratorConfig()) {
            result.setKeyGenerator(createYamlKeyGeneratorConfiguration(tableRuleConfiguration.getKeyGeneratorConfig()));
        }
        if (null != tableRuleConfiguration.getEncryptorConfig()) {
            result.setEncryptor(new YamlEncryptorConfiguration(tableRuleConfiguration.getEncryptorConfig()));
        }
        return result;
    }
    
    private YamlKeyGeneratorConfiguration createYamlKeyGeneratorConfiguration(final KeyGeneratorConfiguration keyGeneratorConfiguration) {
        YamlKeyGeneratorConfiguration result = new YamlKeyGeneratorConfiguration();
        result.setColumn(keyGeneratorConfiguration.getColumn());
        result.setType(keyGeneratorConfiguration.getType());
        result.setProps(keyGeneratorConfiguration.getProps());
        return result;
    }
}
