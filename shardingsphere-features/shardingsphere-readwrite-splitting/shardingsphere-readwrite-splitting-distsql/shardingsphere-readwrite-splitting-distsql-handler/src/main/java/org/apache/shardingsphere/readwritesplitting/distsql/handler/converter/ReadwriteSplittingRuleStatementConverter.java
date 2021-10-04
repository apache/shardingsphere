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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;

import java.util.Collection;
import java.util.Collections;
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
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new HashMap<>(ruleSegments.size(), 1);
        for (ReadwriteSplittingRuleSegment each : ruleSegments) {
            String loadBalancerName = getLoadBalancerName(each.getName(), each.getLoadBalancer());
            dataSources.add(createDataSourceRuleConfiguration(each.getName(), loadBalancerName, each));
            loadBalancers.put(loadBalancerName, createLoadBalancer(each));
        }
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancers);
    }
    
    private static ReadwriteSplittingDataSourceRuleConfiguration createDataSourceRuleConfiguration(final String name, final String loadBalancerName, final ReadwriteSplittingRuleSegment ruleSegment) {
        return Strings.isNullOrEmpty(ruleSegment.getAutoAwareResource())
                ? new ReadwriteSplittingDataSourceRuleConfiguration(name, null, ruleSegment.getWriteDataSource(), new LinkedList<>(ruleSegment.getReadDataSources()), loadBalancerName, false)
                : new ReadwriteSplittingDataSourceRuleConfiguration(name, ruleSegment.getAutoAwareResource(), null, Collections.emptyList(), loadBalancerName, false);
    }
    
    private static ShardingSphereAlgorithmConfiguration createLoadBalancer(final ReadwriteSplittingRuleSegment ruleSegment) {
        return new ShardingSphereAlgorithmConfiguration(ruleSegment.getLoadBalancer(), ruleSegment.getProps());
    }
    
    private static String getLoadBalancerName(final String ruleName, final String type) {
        return String.format("%s_%s", ruleName, type);
    }
}
