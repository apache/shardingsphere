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
import org.apache.shardingsphere.core.masterslave.MasterSlaveLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.spi.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;

import java.util.Collection;

/**
 * Databases and tables master-slave rule.
 * 
 * @author zhangliang
 * @author panjuan
 */
@Getter
public class MasterSlaveRule {
    
    private final String name;
    
    private final String masterDataSourceName;
    
    private final Collection<String> slaveDataSourceNames;
    
    private final MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration;
    
    public MasterSlaveRule(final MasterSlaveRuleConfiguration config) {
        name = config.getName();
        masterDataSourceName = config.getMasterDataSourceName();
        slaveDataSourceNames = config.getSlaveDataSourceNames();
        loadBalanceAlgorithm = createMasterSlaveLoadBalanceAlgorithm(config.getLoadBalanceStrategyConfiguration());
        masterSlaveRuleConfiguration = config;
    }
    
    private MasterSlaveLoadBalanceAlgorithm createMasterSlaveLoadBalanceAlgorithm(final LoadBalanceStrategyConfiguration loadBalanceStrategyConfiguration) {
        MasterSlaveLoadBalanceAlgorithmFactory factory = MasterSlaveLoadBalanceAlgorithmFactory.getInstance();
        return null == loadBalanceStrategyConfiguration ? factory.newAlgorithm() : factory.newAlgorithm(loadBalanceStrategyConfiguration.getType(), loadBalanceStrategyConfiguration.getProperties());
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
}
