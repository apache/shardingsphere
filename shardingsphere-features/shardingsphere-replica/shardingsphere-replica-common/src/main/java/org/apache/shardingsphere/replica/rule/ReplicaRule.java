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
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaTableRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replica rule.
 */
@Getter
@Slf4j
public final class ReplicaRule implements ShardingSphereRule {
    
    private final String schemaName;
    
    private final Collection<ReplicaTableRule> replicaTableRules;
    
    private final Map<String, ReplicaTableRule> physicsTableRules;
    
    public ReplicaRule(final String schemaName, final ReplicaRuleConfiguration configuration) {
        this.schemaName = schemaName;
        Collection<ReplicaTableRule> replicaTableRules = new ArrayList<>();
        Map<String, ReplicaTableRule> physicsTableRules = new ConcurrentHashMap<>();
        for (Map.Entry<String, ReplicaTableRuleConfiguration[]> entry : configuration.getTables().entrySet()) {
            for (ReplicaTableRuleConfiguration each : entry.getValue()) {
                String physicsTable = each.getPhysicsTable();
                ReplicaTableRule replaced = physicsTableRules.putIfAbsent(physicsTable, new ReplicaTableRule(each));
                if (replaced != null) {
                    log.error("key already exists, key={}", physicsTable);
                    throw new IllegalArgumentException("key already exists, key=" + physicsTable);
                }
                replicaTableRules.add(new ReplicaTableRule(each));
            }
        }
        configuration.getTables().forEach((logicTable, replicaTableRuleConfigurations) -> {
        
        });
        this.replicaTableRules = replicaTableRules;
        this.physicsTableRules = physicsTableRules;
    }
    
    /**
     * Find routing by table.
     *
     * @param physicsTable physics table name
     * @return ReplicaTableRule
     */
    public Optional<ReplicaTableRule> findRoutingByTable(final String physicsTable) {
        ReplicaTableRule rule = physicsTableRules.get(physicsTable);
        return Optional.ofNullable(rule);
    }
}
