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

package org.apache.shardingsphere.traffic.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class YamlTrafficRuleConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlTrafficRuleConfiguration actual = new YamlTrafficRuleConfigurationSwapper().swapToYamlConfiguration(createTrafficRuleConfiguration());
        assertThat(actual.getTrafficStrategies().size(), is(1));
        assertTrue(actual.getTrafficStrategies().containsKey("group_by_traffic"));
        assertThat(actual.getTrafficAlgorithms().size(), is(1));
        assertTrue(actual.getTrafficAlgorithms().containsKey("group_by_algorithm"));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertTrue(actual.getLoadBalancers().containsKey("random"));
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("group_by_traffic", Arrays.asList("OLTP", "OLAP"), "group_by_algorithm", "random"));
        result.getTrafficAlgorithms().put("group_by_algorithm", createTrafficAlgorithm());
        result.getLoadBalancers().put("random", createLoadBalancer());
        return result;
    }
    
    private AlgorithmConfiguration createTrafficAlgorithm() {
        AlgorithmConfiguration result = mock(AlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SIMPLE");
        return result;
    }
    
    private AlgorithmConfiguration createLoadBalancer() {
        AlgorithmConfiguration result = mock(AlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        TrafficRuleConfiguration actual = new YamlTrafficRuleConfigurationSwapper().swapToObject(createYamlTrafficRuleConfiguration());
        assertThat(actual.getTrafficStrategies().size(), is(1));
        TrafficStrategyConfiguration strategyConfig = actual.getTrafficStrategies().iterator().next();
        assertThat(strategyConfig.getName(), is("group_by_traffic"));
        assertThat(strategyConfig.getLabels(), is(Arrays.asList("OLTP", "OLAP")));
        assertThat(strategyConfig.getAlgorithmName(), is("group_by_algorithm"));
        assertThat(strategyConfig.getLoadBalancerName(), is("random"));
        assertThat(actual.getTrafficAlgorithms().size(), is(1));
        assertTrue(actual.getTrafficAlgorithms().containsKey("group_by_algorithm"));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertTrue(actual.getLoadBalancers().containsKey("random"));
    }
    
    private YamlTrafficRuleConfiguration createYamlTrafficRuleConfiguration() {
        YamlTrafficStrategyConfiguration trafficStrategyConfig = new YamlTrafficStrategyConfiguration();
        trafficStrategyConfig.setLabels(Arrays.asList("OLTP", "OLAP"));
        trafficStrategyConfig.setAlgorithmName("group_by_algorithm");
        trafficStrategyConfig.setLoadBalancerName("random");
        YamlTrafficRuleConfiguration result = new YamlTrafficRuleConfiguration();
        result.getTrafficStrategies().put("group_by_traffic", trafficStrategyConfig);
        result.getTrafficAlgorithms().put("group_by_algorithm", createYamlTrafficAlgorithm());
        result.getLoadBalancers().put("random", createYamlLoadBalancer());
        return result;
    }
    
    private YamlAlgorithmConfiguration createYamlTrafficAlgorithm() {
        YamlAlgorithmConfiguration result = mock(YamlAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SIMPLE");
        return result;
    }
    
    private YamlAlgorithmConfiguration createYamlLoadBalancer() {
        YamlAlgorithmConfiguration result = mock(YamlAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
}
