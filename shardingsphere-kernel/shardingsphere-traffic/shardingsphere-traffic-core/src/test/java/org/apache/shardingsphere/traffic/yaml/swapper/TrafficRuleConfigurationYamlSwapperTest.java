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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.traffic.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TrafficRuleConfigurationYamlSwapperTest {
    
    private final TrafficRuleConfigurationYamlSwapper swapper = new TrafficRuleConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlTrafficRuleConfiguration actual = swapper.swapToYamlConfiguration(createTrafficRuleConfiguration());
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
    
    private ShardingSphereAlgorithmConfiguration createTrafficAlgorithm() {
        ShardingSphereAlgorithmConfiguration result = mock(ShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SIMPLE");
        return result;
    }
    
    private ShardingSphereAlgorithmConfiguration createLoadBalancer() {
        ShardingSphereAlgorithmConfiguration result = mock(ShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        TrafficRuleConfiguration actual = swapper.swapToObject(createYamlTrafficRuleConfiguration());
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
    
    private YamlShardingSphereAlgorithmConfiguration createYamlTrafficAlgorithm() {
        YamlShardingSphereAlgorithmConfiguration result = mock(YamlShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SIMPLE");
        return result;
    }
    
    private YamlShardingSphereAlgorithmConfiguration createYamlLoadBalancer() {
        YamlShardingSphereAlgorithmConfiguration result = mock(YamlShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(TrafficRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("TRAFFIC"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(800));
    }
}
