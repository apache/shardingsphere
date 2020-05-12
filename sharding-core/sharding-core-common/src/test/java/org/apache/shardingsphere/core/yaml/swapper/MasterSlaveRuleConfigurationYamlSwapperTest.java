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

import org.apache.shardingsphere.masterslave.api.config.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MasterSlaveRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        MasterSlaveDataSourceConfiguration dataSourceConfiguration = new MasterSlaveDataSourceConfiguration(
                "ds", "master", Collections.singletonList("slave"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN"));
        YamlMasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(new MasterSlaveRuleConfiguration(Collections.singleton(dataSourceConfiguration)));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getMasterDataSourceName(), is("master"));
        assertThat(actual.getDataSources().get("ds").getSlaveDataSourceNames(), is(Collections.singletonList("slave")));
        assertThat(actual.getDataSources().get("ds").getLoadBalanceAlgorithmType(), is("ROUND_ROBIN"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        MasterSlaveDataSourceConfiguration dataSourceConfiguration = new MasterSlaveDataSourceConfiguration("ds", "master", Collections.singletonList("slave"));
        YamlMasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(new MasterSlaveRuleConfiguration(Collections.singleton(dataSourceConfiguration)));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getMasterDataSourceName(), is("master"));
        assertThat(actual.getDataSources().get("ds").getSlaveDataSourceNames(), is(Collections.singletonList("slave")));
        assertNull(actual.getDataSources().get("ds").getLoadBalanceAlgorithmType());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlMasterSlaveRuleConfiguration yamlConfiguration = createYamlMasterSlaveRuleConfiguration();
        yamlConfiguration.getDataSources().get("master_slave_ds").setLoadBalanceAlgorithmType("RANDOM");
        MasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(yamlConfiguration);
        assertMasterSlaveRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalanceStrategyConfiguration().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlMasterSlaveRuleConfiguration yamlConfiguration = createYamlMasterSlaveRuleConfiguration();
        MasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(yamlConfiguration);
        assertMasterSlaveRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalanceStrategyConfiguration());
    }
    
    private YamlMasterSlaveRuleConfiguration createYamlMasterSlaveRuleConfiguration() {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.getDataSources().put("master_slave_ds", new YamlMasterSlaveDataSourceConfiguration());
        result.getDataSources().get("master_slave_ds").setName("master_slave_ds");
        result.getDataSources().get("master_slave_ds").setMasterDataSourceName("master_ds");
        result.getDataSources().get("master_slave_ds").setSlaveDataSourceNames(Arrays.asList("slave_ds_0", "slave_ds_1"));
        return result;
    }
    
    private void assertMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration actual) {
        MasterSlaveDataSourceConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("master_slave_ds"));
        assertThat(group.getMasterDataSourceName(), is("master_ds"));
        assertThat(group.getSlaveDataSourceNames(), is(Arrays.asList("slave_ds_0", "slave_ds_1")));
    }
}
