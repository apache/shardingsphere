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

package org.apache.shardingsphere.traffic.algorithm.engine;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TrafficEngineTest {
    
    @Mock
    private TrafficRule trafficRule;
    
    @Mock
    private InstanceContext instanceContext;
    
    @Mock
    private TrafficStrategyRule strategyRule;
    
    @Mock
    private QueryContext queryContext;
    
    @Test
    public void assertDispatchWhenNotExistTrafficStrategyRule() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(queryContext, false)).thenReturn(Optional.empty());
        Optional<String> actual = trafficEngine.dispatch(queryContext, false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertDispatchWhenTrafficStrategyRuleInvalid() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        TrafficStrategyRule strategyRule = mock(TrafficStrategyRule.class);
        when(strategyRule.getLabels()).thenReturn(Collections.emptyList());
        when(trafficRule.findMatchedStrategyRule(queryContext, false)).thenReturn(Optional.of(strategyRule));
        Optional<String> actual = trafficEngine.dispatch(queryContext, false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleNotExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(queryContext, false)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        Optional<String> actual = trafficEngine.dispatch(queryContext, false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(queryContext, false)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        TrafficLoadBalanceAlgorithm loadBalancer = mock(TrafficLoadBalanceAlgorithm.class);
        List<InstanceMetaData> instanceIds = mockComputeNodeInstances();
        when(loadBalancer.getInstanceId("traffic", instanceIds)).thenReturn(new ProxyInstanceMetaData("foo_id", 3307));
        when(strategyRule.getLoadBalancer()).thenReturn(loadBalancer);
        when(strategyRule.getName()).thenReturn("traffic");
        when(instanceContext.getAllClusterInstances(InstanceType.PROXY, Arrays.asList("OLTP", "OLAP"))).thenReturn(instanceIds);
        Optional<String> actual = trafficEngine.dispatch(queryContext, false);
        assertThat(actual, is(Optional.of("foo_id")));
    }
    
    private List<InstanceMetaData> mockComputeNodeInstances() {
        List<InstanceMetaData> result = new ArrayList<>();
        result.add(new ProxyInstanceMetaData("foo_id", "127.0.0.1@3307"));
        result.add(new ProxyInstanceMetaData("bar_id", "127.0.0.1@3308"));
        return result;
    }
}
