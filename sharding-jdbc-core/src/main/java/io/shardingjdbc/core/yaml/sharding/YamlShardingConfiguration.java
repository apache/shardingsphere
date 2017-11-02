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

package io.shardingjdbc.core.yaml.sharding;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlShardingConfiguration {
    
    private Map<String, DataSource> dataSources = new HashMap<>();
    
    private YamlShardingRuleConfiguration shardingRule;
    
    /**
     * Get sharding rule from yaml.
     *
     * @param dataSourceMap data source map
     * @return sharding rule from yaml
     * @throws SQLException SQL exception
     */
    public ShardingRule getShardingRule(final Map<String, DataSource> dataSourceMap) throws SQLException {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        config.setDefaultDataSourceName(shardingRule.getDefaultDataSourceName());
        for (Map.Entry<String, YamlTableRuleConfiguration> entry : shardingRule.getTables().entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            config.getTableRuleConfigs().add(tableRuleConfig.build());
        }
        config.getBindingTableGroups().addAll(shardingRule.getBindingTables());
        if (null != shardingRule.getDefaultDatabaseStrategy()) {
            config.setDefaultDatabaseShardingStrategyConfig(shardingRule.getDefaultDatabaseStrategy().build());
        }
        if (null != shardingRule.getDefaultTableStrategy()) {
            config.setDefaultTableShardingStrategyConfig(shardingRule.getDefaultTableStrategy().build());
        }
        config.setDefaultKeyGeneratorClass(shardingRule.getDefaultKeyGeneratorClass());
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        for (Map.Entry<String, YamlMasterSlaveRuleConfiguration> each : shardingRule.getMasterSlaveRules().entrySet()) {
            MasterSlaveRuleConfiguration msRuleConfig = new MasterSlaveRuleConfiguration();
            msRuleConfig.setName(each.getKey());
            msRuleConfig.setMasterDataSourceName(each.getValue().getMasterDataSourceName());
            msRuleConfig.setSlaveDataSourceNames(each.getValue().getSlaveDataSourceNames());
            msRuleConfig.setLoadBalanceAlgorithmType(each.getValue().getLoadBalanceAlgorithmType());
            msRuleConfig.setLoadBalanceAlgorithmClassName(each.getValue().getLoadBalanceAlgorithmClassName());
            masterSlaveRuleConfigs.add(msRuleConfig);
        }
        config.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return config.build(dataSourceMap.isEmpty() ? dataSources : dataSourceMap);
    }
}
