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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.traffic.context.TrafficContext;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
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
    private LogicSQL logicSQL;
    
    @Test
    public void assertDispatchWhenNotExistTrafficStrategyRule() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(logicSQL, false)).thenReturn(Optional.empty());
        TrafficContext actual = trafficEngine.dispatch(logicSQL, false);
        assertNull(actual.getInstanceId());
    }
    
    @Test
    public void assertDispatchWhenTrafficStrategyRuleInvalid() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        TrafficStrategyRule strategyRule = mock(TrafficStrategyRule.class);
        when(strategyRule.getLabels()).thenReturn(Collections.emptyList());
        when(trafficRule.findMatchedStrategyRule(logicSQL, false)).thenReturn(Optional.of(strategyRule));
        TrafficContext actual = trafficEngine.dispatch(logicSQL, false);
        assertNull(actual.getInstanceId());
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleNotExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(logicSQL, false)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        TrafficContext actual = trafficEngine.dispatch(logicSQL, false);
        assertNull(actual.getInstanceId());
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, instanceContext);
        when(trafficRule.findMatchedStrategyRule(logicSQL, false)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        TrafficLoadBalanceAlgorithm loadBalancer = mock(TrafficLoadBalanceAlgorithm.class);
        when(loadBalancer.getInstanceId("traffic", Arrays.asList("127.0.0.1@3307", "127.0.0.1@3308"))).thenReturn("127.0.0.1@3307");
        when(strategyRule.getLoadBalancer()).thenReturn(loadBalancer);
        when(strategyRule.getName()).thenReturn("traffic");
        when(instanceContext.getComputeNodeInstances(InstanceType.PROXY, Arrays.asList("OLTP", "OLAP"))).thenReturn(mockComputeNodeInstances());
        TrafficContext actual = trafficEngine.dispatch(logicSQL, false);
        assertThat(actual.getInstanceId(), is("127.0.0.1@3307"));
    }
    
    private Collection<ComputeNodeInstance> mockComputeNodeInstances() {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        ComputeNodeInstance instanceOLAP = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        instanceOLAP.setLabels(Collections.singletonList("OLAP"));
        result.add(instanceOLAP);
        ComputeNodeInstance instanceOLTP = new ComputeNodeInstance(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3308"));
        instanceOLTP.setLabels(Collections.singletonList("OLTP"));
        result.add(instanceOLTP);
        return result;
    }
}
