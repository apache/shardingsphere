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

package org.apache.shardingsphere.readwritesplitting.yaml.converter;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;

/**
 * Readwrite splitting rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingRuleStatementConverter {
    
    /**
     * Convert create readwrite splitting rule statement to YAML readwrite splitting rule configuration.
     *
     * @param sqlStatement create readwrite splitting rule statement
     * @return YAML readwrite splitting rule configuration
     */
    public static YamlReadwriteSplittingRuleConfiguration convert(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        return convert(sqlStatement.getRules());
    }

    /**
     * Convert alter readwrite splitting rule statement to YAML readwrite splitting rule configuration.
     *
     * @param sqlStatement alter readwrite splitting rule statement
     * @return YAML readwrite splitting rule configuration
     */
    public static YamlReadwriteSplittingRuleConfiguration convert(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return convert(sqlStatement.getRules());
    }

    private static YamlReadwriteSplittingRuleConfiguration convert(final Collection<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegments) {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        for (ReadwriteSplittingRuleSegment each : readwriteSplittingRuleSegments) {
            String loadBalancerName = getLoadBalancerName(each.getName(), each.getLoadBalancer());
            result.getDataSources().put(each.getName(), buildDataSourceRuleConfiguration(loadBalancerName, each));
            result.getLoadBalancers().put(loadBalancerName, buildLoadBalancer(each));
        }
        return result;
    }

    private static YamlReadwriteSplittingDataSourceRuleConfiguration buildDataSourceRuleConfiguration(final String loadBalancerName,
                                                                                                      final ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment) {
        YamlReadwriteSplittingDataSourceRuleConfiguration result = new YamlReadwriteSplittingDataSourceRuleConfiguration();
        if (Strings.isNullOrEmpty(readwriteSplittingRuleSegment.getAutoAwareResource())) {
            result.setWriteDataSourceName(readwriteSplittingRuleSegment.getWriteDataSource());
            result.getReadDataSourceNames().addAll(readwriteSplittingRuleSegment.getReadDataSources());
        } else {
            result.setAutoAwareDataSourceName(readwriteSplittingRuleSegment.getAutoAwareResource());
        }
        result.setLoadBalancerName(loadBalancerName);
        result.setProps(readwriteSplittingRuleSegment.getProps());
        return result;
    }

    private static YamlShardingSphereAlgorithmConfiguration buildLoadBalancer(final ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(readwriteSplittingRuleSegment.getLoadBalancer());
        result.setProps(readwriteSplittingRuleSegment.getProps());
        return result;
    }

    private static String getLoadBalancerName(final String ruleName, final String loadBalancerType) {
        return String.format("%s_%s", ruleName, loadBalancerType);
    }
}
