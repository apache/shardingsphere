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
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.List;
import java.util.Optional;

/**
 * Traffic engine.
 */
@RequiredArgsConstructor
public final class TrafficEngine {
    
    private final TrafficRule trafficRule;
    
    private final InstanceContext instanceContext;
    
    /**
     * Dispatch.
     *
     * @param queryContext query context
     * @param inTransaction is in transaction
     * @return instance id
     */
    public Optional<String> dispatch(final QueryContext queryContext, final boolean inTransaction) {
        Optional<TrafficStrategyRule> strategyRule = trafficRule.findMatchedStrategyRule(queryContext, inTransaction);
        if (!strategyRule.isPresent() || isInvalidStrategyRule(strategyRule.get())) {
            return Optional.empty();
        }
        List<InstanceMetaData> instances = instanceContext.getAllClusterInstances(InstanceType.PROXY, strategyRule.get().getLabels());
        if (!instances.isEmpty()) {
            TrafficLoadBalanceAlgorithm loadBalancer = strategyRule.get().getLoadBalancer();
            InstanceMetaData instanceMetaData = 1 == instances.size() ? instances.iterator().next() : loadBalancer.getInstanceId(strategyRule.get().getName(), instances);
            return Optional.of(instanceMetaData.getId());
        }
        return Optional.empty();
    }
    
    private boolean isInvalidStrategyRule(final TrafficStrategyRule strategyRule) {
        return strategyRule.getLabels().isEmpty() || null == strategyRule.getLoadBalancer();
    }
}
