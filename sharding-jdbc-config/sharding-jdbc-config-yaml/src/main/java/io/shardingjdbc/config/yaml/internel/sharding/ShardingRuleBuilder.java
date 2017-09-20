/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.config.yaml.internel.sharding;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.config.yaml.internel.ms.YamlMasterSlaveConfig;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding rule builder from yaml.
 *
 * @author caohao
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingRuleBuilder {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final YamlShardingConfig yamlShardingConfig;
    
    /**
     * Build sharding rule from yaml.
     * 
     * @return sharding rule from yaml
     * @throws SQLException SQL exception
     */
    public ShardingRule build() throws SQLException {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDataSourceName(yamlShardingConfig.getDefaultDataSourceName());
        for (Entry<String, YamlTableRuleConfig> entry : yamlShardingConfig.getTables().entrySet()) {
            YamlTableRuleConfig tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTableRuleConfigs().add(tableRuleConfig.getTableRuleConfig());
        }
        result.getBindingTableGroups().addAll(yamlShardingConfig.getBindingTables());
        if (null != yamlShardingConfig.getDefaultDatabaseStrategy()) {
            result.setDefaultDatabaseShardingStrategyConfig(yamlShardingConfig.getDefaultDatabaseStrategy().getShardingStrategy());
        }
        if (null != yamlShardingConfig.getDefaultTableStrategy()) {
            result.setDefaultTableShardingStrategyConfig(yamlShardingConfig.getDefaultTableStrategy().getShardingStrategy());
        }
        result.setDefaultKeyGeneratorClass(yamlShardingConfig.getDefaultKeyGeneratorClass());
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        for (Map.Entry<String, YamlMasterSlaveConfig> each : yamlShardingConfig.getMasterSlaveRules().entrySet()) {
            MasterSlaveRuleConfiguration config = new MasterSlaveRuleConfiguration();
            config.setName(each.getKey());
            config.setMasterDataSourceName(each.getValue().getMasterDataSourceName());
            config.setSlaveDataSourceNames(each.getValue().getSlaveDataSourceNames());
            config.setMasterSlaveLoadBalanceStrategyClassName(each.getValue().getMasterSlaveLoadBalanceStrategyClassName());
            masterSlaveRuleConfigs.add(config);
        }
        result.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return result.build(dataSourceMap.isEmpty() ? yamlShardingConfig.getDataSources() : dataSourceMap);
    }
}
