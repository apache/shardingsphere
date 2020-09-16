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

import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.constant.ConsensusReplicationOrder;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationRuleConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Consensus replication rule configuration YAML swapper.
 */
public final class ConsensusReplicationRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlConsensusReplicationRuleConfiguration, ConsensusReplicationRuleConfiguration> {
    
    private final ConsensusReplicationLogicTableRuleConfigurationYamlSwapper logicTableRuleConfigurationYamlSwapper = new ConsensusReplicationLogicTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlConsensusReplicationRuleConfiguration swapToYamlConfiguration(final ConsensusReplicationRuleConfiguration data) {
        Collection<YamlConsensusReplicationLogicTableRuleConfiguration> yamlTables = data.getTables().stream()
                .map(logicTableRuleConfigurationYamlSwapper::swapToYamlConfiguration)
                .collect(Collectors.toList());
        YamlConsensusReplicationRuleConfiguration result = new YamlConsensusReplicationRuleConfiguration();
        result.setTables(yamlTables);
        return result;
    }
    
    @Override
    public ConsensusReplicationRuleConfiguration swapToObject(final YamlConsensusReplicationRuleConfiguration yamlConfiguration) {
        Collection<ConsensusReplicationLogicTableRuleConfiguration> tables = yamlConfiguration.getTables().stream()
                .map(logicTableRuleConfigurationYamlSwapper::swapToObject)
                .collect(Collectors.toList());
        return new ConsensusReplicationRuleConfiguration(tables);
    }
    
    @Override
    public Class<ConsensusReplicationRuleConfiguration> getTypeClass() {
        return ConsensusReplicationRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "REPLICA";
    }
    
    @Override
    public int getOrder() {
        return ConsensusReplicationOrder.ORDER;
    }
}
