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

package org.apache.shardingsphere.traffic.distsql.handler.convert;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;

import java.util.Collection;
import java.util.Optional;

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
        for (TrafficRuleSegment each : segments) {
            setConfigurationData(result, each);
        }
        return result;
    }
    
    private static void setConfigurationData(final TrafficRuleConfiguration trafficRuleConfig, final TrafficRuleSegment segment) {
        AlgorithmConfiguration trafficAlgorithm = createAlgorithmConfiguration(segment.getAlgorithm());
        AlgorithmConfiguration loadBalancer = createAlgorithmConfiguration(segment.getLoadBalancer());
        String trafficAlgorithmName = createAlgorithmName(segment.getName(), trafficAlgorithm);
        String loadBalancerName = createAlgorithmName(segment.getName(), loadBalancer);
        TrafficStrategyConfiguration trafficStrategy = createTrafficStrategy(segment, trafficAlgorithmName, loadBalancerName);
        trafficRuleConfig.getTrafficStrategies().add(trafficStrategy);
        trafficRuleConfig.getTrafficAlgorithms().put(trafficAlgorithmName, trafficAlgorithm);
        Optional.ofNullable(loadBalancerName).ifPresent(optional -> trafficRuleConfig.getLoadBalancers().put(loadBalancerName, loadBalancer));
    }
    
    private static AlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return null == segment ? null : new AlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private static String createAlgorithmName(final String ruleName, final AlgorithmConfiguration algorithm) {
        return null == algorithm ? null : String.format("%s_%s", ruleName, algorithm.getType()).toLowerCase();
    }
    
    private static TrafficStrategyConfiguration createTrafficStrategy(final TrafficRuleSegment trafficRuleSegment, final String trafficAlgorithmName, final String loadBalancerName) {
        return new TrafficStrategyConfiguration(trafficRuleSegment.getName(), trafficRuleSegment.getLabels(), trafficAlgorithmName, loadBalancerName);
    }
}
