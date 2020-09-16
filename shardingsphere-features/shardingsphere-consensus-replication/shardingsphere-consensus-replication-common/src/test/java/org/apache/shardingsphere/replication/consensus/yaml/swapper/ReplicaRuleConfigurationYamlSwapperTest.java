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
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlReplicaActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlReplicaRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReplicaRuleConfigurationYamlSwapperTest {
    
    private final ReplicaRuleConfigurationYamlSwapper swapper = new ReplicaRuleConfigurationYamlSwapper();
    
    private final String logicTableName = "t_order";
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeers = "127.0.0.1:9090";
    
    @Test
    public void assertSwapToYamlConfigurationWithMinProperties() {
        YamlReplicaRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(new ConsensusReplicationRuleConfiguration(
                Collections.singleton(new ConsensusReplicationLogicTableRuleConfiguration(logicTableName, null))));
        assertNotNull(yamlConfiguration);
        assertNotNull(yamlConfiguration.getTables());
        assertThat(yamlConfiguration.getTables().size(), is(1));
        Collection<YamlReplicaActualTableRuleConfiguration> resultReplicaGroups = yamlConfiguration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertTrue(resultReplicaGroups.isEmpty());
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        ConsensusReplicationActualTableRuleConfiguration replicaGroup = new ConsensusReplicationActualTableRuleConfiguration(physicsTable, replicaGroupId, replicaPeers, dataSourceName);
        Collection<ConsensusReplicationActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        ConsensusReplicationLogicTableRuleConfiguration table = new ConsensusReplicationLogicTableRuleConfiguration(logicTableName, replicaGroups);
        YamlReplicaRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(new ConsensusReplicationRuleConfiguration(Collections.singleton(table)));
        assertNotNull(yamlConfiguration);
        assertNotNull(yamlConfiguration.getTables());
        assertThat(yamlConfiguration.getTables().size(), is(1));
        Collection<YamlReplicaActualTableRuleConfiguration> resultReplicaGroups = yamlConfiguration.getTables().iterator().next().getReplicaGroups();
        assertNotNull(resultReplicaGroups);
        assertThat(resultReplicaGroups.size(), is(1));
        YamlReplicaActualTableRuleConfiguration resultReplicaGroup = resultReplicaGroups.iterator().next();
        assertThat(resultReplicaGroup.getDataSourceName(), is(dataSourceName));
        assertThat(resultReplicaGroup.getPhysicsTable(), is(physicsTable));
        assertThat(resultReplicaGroup.getReplicaGroupId(), is(replicaGroupId));
        assertThat(resultReplicaGroup.getReplicaPeers(), is(replicaPeers));
    }
    
    @Test
    public void assertSwapToObjectWithMinProperties() {
        YamlReplicaLogicTableRuleConfiguration yamlLogicTable = new YamlReplicaLogicTableRuleConfiguration();
        yamlLogicTable.setLogicTable(logicTableName);
        YamlReplicaRuleConfiguration yamlConfiguration = new YamlReplicaRuleConfiguration();
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
        YamlReplicaActualTableRuleConfiguration replicaGroup = new YamlReplicaActualTableRuleConfiguration();
        replicaGroup.setPhysicsTable(physicsTable);
        replicaGroup.setReplicaGroupId(replicaGroupId);
        replicaGroup.setReplicaPeers(replicaPeers);
        replicaGroup.setDataSourceName(dataSourceName);
        Collection<YamlReplicaActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        YamlReplicaLogicTableRuleConfiguration table = new YamlReplicaLogicTableRuleConfiguration();
        table.setLogicTable(logicTableName);
        table.setReplicaGroups(replicaGroups);
        YamlReplicaRuleConfiguration yamlConfiguration = new YamlReplicaRuleConfiguration();
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
