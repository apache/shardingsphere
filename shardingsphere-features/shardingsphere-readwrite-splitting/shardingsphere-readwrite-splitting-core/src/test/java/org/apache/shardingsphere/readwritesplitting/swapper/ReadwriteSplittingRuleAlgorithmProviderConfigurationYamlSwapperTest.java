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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper swapper = new ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlReadwriteSplittingRuleConfiguration actual = createYamlReadwriteSplittingRuleConfiguration();
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertThat(actual.getDataSources().keySet(), is(Collections.singleton("name")));
        Properties props = actual.getDataSources().get("name").getProps();
        assertNotNull(props);
        assertThat(props.getProperty("write-data-source-name"), is("writeDataSourceName"));
        assertThat(props.getProperty("read-data-source-names"), is("readDataSourceName"));
        assertThat(actual.getDataSources().get("name").getLoadBalancerName(), is("loadBalancerName"));
        assertNotNull(actual.getLoadBalancers());
        assertThat(actual.getLoadBalancers().keySet(), is(Collections.singleton("name")));
        assertNotNull(actual.getLoadBalancers().get("name"));
        assertThat(actual.getLoadBalancers().get("name").getType(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration actual = swapper.swapToObject(createYamlReadwriteSplittingRuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfig);
        assertThat(ruleConfig.getName(), is("name"));
        assertNotNull(ruleConfig.getProps());
        assertThat(ruleConfig.getProps().getProperty("write-data-source-name"), is("writeDataSourceName"));
        assertThat(ruleConfig.getProps().getProperty("read-data-source-names"), is("readDataSourceName"));
        assertThat(ruleConfig.getLoadBalancerName(), is("loadBalancerName"));
        assertThat(actual.getLoadBalanceAlgorithms(), is(Collections.emptyMap()));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("READWRITE_SPLITTING"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(ReadwriteSplittingOrder.ALGORITHM_PROVIDER_ORDER));
    }
    
    private YamlReadwriteSplittingRuleConfiguration createYamlReadwriteSplittingRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("write-data-source-name", "writeDataSourceName");
        props.setProperty("read-data-source-names", "readDataSourceName");
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("name", "Static", props, "loadBalancerName");
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedReadwriteSplittingRuleConfiguration(Collections.singletonList(ruleConfig), ImmutableMap.of("name", new RandomReplicaLoadBalanceAlgorithm())));
    }
}
