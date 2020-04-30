/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.rule;

import lombok.Getter;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Databases and tables master-slave rule.
 */
@Getter
public final class MasterSlaveRule implements BaseRule {
    
    static {
        ShardingSphereServiceLoader.register(MasterSlaveLoadBalanceAlgorithm.class);
    }
    
    private final String name;
    
    private final String masterDataSourceName;
    
    private final List<String> slaveDataSourceNames;
    
    private final MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm;
    
    private final MasterSlaveRuleConfiguration ruleConfiguration;
    
    private final Collection<String> disabledDataSourceNames = new HashSet<>();
    
    public MasterSlaveRule(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames, final MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm) {
        this.name = name;
        this.masterDataSourceName = masterDataSourceName;
        this.slaveDataSourceNames = slaveDataSourceNames;
        this.loadBalanceAlgorithm = null == loadBalanceAlgorithm ? TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class) : loadBalanceAlgorithm;
        ruleConfiguration = new MasterSlaveRuleConfiguration(name, masterDataSourceName, slaveDataSourceNames, 
                new LoadBalanceStrategyConfiguration(this.loadBalanceAlgorithm.getType(), this.loadBalanceAlgorithm.getProperties()));
    }
    
    public MasterSlaveRule(final MasterSlaveRuleConfiguration config) {
        name = config.getName();
        masterDataSourceName = config.getMasterDataSourceName();
        slaveDataSourceNames = config.getSlaveDataSourceNames();
        loadBalanceAlgorithm = createMasterSlaveLoadBalanceAlgorithm(config.getLoadBalanceStrategyConfiguration());
        ruleConfiguration = config;
    }
    
    private MasterSlaveLoadBalanceAlgorithm createMasterSlaveLoadBalanceAlgorithm(final LoadBalanceStrategyConfiguration loadBalanceStrategyConfiguration) {
        return null == loadBalanceStrategyConfiguration ? TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class)
                : TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class, loadBalanceStrategyConfiguration.getType(), loadBalanceStrategyConfiguration.getProperties());
    }
    
    /**
     * Judge whether contain data source name.
     *
     * @param dataSourceName data source name
     * @return contain or not.
     */
    public boolean containDataSourceName(final String dataSourceName) {
        return masterDataSourceName.equals(dataSourceName) || slaveDataSourceNames.contains(dataSourceName);
    }
    
    /**
     * Get slave data source names.
     *
     * @return available slave data source names
     */
    public List<String> getSlaveDataSourceNames() {
        return slaveDataSourceNames.stream().filter(each -> !disabledDataSourceNames.contains(each)).collect(Collectors.toList());
    }
    
    /**
     * Update disabled data source names.
     *
     * @param dataSourceName data source name
     * @param isDisabled is disabled
     */
    public void updateDisabledDataSourceNames(final String dataSourceName, final boolean isDisabled) {
        if (isDisabled) {
            disabledDataSourceNames.add(dataSourceName);
        } else {
            disabledDataSourceNames.remove(dataSourceName);
        }
    }
}
