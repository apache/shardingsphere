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

package org.apache.shardingsphere.masterslave.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.DefaultRouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.masterslave.constant.MasterSlaveOrder;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterSlaveDataSourceRouter;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveDataSourceRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Route decorator for master-slave.
 */
public final class MasterSlaveRouteDecorator implements RouteDecorator<MasterSlaveRule> {
    
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final MasterSlaveRule masterSlaveRule, final ConfigurationProperties props) {
        if (routeContext.getRouteResult().getRouteUnits().isEmpty()) {
            String dataSourceName = new MasterSlaveDataSourceRouter(masterSlaveRule.getSingleDataSourceRule()).route(routeContext.getSqlStatementContext().getSqlStatement());
            RouteResult routeResult = new RouteResult();
            routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(DefaultSchema.LOGIC_NAME, dataSourceName), Collections.emptyList()));
            return new RouteContext(routeContext, routeResult, new DefaultRouteStageContext(), getTypeClass());
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            String dataSourceName = each.getDataSourceMapper().getLogicName();
            Optional<MasterSlaveDataSourceRule> dataSourceRule = masterSlaveRule.findDataSourceRule(dataSourceName);
            if (dataSourceRule.isPresent() && dataSourceRule.get().getName().equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                toBeRemoved.add(each);
                String actualDataSourceName = new MasterSlaveDataSourceRouter(dataSourceRule.get()).route(routeContext.getSqlStatementContext().getSqlStatement());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), actualDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteResult().getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
        return routeContext;
    }
    
    @Override
    public int getOrder() {
        return MasterSlaveOrder.ORDER;
    }
    
    @Override
    public Class<MasterSlaveRule> getTypeClass() {
        return MasterSlaveRule.class;
    }
}
