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

package org.apache.shardingsphere.readwritesplitting.common.yaml.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadWriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadWriteSplittingRuleStatement;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadWriteSplittingDataSourceRuleConfiguration;

/**
 * Create read write splitting rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateReadWriteSplittingRuleStatementConverter {
    
    /**
     * Convert create read write splitting rule statement context to YAML read write splitting rule configuration.
     *
     * @param sqlStatement create read write splitting rule statement
     * @return YAML read write splitting rule configuration
     */
    public static YamlReadWriteSplittingRuleConfiguration convert(final CreateReadWriteSplittingRuleStatement sqlStatement) {
        YamlReadWriteSplittingRuleConfiguration result = new YamlReadWriteSplittingRuleConfiguration();
        for (ReadWriteSplittingRuleSegment each : sqlStatement.getReadWriteSplittingRules()) {
            YamlReadWriteSplittingDataSourceRuleConfiguration dataSourceRuleConfiguration = new YamlReadWriteSplittingDataSourceRuleConfiguration();
            dataSourceRuleConfiguration.setName(each.getName());
            dataSourceRuleConfiguration.setWriteDataSourceName(each.getWriteDataSource());
            dataSourceRuleConfiguration.getReadDataSourceNames().addAll(each.getReadDataSources());
            dataSourceRuleConfiguration.setLoadBalancerName(each.getLoadBalancer());
            dataSourceRuleConfiguration.setProps(each.getProps());
            result.getDataSources().put(each.getName(), dataSourceRuleConfiguration);
            YamlShardingSphereAlgorithmConfiguration loadBalancer = new YamlShardingSphereAlgorithmConfiguration();
            loadBalancer.setType(each.getLoadBalancer());
            loadBalancer.setProps(each.getProps());
            result.getLoadBalancers().put(each.getLoadBalancer(), loadBalancer);
        }
        return result;
    }
}
