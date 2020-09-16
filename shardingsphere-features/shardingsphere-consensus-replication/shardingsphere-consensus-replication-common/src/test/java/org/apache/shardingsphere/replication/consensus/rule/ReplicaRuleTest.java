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

import org.apache.shardingsphere.replication.consensus.api.config.ReplicaActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ReplicaRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReplicaRuleTest {
    
    private final String logicTableName = "t_order";
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String physicsTable = "t_order_1";
    
    private final String replicaGroupId = "raftGroupTest1";
    
    private final String replicaPeers = "127.0.0.1:9090";
    
    private ReplicaRule createReplicaRule() {
        ReplicaActualTableRuleConfiguration replicaGroup = new ReplicaActualTableRuleConfiguration(physicsTable, replicaGroupId, replicaPeers, dataSourceName);
        Collection<ReplicaActualTableRuleConfiguration> replicaGroups = Collections.singleton(replicaGroup);
        ReplicaLogicTableRuleConfiguration table = new ReplicaLogicTableRuleConfiguration(logicTableName, replicaGroups);
        ReplicaRuleConfiguration configuration = new ReplicaRuleConfiguration(Collections.singleton(table));
        return new ReplicaRule(configuration);
    }
    
    @Test
    public void assertCannotFindRouting() {
        ReplicaRule replicaRule = createReplicaRule();
        Optional<ReplicaTableRule> routingRuleOptional = replicaRule.findRoutingByTable("not_exists_table");
        assertFalse(routingRuleOptional.isPresent());
    }
    
    @Test
    public void assertRoutingFound() {
        ReplicaRule replicaRule = createReplicaRule();
        Optional<ReplicaTableRule> routingRuleOptional = replicaRule.findRoutingByTable(physicsTable);
        assertTrue(routingRuleOptional.isPresent());
        ReplicaTableRule routingRule = routingRuleOptional.get();
        assertNotNull(routingRule);
        assertThat(routingRule.getDataSourceName(), is(dataSourceName));
        assertThat(routingRule.getPhysicsTable(), is(physicsTable));
        assertThat(routingRule.getReplicaGroupId(), is(replicaGroupId));
        assertThat(routingRule.getReplicaPeers(), is(replicaPeers));
    }
}
