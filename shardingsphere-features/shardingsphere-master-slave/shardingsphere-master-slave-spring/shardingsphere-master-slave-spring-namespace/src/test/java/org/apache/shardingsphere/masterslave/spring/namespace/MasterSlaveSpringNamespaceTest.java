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

package org.apache.shardingsphere.masterslave.spring.namespace;

import org.apache.shardingsphere.masterslave.algorithm.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/master-slave-application-context.xml")
public final class MasterSlaveSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private MasterSlaveLoadBalanceAlgorithm randomLoadbalancer;
    
    @Resource
    private AlgorithmProvidedMasterSlaveRuleConfiguration defaultMasterSlaveRule;
    
    @Resource
    private AlgorithmProvidedMasterSlaveRuleConfiguration randomMasterSlaveRule;
    
    @Test
    public void assertRandomLoadbalancer() {
        assertThat(randomLoadbalancer.getType(), is("RANDOM"));
    }
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        assertLoadBalancers(defaultMasterSlaveRule.getLoadBalanceAlgorithms());
        assertThat(defaultMasterSlaveRule.getDataSources().size(), is(1));
        assertDefaultMasterSlaveDataSourceRule(defaultMasterSlaveRule.getDataSources().iterator().next());
    }
    
    private void assertLoadBalancers(final Map<String, MasterSlaveLoadBalanceAlgorithm> loadBalances) {
        assertThat(loadBalances.size(), is(1));
        assertThat(loadBalances.get("randomLoadbalancer"), instanceOf(RandomMasterSlaveLoadBalanceAlgorithm.class));
    }
    
    private void assertDefaultMasterSlaveDataSourceRule(final MasterSlaveDataSourceRuleConfiguration dataSourceRuleConfig) {
        assertThat(dataSourceRuleConfig.getName(), is("default_ds"));
        assertThat(dataSourceRuleConfig.getMasterDataSourceName(), is("master_ds"));
        assertThat(dataSourceRuleConfig.getSlaveDataSourceNames(), is(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertThat(dataSourceRuleConfig.getLoadBalancerName(), is(""));
    }
    
    @Test
    public void assertRandomMaserSlaveDataSource() {
        assertLoadBalancers(randomMasterSlaveRule.getLoadBalanceAlgorithms());
        assertThat(randomMasterSlaveRule.getDataSources().size(), is(1));
        assertRandomMasterSlaveDataSourceRule(randomMasterSlaveRule.getDataSources().iterator().next());
    }
    
    private void assertRandomMasterSlaveDataSourceRule(final MasterSlaveDataSourceRuleConfiguration dataSourceRuleConfig) {
        assertThat(dataSourceRuleConfig.getName(), is("random_ds"));
        assertThat(dataSourceRuleConfig.getMasterDataSourceName(), is("master_ds"));
        assertThat(dataSourceRuleConfig.getSlaveDataSourceNames(), is(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertThat(dataSourceRuleConfig.getLoadBalancerName(), is("randomLoadbalancer"));
    }
}
