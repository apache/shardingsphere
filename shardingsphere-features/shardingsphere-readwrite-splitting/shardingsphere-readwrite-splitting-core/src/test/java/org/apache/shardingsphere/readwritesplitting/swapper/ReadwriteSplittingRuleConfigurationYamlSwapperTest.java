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

package org.apache.shardingsphere.readwritesplitting.swapper;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlStaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationYamlSwapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("ds",
                        new StaticReadwriteSplittingStrategyConfiguration("write", Arrays.asList("read")), null, "roundRobin");
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig), Collections.singletonMap("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertNotNull(actual.getDataSources().get("ds").getStaticStrategy());
        assertThat((actual.getDataSources().get("ds").getStaticStrategy()).getWriteDataSourceName(), is("write"));
        assertThat((actual.getDataSources().get("ds").getStaticStrategy()).getReadDataSourceNames(), is(Arrays.asList("read")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds",
                new StaticReadwriteSplittingStrategyConfiguration("write", Arrays.asList("read")), null, null);
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertNotNull(actual.getDataSources().get("ds").getStaticStrategy());
        assertThat(actual.getDataSources().get("ds").getStaticStrategy().getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getStaticStrategy().getReadDataSourceNames(), is(Arrays.asList("read")));
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
        YamlStaticReadwriteSplittingStrategyConfiguration staticConfig = new YamlStaticReadwriteSplittingStrategyConfiguration();
        staticConfig.setWriteDataSourceName("write");
        staticConfig.setReadDataSourceNames(Arrays.asList("read"));
        result.getDataSources().get("read_query_ds").setStaticStrategy(staticConfig);
        return result;
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        ReadwriteSplittingDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("read_query_ds"));
        assertNotNull(group.getStaticStrategy());
        assertThat(group.getStaticStrategy().getWriteDataSourceName(), is("write"));
        assertThat(group.getStaticStrategy().getReadDataSourceNames(), is(Arrays.asList("read")));
    }
    
    private ReadwriteSplittingRuleConfigurationYamlSwapper getReadwriteSplittingRuleConfigurationYamlSwapper() {
        Optional<ReadwriteSplittingRuleConfigurationYamlSwapper> result = YamlRuleConfigurationSwapperFactory.getAllInstances().stream()
                .filter(each -> each instanceof ReadwriteSplittingRuleConfigurationYamlSwapper)
                .map(each -> (ReadwriteSplittingRuleConfigurationYamlSwapper) each)
                .findFirst();
        assertTrue(result.isPresent());
        return result.get();
    }
}
