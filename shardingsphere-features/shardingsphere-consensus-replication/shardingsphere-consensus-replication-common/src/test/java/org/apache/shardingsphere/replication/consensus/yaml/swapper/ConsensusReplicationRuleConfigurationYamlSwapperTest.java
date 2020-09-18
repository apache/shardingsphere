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

import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConsensusReplicationRuleConfigurationYamlSwapperTest {
    
    private final ConsensusReplicationRuleConfigurationYamlSwapper swapper = new ConsensusReplicationRuleConfigurationYamlSwapper();
    
    private final String logicTableName = "t_order";
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeers = "127.0.0.1:9090";
    
    @Test
    public void assertSwapToYamlConfigurationWithMinProperties() {
        YamlConsensusReplicationRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(new ConsensusReplicationRuleConfiguration(
                Collections.singleton(new ConsensusReplicationLogicTableRuleConfiguration(logicTableName, null))));
        assertNotNull(yamlConfiguration);
        assertNotNull(yamlConfiguration.getTables());
        assertThat(yamlConfiguration.getTables().size(), is(1));
        Collection<YamlConsensusReplicationActualTableRuleConfiguration> resultReplicaGroups = yamlConfiguration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertTrue(resultReplicaGroups.isEmpty());
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        ConsensusReplicationActualTableRuleConfiguration replicaGroup = new ConsensusReplicationActualTableRuleConfiguration(physicsTable, replicaGroupId, replicaPeers, dataSourceName);
        Collection<ConsensusReplicationActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        ConsensusReplicationLogicTableRuleConfiguration table = new ConsensusReplicationLogicTableRuleConfiguration(logicTableName, replicaGroups);
        YamlConsensusReplicationRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(new ConsensusReplicationRuleConfiguration(Collections.singleton(table)));
        assertNotNull(yamlConfiguration);
        assertNotNull(yamlConfiguration.getTables());
        assertThat(yamlConfiguration.getTables().size(), is(1));
        Collection<YamlConsensusReplicationActualTableRuleConfiguration> resultReplicaGroups = yamlConfiguration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertThat(resultReplicaGroups.size(), is(1));
        YamlConsensusReplicationActualTableRuleConfiguration resultReplicaGroup = resultReplicaGroups.iterator().next();
        assertThat(resultReplicaGroup.getDataSourceName(), is(dataSourceName));
        assertThat(resultReplicaGroup.getPhysicsTable(), is(physicsTable));
        assertThat(resultReplicaGroup.getReplicaGroupId(), is(replicaGroupId));
        assertThat(resultReplicaGroup.getReplicaPeers(), is(replicaPeers));
    }
    
    @Test
    public void assertSwapToObjectWithMinProperties() {
        YamlConsensusReplicationLogicTableRuleConfiguration yamlLogicTable = new YamlConsensusReplicationLogicTableRuleConfiguration();
        yamlLogicTable.setLogicTable(logicTableName);
        YamlConsensusReplicationRuleConfiguration yamlConfiguration = new YamlConsensusReplicationRuleConfiguration();
        yamlConfiguration.setTables(Collections.singleton(yamlLogicTable));
        ConsensusReplicationRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        assertNotNull(configuration);
        assertNotNull(configuration.getTables());
        assertThat(configuration.getTables().size(), is(1));
        Collection<ConsensusReplicationActualTableRuleConfiguration> resultReplicaGroups = configuration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertTrue(resultReplicaGroups.isEmpty());
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlConsensusReplicationActualTableRuleConfiguration replicaGroup = new YamlConsensusReplicationActualTableRuleConfiguration();
        replicaGroup.setPhysicsTable(physicsTable);
        replicaGroup.setReplicaGroupId(replicaGroupId);
        replicaGroup.setReplicaPeers(replicaPeers);
        replicaGroup.setDataSourceName(dataSourceName);
        Collection<YamlConsensusReplicationActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        YamlConsensusReplicationLogicTableRuleConfiguration table = new YamlConsensusReplicationLogicTableRuleConfiguration();
        table.setLogicTable(logicTableName);
        table.setReplicaGroups(replicaGroups);
        YamlConsensusReplicationRuleConfiguration yamlConfiguration = new YamlConsensusReplicationRuleConfiguration();
        yamlConfiguration.setTables(Collections.singleton(table));
        ConsensusReplicationRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        assertNotNull(configuration);
        assertNotNull(configuration.getTables());
        assertThat(configuration.getTables().size(), is(1));
        Collection<ConsensusReplicationActualTableRuleConfiguration> resultReplicaGroups = configuration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertThat(resultReplicaGroups.size(), is(1));
        ConsensusReplicationActualTableRuleConfiguration resultReplicaGroup = resultReplicaGroups.iterator().next();
        assertThat(resultReplicaGroup.getDataSourceName(), is(dataSourceName));
        assertThat(resultReplicaGroup.getPhysicsTable(), is(physicsTable));
        assertThat(resultReplicaGroup.getReplicaGroupId(), is(replicaGroupId));
        assertThat(resultReplicaGroup.getReplicaPeers(), is(replicaPeers));
    }
}
