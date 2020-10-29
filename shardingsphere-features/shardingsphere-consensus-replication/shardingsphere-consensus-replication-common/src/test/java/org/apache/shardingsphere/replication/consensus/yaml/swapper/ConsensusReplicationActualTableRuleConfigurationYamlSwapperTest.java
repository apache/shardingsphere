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
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationNodeRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationNodeRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConsensusReplicationActualTableRuleConfigurationYamlSwapperTest {
    
    private final ConsensusReplicationActualTableRuleConfigurationYamlSwapper swapper = new ConsensusReplicationActualTableRuleConfigurationYamlSwapper();
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeer = "127.0.0.1:9090";
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToYamlConfigurationWithMinProperties() {
        swapper.swapToYamlConfiguration(new ConsensusReplicationActualTableRuleConfiguration(null, null, null));
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        YamlConsensusReplicationActualTableRuleConfiguration yamlConfig = swapper.swapToYamlConfiguration(
                new ConsensusReplicationActualTableRuleConfiguration(physicsTable, replicaGroupId,
                        Collections.singletonList(new ConsensusReplicationNodeRuleConfiguration(replicaPeer, dataSourceName))));
        assertThat(yamlConfig.getPhysicsTable(), is(physicsTable));
        assertThat(yamlConfig.getReplicaGroupId(), is(replicaGroupId));
        assertNotNull(yamlConfig.getReplicaNodes());
        assertThat(yamlConfig.getReplicaNodes().size(), is(1));
        YamlConsensusReplicationNodeRuleConfiguration resultReplicaNode = yamlConfig.getReplicaNodes().iterator().next();
        assertThat(resultReplicaNode.getReplicaPeer(), is(replicaPeer));
        assertThat(resultReplicaNode.getDataSourceName(), is(dataSourceName));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToObjectWithMinProperties() {
        new ConsensusReplicationActualTableRuleConfiguration(null, null, null);
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlConsensusReplicationActualTableRuleConfiguration yamlConfig = new YamlConsensusReplicationActualTableRuleConfiguration();
        yamlConfig.setPhysicsTable(physicsTable);
        yamlConfig.setReplicaGroupId(replicaGroupId);
        YamlConsensusReplicationNodeRuleConfiguration replicaNode = new YamlConsensusReplicationNodeRuleConfiguration();
        replicaNode.setReplicaPeer(replicaPeer);
        replicaNode.setDataSourceName(dataSourceName);
        yamlConfig.setReplicaNodes(Collections.singletonList(replicaNode));
        ConsensusReplicationActualTableRuleConfiguration config = swapper.swapToObject(yamlConfig);
        assertThat(config.getPhysicsTable(), is(physicsTable));
        assertThat(config.getReplicaGroupId(), is(replicaGroupId));
        assertNotNull(config.getReplicaNodes());
        assertThat(config.getReplicaNodes().size(), is(1));
        ConsensusReplicationNodeRuleConfiguration resultReplicaNode = config.getReplicaNodes().iterator().next();
        assertThat(resultReplicaNode.getReplicaPeer(), is(replicaPeer));
        assertThat(resultReplicaNode.getDataSourceName(), is(dataSourceName));
    }
}
