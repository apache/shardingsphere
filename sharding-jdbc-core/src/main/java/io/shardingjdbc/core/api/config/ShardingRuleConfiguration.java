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

package io.shardingjdbc.core.api.config;

import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.keygen.DefaultKeyGenerator;
import io.shardingjdbc.core.keygen.KeyGenerator;
import io.shardingjdbc.core.keygen.KeyGeneratorFactory;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import com.google.common.base.Preconditions;
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
        Preconditions.checkNotNull(dataSourceMap, "dataSources cannot be null.");
        Preconditions.checkArgument(!dataSourceMap.isEmpty(), "dataSources cannot be null.");
        processDataSourceMapWithMasterSlave(dataSourceMap);
        Collection<TableRule> tableRules = new LinkedList<>();
        for (TableRuleConfiguration each : tableRuleConfigs) {
            tableRules.add(each.build(dataSourceMap));
        }
        ShardingStrategy defaultDatabaseShardingStrategy = null == defaultDatabaseShardingStrategyConfig ? null : defaultDatabaseShardingStrategyConfig.build();
        ShardingStrategy defaultTableShardingStrategy = null == defaultTableShardingStrategyConfig ? null :  defaultTableShardingStrategyConfig.build();
        KeyGenerator keyGenerator = KeyGeneratorFactory.newInstance(null == defaultKeyGeneratorClass ? DefaultKeyGenerator.class.getName() : defaultKeyGeneratorClass);
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
