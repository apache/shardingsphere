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

package org.apache.shardingsphere.replication.primaryreplica.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.rule.YamlPrimaryReplicaReplicationDataSourceRuleConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Primary-replica replication rule configuration for YAML.
 */
@Getter
@Setter
public final class YamlPrimaryReplicaReplicationRuleConfiguration implements YamlRuleConfiguration {
    
    private Map<String, YamlPrimaryReplicaReplicationDataSourceRuleConfiguration> dataSources = new LinkedHashMap<>();
    
    private Map<String, YamlShardingSphereAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
    
    @Override
    public Class<PrimaryReplicaReplicationRuleConfiguration> getRuleConfigurationType() {
        return PrimaryReplicaReplicationRuleConfiguration.class;
    }
}
