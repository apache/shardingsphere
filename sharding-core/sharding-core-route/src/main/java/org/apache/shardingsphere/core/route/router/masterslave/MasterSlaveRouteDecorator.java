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

package org.apache.shardingsphere.core.route.router.masterslave;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.route.RouteResult;
import org.apache.shardingsphere.core.route.router.DateNodeRouteDecorator;
import org.apache.shardingsphere.underlying.route.RoutingUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Route decorator for master-slave.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MasterSlaveRouteDecorator implements DateNodeRouteDecorator {
    
    private final MasterSlaveRule masterSlaveRule;
    
    @Override
    public RouteResult decorate(final RouteResult routeResult) {
        Collection<RoutingUnit> toBeRemoved = new LinkedList<>();
        Collection<RoutingUnit> toBeAdded = new LinkedList<>();
        for (RoutingUnit each : routeResult.getRoutingResult().getRoutingUnits()) {
            if (masterSlaveRule.getName().equalsIgnoreCase(each.getActualDataSourceName())) {
                toBeRemoved.add(each);
                toBeAdded.add(createNewRoutingUnit(new MasterSlaveDataSourceRouter(masterSlaveRule).route(routeResult.getSqlStatementContext().getSqlStatement()), each));
            }
        }
        routeResult.getRoutingResult().getRoutingUnits().removeAll(toBeRemoved);
        routeResult.getRoutingResult().getRoutingUnits().addAll(toBeAdded);
        return routeResult;
    }
    
    private RoutingUnit createNewRoutingUnit(final String actualDataSourceName, final RoutingUnit originalTableUnit) {
        RoutingUnit result = new RoutingUnit(originalTableUnit.getLogicDataSourceName(), actualDataSourceName);
        result.getTableUnits().addAll(originalTableUnit.getTableUnits());
        return result;
    }
}
