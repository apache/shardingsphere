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

package org.apache.shardingsphere.traffic.rule;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Traffic rule.
 */
public final class TrafficRule implements GlobalRule {
    
    static {
        ShardingSphereServiceLoader.register(TrafficAlgorithm.class);
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    private final Collection<TrafficStrategyRule> trafficStrategyRules = new LinkedList<>();
    
    private final Map<String, TrafficAlgorithm> trafficAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    public TrafficRule(final TrafficRuleConfiguration config) {
        config.getTrafficStrategies().forEach(each -> trafficStrategyRules.add(new TrafficStrategyRule(each.getName(), each.getLabels(), each.getAlgorithmName(), each.getLoadBalancerName())));
        config.getTrafficAlgorithms().forEach((key, value) -> trafficAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficAlgorithm.class)));
        config.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficLoadBalanceAlgorithm.class)));
    }
    
    @Override
    public String getType() {
        return TrafficRule.class.getSimpleName();
    }
    
    /**
     * Find matched strategy rule.
     * 
     * @param logicSQL logic SQL
     * @return matched strategy rule
     */
    public Optional<TrafficStrategyRule> findMatchedStrategyRule(final LogicSQL logicSQL) {
        for (TrafficStrategyRule each : trafficStrategyRules) {
            TrafficAlgorithm trafficAlgorithm = trafficAlgorithms.get(each.getAlgorithmName());
            Preconditions.checkState(null != trafficAlgorithm, "Traffic strategy rule configuration must match traffic algorithm.");
            // TODO add trafficAlgorithm match logic
            return Optional.of(each);
        }
        return Optional.empty();
    }
    
    /**
     * Find load balancer.
     * 
     * @param loadBalancerName load balancer name
     * @return load balancer
     */
    public TrafficLoadBalanceAlgorithm findLoadBalancer(final String loadBalancerName) {
        TrafficLoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.get(loadBalancerName);
        Preconditions.checkState(null != loadBalanceAlgorithm, "Traffic load balance algorithm can not be null.");
        return loadBalanceAlgorithm;
    }
}
