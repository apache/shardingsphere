/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.api.config;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.keygen.DefaultKeyGenerator;
import io.shardingjdbc.core.keygen.KeyGenerator;
import io.shardingjdbc.core.keygen.KeyGeneratorFactory;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Sharding rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class ShardingRuleConfiguration {
    
    private String defaultDataSourceName;
    
    private Collection<TableRuleConfiguration> tableRuleConfigs = new LinkedList<>();
    
    private Collection<String> bindingTableGroups = new LinkedList<>();
    
    private ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig;
    
    private ShardingStrategyConfiguration defaultTableShardingStrategyConfig;
    
    private String defaultKeyGeneratorClass;
    
    private Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
    
    /**
     * Build sharding rule.
     *
     * @param dataSourceNames data source names
     * @return sharding rule
     */
    public ShardingRule build(final Collection<String> dataSourceNames) {
        Preconditions.checkNotNull(dataSourceNames, "dataSources cannot be null.");
        Preconditions.checkArgument(!dataSourceNames.isEmpty(), "dataSources cannot be null.");
        Set<String> dataSourceNameSet = new LinkedHashSet<>(dataSourceNames);
        Collection<TableRule> tableRules = new LinkedList<>();
        for (TableRuleConfiguration each : tableRuleConfigs) {
            tableRules.add(new TableRule(each, dataSourceNameSet));
        }
        ShardingStrategy defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategyConfig ? null : defaultDatabaseShardingStrategyConfig.build();
        ShardingStrategy defaultTableShardingStrategy = null == defaultTableShardingStrategyConfig ? null : defaultTableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = KeyGeneratorFactory.newInstance(null == defaultKeyGeneratorClass ? DefaultKeyGenerator.class.getName() : defaultKeyGeneratorClass);
        return new ShardingRule(dataSourceNameSet, defaultDataSourceName, tableRules, bindingTableGroups, defaultDatabaseShardingStrategy, defaultTableShardingStrategy, keyGenerator);
    }
}
