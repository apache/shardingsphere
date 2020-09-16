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

package org.apache.shardingsphere.replica.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.rule.ReplicaRule;
import org.apache.shardingsphere.replica.rule.ReplicaTableRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Route decorator for replica.
 */
public final class ReplicaRouteDecorator implements RouteDecorator<ReplicaRule> {
    
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ReplicaRule replicaRule, final ConfigurationProperties props) {
        Map<String, ReplicaGroup> replicaGroups = new HashMap<>();
        String schemaName = metaData.getSchemaName();
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        if (routeContext.getRouteResult().getRouteUnits().isEmpty()) {
            ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
            ReplicaGroup replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                    replicaRoutingRule.getDataSourceName());
            replicaGroups.put(ReplicaGroup.BLANK_REPLICA_GROUP_KEY, replicaGroup);
            return new RouteContext(routeContext, routeContext.getRouteResult(), new ReplicaRouteStageContext(schemaName, replicaGroups, sqlStatementContext.isReadOnly()), getTypeClass());
        }
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            Collection<RouteMapper> routeMappers = each.getTableMappers();
            if (null == routeMappers || routeMappers.isEmpty()) {
                ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
                ReplicaGroup replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
                replicaGroups.put(ReplicaGroup.BLANK_REPLICA_GROUP_KEY, replicaGroup);
            } else {
                routeReplicaGroups(routeMappers, replicaRule, replicaGroups);
            }
        }
        return new RouteContext(routeContext, routeContext.getRouteResult(), new ReplicaRouteStageContext(schemaName, replicaGroups, sqlStatementContext.isReadOnly()), getTypeClass());
    }
    
    private void routeReplicaGroups(final Collection<RouteMapper> routeMappers, final ReplicaRule replicaRule, final Map<String, ReplicaGroup> replicaGroups) {
        for (RouteMapper each : routeMappers) {
            String actualTableName = each.getActualName();
            Optional<ReplicaTableRule> replicaRoutingRuleOptional = replicaRule.findRoutingByTable(actualTableName);
            ReplicaGroup replicaGroup;
            if (replicaRoutingRuleOptional.isPresent()) {
                ReplicaTableRule replicaRoutingRule = replicaRoutingRuleOptional.get();
                replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
                replicaGroups.put(actualTableName, replicaGroup);
            } else {
                ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
                replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
            }
            replicaGroups.put(actualTableName, replicaGroup);
        }
    }
    
    @Override
    public int getOrder() {
        return ReplicaOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
