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

package org.apache.shardingsphere.readwritesplitting.yaml;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ReadwriteSplittingRuleConfigurationYamlIT() {
        super("yaml/readwrite-splitting-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static ReadwriteSplittingRuleConfiguration getExpectedRuleConfiguration() {
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = Arrays.asList(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("ds_0", "write_ds_0", Arrays.asList("write_ds_0_read_0", "write_ds_0_read_1"), "roundRobin"),
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("ds_1", "write_ds_1", Arrays.asList("write_ds_1_read_0", "write_ds_1_read_1"), "random"));
        Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>(2, 1F);
        loadBalancers.put("random", new AlgorithmConfiguration("RANDOM", new Properties()));
        loadBalancers.put("roundRobin", new AlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(dataSourceGroups, loadBalancers);
    }
    
    @Override
    protected boolean assertYamlConfiguration(final YamlRuleConfiguration actual) {
        assertReadwriteSplittingRule((YamlReadwriteSplittingRuleConfiguration) actual);
        return true;
    }
    
    private void assertReadwriteSplittingRule(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSourceGroups().size(), is(2));
        assertReadwriteSplittingRuleForDs0(actual);
        assertReadwriteSplittingRuleForDs1(actual);
    }
    
    private void assertReadwriteSplittingRuleForDs0(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSourceGroups().get("ds_0").getWriteDataSourceName(), is("write_ds_0"));
        assertThat(actual.getDataSourceGroups().get("ds_0").getLoadBalancerName(), is("roundRobin"));
    }
    
    private void assertReadwriteSplittingRuleForDs1(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSourceGroups().get("ds_1").getWriteDataSourceName(), is("write_ds_1"));
        assertThat(actual.getDataSourceGroups().get("ds_1").getLoadBalancerName(), is("random"));
    }
}
