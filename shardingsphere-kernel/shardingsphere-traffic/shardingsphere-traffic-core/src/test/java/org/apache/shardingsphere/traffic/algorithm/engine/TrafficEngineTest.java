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
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.traffic.context.TrafficContext;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.junit.Before;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TrafficEngineTest {
    
    @Mock
    private TrafficRule trafficRule;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TrafficStrategyRule strategyRule;
    
    @Mock
    private LogicSQL logicSQL;
    
    @Mock
    private MetaDataPersistService metaDataPersistService;
    
    @Before
    public void setUp() {
        when(metaDataPersistService.getComputeNodePersistService()).thenReturn(mock(ComputeNodePersistService.class));
    }
    
    @Test
    public void assertDispatchWhenNotExistTrafficStrategyRule() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, metaDataContexts);
        when(trafficRule.findMatchedStrategyRule(logicSQL)).thenReturn(Optional.empty());
        TrafficContext actual = trafficEngine.dispatch(logicSQL);
        assertThat(actual.getExecutionUnits().size(), is(0));
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleNotExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, metaDataContexts);
        when(trafficRule.findMatchedStrategyRule(logicSQL)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        when(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstances(InstanceType.PROXY, Arrays.asList("OLTP", "OLAP"))).thenReturn(Collections.emptyList());
        TrafficContext actual = trafficEngine.dispatch(logicSQL);
        assertThat(actual.getExecutionUnits().size(), is(0));
    }
    
    @Test
    public void assertDispatchWhenExistTrafficStrategyRuleExistComputeNodeInstances() {
        TrafficEngine trafficEngine = new TrafficEngine(trafficRule, metaDataContexts);
        when(trafficRule.findMatchedStrategyRule(logicSQL)).thenReturn(Optional.of(strategyRule));
        when(strategyRule.getLabels()).thenReturn(Arrays.asList("OLTP", "OLAP"));
        when(strategyRule.getLoadBalancerName()).thenReturn("RANDOM");
        when(strategyRule.getName()).thenReturn("traffic");
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        when(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstances(InstanceType.PROXY, Arrays.asList("OLTP", "OLAP"))).thenReturn(mockComputeNodeInstances());
        TrafficLoadBalanceAlgorithm algorithm = mock(TrafficLoadBalanceAlgorithm.class);
        when(algorithm.getInstanceId("traffic", Arrays.asList("127.0.0.1@3307", "127.0.0.1@3308"))).thenReturn("127.0.0.1@3307");
        when(trafficRule.findLoadBalancer("RANDOM")).thenReturn(algorithm);
        TrafficContext actual = trafficEngine.dispatch(logicSQL);
        assertThat(actual.getExecutionUnits().size(), is(1));
        assertThat(actual.getExecutionUnits().iterator().next().getDataSourceName(), is("127.0.0.1@3307"));
    }
    
    private Collection<ComputeNodeInstance> mockComputeNodeInstances() {
        Collection<ComputeNodeInstance> result = new LinkedList<>();
        ComputeNodeInstance instanceOlap = new ComputeNodeInstance();
        instanceOlap.setLabels(Collections.singletonList("OLAP"));
        instanceOlap.setInstanceDefinition(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3307"));
        result.add(instanceOlap);
        ComputeNodeInstance instanceOltp = new ComputeNodeInstance();
        instanceOltp.setLabels(Collections.singletonList("OLTP"));
        instanceOltp.setInstanceDefinition(new InstanceDefinition(InstanceType.PROXY, "127.0.0.1@3308"));
        result.add(instanceOltp);
        return result;
    }
}
