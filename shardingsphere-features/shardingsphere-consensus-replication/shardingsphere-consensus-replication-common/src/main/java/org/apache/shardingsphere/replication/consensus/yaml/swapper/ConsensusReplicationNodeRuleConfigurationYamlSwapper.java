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

package org.apache.shardingsphere.replication.consensus.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationNodeRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationNodeRuleConfiguration;

/**
 * Consensus replication node rule configuration YAML swapper.
 */
public final class ConsensusReplicationNodeRuleConfigurationYamlSwapper
        implements YamlSwapper<YamlConsensusReplicationNodeRuleConfiguration, ConsensusReplicationNodeRuleConfiguration> {
    
    @Override
    public YamlConsensusReplicationNodeRuleConfiguration swapToYamlConfiguration(final ConsensusReplicationNodeRuleConfiguration data) {
        YamlConsensusReplicationNodeRuleConfiguration result = new YamlConsensusReplicationNodeRuleConfiguration();
        result.setReplicaPeer(data.getReplicaPeer());
        result.setDataSourceName(data.getDataSourceName());
        return result;
    }
    
    @Override
    public ConsensusReplicationNodeRuleConfiguration swapToObject(final YamlConsensusReplicationNodeRuleConfiguration yamlConfig) {
        return new ConsensusReplicationNodeRuleConfiguration(yamlConfig.getReplicaPeer(), yamlConfig.getDataSourceName());
    }
}
