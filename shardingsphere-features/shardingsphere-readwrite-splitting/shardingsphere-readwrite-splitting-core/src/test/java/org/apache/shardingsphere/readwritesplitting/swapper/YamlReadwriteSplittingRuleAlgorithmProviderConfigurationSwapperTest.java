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

import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapper;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapperTest {
    
    private final YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapper swapper = new YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlReadwriteSplittingRuleConfiguration actual = createYamlReadwriteSplittingRuleConfiguration();
        assertThat(actual.getDataSources().keySet(), is(Collections.singleton("name")));
        assertNotNull(actual.getDataSources().get("name").getStaticStrategy());
        assertThat(actual.getDataSources().get("name").getStaticStrategy().getWriteDataSourceName(), is("writeDataSourceName"));
        assertThat(actual.getDataSources().get("name").getStaticStrategy().getReadDataSourceNames(), is(Collections.singletonList("readDataSourceName")));
        assertThat(actual.getDataSources().get("name").getLoadBalancerName(), is("loadBalancerName"));
        assertThat(actual.getLoadBalancers().keySet(), is(Collections.singleton("name")));
        assertThat(actual.getLoadBalancers().get("name").getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration actual = swapper.swapToObject(createYamlReadwriteSplittingRuleConfiguration());
        assertTrue(actual.getDataSources().iterator().hasNext());
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = actual.getDataSources().iterator().next();
        assertThat(ruleConfig.getName(), is("name"));
        assertNotNull(ruleConfig.getStaticStrategy());
        assertThat(ruleConfig.getStaticStrategy().getWriteDataSourceName(), is("writeDataSourceName"));
        assertThat(ruleConfig.getStaticStrategy().getReadDataSourceNames(), is(Collections.singletonList("readDataSourceName")));
        assertThat(ruleConfig.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(actual.getLoadBalanceAlgorithms(), is(Collections.emptyMap()));
    }
    
    private YamlReadwriteSplittingRuleConfiguration createYamlReadwriteSplittingRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("name",
                new StaticReadwriteSplittingStrategyConfiguration("writeDataSourceName", Collections.singletonList("readDataSourceName")), null, "loadBalancerName");
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedReadwriteSplittingRuleConfiguration(Collections.singletonList(ruleConfig), Collections.singletonMap("name", new RandomReadQueryLoadBalanceAlgorithm())));
    }
}
