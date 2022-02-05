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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.convert;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;

import java.util.Collection;

/**
 * Traffic rule converter.
 */
public final class TrafficRuleConverter {
    
    /**
     * Convert traffic rule segment to traffic rule configuration.
     *
     * @param segments traffic rule segments
     * @return traffic rule configuration
     */
    public static TrafficRuleConfiguration convert(final Collection<TrafficRuleSegment> segments) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        segments.forEach(each -> setConfigurationData(result, each));
        return result;
    }
    
    private static void setConfigurationData(final TrafficRuleConfiguration result, final TrafficRuleSegment each) {
        ShardingSphereAlgorithmConfiguration trafficAlgorithm = createAlgorithmConfiguration(each.getAlgorithm());
        ShardingSphereAlgorithmConfiguration loadBalancer = createAlgorithmConfiguration(each.getLoadBalancer());
        String trafficAlgorithmName = createAlgorithmName(each.getName(), trafficAlgorithm);
        String loadBalancerName = createAlgorithmName(each.getName(), loadBalancer);
        TrafficStrategyConfiguration trafficStrategy = createTrafficStrategy(each, trafficAlgorithmName, loadBalancerName);
        result.getTrafficStrategies().add(trafficStrategy);
        result.getTrafficAlgorithms().put(trafficAlgorithmName, trafficAlgorithm);
        result.getLoadBalancers().put(loadBalancerName, loadBalancer);
    }
    
    private static ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private static TrafficStrategyConfiguration createTrafficStrategy(final TrafficRuleSegment trafficRuleSegment, final String trafficAlgorithmName, final String loadBalancerName) {
        return new TrafficStrategyConfiguration(trafficRuleSegment.getName(), trafficRuleSegment.getLabels(), trafficAlgorithmName, loadBalancerName);
    }
    
    private static String createAlgorithmName(final String ruleName, final ShardingSphereAlgorithmConfiguration algorithm) {
        return String.format("%s_%s", ruleName, algorithm.getType()).toLowerCase();
    }
}
