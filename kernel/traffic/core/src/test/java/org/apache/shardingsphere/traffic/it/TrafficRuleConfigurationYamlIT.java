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

package org.apache.shardingsphere.traffic.it;

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrafficRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    TrafficRuleConfigurationYamlIT() {
        super("yaml/traffic-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertTrafficRule((YamlTrafficRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertTrafficRule(final YamlTrafficRuleConfiguration actual) {
        assertTrafficStrategies(actual.getTrafficStrategies());
        assertTrafficAlgorithms(actual.getTrafficAlgorithms());
        assertLoadBalancers(actual.getLoadBalancers());
    }
    
    private void assertTrafficStrategies(final Map<String, YamlTrafficStrategyConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_strategy").getName(), is("foo_strategy"));
        assertThat(actual.get("foo_strategy").getLabels(), is(Arrays.asList("label_0", "label_1")));
        assertThat(actual.get("foo_strategy").getAlgorithmName(), is("foo_traffic"));
        assertThat(actual.get("foo_strategy").getLoadBalancerName(), is("foo_loadbalancer"));
    }
    
    private void assertTrafficAlgorithms(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_traffic").getType(), is("TRAFFIC_FIXTURE"));
        assertTrue(actual.get("foo_traffic").getProps().isEmpty());
    }
    
    private void assertLoadBalancers(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_load_balancers").getType(), is("LOAD_BALANCER_FIXTURE"));
        assertTrue(actual.get("foo_load_balancers").getProps().isEmpty());
    }
}
