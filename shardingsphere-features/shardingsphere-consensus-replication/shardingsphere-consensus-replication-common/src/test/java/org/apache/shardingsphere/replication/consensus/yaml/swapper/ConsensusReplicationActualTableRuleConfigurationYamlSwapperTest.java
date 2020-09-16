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
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationActualTableRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConsensusReplicationActualTableRuleConfigurationYamlSwapperTest {
    
    private final ConsensusReplicationActualTableRuleConfigurationYamlSwapper swapper = new ConsensusReplicationActualTableRuleConfigurationYamlSwapper();
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeers = "127.0.0.1:9090";
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToYamlConfigurationWithMinProperties() {
        swapper.swapToYamlConfiguration(new ConsensusReplicationActualTableRuleConfiguration(null, null, null, null));
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        YamlConsensusReplicationActualTableRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(
                new ConsensusReplicationActualTableRuleConfiguration(physicsTable, replicaGroupId, replicaPeers, dataSourceName));
        assertThat(yamlConfiguration.getDataSourceName(), is(dataSourceName));
        assertThat(yamlConfiguration.getPhysicsTable(), is(physicsTable));
        assertThat(yamlConfiguration.getReplicaGroupId(), is(replicaGroupId));
        assertThat(yamlConfiguration.getReplicaPeers(), is(replicaPeers));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToObjectWithMinProperties() {
        new ConsensusReplicationActualTableRuleConfiguration(null, null, null, null);
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlConsensusReplicationActualTableRuleConfiguration yamlConfiguration = new YamlConsensusReplicationActualTableRuleConfiguration();
        yamlConfiguration.setPhysicsTable(physicsTable);
        yamlConfiguration.setReplicaGroupId(replicaGroupId);
        yamlConfiguration.setReplicaPeers(replicaPeers);
        yamlConfiguration.setDataSourceName(dataSourceName);
        ConsensusReplicationActualTableRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        assertThat(configuration.getDataSourceName(), is(dataSourceName));
        assertThat(configuration.getPhysicsTable(), is(physicsTable));
        assertThat(configuration.getReplicaGroupId(), is(replicaGroupId));
        assertThat(configuration.getReplicaPeers(), is(replicaPeers));
    }
}
