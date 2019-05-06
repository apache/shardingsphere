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

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlMasterSlaveRuleConfigurationTest {
    
    @Test
    public void assertGetMasterSlaveRuleConfigurationWithLoadBalanceAlgorithmType() {
        YamlMasterSlaveRuleConfiguration yamlConfig = createYamlMasterSlaveRuleConfig();
        yamlConfig.setLoadBalanceAlgorithmType(MasterSlaveLoadBalanceAlgorithmType.RANDOM);
        MasterSlaveRuleConfiguration actual = yamlConfig.getMasterSlaveRuleConfiguration();
        assertMasterSlaveRuleConfig(actual);
        assertThat(actual.getLoadBalanceAlgorithm(), is(MasterSlaveLoadBalanceAlgorithmType.RANDOM.getAlgorithm()));
    }
    
    @Test
    public void assertGetMasterSlaveRuleConfigurationWithLoadBalanceAlgorithmClassName() {
        YamlMasterSlaveRuleConfiguration yamlConfig = createYamlMasterSlaveRuleConfig();
        yamlConfig.setLoadBalanceAlgorithmClassName(RoundRobinMasterSlaveLoadBalanceAlgorithm.class.getName());
        MasterSlaveRuleConfiguration actual = yamlConfig.getMasterSlaveRuleConfiguration();
        assertMasterSlaveRuleConfig(actual);
        assertThat(actual.getLoadBalanceAlgorithm(), instanceOf(RoundRobinMasterSlaveLoadBalanceAlgorithm.class));
    }
    
    @Test
    public void assertGetMasterSlaveRuleConfigurationWithoutLoadBalanceAlgorithm() {
        YamlMasterSlaveRuleConfiguration yamlConfig = createYamlMasterSlaveRuleConfig();
        MasterSlaveRuleConfiguration actual = yamlConfig.getMasterSlaveRuleConfiguration();
        assertMasterSlaveRuleConfig(actual);
        assertNull(actual.getLoadBalanceAlgorithm());
    }
    
    private YamlMasterSlaveRuleConfiguration createYamlMasterSlaveRuleConfig() {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setName("master_slave_ds");
        result.setMasterDataSourceName("master_ds");
        result.setSlaveDataSourceNames(Arrays.asList("slave_ds_0", "slave_ds_1"));
        return result;
    }
    
    private void assertMasterSlaveRuleConfig(final MasterSlaveRuleConfiguration actual) {
        assertThat(actual.getName(), is("master_slave_ds"));
        assertThat(actual.getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Arrays.asList("slave_ds_0", "slave_ds_1")));
    }
}
