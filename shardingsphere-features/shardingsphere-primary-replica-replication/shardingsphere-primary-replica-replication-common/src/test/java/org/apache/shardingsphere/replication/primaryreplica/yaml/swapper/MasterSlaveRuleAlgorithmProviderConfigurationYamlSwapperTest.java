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

package org.apache.shardingsphere.replication.primaryreplica.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.constant.MasterSlaveOrder;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper swapper = new MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlMasterSlaveRuleConfiguration actual = createYamlMasterSlaveRuleConfiguration();
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertThat(actual.getDataSources().keySet(), is(Collections.singleton("name")));
        assertThat(actual.getDataSources().get("name").getName(), is("name"));
        assertThat(actual.getDataSources().get("name").getMasterDataSourceName(), is("masterDataSourceName"));
        assertThat(actual.getDataSources().get("name").getLoadBalancerName(), is("loadBalancerName"));
        assertThat(actual.getDataSources().get("name").getSlaveDataSourceNames(), is(Collections.singletonList("slaveDataSourceName")));
        assertNotNull(actual.getLoadBalancers());
        assertThat(actual.getLoadBalancers().keySet(), is(Collections.singleton("name")));
        assertNotNull(actual.getLoadBalancers().get("name"));
        assertThat(actual.getLoadBalancers().get("name").getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedMasterSlaveRuleConfiguration actual = swapper.swapToObject(createYamlMasterSlaveRuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        MasterSlaveDataSourceRuleConfiguration ruleConfiguration = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfiguration);
        assertThat(ruleConfiguration.getName(), is("name"));
        assertThat(ruleConfiguration.getMasterDataSourceName(), is("masterDataSourceName"));
        assertThat(ruleConfiguration.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(ruleConfiguration.getSlaveDataSourceNames(), is(Collections.singletonList("slaveDataSourceName")));
        assertThat(actual.getLoadBalanceAlgorithms(), is(Collections.emptyMap()));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedMasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("MASTER_SLAVE"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), CoreMatchers.is(MasterSlaveOrder.ALGORITHM_PROVIDER_MASTER_SLAVE_ORDER));
    }
    
    private YamlMasterSlaveRuleConfiguration createYamlMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceRuleConfiguration ruleConfiguration = new MasterSlaveDataSourceRuleConfiguration("name", "masterDataSourceName",
                Collections.singletonList("slaveDataSourceName"), "loadBalancerName");
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedMasterSlaveRuleConfiguration(Collections.singletonList(ruleConfiguration), ImmutableMap.of("name", new RandomMasterSlaveLoadBalanceAlgorithm())));
    }
}
