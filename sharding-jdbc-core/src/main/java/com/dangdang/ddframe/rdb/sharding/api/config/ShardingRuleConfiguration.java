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

import com.dangdang.ddframe.rdb.sharding.api.MasterSlaveDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.rule.TableRule;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public class ShardingRuleConfiguration {
    
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
     * @param dataSourceMap data source map
     * @return sharding rule
     */
    public ShardingRule build(final Map<String, DataSource> dataSourceMap) throws SQLException {
        // TODO should not be null, for parsing only
        // Preconditions.checkNotNull(dataSources, "dataSources cannot be null.");
        Collection<TableRule> tableRules = new LinkedList<>();
        for (TableRuleConfiguration each : tableRuleConfigs) {
            tableRules.add(each.build(dataSourceMap));
        }
        ShardingStrategy defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategyConfig ? null : defaultDatabaseShardingStrategyConfig.build();
        ShardingStrategy defaultTableShardingStrategy = null == defaultTableShardingStrategyConfig ? null :  defaultTableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = KeyGeneratorFactory.newInstance(null == defaultKeyGeneratorClass ? DefaultKeyGenerator.class.getName() : defaultKeyGeneratorClass);
        processDataSourceMapWithMasterSlave(dataSourceMap);
        return new ShardingRule(dataSourceMap, defaultDataSourceName, tableRules, bindingTableGroups, defaultDatabaseShardingStrategy, defaultTableShardingStrategy, keyGenerator);
    }
    
    private void processDataSourceMapWithMasterSlave(final Map<String, DataSource> dataSourceMap) throws SQLException {
        for (MasterSlaveRuleConfiguration each : masterSlaveRuleConfigs) {
            dataSourceMap.put(each.getName(), MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, each));
            dataSourceMap.remove(each.getMasterDataSourceName());
            for (String slaveDataSourceName : each.getSlaveDataSourceNames()) {
                dataSourceMap.remove(slaveDataSourceName);
            }
        }
    }
}
