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

package org.apache.shardingsphere.replication.primaryreplica.rule.biulder;

import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.replication.primaryreplica.constant.PrimaryReplicaReplicationOrder;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;

import java.util.Collection;

/**
 * Replica query rule builder.
 */
public final class PrimaryReplicaReplicationRuleBuilder implements ShardingSphereRuleBuilder<PrimaryReplicaReplicationRule, PrimaryReplicaReplicationRuleConfiguration> {
    
    @Override
    public PrimaryReplicaReplicationRule build(final PrimaryReplicaReplicationRuleConfiguration ruleConfig, final Collection<String> dataSourceNames) {
        return new PrimaryReplicaReplicationRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaReplicationOrder.ORDER;
    }
    
    @Override
    public Class<PrimaryReplicaReplicationRuleConfiguration> getTypeClass() {
        return PrimaryReplicaReplicationRuleConfiguration.class;
    }
}
