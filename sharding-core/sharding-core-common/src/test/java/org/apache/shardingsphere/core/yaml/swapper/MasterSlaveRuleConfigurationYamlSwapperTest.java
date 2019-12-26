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

import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MasterSlaveRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        YamlMasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(
                new MasterSlaveRuleConfiguration("ds", "master", Collections.singletonList("slave"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        assertThat(actual.getName(), is("ds"));
        assertThat(actual.getMasterDataSourceName(), is("master"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Collections.singletonList("slave")));
        assertThat(actual.getLoadBalanceAlgorithmType(), is("ROUND_ROBIN"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        YamlMasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(new MasterSlaveRuleConfiguration("ds", "master", Collections.singletonList("slave")));
        assertThat(actual.getName(), is("ds"));
        assertThat(actual.getMasterDataSourceName(), is("master"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Collections.singletonList("slave")));
        assertNull(actual.getLoadBalanceAlgorithmType());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlMasterSlaveRuleConfiguration yamlConfiguration = createYamlMasterSlaveRuleConfiguration();
        yamlConfiguration.setLoadBalanceAlgorithmType("RANDOM");
        MasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(yamlConfiguration);
        assertMasterSlaveRuleConfiguration(actual);
        assertThat(actual.getLoadBalanceStrategyConfiguration().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlMasterSlaveRuleConfiguration yamlConfiguration = createYamlMasterSlaveRuleConfiguration();
        MasterSlaveRuleConfiguration actual = new MasterSlaveRuleConfigurationYamlSwapper().swap(yamlConfiguration);
        assertMasterSlaveRuleConfiguration(actual);
        assertNull(actual.getLoadBalanceStrategyConfiguration());
    }
    
    private YamlMasterSlaveRuleConfiguration createYamlMasterSlaveRuleConfiguration() {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setName("master_slave_ds");
        result.setMasterDataSourceName("master_ds");
        result.setSlaveDataSourceNames(Arrays.asList("slave_ds_0", "slave_ds_1"));
        return result;
    }
    
    private void assertMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration actual) {
        assertThat(actual.getName(), is("master_slave_ds"));
        assertThat(actual.getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Arrays.asList("slave_ds_0", "slave_ds_1")));
    }
}
