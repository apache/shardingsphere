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
import io.shardingjdbc.core.yaml.masterslave.YamMasterSlaveRuleConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlShardingRuleConfiguration {
    
    private Map<String, DataSource> dataSources = new HashMap<>();
    
    private String defaultDataSourceName;
    
    private Map<String, YamlTableRuleConfiguration> tables = new HashMap<>();
    
    private List<String> bindingTables = new ArrayList<>();
    
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    private String defaultKeyGeneratorClass;
    
    private Map<String, YamMasterSlaveRuleConfiguration> masterSlaveRules = new HashMap<>();
    
    private Properties props = new Properties();
    
    /**
     * Get sharding rule from yaml.
     *
     * @param dataSourceMap data source map
     * @return sharding rule from yaml
     * @throws SQLException SQL exception
     */
    public ShardingRule getShardingRule(final Map<String, DataSource> dataSourceMap) throws SQLException {
        return getShardingRuleConfiguration().build(dataSourceMap.isEmpty() ? dataSources : dataSourceMap);
    }
    
    /**
     * Get sharding rule configuration from yaml.
     *
     * @return sharding rule configuration from yaml
     */
    public ShardingRuleConfiguration getShardingRuleConfiguration() {
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
        for (Map.Entry<String, YamMasterSlaveRuleConfiguration> each : masterSlaveRules.entrySet()) {
            MasterSlaveRuleConfiguration config = new MasterSlaveRuleConfiguration();
            config.setName(each.getKey());
            config.setMasterDataSourceName(each.getValue().getMasterDataSourceName());
            config.setSlaveDataSourceNames(each.getValue().getSlaveDataSourceNames());
            config.setLoadBalanceAlgorithmType(each.getValue().getLoadBalanceAlgorithmType());
            config.setLoadBalanceAlgorithmClassName(each.getValue().getLoadBalanceAlgorithmClassName());
            masterSlaveRuleConfigs.add(config);
        }
        result.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return result;
    }
}
