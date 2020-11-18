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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReplicaQueryRuleConfigurationYamlSwapperTest {
    
    private final Collection<YamlRuleConfigurationSwapper> collection = ShardingSphereServiceLoader.newServiceInstances(YamlRuleConfigurationSwapper.class);
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReplicaQueryDataSourceRuleConfiguration dataSourceConfig = 
                new ReplicaQueryDataSourceRuleConfiguration("ds", "primary", Collections.singletonList("replica"), "roundRobin");
        YamlReplicaQueryRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToYamlConfiguration(new ReplicaQueryRuleConfiguration(
                Collections.singleton(dataSourceConfig), ImmutableMap.of("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getPrimaryDataSourceName(), is("primary"));
        assertThat(actual.getDataSources().get("ds").getReplicaDataSourceNames(), is(Collections.singletonList("replica")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReplicaQueryDataSourceRuleConfiguration dataSourceConfig = new ReplicaQueryDataSourceRuleConfiguration("ds", "primary", Collections.singletonList("replica"), null);
        YamlReplicaQueryRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new ReplicaQueryRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getPrimaryDataSourceName(), is("primary"));
        assertThat(actual.getDataSources().get("ds").getReplicaDataSourceNames(), is(Collections.singletonList("replica")));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlReplicaQueryRuleConfiguration yamlConfig = createYamlReplicaQueryRuleConfiguration();
        yamlConfig.getDataSources().get("replica_query_ds").setLoadBalancerName("RANDOM");
        ReplicaQueryRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReplicaQueryRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalancerName(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlReplicaQueryRuleConfiguration yamlConfig = createYamlReplicaQueryRuleConfiguration();
        ReplicaQueryRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReplicaQueryRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalancerName());
    }
    
    private YamlReplicaQueryRuleConfiguration createYamlReplicaQueryRuleConfiguration() {
        YamlReplicaQueryRuleConfiguration result = new YamlReplicaQueryRuleConfiguration();
        result.getDataSources().put("replica_query_ds", new YamlReplicaQueryDataSourceRuleConfiguration());
        result.getDataSources().get("replica_query_ds").setName("replica_query_ds");
        result.getDataSources().get("replica_query_ds").setPrimaryDataSourceName("primary_ds");
        result.getDataSources().get("replica_query_ds").setReplicaDataSourceNames(Arrays.asList("replica_ds_0", "replica_ds_1"));
        return result;
    }
    
    private void assertReplicaQueryRuleConfiguration(final ReplicaQueryRuleConfiguration actual) {
        ReplicaQueryDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("replica_query_ds"));
        assertThat(group.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(group.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetTypeClass() {
        ReplicaQueryRuleConfigurationYamlSwapper swapper = getReplicaQueryRuleConfigurationYamlSwapper();
        Class<ReplicaQueryRuleConfiguration> actual = swapper.getTypeClass();
        assertTrue(actual.isAssignableFrom(ReplicaQueryRuleConfiguration.class));
    }
    
    @Test
    public void assertGetOrder() {
        ReplicaQueryRuleConfigurationYamlSwapper swapper = getReplicaQueryRuleConfigurationYamlSwapper();
        int actual = swapper.getOrder();
        assertThat(actual, is(ReplicaQueryOrder.ORDER));
    }
    
    private ReplicaQueryRuleConfigurationYamlSwapper getReplicaQueryRuleConfigurationYamlSwapper() {
        Optional<ReplicaQueryRuleConfigurationYamlSwapper> optional = collection.stream()
                .filter(swapper -> swapper instanceof ReplicaQueryRuleConfigurationYamlSwapper)
                .map(swapper -> (ReplicaQueryRuleConfigurationYamlSwapper) swapper)
                .findFirst();
        assertTrue(optional.isPresent());
        return optional.get();
    }
}
