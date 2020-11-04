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

package org.apache.shardingsphere.ha.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.apache.shardingsphere.ha.constant.HAOrder;
import org.apache.shardingsphere.ha.yaml.config.YamlHARuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HARuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final HARuleAlgorithmProviderConfigurationYamlSwapper swapper = new HARuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlHARuleConfiguration actual = createYamlHARuleConfiguration();
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertThat(actual.getDataSources().keySet(), is(Collections.singleton("name")));
        assertThat(actual.getDataSources().get("name").getName(), is("name"));
        assertThat(actual.getDataSources().get("name").getPrimaryDataSourceName(), is("primaryDataSourceName"));
        assertThat(actual.getDataSources().get("name").getLoadBalancerName(), is("loadBalancerName"));
        assertThat(actual.getDataSources().get("name").getReplicaDataSourceNames(), is(Collections.singletonList("replicaDataSourceName")));
        assertNotNull(actual.getLoadBalancers());
        assertThat(actual.getLoadBalancers().keySet(), is(Collections.singleton("name")));
        assertNotNull(actual.getLoadBalancers().get("name"));
        assertThat(actual.getLoadBalancers().get("name").getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedHARuleConfiguration actual = swapper.swapToObject(createYamlHARuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        HADataSourceRuleConfiguration ruleConfig = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfig);
        assertThat(ruleConfig.getName(), is("name"));
        assertThat(ruleConfig.getPrimaryDataSourceName(), is("primaryDataSourceName"));
        assertThat(ruleConfig.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(ruleConfig.getReplicaDataSourceNames(), is(Collections.singletonList("replicaDataSourceName")));
        assertThat(actual.getLoadBalanceAlgorithms(), is(Collections.emptyMap()));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedHARuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("REPLICA_QUERY"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(HAOrder.ALGORITHM_PROVIDER_ORDER));
    }
    
    private YamlHARuleConfiguration createYamlHARuleConfiguration() {
        HADataSourceRuleConfiguration ruleConfig = new HADataSourceRuleConfiguration("name", "primaryDataSourceName",
                Collections.singletonList("replicaDataSourceName"), "loadBalancerName");
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedHARuleConfiguration(Collections.singletonList(ruleConfig), ImmutableMap.of("name", new RandomReplicaLoadBalanceAlgorithm())));
    }
}
