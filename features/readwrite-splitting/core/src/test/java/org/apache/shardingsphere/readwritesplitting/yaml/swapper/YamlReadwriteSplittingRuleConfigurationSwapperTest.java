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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YamlReadwriteSplittingRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlReadwriteSplittingRuleConfiguration actual = getSwapper().swapToYamlConfiguration(creatReadwriteSplittingRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertReadwriteSplittingRule(actual);
    }
    
    void assertReadwriteSplittingRule(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertNotNull(actual.getDataSources().get("readwrite"));
        YamlReadwriteSplittingDataSourceRuleConfiguration config = actual.getDataSources().get("readwrite");
        assertThat(config.getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.getDataSources().get("readwrite").getLoadBalancerName(), is("random"));
    }
    
    void assertReadwriteSplittingRule(final ReadwriteSplittingRuleConfiguration actual) {
        ReadwriteSplittingDataSourceRuleConfiguration config = actual.getDataSources().iterator().next();
        assertThat(config.getName(), is("t_readwrite"));
        assertThat(config.getWriteDataSourceName(), is("write_ds"));
        assertThat(config.getLoadBalancerName(), is("random"));
    }
    
    private ReadwriteSplittingRuleConfiguration creatReadwriteSplittingRuleConfiguration() {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = Collections.singleton(
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random"));
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("myLoadBalancer", new AlgorithmConfiguration("RANDOM", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancers);
    }
    
    @Test
    void assertSwapToObject() {
        ReadwriteSplittingRuleConfiguration actual = getSwapper().swapToObject(createYamlReadwriteSplittingRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertReadwriteSplittingRule(actual);
    }
    
    private YamlReadwriteSplittingRuleConfiguration createYamlReadwriteSplittingRuleConfiguration() {
        YamlReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new YamlReadwriteSplittingDataSourceRuleConfiguration();
        dataSourceRuleConfig.setReadDataSourceNames(Arrays.asList("read_ds_0", "read_ds_1"));
        dataSourceRuleConfig.setWriteDataSourceName("write_ds");
        dataSourceRuleConfig.setLoadBalancerName("random");
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        result.getDataSources().put("t_readwrite", dataSourceRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("RANDOM");
        result.getLoadBalancers().put("random_loadbalancer", algorithmConfig);
        return result;
    }
    
    private YamlReadwriteSplittingRuleConfigurationSwapper getSwapper() {
        return new YamlReadwriteSplittingRuleConfigurationSwapper();
    }
}
