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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.traffic.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traffic rule.
 */
public final class TrafficRule implements GlobalRule {
    
    static {
        ShardingSphereServiceLoader.register(TrafficAlgorithm.class);
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    private final Map<String, TrafficAlgorithm> trafficAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    public TrafficRule(final TrafficRuleConfiguration config) {
        config.getTrafficAlgorithms().forEach((key, value) -> trafficAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficAlgorithm.class)));
        config.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficLoadBalanceAlgorithm.class)));
    }
    
    @Override
    public String getType() {
        return TrafficRule.class.getSimpleName();
    }
}
