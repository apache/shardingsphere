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

package org.apache.shardingsphere.infra.executor.sql.federate.schema.table.generator;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Route context filter.
 */
public final class RouteContextFilter {
    
    /**
     * Filter route context.
     * 
     * @param toBeKeptTableName to be kept table name
     * @param routeContext route context
     * @return filtered route context
     */
    public RouteContext filter(final String toBeKeptTableName, final RouteContext routeContext) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().addAll(filterRouteUnits(toBeKeptTableName, routeContext));
        return result;
    }
    
    private Collection<RouteUnit> filterRouteUnits(final String toBeKeptTableName, final RouteContext routeContext) {
        return routeContext.getRouteUnits().stream().map(each -> filterRouteUnit(toBeKeptTableName, each)).filter(each -> !each.getTableMappers().isEmpty()).collect(Collectors.toList());
    }
    
    private RouteUnit filterRouteUnit(final String toBeKeptTableName, final RouteUnit routeUnit) {
        return new RouteUnit(routeUnit.getDataSourceMapper(), filterTableMappers(toBeKeptTableName, routeUnit));
    }
    
    private List<RouteMapper> filterTableMappers(final String toBeKeptTableName, final RouteUnit routeUnit) {
        return routeUnit.getTableMappers().stream().filter(each -> each.getLogicName().equalsIgnoreCase(toBeKeptTableName)).collect(Collectors.toList());
    }
}
