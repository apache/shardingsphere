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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterSlaveDataSourceRouter;
import org.apache.shardingsphere.underlying.route.DateNodeRouteDecorator;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteMapper;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Route decorator for master-slave.
 */
@RequiredArgsConstructor
public final class MasterSlaveRouteDecorator implements DateNodeRouteDecorator {
    
    private final MasterSlaveRule masterSlaveRule;
    
    @Override
    public RouteContext decorate(final RouteContext routeContext) {
        if (routeContext.getRouteResult().getRouteUnits().isEmpty()) {
            String dataSourceName = new MasterSlaveDataSourceRouter(masterSlaveRule).route(routeContext.getSqlStatementContext().getSqlStatement());
            RouteResult routeResult = new RouteResult();
            routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.emptyList()));
            return new RouteContext(routeContext.getSqlStatementContext(), routeResult);
        }
        Collection<RouteUnit> toBeRemoved = new LinkedList<>();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteResult().getRouteUnits()) {
            if (masterSlaveRule.getName().equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                toBeRemoved.add(each);
                String actualDataSourceName = new MasterSlaveDataSourceRouter(masterSlaveRule).route(routeContext.getSqlStatementContext().getSqlStatement());
                toBeAdded.add(new RouteUnit(new RouteMapper(each.getDataSourceMapper().getLogicName(), actualDataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteResult().getRouteUnits().removeAll(toBeRemoved);
        routeContext.getRouteResult().getRouteUnits().addAll(toBeAdded);
        return routeContext;
    }
}
