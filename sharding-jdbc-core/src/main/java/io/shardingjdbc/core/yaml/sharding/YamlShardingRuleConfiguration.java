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
import io.shardingjdbc.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding rule configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlShardingRuleConfiguration {
    
    private String defaultDataSourceName;
    
    private Map<String, YamlTableRuleConfiguration> tables = new HashMap<>();
    
    private List<String> bindingTables = new ArrayList<>();
    
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    private String defaultKeyGeneratorClass;
    
    private Map<String, YamlMasterSlaveRuleConfiguration> masterSlaveRules = new HashMap<>();
    
    private Properties props = new Properties();
    
    /**
     * Get sharding rule configuration from yaml.
     *
     * @return sharding rule configuration from yaml
     */
    public ShardingRuleConfiguration getShardingRuleConfiguration() throws SQLException {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDataSourceName(defaultDataSourceName);
        for (Map.Entry<String, YamlTableRuleConfiguration> entry : tables.entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTableRuleConfigs().add(tableRuleConfig.build());
        }
        result.getBindingTableGroups().addAll(bindingTables);
        if (null != defaultDatabaseStrategy) {
            result.setDefaultDatabaseShardingStrategyConfig(defaultDatabaseStrategy.build());
        }
        if (null != defaultTableStrategy) {
            result.setDefaultTableShardingStrategyConfig(defaultTableStrategy.build());
        }
        result.setDefaultKeyGeneratorClass(defaultKeyGeneratorClass);
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        for (Map.Entry<String, YamlMasterSlaveRuleConfiguration> each : masterSlaveRules.entrySet()) {
            MasterSlaveRuleConfiguration msRuleConfig = new MasterSlaveRuleConfiguration();
            msRuleConfig.setName(each.getKey());
            msRuleConfig.setMasterDataSourceName(each.getValue().getMasterDataSourceName());
            msRuleConfig.setSlaveDataSourceNames(each.getValue().getSlaveDataSourceNames());
            msRuleConfig.setLoadBalanceAlgorithmType(each.getValue().getLoadBalanceAlgorithmType());
            msRuleConfig.setLoadBalanceAlgorithmClassName(each.getValue().getLoadBalanceAlgorithmClassName());
            masterSlaveRuleConfigs.add(msRuleConfig);
        }
        result.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return result;
    }
}
