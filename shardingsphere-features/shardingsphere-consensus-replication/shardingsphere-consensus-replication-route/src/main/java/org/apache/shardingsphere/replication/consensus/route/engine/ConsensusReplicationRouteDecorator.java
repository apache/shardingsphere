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

package org.apache.shardingsphere.replication.consensus.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.replication.consensus.constant.ConsensusReplicationOrder;
import org.apache.shardingsphere.replication.consensus.rule.ConsensusReplicationRule;
import org.apache.shardingsphere.replication.consensus.rule.ConsensusReplicationTableRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Route decorator for consensus replication.
 */
public final class ConsensusReplicationRouteDecorator implements RouteDecorator<ConsensusReplicationRule> {
    
    @Override
    public void decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ConsensusReplicationRule consensusReplicationRule, final ConfigurationProperties props) {
        Map<String, ConsensusReplicationGroup> replicaGroups = new HashMap<>();
        String schemaName = metaData.getSchemaName();
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        if (routeContext.getRouteResult().getRouteUnits().isEmpty()) {
            ConsensusReplicationTableRule replicaRoutingRule = consensusReplicationRule.getReplicaTableRules().iterator().next();
            ConsensusReplicationGroup replicaGroup = new ConsensusReplicationGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                    replicaRoutingRule.getDataSourceName());
            replicaGroups.put(ConsensusReplicationGroup.BLANK_CONSENSUS_REPLICATION_GROUP_KEY, replicaGroup);
            routeContext.addNextRouteStageContext(getTypeClass(), new ConsensusReplicationRouteStageContext(schemaName, replicaGroups, sqlStatementContext.isReadOnly()));
            return;
        }
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Collection<RouteMapper> routeMappers = each.getTableMappers();
            if (null == routeMappers || routeMappers.isEmpty()) {
                ConsensusReplicationTableRule tableRule = consensusReplicationRule.getReplicaTableRules().iterator().next();
                ConsensusReplicationGroup replicaGroup = new ConsensusReplicationGroup(
                        tableRule.getPhysicsTable(), tableRule.getReplicaGroupId(), tableRule.getReplicaPeers(), tableRule.getDataSourceName());
                replicaGroups.put(ConsensusReplicationGroup.BLANK_CONSENSUS_REPLICATION_GROUP_KEY, replicaGroup);
            } else {
                routeReplicaGroups(routeMappers, consensusReplicationRule, replicaGroups);
            }
        }
        routeContext.addNextRouteStageContext(getTypeClass(), new ConsensusReplicationRouteStageContext(schemaName, replicaGroups, sqlStatementContext.isReadOnly()));
    }
    
    private void routeReplicaGroups(final Collection<RouteMapper> routeMappers, final ConsensusReplicationRule replicaRule, final Map<String, ConsensusReplicationGroup> replicaGroups) {
        for (RouteMapper each : routeMappers) {
            String actualTableName = each.getActualName();
            Optional<ConsensusReplicationTableRule> replicaRoutingRuleOptional = replicaRule.findRoutingByTable(actualTableName);
            ConsensusReplicationGroup replicaGroup;
            if (replicaRoutingRuleOptional.isPresent()) {
                ConsensusReplicationTableRule replicaRoutingRule = replicaRoutingRuleOptional.get();
                replicaGroup = new ConsensusReplicationGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
                replicaGroups.put(actualTableName, replicaGroup);
            } else {
                ConsensusReplicationTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
                replicaGroup = new ConsensusReplicationGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
            }
            replicaGroups.put(actualTableName, replicaGroup);
        }
    }
    
    @Override
    public int getOrder() {
        return ConsensusReplicationOrder.ORDER;
    }
    
    @Override
    public Class<ConsensusReplicationRule> getTypeClass() {
        return ConsensusReplicationRule.class;
    }
}
