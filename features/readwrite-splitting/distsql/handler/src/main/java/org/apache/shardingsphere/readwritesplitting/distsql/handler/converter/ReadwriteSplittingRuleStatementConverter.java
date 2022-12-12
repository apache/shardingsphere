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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Readwrite splitting rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingRuleStatementConverter {
    
    /**
     * Convert readwrite splitting rule segments to readwrite splitting rule configuration.
     *
     * @param ruleSegments readwrite splitting rule segments
     * @return readwrite splitting rule configuration
     */
    public static ReadwriteSplittingRuleConfiguration convert(final Collection<ReadwriteSplittingRuleSegment> ruleSegments) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        Map<String, AlgorithmConfiguration> loadBalancers = new HashMap<>(ruleSegments.size(), 1);
        for (ReadwriteSplittingRuleSegment each : ruleSegments) {
            if (null == each.getLoadBalancer()) {
                dataSources.add(createDataSourceRuleConfiguration(each, null, each.isAutoAware()));
            } else {
                String loadBalancerName = getLoadBalancerName(each.getName(), each.getLoadBalancer());
                loadBalancers.put(loadBalancerName, createLoadBalancer(each));
                dataSources.add(createDataSourceRuleConfiguration(each, loadBalancerName, each.isAutoAware()));
            }
        }
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancers);
    }
    
    private static ReadwriteSplittingDataSourceRuleConfiguration createDataSourceRuleConfiguration(final ReadwriteSplittingRuleSegment segment,
                                                                                                   final String loadBalancerName, final boolean isAutoAware) {
        return isAutoAware ? new ReadwriteSplittingDataSourceRuleConfiguration(segment.getName(), null,
                new DynamicReadwriteSplittingStrategyConfiguration(segment.getAutoAwareResource(), segment.getWriteDataSourceQueryEnabled()), loadBalancerName)
                : new ReadwriteSplittingDataSourceRuleConfiguration(segment.getName(),
                        new StaticReadwriteSplittingStrategyConfiguration(segment.getWriteDataSource(), new ArrayList<>(segment.getReadDataSources())), null, loadBalancerName);
    }
    
    private static AlgorithmConfiguration createLoadBalancer(final ReadwriteSplittingRuleSegment ruleSegment) {
        return new AlgorithmConfiguration(ruleSegment.getLoadBalancer(), ruleSegment.getProps());
    }
    
    private static String getLoadBalancerName(final String ruleName, final String type) {
        return String.format("%s_%s", ruleName, type);
    }
}
