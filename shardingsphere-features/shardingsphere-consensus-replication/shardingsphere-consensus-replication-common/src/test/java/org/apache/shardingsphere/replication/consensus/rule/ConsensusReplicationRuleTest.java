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

package org.apache.shardingsphere.replication.consensus.rule;

import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConsensusReplicationRuleTest {
    
    private final String logicTableName = "t_order";
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeers = "127.0.0.1:9090";
    
    @Test
    public void assertCannotFindRouting() {
        ConsensusReplicationRule consensusReplicationRule = createConsensusReplicationRule();
        Optional<ConsensusReplicationTableRule> routingRuleOptional = consensusReplicationRule.findRoutingByTable("not_exists_table");
        assertFalse(routingRuleOptional.isPresent());
    }
    
    @Test
    public void assertRoutingFound() {
        ConsensusReplicationRule replicaRule = createConsensusReplicationRule();
        Optional<ConsensusReplicationTableRule> routingRuleOptional = replicaRule.findRoutingByTable(physicsTable);
        assertTrue(routingRuleOptional.isPresent());
        ConsensusReplicationTableRule routingRule = routingRuleOptional.get();
        assertNotNull(routingRule);
        assertThat(routingRule.getDataSourceName(), is(dataSourceName));
        assertThat(routingRule.getPhysicsTable(), is(physicsTable));
        assertThat(routingRule.getReplicaGroupId(), is(replicaGroupId));
        assertThat(routingRule.getReplicaPeers(), is(replicaPeers));
    }
    
    private ConsensusReplicationRule createConsensusReplicationRule() {
        ConsensusReplicationActualTableRuleConfiguration replicaGroup = new ConsensusReplicationActualTableRuleConfiguration(physicsTable, replicaGroupId, replicaPeers, dataSourceName);
        Collection<ConsensusReplicationActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        ConsensusReplicationLogicTableRuleConfiguration table = new ConsensusReplicationLogicTableRuleConfiguration(logicTableName, replicaGroups);
        ConsensusReplicationRuleConfiguration config = new ConsensusReplicationRuleConfiguration(Collections.singleton(table));
        return new ConsensusReplicationRule(config);
    }
}
