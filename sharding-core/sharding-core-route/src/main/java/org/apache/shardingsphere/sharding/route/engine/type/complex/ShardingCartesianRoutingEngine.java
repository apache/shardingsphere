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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.underlying.route.context.TableUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Sharding cartesian routing engine.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class ShardingCartesianRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<RouteResult> routeResults;
    
    @Override
    public RouteResult route(final ShardingRule shardingRule) {
        RouteResult result = new RouteResult();
        for (Entry<String, Set<String>> entry : getDataSourceLogicTablesMap().entrySet()) {
            List<Set<String>> actualTableGroups = getActualTableGroups(entry.getKey(), entry.getValue());
            List<Set<TableUnit>> routingTableGroups = toRoutingTableGroups(entry.getKey(), actualTableGroups);
            result.getRouteUnits().addAll(getRouteUnits(entry.getKey(), Sets.cartesianProduct(routingTableGroups)));
        }
        return result;
    }
    
    private Map<String, Set<String>> getDataSourceLogicTablesMap() {
        Collection<String> intersectionDataSources = getIntersectionDataSources();
        Map<String, Set<String>> result = new HashMap<>(routeResults.size());
        for (RouteResult each : routeResults) {
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
        for (RouteResult each : routeResults) {
            if (result.isEmpty()) {
                result.addAll(each.getDataSourceNames());
            }
            result.retainAll(each.getDataSourceNames());
        }
        return result;
    }
    
    private List<Set<String>> getActualTableGroups(final String dataSourceName, final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>(logicTables.size());
        for (RouteResult each : routeResults) {
            result.addAll(each.getActualTableNameGroups(dataSourceName, logicTables));
        }
        return result;
    }
    
    private List<Set<TableUnit>> toRoutingTableGroups(final String dataSource, final List<Set<String>> actualTableGroups) {
        List<Set<TableUnit>> result = new ArrayList<>(actualTableGroups.size());
        for (Set<String> each : actualTableGroups) {
            result.add(new HashSet<>(Lists.transform(new ArrayList<>(each), new Function<String, TableUnit>() {
    
                @Override
                public TableUnit apply(final String input) {
                    return findRoutingTable(dataSource, input);
                }
            })));
        }
        return result;
    }
    
    private TableUnit findRoutingTable(final String dataSource, final String actualTable) {
        for (RouteResult each : routeResults) {
            Optional<TableUnit> result = each.getTableUnit(dataSource, actualTable);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException(String.format("Cannot found routing table factor, data source: %s, actual table: %s", dataSource, actualTable));
    }
    
    private Collection<RouteUnit> getRouteUnits(final String dataSource, final Set<List<TableUnit>> cartesianRoutingTableGroups) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (List<TableUnit> each : cartesianRoutingTableGroups) {
            RouteUnit routeUnit = new RouteUnit(dataSource);
            routeUnit.getTableUnits().addAll(each);
            result.add(routeUnit);
        }
        return result;
    }
}
