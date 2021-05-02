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

package org.apache.shardingsphere.readwritesplitting.common.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.constant.ReadWriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadWriteSplittingDataSourceRuleConfiguration;
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

public final class ReadWriteSplittingRuleConfigurationYamlSwapperTest {
    
    private final Collection<YamlRuleConfigurationSwapper> collection = ShardingSphereServiceLoader.getSingletonServiceInstances(YamlRuleConfigurationSwapper.class);
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReadWriteSplittingDataSourceRuleConfiguration dataSourceConfig = 
                new ReadWriteSplittingDataSourceRuleConfiguration("ds", "", "write", Collections.singletonList("read"), "roundRobin");
        YamlReadWriteSplittingRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToYamlConfiguration(new ReadWriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig), ImmutableMap.of("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReadWriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadWriteSplittingDataSourceRuleConfiguration("ds", "", "write", Collections.singletonList("read"), null);
        YamlReadWriteSplittingRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new ReadWriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlReadWriteSplittingRuleConfiguration yamlConfig = createYamlReadWriteSplittingRuleConfiguration();
        yamlConfig.getDataSources().get("read_query_ds").setLoadBalancerName("RANDOM");
        ReadWriteSplittingRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReadWriteSplittingRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalancerName(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlReadWriteSplittingRuleConfiguration yamlConfig = createYamlReadWriteSplittingRuleConfiguration();
        ReadWriteSplittingRuleConfiguration actual = getReplicaQueryRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReadWriteSplittingRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalancerName());
    }
    
    private YamlReadWriteSplittingRuleConfiguration createYamlReadWriteSplittingRuleConfiguration() {
        YamlReadWriteSplittingRuleConfiguration result = new YamlReadWriteSplittingRuleConfiguration();
        result.getDataSources().put("read_query_ds", new YamlReadWriteSplittingDataSourceRuleConfiguration());
        result.getDataSources().get("read_query_ds").setName("read_query_ds");
        result.getDataSources().get("read_query_ds").setWriteDataSourceName("write_ds");
        result.getDataSources().get("read_query_ds").setReadDataSourceNames(Arrays.asList("read_ds_0", "read_ds_1"));
        return result;
    }
    
    private void assertReadWriteSplittingRuleConfiguration(final ReadWriteSplittingRuleConfiguration actual) {
        ReadWriteSplittingDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("read_query_ds"));
        assertThat(group.getWriteDataSourceName(), is("write_ds"));
        assertThat(group.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetTypeClass() {
        ReadWriteSplittingRuleConfigurationYamlSwapper swapper = getReplicaQueryRuleConfigurationYamlSwapper();
        Class<ReadWriteSplittingRuleConfiguration> actual = swapper.getTypeClass();
        assertTrue(actual.isAssignableFrom(ReadWriteSplittingRuleConfiguration.class));
    }
    
    @Test
    public void assertGetOrder() {
        ReadWriteSplittingRuleConfigurationYamlSwapper swapper = getReplicaQueryRuleConfigurationYamlSwapper();
        int actual = swapper.getOrder();
        assertThat(actual, is(ReadWriteSplittingOrder.ORDER));
    }
    
    private ReadWriteSplittingRuleConfigurationYamlSwapper getReplicaQueryRuleConfigurationYamlSwapper() {
        Optional<ReadWriteSplittingRuleConfigurationYamlSwapper> optional = collection.stream()
                .filter(swapper -> swapper instanceof ReadWriteSplittingRuleConfigurationYamlSwapper)
                .map(swapper -> (ReadWriteSplittingRuleConfigurationYamlSwapper) swapper)
                .findFirst();
        assertTrue(optional.isPresent());
        return optional.get();
    }
}
