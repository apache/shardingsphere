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

package org.apache.shardingsphere.sharding.route.engine.type.complex;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding cartesian routing engine.
 */
@RequiredArgsConstructor
public final class ShardingCartesianRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<RouteContext> routeContexts;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        for (Entry<String, Set<String>> entry : getDataSourceLogicTablesMap().entrySet()) {
            List<Set<String>> actualTableGroups = getActualTableGroups(entry.getKey(), entry.getValue());
            List<Set<RouteMapper>> routingTableGroups = toRoutingTableGroups(entry.getKey(), actualTableGroups);
            result.getRouteUnits().addAll(getRouteUnits(entry.getKey(), Sets.cartesianProduct(routingTableGroups)));
        }
        routeContext.getOriginalDataNodes().addAll(result.getOriginalDataNodes());
        routeContext.getRouteUnits().addAll(result.getRouteUnits());
    }
    
    private Map<String, Set<String>> getDataSourceLogicTablesMap() {
        Collection<String> intersectionDataSources = getIntersectionDataSources();
        Map<String, Set<String>> result = new HashMap<>(routeContexts.size());
        for (RouteContext each : routeContexts) {
            for (Entry<String, Set<String>> entry : each.getDataSourceLogicTablesMap(intersectionDataSources).entrySet()) {
                if (result.containsKey(entry.getKey())) {
                    result.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
    
    private Collection<String> getIntersectionDataSources() {
        Collection<String> result = new HashSet<>();
        for (RouteContext each : routeContexts) {
            if (result.isEmpty()) {
                result.addAll(each.getActualDataSourceNames());
            }
            result.retainAll(each.getActualDataSourceNames());
        }
        return result;
    }
    
    private List<Set<String>> getActualTableGroups(final String dataSourceName, final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>(logicTables.size());
        for (RouteContext each : routeContexts) {
            result.addAll(each.getActualTableNameGroups(dataSourceName, logicTables));
        }
        return result;
    }
    
    private List<Set<RouteMapper>> toRoutingTableGroups(final String dataSource, final List<Set<String>> actualTableGroups) {
        List<Set<RouteMapper>> result = new ArrayList<>(actualTableGroups.size());
        for (Set<String> each : actualTableGroups) {
            result.add(new HashSet<>(new ArrayList<>(each).stream().map(input -> findRoutingTable(dataSource, input)).collect(Collectors.toList())));
        }
        return result;
    }
    
    private RouteMapper findRoutingTable(final String dataSource, final String actualTable) {
        for (RouteContext each : routeContexts) {
            Optional<RouteMapper> result = each.findTableMapper(dataSource, actualTable);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException(String.format("Cannot found routing table factor, data source: %s, actual table: %s", dataSource, actualTable));
    }
    
    private Collection<RouteUnit> getRouteUnits(final String dataSource, final Set<List<RouteMapper>> cartesianRoutingTableGroups) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (List<RouteMapper> each : cartesianRoutingTableGroups) {
            result.add(new RouteUnit(new RouteMapper(dataSource, dataSource), each));
        }
        return result;
    }
}
