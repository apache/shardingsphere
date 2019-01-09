/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.keygen.BuiltinKeyGeneratorType;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding rule configuration for yaml.
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
    
    private YamlKeyGeneratorConfiguration defaultKeyGenerator = new YamlKeyGeneratorConfiguration();
    
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
        defaultKeyGenerator = null == shardingRuleConfiguration.getDefaultKeyGenerator() ? null : getYamlKeyGeneratorConfiguration(shardingRuleConfiguration);
        for (MasterSlaveRuleConfiguration each : shardingRuleConfiguration.getMasterSlaveRuleConfigs()) {
            masterSlaveRules.put(each.getName(), new YamlMasterSlaveRuleConfiguration(each));
        }
    }
    
    private YamlKeyGeneratorConfiguration getYamlKeyGeneratorConfiguration(final ShardingRuleConfiguration shardingRuleConfiguration) {
        YamlKeyGeneratorConfiguration result = new YamlKeyGeneratorConfiguration();
        String keyGeneratorClassName = shardingRuleConfiguration.getDefaultKeyGenerator().getClass().getName();
        BuiltinKeyGeneratorType keyGeneratorType = BuiltinKeyGeneratorType.getBuiltinKeyGeneratorType(keyGeneratorClassName);
        if (null == keyGeneratorType) {
            result.setKeyGeneratorClassName(keyGeneratorClassName);
        } else {
            result.setKeyGeneratorType(keyGeneratorType.name());
        }
        result.setProps(shardingRuleConfiguration.getDefaultKeyGenerator().getKeyGeneratorProperties());
        return result;
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
        result.setDefaultKeyGenerator(defaultKeyGenerator.getKeyGenerator());
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
