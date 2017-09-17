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

package com.dangdang.ddframe.rdb.sharding.config.yaml.internel.sharding;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding rule builder from yaml.
 *
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
     */
    public ShardingRule build() throws SQLException {
        ShardingRuleConfig result = new ShardingRuleConfig();
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
        return result.build(dataSourceMap.isEmpty() ? yamlShardingConfig.getDataSources() : dataSourceMap);
    }
}
