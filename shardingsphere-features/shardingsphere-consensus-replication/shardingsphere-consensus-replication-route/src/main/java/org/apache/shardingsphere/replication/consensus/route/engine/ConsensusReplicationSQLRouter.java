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
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.replication.consensus.constant.ConsensusReplicationOrder;
import org.apache.shardingsphere.replication.consensus.rule.ConsensusReplicationRule;
import org.apache.shardingsphere.replication.consensus.rule.ConsensusReplicationTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Consensus replication SQL router.
 */
public final class ConsensusReplicationSQLRouter implements SQLRouter<ConsensusReplicationRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereSchema schema, final ConsensusReplicationRule rule, final ConfigurationProperties props) {
        RouteContext result = new RouteContext();
        ConsensusReplicationTableRule tableRule = rule.getReplicaTableRules().iterator().next();
        ConsensusReplicationGroup replicaGroup = new ConsensusReplicationGroup(
                tableRule.getPhysicsTable(), tableRule.getReplicaGroupId(),
                tableRule.getReplicaNodeRules().stream().map(ConsensusReplicationGroupNode::new).collect(Collectors.toList()));
        Map<String, ConsensusReplicationGroup> replicaGroups = Collections.singletonMap(ConsensusReplicationGroup.BLANK_CONSENSUS_REPLICATION_GROUP_KEY, replicaGroup);
        boolean isReadOnly = SQLUtil.isReadOnly(logicSQL.getSqlStatementContext().getSqlStatement());
        result.getRouteStageContexts().put(getTypeClass(), new ConsensusReplicationRouteStageContext(schema.getName(), replicaGroups, isReadOnly));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext,
                                     final LogicSQL logicSQL, final ShardingSphereSchema schema, final ConsensusReplicationRule rule, final ConfigurationProperties props) {
        Map<String, ConsensusReplicationGroup> replicaGroups = new HashMap<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            Collection<RouteMapper> routeMappers = each.getTableMappers();
            if (null == routeMappers || routeMappers.isEmpty()) {
                ConsensusReplicationTableRule tableRule = rule.getReplicaTableRules().iterator().next();
                ConsensusReplicationGroup replicaGroup = new ConsensusReplicationGroup(
                        tableRule.getPhysicsTable(), tableRule.getReplicaGroupId(),
                        tableRule.getReplicaNodeRules().stream().map(ConsensusReplicationGroupNode::new).collect(Collectors.toList()));
                replicaGroups.put(ConsensusReplicationGroup.BLANK_CONSENSUS_REPLICATION_GROUP_KEY, replicaGroup);
            } else {
                routeReplicaGroups(routeMappers, rule, replicaGroups);
            }
        }
        boolean isReadOnly = SQLUtil.isReadOnly(logicSQL.getSqlStatementContext().getSqlStatement());
        routeContext.getRouteStageContexts().put(getTypeClass(), new ConsensusReplicationRouteStageContext(schema.getName(), replicaGroups, isReadOnly));
    }
    
    private void routeReplicaGroups(final Collection<RouteMapper> routeMappers, final ConsensusReplicationRule rule, final Map<String, ConsensusReplicationGroup> replicaGroups) {
        for (RouteMapper each : routeMappers) {
            String actualTableName = each.getActualName();
            Optional<ConsensusReplicationTableRule> tableRuleOptional = rule.findRoutingByTable(actualTableName);
            ConsensusReplicationGroup replicaGroup;
            if (tableRuleOptional.isPresent()) {
                ConsensusReplicationTableRule tableRule = tableRuleOptional.get();
                replicaGroup = new ConsensusReplicationGroup(
                        tableRule.getPhysicsTable(), tableRule.getReplicaGroupId(),
                        tableRule.getReplicaNodeRules().stream().map(ConsensusReplicationGroupNode::new).collect(Collectors.toList()));
                replicaGroups.put(actualTableName, replicaGroup);
            } else {
                ConsensusReplicationTableRule tableRule = rule.getReplicaTableRules().iterator().next();
                replicaGroup = new ConsensusReplicationGroup(
                        tableRule.getPhysicsTable(), tableRule.getReplicaGroupId(),
                        tableRule.getReplicaNodeRules().stream().map(ConsensusReplicationGroupNode::new).collect(Collectors.toList()));
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
