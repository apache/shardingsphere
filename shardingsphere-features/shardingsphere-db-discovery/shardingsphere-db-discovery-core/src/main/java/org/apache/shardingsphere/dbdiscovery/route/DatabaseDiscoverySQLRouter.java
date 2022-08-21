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

package org.apache.shardingsphere.dbdiscovery.route;

import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.dbdiscovery.route.impl.DatabaseDiscoveryDataSourceRouter;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryDataSourceRule;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.ConnectionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Database discovery SQL router.
 */
public final class DatabaseDiscoverySQLRouter implements SQLRouter<DatabaseDiscoveryRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereDatabase database,
                                           final DatabaseDiscoveryRule rule, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        RouteContext result = new RouteContext();
        DatabaseDiscoveryDataSourceRule singleDataSourceRule = rule.getSingleDataSourceRule();
        String dataSourceName = new DatabaseDiscoveryDataSourceRouter(singleDataSourceRule).route();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(singleDataSourceRule.getGroupName(), dataSourceName), Collections.emptyList()));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext,
                                     final LogicSQL logicSQL, final ShardingSphereDatabase database, final DatabaseDiscoveryRule rule,
                                     final ConfigurationProperties props, final ConnectionContext connectionContext) {
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String dataSourceName = each.getDataSourceMapper().getLogicName();
            Optional<DatabaseDiscoveryDataSourceRule> dataSourceRule = rule.findDataSourceRule(dataSourceName);
            if (dataSourceRule.isPresent() && dataSourceRule.get().getGroupName().equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                toBeRemoved.add(each);
                String actualDataSourceName = new DatabaseDiscoveryDataSourceRouter(dataSourceRule.get()).route();
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), actualDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ORDER;
    }
    
    @Override
    public Class<DatabaseDiscoveryRule> getTypeClass() {
        return DatabaseDiscoveryRule.class;
    }
}
