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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationActualTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationLogicTableRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consensus replication rule.
 */
@Slf4j
public final class ConsensusReplicationRule implements ShardingSphereRule {
    
    @Getter
    private final Collection<ConsensusReplicationTableRule> replicaTableRules;
    
    private final Map<String, ConsensusReplicationTableRule> physicsTableRules;
    
    public ConsensusReplicationRule(final ConsensusReplicationRuleConfiguration config) {
        Collection<ConsensusReplicationTableRule> replicaTableRules = new ArrayList<>();
        Map<String, ConsensusReplicationTableRule> physicsTableRules = new ConcurrentHashMap<>();
        for (ConsensusReplicationLogicTableRuleConfiguration entry : config.getTables()) {
            for (ConsensusReplicationActualTableRuleConfiguration each : entry.getReplicaGroups()) {
                String physicsTable = each.getPhysicsTable();
                ConsensusReplicationTableRule replaced = physicsTableRules.putIfAbsent(physicsTable, new ConsensusReplicationTableRule(each));
                if (null != replaced) {
                    log.error("key already exists, key={}", physicsTable);
                    throw new IllegalArgumentException("key already exists, key=" + physicsTable);
                }
                replicaTableRules.add(new ConsensusReplicationTableRule(each));
            }
        }
        this.replicaTableRules = replicaTableRules;
        this.physicsTableRules = physicsTableRules;
    }
    
    /**
     * Find routing by table.
     *
     * @param physicsTable physics table name
     * @return consensus replication table rule
     */
    public Optional<ConsensusReplicationTableRule> findRoutingByTable(final String physicsTable) {
        return Optional.ofNullable(physicsTableRules.get(physicsTable));
    }
}
