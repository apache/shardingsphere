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

package org.apache.shardingsphere.core.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

/**
 * Master-slave rule configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class MasterSlaveRuleConfigurationYamlSwapper implements YamlSwapper<YamlMasterSlaveRuleConfiguration, MasterSlaveRuleConfiguration> {
    
    @Override
    public YamlMasterSlaveRuleConfiguration swap(final MasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setName(data.getName());
        result.setMasterDataSourceName(data.getMasterDataSourceName());
        result.setSlaveDataSourceNames(data.getSlaveDataSourceNames());
        if (null != data.getLoadBalanceStrategyConfiguration()) {
            result.setLoadBalanceAlgorithmType(data.getLoadBalanceStrategyConfiguration().getType());
        }
        return result;
    }
    
    @Override
    public MasterSlaveRuleConfiguration swap(final YamlMasterSlaveRuleConfiguration yamlConfiguration) {
        return new MasterSlaveRuleConfiguration(yamlConfiguration.getName(), 
                yamlConfiguration.getMasterDataSourceName(), yamlConfiguration.getSlaveDataSourceNames(), getLoadBalanceStrategyConfiguration(yamlConfiguration));
    }
    
    private LoadBalanceStrategyConfiguration getLoadBalanceStrategyConfiguration(final YamlMasterSlaveRuleConfiguration yamlConfiguration) {
        return Strings.isNullOrEmpty(yamlConfiguration.getLoadBalanceAlgorithmType()) ? null
            : new LoadBalanceStrategyConfiguration(yamlConfiguration.getLoadBalanceAlgorithmType(), yamlConfiguration.getProps());
    }
}
