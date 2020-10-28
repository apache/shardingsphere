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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.constant.PrimaryReplicaReplicationOrder;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.rule.YamlPrimaryReplicaReplicationDataSourceRuleConfiguration;
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

public final class PrimaryReplicaReplicationRuleConfigurationYamlSwapperTest {
    
    private final Collection<YamlRuleConfigurationSwapper> collection = ShardingSphereServiceLoader.newServiceInstances(YamlRuleConfigurationSwapper.class);
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        PrimaryReplicaReplicationDataSourceRuleConfiguration dataSourceConfig = 
                new PrimaryReplicaReplicationDataSourceRuleConfiguration("ds", "primary", Collections.singletonList("replica"), "roundRobin");
        YamlPrimaryReplicaReplicationRuleConfiguration actual = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper().swapToYamlConfiguration(new PrimaryReplicaReplicationRuleConfiguration(
                Collections.singleton(dataSourceConfig), ImmutableMap.of("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getPrimaryDataSourceName(), is("primary"));
        assertThat(actual.getDataSources().get("ds").getReplicaDataSourceNames(), is(Collections.singletonList("replica")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        PrimaryReplicaReplicationDataSourceRuleConfiguration dataSourceConfig = new PrimaryReplicaReplicationDataSourceRuleConfiguration("ds", "primary", Collections.singletonList("replica"), null);
        YamlPrimaryReplicaReplicationRuleConfiguration actual = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new PrimaryReplicaReplicationRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getPrimaryDataSourceName(), is("primary"));
        assertThat(actual.getDataSources().get("ds").getReplicaDataSourceNames(), is(Collections.singletonList("replica")));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlPrimaryReplicaReplicationRuleConfiguration yamlConfig = createYamlPrimaryReplicaReplicationRuleConfiguration();
        yamlConfig.getDataSources().get("replica_query_ds").setLoadBalancerName("RANDOM");
        PrimaryReplicaReplicationRuleConfiguration actual = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertPrimaryReplicaReplicationRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalancerName(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlPrimaryReplicaReplicationRuleConfiguration yamlConfig = createYamlPrimaryReplicaReplicationRuleConfiguration();
        PrimaryReplicaReplicationRuleConfiguration actual = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertPrimaryReplicaReplicationRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalancerName());
    }
    
    private YamlPrimaryReplicaReplicationRuleConfiguration createYamlPrimaryReplicaReplicationRuleConfiguration() {
        YamlPrimaryReplicaReplicationRuleConfiguration result = new YamlPrimaryReplicaReplicationRuleConfiguration();
        result.getDataSources().put("replica_query_ds", new YamlPrimaryReplicaReplicationDataSourceRuleConfiguration());
        result.getDataSources().get("replica_query_ds").setName("replica_query_ds");
        result.getDataSources().get("replica_query_ds").setPrimaryDataSourceName("primary_ds");
        result.getDataSources().get("replica_query_ds").setReplicaDataSourceNames(Arrays.asList("replica_ds_0", "replica_ds_1"));
        return result;
    }
    
    private void assertPrimaryReplicaReplicationRuleConfiguration(final PrimaryReplicaReplicationRuleConfiguration actual) {
        PrimaryReplicaReplicationDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("replica_query_ds"));
        assertThat(group.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(group.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetTypeClass() {
        PrimaryReplicaReplicationRuleConfigurationYamlSwapper swapper = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper();
        Class<PrimaryReplicaReplicationRuleConfiguration> actual = swapper.getTypeClass();
        assertTrue(actual.isAssignableFrom(PrimaryReplicaReplicationRuleConfiguration.class));
    }
    
    @Test
    public void assertGetOrder() {
        PrimaryReplicaReplicationRuleConfigurationYamlSwapper swapper = getPrimaryReplicaReplicationRuleConfigurationYamlSwapper();
        int actual = swapper.getOrder();
        assertThat(actual, is(PrimaryReplicaReplicationOrder.ORDER));
    }
    
    private PrimaryReplicaReplicationRuleConfigurationYamlSwapper getPrimaryReplicaReplicationRuleConfigurationYamlSwapper() {
        Optional<PrimaryReplicaReplicationRuleConfigurationYamlSwapper> optional = collection.stream()
                .filter(swapper -> swapper instanceof PrimaryReplicaReplicationRuleConfigurationYamlSwapper)
                .map(swapper -> (PrimaryReplicaReplicationRuleConfigurationYamlSwapper) swapper)
                .findFirst();
        assertTrue(optional.isPresent());
        return optional.get();
    }
}
