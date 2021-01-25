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

package org.apache.shardingsphere.replicaquery.yaml.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReplicaQueryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReplicaQueryRuleStatement;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;

/**
 * Create replica query rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateReplicaQueryRuleStatementConverter {
    
    /**
     * Convert create replica query rule statement context to YAML replica query rule configuration.
     *
     * @param sqlStatement create replica query rule statement
     * @return YAML replica query rule configuration
     */
    public static YamlReplicaQueryRuleConfiguration convert(final CreateReplicaQueryRuleStatement sqlStatement) {
        YamlReplicaQueryRuleConfiguration result = new YamlReplicaQueryRuleConfiguration();
        for (ReplicaQueryRuleSegment each : sqlStatement.getReplicaQueryRules()) {
            YamlReplicaQueryDataSourceRuleConfiguration dataSourceRuleConfiguration = new YamlReplicaQueryDataSourceRuleConfiguration();
            dataSourceRuleConfiguration.setName(each.getName());
            dataSourceRuleConfiguration.setPrimaryDataSourceName(each.getPrimaryDataSource());
            dataSourceRuleConfiguration.getReplicaDataSourceNames().addAll(each.getReplicaDataSources());
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
