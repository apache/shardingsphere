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

package com.dangdang.ddframe.rdb.sharding.api.config;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public class ShardingRuleConfig {
    
    private Map<String, DataSource> dataSources = new HashMap<>();
    
    private String defaultDataSourceName;
    
    private Collection<TableRuleConfig> tableRuleConfigs = new LinkedList<>();
    
    private Collection<String> bindingTableGroups = new LinkedList<>();
    
    private ShardingStrategyConfig defaultDatabaseShardingStrategyConfig;
    
    private ShardingStrategyConfig defaultTableShardingStrategyConfig;
    
    private String defaultKeyGeneratorClass;
    
    /**
     * Build sharding rule.
     *
     * @return sharding rule
     */
    public ShardingRule build() {
        // TODO should not be null, for parsing only
        // Preconditions.checkNotNull(dataSources, "dataSources cannot be null.");
        DataSourceRule dataSourceRule = new DataSourceRule(dataSources, defaultDataSourceName);
        Collection<TableRule> tableRules = new LinkedList<>();
        for (TableRuleConfig each : tableRuleConfigs) {
            tableRules.add(each.build(dataSources));
        }
        ShardingStrategy defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategyConfig ? null : defaultDatabaseShardingStrategyConfig.build();
        ShardingStrategy defaultTableShardingStrategy = null == defaultTableShardingStrategyConfig ? null :  defaultTableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = KeyGeneratorFactory.newInstance(null == defaultKeyGeneratorClass ? DefaultKeyGenerator.class.getName() : defaultKeyGeneratorClass);
        return new ShardingRule(dataSourceRule, tableRules, bindingTableGroups, defaultDatabaseShardingStrategy, defaultTableShardingStrategy, keyGenerator);
    }
}
