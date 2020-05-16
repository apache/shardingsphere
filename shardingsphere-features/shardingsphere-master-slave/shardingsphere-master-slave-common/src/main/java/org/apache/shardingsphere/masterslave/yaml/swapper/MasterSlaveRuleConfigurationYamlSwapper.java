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

package org.apache.shardingsphere.masterslave.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.masterslave.api.config.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Master-slave rule configuration YAML swapper.
 */
public final class MasterSlaveRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlMasterSlaveRuleConfiguration, MasterSlaveRuleConfiguration> {
    
    @Override
    public YamlMasterSlaveRuleConfiguration swap(final MasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(Collectors.toMap(MasterSlaveDataSourceConfiguration::getName, this::swap, (a, b) -> b, LinkedHashMap::new)));
        return result;
    }
    
    private YamlMasterSlaveDataSourceConfiguration swap(final MasterSlaveDataSourceConfiguration group) {
        YamlMasterSlaveDataSourceConfiguration result = new YamlMasterSlaveDataSourceConfiguration();
        result.setName(group.getName());
        result.setMasterDataSourceName(group.getMasterDataSourceName());
        result.setSlaveDataSourceNames(group.getSlaveDataSourceNames());
        if (null != group.getLoadBalanceStrategyConfiguration()) {
            result.setLoadBalanceAlgorithmType(group.getLoadBalanceStrategyConfiguration().getType());
        }
        return result;
    }
    
    @Override
    public MasterSlaveRuleConfiguration swap(final YamlMasterSlaveRuleConfiguration yamlConfiguration) {
        Collection<MasterSlaveDataSourceConfiguration> groups = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveDataSourceConfiguration> entry : yamlConfiguration.getDataSources().entrySet()) {
            groups.add(swap(entry.getKey(), entry.getValue()));
        }
        return new MasterSlaveRuleConfiguration(groups);
    }
    
    private MasterSlaveDataSourceConfiguration swap(final String name, final YamlMasterSlaveDataSourceConfiguration yamlGroup) {
        return new MasterSlaveDataSourceConfiguration(name, yamlGroup.getMasterDataSourceName(), yamlGroup.getSlaveDataSourceNames(), getLoadBalanceStrategyConfiguration(yamlGroup));
    }
    
    private LoadBalanceStrategyConfiguration getLoadBalanceStrategyConfiguration(final YamlMasterSlaveDataSourceConfiguration yamlGroup) {
        return Strings.isNullOrEmpty(yamlGroup.getLoadBalanceAlgorithmType()) ? null : new LoadBalanceStrategyConfiguration(yamlGroup.getLoadBalanceAlgorithmType(), yamlGroup.getProps());
    }
    
    @Override
    public Class<MasterSlaveRuleConfiguration> getTypeClass() {
        return MasterSlaveRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "MASTER_SLAVE";
    }
    
    @Override
    public int getOrder() {
        return 10;
    }
}
