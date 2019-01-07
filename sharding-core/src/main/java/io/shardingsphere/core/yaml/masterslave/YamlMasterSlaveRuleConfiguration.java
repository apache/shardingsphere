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

package io.shardingsphere.core.yaml.masterslave;

import com.google.common.base.Strings;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Master-slave rule configuration for yaml.
 *
 * @author caohao
 * @author panjuan
 */
@NoArgsConstructor
@Getter
@Setter
public class YamlMasterSlaveRuleConfiguration {
    
    private String name;
    
    private String masterDataSourceName;
    
    private Collection<String> slaveDataSourceNames = new ArrayList<>();
    
    private MasterSlaveLoadBalanceAlgorithmType loadBalanceAlgorithmType;
    
    private String loadBalanceAlgorithmClassName;
    
    public YamlMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        name = masterSlaveRuleConfiguration.getName();
        masterDataSourceName = masterSlaveRuleConfiguration.getMasterDataSourceName();
        slaveDataSourceNames = masterSlaveRuleConfiguration.getSlaveDataSourceNames();
        loadBalanceAlgorithmClassName = null == masterSlaveRuleConfiguration.getLoadBalanceAlgorithm() ? null : masterSlaveRuleConfiguration.getLoadBalanceAlgorithm().getClass().getName();
    }
    
    /**
     * Get master-slave rule configuration from yaml.
     *
     * @return master-slave rule configuration from yaml
     */
    public MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm = null;
        if (!Strings.isNullOrEmpty(loadBalanceAlgorithmClassName)) {
            loadBalanceAlgorithm = newInstance(loadBalanceAlgorithmClassName);
        } else if (null != loadBalanceAlgorithmType) {
            loadBalanceAlgorithm = loadBalanceAlgorithmType.getAlgorithm();
        }
        return new MasterSlaveRuleConfiguration(name, masterDataSourceName, slaveDataSourceNames, loadBalanceAlgorithm);
    }
    
    private MasterSlaveLoadBalanceAlgorithm newInstance(final String masterSlaveLoadBalanceAlgorithmClassName) {
        try {
            Class<?> result = Class.forName(masterSlaveLoadBalanceAlgorithmClassName);
            if (!MasterSlaveLoadBalanceAlgorithm.class.isAssignableFrom(result)) {
                throw new ShardingConfigurationException("Class %s should be implement %s", masterSlaveLoadBalanceAlgorithmClassName, MasterSlaveLoadBalanceAlgorithm.class.getName());
            }
            return (MasterSlaveLoadBalanceAlgorithm) result.newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException("Class %s should have public privilege and no argument constructor", masterSlaveLoadBalanceAlgorithmClassName);
        }
    }
}
