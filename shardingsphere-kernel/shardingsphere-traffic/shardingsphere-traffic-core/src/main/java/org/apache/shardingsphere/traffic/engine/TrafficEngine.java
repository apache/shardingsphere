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

package org.apache.shardingsphere.traffic.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.traffic.context.TrafficContext;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Traffic engine.
 */
@RequiredArgsConstructor
public final class TrafficEngine {
    
    private final TrafficRule trafficRule;
    
    private final MetaDataContexts metaDataContexts;
    
    /**
     * Dispatch.
     *
     * @param logicSQL logic SQL
     * @return traffic context
     */
    public TrafficContext dispatch(final LogicSQL logicSQL) {
        Optional<TrafficStrategyRule> strategyRule = trafficRule.findMatchedStrategyRule(logicSQL);
        TrafficContext result = new TrafficContext();
        if (!strategyRule.isPresent()) {
            return result;
        }
        List<String> instanceIds = getInstanceIdsByLabels(strategyRule.get().getLabels());
        if (!instanceIds.isEmpty()) {
            TrafficLoadBalanceAlgorithm loadBalancer = trafficRule.findLoadBalancer(strategyRule.get().getLoadBalancerName());
            String instanceId = loadBalancer.getInstanceId(strategyRule.get().getName(), instanceIds);
            result.getExecutionUnits().add(createExecutionUnit(logicSQL, instanceId));
        }
        return result;
    }
    
    private ExecutionUnit createExecutionUnit(final LogicSQL logicSQL, final String instanceId) {
        return new ExecutionUnit(instanceId, new SQLUnit(logicSQL.getSql(), logicSQL.getParameters()));
    }
    
    private List<String> getInstanceIdsByLabels(final Collection<String> labels) {
        List<String> result = new ArrayList<>();
        if (metaDataContexts.getMetaDataPersistService().isPresent()) {
            Collection<ComputeNodeInstance> instances = metaDataContexts.getMetaDataPersistService().get().getComputeNodePersistService().loadComputeNodeInstances(InstanceType.PROXY, labels);
            for (ComputeNodeInstance each : instances) {
                result.add(each.getInstanceDefinition().getInstanceId().getId());
            }
        }
        return result;
    }
}
