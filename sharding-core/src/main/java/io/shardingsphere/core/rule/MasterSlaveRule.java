/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.rule;

import com.google.common.base.Preconditions;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import lombok.Getter;

import java.util.Collection;

/**
 * Databases and tables master-slave rule configuration.
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
        Preconditions.checkNotNull(config.getName(), "Master-slave rule name cannot be null.");
        Preconditions.checkNotNull(config.getMasterDataSourceName(), "Master data source name cannot be null.");
        Preconditions.checkNotNull(config.getSlaveDataSourceNames(), "Slave data source names cannot be null.");
        Preconditions.checkState(!config.getSlaveDataSourceNames().isEmpty(), "Slave data source names cannot be empty.");
        name = config.getName();
        masterDataSourceName = config.getMasterDataSourceName();
        slaveDataSourceNames = config.getSlaveDataSourceNames();
        loadBalanceAlgorithm = null == config.getLoadBalanceAlgorithm() ? MasterSlaveLoadBalanceAlgorithmType.getDefaultAlgorithmType().getAlgorithm() : config.getLoadBalanceAlgorithm();
        masterSlaveRuleConfiguration = config;
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
