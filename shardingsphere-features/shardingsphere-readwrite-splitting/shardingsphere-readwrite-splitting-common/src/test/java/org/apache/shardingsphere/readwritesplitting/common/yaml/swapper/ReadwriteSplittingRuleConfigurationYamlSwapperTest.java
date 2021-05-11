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
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
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

public final class ReadwriteSplittingRuleConfigurationYamlSwapperTest {
    
    private final Collection<YamlRuleConfigurationSwapper> collection = ShardingSphereServiceLoader.getSingletonServiceInstances(YamlRuleConfigurationSwapper.class);
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = 
                new ReadwriteSplittingDataSourceRuleConfiguration("ds", "", "write", Collections.singletonList("read"), "roundRobin");
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig), ImmutableMap.of("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "", "write", Collections.singletonList("read"), null);
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlReadwriteSplittingRuleConfiguration yamlConfig = createYamlReadwriteSplittingRuleConfiguration();
        yamlConfig.getDataSources().get("read_query_ds").setLoadBalancerName("RANDOM");
        ReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReadwriteSplittingRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalancerName(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlReadwriteSplittingRuleConfiguration yamlConfig = createYamlReadwriteSplittingRuleConfiguration();
        ReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertReadwriteSplittingRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalancerName());
    }
    
    private YamlReadwriteSplittingRuleConfiguration createYamlReadwriteSplittingRuleConfiguration() {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        result.getDataSources().put("read_query_ds", new YamlReadwriteSplittingDataSourceRuleConfiguration());
        result.getDataSources().get("read_query_ds").setWriteDataSourceName("write_ds");
        result.getDataSources().get("read_query_ds").setReadDataSourceNames(Arrays.asList("read_ds_0", "read_ds_1"));
        return result;
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        ReadwriteSplittingDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("read_query_ds"));
        assertThat(group.getWriteDataSourceName(), is("write_ds"));
        assertThat(group.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetTypeClass() {
        ReadwriteSplittingRuleConfigurationYamlSwapper swapper = getReadwriteSplittingRuleConfigurationYamlSwapper();
        Class<ReadwriteSplittingRuleConfiguration> actual = swapper.getTypeClass();
        assertTrue(actual.isAssignableFrom(ReadwriteSplittingRuleConfiguration.class));
    }
    
    @Test
    public void assertGetOrder() {
        ReadwriteSplittingRuleConfigurationYamlSwapper swapper = getReadwriteSplittingRuleConfigurationYamlSwapper();
        int actual = swapper.getOrder();
        assertThat(actual, is(ReadwriteSplittingOrder.ORDER));
    }
    
    private ReadwriteSplittingRuleConfigurationYamlSwapper getReadwriteSplittingRuleConfigurationYamlSwapper() {
        Optional<ReadwriteSplittingRuleConfigurationYamlSwapper> optional = collection.stream()
                .filter(swapper -> swapper instanceof ReadwriteSplittingRuleConfigurationYamlSwapper)
                .map(swapper -> (ReadwriteSplittingRuleConfigurationYamlSwapper) swapper)
                .findFirst();
        assertTrue(optional.isPresent());
        return optional.get();
    }
}
