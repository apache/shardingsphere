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

package org.apache.shardingsphere.primaryreplica.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.primaryreplica.algorithm.RandomPrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.constant.PrimaryReplicaOrder;
import org.apache.shardingsphere.primaryreplica.yaml.config.YamlPrimaryReplicaRuleConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapper swapper = new PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlPrimaryReplicaRuleConfiguration actual = createYamlPrimaryReplicaRuleConfiguration();
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
        AlgorithmProvidedPrimaryReplicaRuleConfiguration actual = swapper.swapToObject(createYamlPrimaryReplicaRuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        PrimaryReplicaDataSourceRuleConfiguration ruleConfiguration = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfiguration);
        assertThat(ruleConfiguration.getName(), is("name"));
        assertThat(ruleConfiguration.getPrimaryDataSourceName(), is("primaryDataSourceName"));
        assertThat(ruleConfiguration.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(ruleConfiguration.getReplicaDataSourceNames(), is(Collections.singletonList("replicaDataSourceName")));
        assertThat(actual.getLoadBalanceAlgorithms(), is(new LinkedHashMap<>()));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedPrimaryReplicaRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("PRIMARY_REPLICA"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(PrimaryReplicaOrder.ALGORITHM_PROVIDER_PRIMARY_REPLICA_ORDER));
    }
    
    private YamlPrimaryReplicaRuleConfiguration createYamlPrimaryReplicaRuleConfiguration() {
        PrimaryReplicaDataSourceRuleConfiguration ruleConfiguration = new PrimaryReplicaDataSourceRuleConfiguration("name", "primaryDataSourceName",
                Collections.singletonList("replicaDataSourceName"), "loadBalancerName");
        YamlPrimaryReplicaRuleConfiguration result = swapper.swapToYamlConfiguration(
                new AlgorithmProvidedPrimaryReplicaRuleConfiguration(Collections.singletonList(ruleConfiguration), ImmutableMap.of("name", new RandomPrimaryReplicaLoadBalanceAlgorithm())));
        return result;
    }
}
