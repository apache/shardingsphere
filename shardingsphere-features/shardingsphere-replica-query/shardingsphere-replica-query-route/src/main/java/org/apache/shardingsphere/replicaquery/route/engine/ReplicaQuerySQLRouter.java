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

package org.apache.shardingsphere.replicaquery.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.replicaquery.route.engine.impl.ReplicaQueryDataSourceRouter;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Replica query SQL router.
 */
public final class ReplicaQuerySQLRouter implements SQLRouter<ReplicaQueryRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereSchema schema, final ReplicaQueryRule rule, final ConfigurationProperties props) {
        RouteContext result = new RouteContext();
        String dataSourceName = new ReplicaQueryDataSourceRouter(rule.getSingleDataSourceRule()).route(logicSQL.getSqlStatementContext().getSqlStatement());
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(DefaultSchema.LOGIC_NAME, dataSourceName), Collections.emptyList()));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext,
                                     final LogicSQL logicSQL, final ShardingSphereSchema schema, final ReplicaQueryRule rule, final ConfigurationProperties props) {
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String dataSourceName = each.getDataSourceMapper().getLogicName();
            Optional<ReplicaQueryDataSourceRule> dataSourceRule = rule.findDataSourceRule(dataSourceName);
            if (dataSourceRule.isPresent() && dataSourceRule.get().getName().equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                toBeRemoved.add(each);
                String actualDataSourceName = new ReplicaQueryDataSourceRouter(dataSourceRule.get()).route(logicSQL.getSqlStatementContext().getSqlStatement());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), actualDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    @Override
    public int getOrder() {
        return ReplicaQueryOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaQueryRule> getTypeClass() {
        return ReplicaQueryRule.class;
    }
}
