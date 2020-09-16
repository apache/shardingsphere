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
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationLogicTableRuleConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Consensus replication logic table rule configuration YAML swapper.
 */
public final class ConsensusReplicationLogicTableRuleConfigurationYamlSwapper 
        implements YamlSwapper<YamlConsensusReplicationLogicTableRuleConfiguration, ConsensusReplicationLogicTableRuleConfiguration> {
    
    private final ConsensusReplicationActualTableRuleConfigurationYamlSwapper actualTableRuleConfigurationYamlSwapper = new ConsensusReplicationActualTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlConsensusReplicationLogicTableRuleConfiguration swapToYamlConfiguration(final ConsensusReplicationLogicTableRuleConfiguration data) {
        Collection<YamlConsensusReplicationActualTableRuleConfiguration> replicaGroups = data.getReplicaGroups().stream()
                .map(actualTableRuleConfigurationYamlSwapper::swapToYamlConfiguration).collect(Collectors.toList());
        YamlConsensusReplicationLogicTableRuleConfiguration result = new YamlConsensusReplicationLogicTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setReplicaGroups(replicaGroups);
        return result;
    }
    
    @Override
    public ConsensusReplicationLogicTableRuleConfiguration swapToObject(final YamlConsensusReplicationLogicTableRuleConfiguration yamlConfiguration) {
        Collection<ConsensusReplicationActualTableRuleConfiguration> replicaGroups = yamlConfiguration.getReplicaGroups().stream()
                .map(actualTableRuleConfigurationYamlSwapper::swapToObject).collect(Collectors.toList());
        return new ConsensusReplicationLogicTableRuleConfiguration(yamlConfiguration.getLogicTable(), replicaGroups);
    }
}
