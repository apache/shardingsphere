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

import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.rule.MasterSlaveRule;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Master-slave rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public class MasterSlaveRuleConfig {
    
    private String name;
    
    private String masterDataSourceName;
    
    private Collection<String> slaveDataSourceNames;
    
    private String masterSlaveLoadBalanceStrategyClassName;
    
    /**
     * Build master-slave rule.
     *
     * @param dataSourceMap data source map
     * @return sharding rule
     */
    public MasterSlaveRule build(final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkNotNull(name, "name cannot be null.");
        Preconditions.checkNotNull(masterDataSourceName, "masterDataSourceName cannot be null.");
        Preconditions.checkNotNull(slaveDataSourceNames, "slaveDataSourceNames cannot be null.");
        Preconditions.checkArgument(!slaveDataSourceNames.isEmpty(), "slaveDataSourceNames cannot be empty.");
        Map<String, DataSource> slaveDataSources = new HashMap<>(slaveDataSourceNames.size(), 1);
        for (String each : slaveDataSourceNames) {
            slaveDataSources.put(each, dataSourceMap.get(each));
        }
        MasterSlaveLoadBalanceStrategy slaveLoadBalanceStrategy = Strings.isNullOrEmpty(masterSlaveLoadBalanceStrategyClassName) ? null : newInstance(masterSlaveLoadBalanceStrategyClassName);
        return new MasterSlaveRule(name, masterDataSourceName, dataSourceMap.get(masterDataSourceName), slaveDataSources, slaveLoadBalanceStrategy);
    }
    
    public MasterSlaveLoadBalanceStrategy newInstance(final String masterSlaveLoadBalanceStrategyClassName) {
        try {
            Class<?> result = Class.forName(masterSlaveLoadBalanceStrategyClassName);
            if (!MasterSlaveLoadBalanceStrategy.class.isAssignableFrom(result)) {
                throw new ShardingJdbcException("Class %s should be implement %s", masterSlaveLoadBalanceStrategyClassName, MasterSlaveLoadBalanceStrategy.class.getName());
            }
            return (MasterSlaveLoadBalanceStrategy) result.newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingJdbcException("Class %s should have public privilege and no argument constructor", masterSlaveLoadBalanceStrategyClassName);
        }
    }
}
