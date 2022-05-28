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
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationYamlSwapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties(), "roundRobin");
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig), Collections.singletonMap("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getProps().getProperty("write-data-source-name"), is("write"));
        assertThat(actual.getDataSources().get("ds").getProps().getProperty("read-data-source-names"), is("read"));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties(), null);
        YamlReadwriteSplittingRuleConfiguration actual = getReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getProps().getProperty("write-data-source-name"), is("write"));
        assertThat(actual.getDataSources().get("ds").getProps().getProperty("read-data-source-names"), is("read"));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", "write");
        result.setProperty("read-data-source-names", "read");
        return result;
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
        result.getDataSources().get("read_query_ds").setType("Static");
        result.getDataSources().get("read_query_ds").setProps(getProperties());
        return result;
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        ReadwriteSplittingDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("read_query_ds"));
        assertThat(group.getProps().getProperty("write-data-source-name"), is("write"));
        assertThat(group.getProps().getProperty("read-data-source-names"), is("read"));
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
