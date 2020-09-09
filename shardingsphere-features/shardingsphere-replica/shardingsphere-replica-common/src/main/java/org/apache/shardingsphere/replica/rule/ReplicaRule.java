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

package org.apache.shardingsphere.replica.rule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.replica.api.config.ReplicaActualTableRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replica rule.
 */
@Slf4j
public final class ReplicaRule implements ShardingSphereRule {
    
    @Getter
    private final Collection<ReplicaTableRule> replicaTableRules;
    
    private final Map<String, ReplicaTableRule> physicsTableRules;
    
    public ReplicaRule(final ReplicaRuleConfiguration configuration) {
        Collection<ReplicaTableRule> replicaTableRules = new ArrayList<>();
        Map<String, ReplicaTableRule> physicsTableRules = new ConcurrentHashMap<>();
        for (ReplicaLogicTableRuleConfiguration entry : configuration.getTables()) {
            for (ReplicaActualTableRuleConfiguration each : entry.getReplicaGroups()) {
                String physicsTable = each.getPhysicsTable();
                ReplicaTableRule replaced = physicsTableRules.putIfAbsent(physicsTable, new ReplicaTableRule(each));
                if (null != replaced) {
                    log.error("key already exists, key={}", physicsTable);
                    throw new IllegalArgumentException("key already exists, key=" + physicsTable);
                }
                replicaTableRules.add(new ReplicaTableRule(each));
            }
        }
        this.replicaTableRules = replicaTableRules;
        this.physicsTableRules = physicsTableRules;
    }
    
    /**
     * Find routing by table.
     *
     * @param physicsTable physics table name
     * @return replica table rule
     */
    public Optional<ReplicaTableRule> findRoutingByTable(final String physicsTable) {
        return Optional.ofNullable(physicsTableRules.get(physicsTable));
    }
}
