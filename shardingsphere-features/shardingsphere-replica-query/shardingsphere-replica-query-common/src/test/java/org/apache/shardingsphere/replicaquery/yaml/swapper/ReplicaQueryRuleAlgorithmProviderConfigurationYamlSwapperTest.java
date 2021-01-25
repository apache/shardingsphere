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

package org.apache.shardingsphere.replicaquery.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.algorithm.config.AlgorithmProvidedReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper swapper = new ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlReplicaQueryRuleConfiguration actual = createYamlReplicaQueryRuleConfiguration();
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
        AlgorithmProvidedReplicaQueryRuleConfiguration actual = swapper.swapToObject(createYamlReplicaQueryRuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        ReplicaQueryDataSourceRuleConfiguration ruleConfig = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfig);
        assertThat(ruleConfig.getName(), is("name"));
        assertThat(ruleConfig.getPrimaryDataSourceName(), is("primaryDataSourceName"));
        assertThat(ruleConfig.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(ruleConfig.getReplicaDataSourceNames(), is(Collections.singletonList("replicaDataSourceName")));
        assertThat(actual.getLoadBalanceAlgorithms(), is(Collections.emptyMap()));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedReplicaQueryRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("REPLICA_QUERY"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(ReplicaQueryOrder.ALGORITHM_PROVIDER_ORDER));
    }
    
    private YamlReplicaQueryRuleConfiguration createYamlReplicaQueryRuleConfiguration() {
        ReplicaQueryDataSourceRuleConfiguration ruleConfig = new ReplicaQueryDataSourceRuleConfiguration("name", "primaryDataSourceName",
                Collections.singletonList("replicaDataSourceName"), "loadBalancerName");
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedReplicaQueryRuleConfiguration(Collections.singletonList(ruleConfig), ImmutableMap.of("name", new RandomReplicaLoadBalanceAlgorithm())));
    }
}
