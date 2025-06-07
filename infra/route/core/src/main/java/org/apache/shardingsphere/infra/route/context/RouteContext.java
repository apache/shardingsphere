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

package org.apache.shardingsphere.infra.route.context;

import lombok.Getter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Route context.
 */
@Getter
public final class RouteContext {
    
    private final Collection<Collection<DataNode>> originalDataNodes = new LinkedList<>();
    
    private final Collection<RouteUnit> routeUnits = new LinkedHashSet<>();
    
    private final Map<Class<? extends ShardingSphereRule>, RouteStageContext> routeStageContexts = new LinkedHashMap<>();
    
    /**
     * Judge is route for single database and table only or not.
     *
     * @return is route for single database and table only or not
     */
    public boolean isSingleRouting() {
        return 1 == routeUnits.size();
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDataSourceNames() {
        return routeUnits.stream().map(each -> each.getDataSourceMapper().getActualName()).collect(Collectors.toSet());
    }
    
    /**
     * Get actual tables groups.
     *
     * <p>
     * Actual tables in same group are belong one logic name.
     * </p>
     *
     * @param actualDataSourceName actual data source name
     * @param logicTableNames logic table names
     * @return actual table groups
     */
    public List<Set<String>> getActualTableNameGroups(final String actualDataSourceName, final Set<String> logicTableNames) {
        return logicTableNames.stream().map(each -> getActualTableNames(actualDataSourceName, each)).filter(each -> !each.isEmpty()).collect(Collectors.toList());
    }
    
    private Set<String> getActualTableNames(final String actualDataSourceName, final String logicTableName) {
        Set<String> result = new LinkedHashSet<>();
        for (RouteUnit each : routeUnits) {
            if (actualDataSourceName.equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                result.addAll(each.getActualTableNames(logicTableName));
            }
        }
        return result;
    }
    
    /**
     * Get map relationship between actual data source and logic tables.
     *
     * @param actualDataSourceNames actual data source names
     * @return map relationship between data source and logic tables
     */
    public Map<String, Set<String>> getDataSourceLogicTablesMap(final Collection<String> actualDataSourceNames) {
        Map<String, Set<String>> result = new HashMap<>(actualDataSourceNames.size(), 1F);
        for (String each : actualDataSourceNames) {
            Set<String> logicTableNames = getLogicTableNames(each);
            if (!logicTableNames.isEmpty()) {
                result.put(each, logicTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getLogicTableNames(final String actualDataSourceName) {
        Set<String> result = new HashSet<>();
        for (RouteUnit each : routeUnits) {
            if (actualDataSourceName.equalsIgnoreCase(each.getDataSourceMapper().getActualName())) {
                result.addAll(each.getLogicTableNames());
            }
        }
        return result;
    }
    
    /**
     * Find table mapper.
     *
     * @param logicDataSourceName logic data source name
     * @param actualTableName actual table name
     * @return table mapper
     */
    public Optional<RouteMapper> findTableMapper(final String logicDataSourceName, final String actualTableName) {
        for (RouteUnit each : routeUnits) {
            Optional<RouteMapper> result = each.findTableMapper(logicDataSourceName, actualTableName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Put route unit.
     *
     * @param dataSourceMapper database mapper
     * @param tableMappers table mapper collection
     */
    public void putRouteUnit(final RouteMapper dataSourceMapper, final Collection<RouteMapper> tableMappers) {
        Collection<RouteUnit> targets = getTargetRouteUnits(dataSourceMapper);
        if (targets.isEmpty()) {
            RouteUnit unit = new RouteUnit(dataSourceMapper, new LinkedHashSet<>());
            unit.getTableMappers().addAll(tableMappers);
            routeUnits.add(unit);
        } else {
            Collection<RouteUnit> toBeAdded = new LinkedList<>();
            Collection<RouteUnit> toBeRemoved = new LinkedList<>();
            for (RouteUnit each : targets) {
                RouteUnit unit = new RouteUnit(dataSourceMapper, new LinkedHashSet<>());
                unit.getTableMappers().addAll(each.getTableMappers());
                unit.getTableMappers().addAll(tableMappers);
                toBeAdded.add(unit);
                toBeRemoved.add(each);
            }
            boolean success = routeUnits.addAll(toBeAdded);
            if (success) {
                routeUnits.removeAll(toBeRemoved);
            }
        }
    }
    
    private Collection<RouteUnit> getTargetRouteUnits(final RouteMapper dataSourceMapper) {
        Collection<RouteUnit> result = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            if (each.getDataSourceMapper().equals(dataSourceMapper)) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Judge whether route context contains table sharding or not.
     *
     * @return whether route context contains table sharding or not
     */
    public boolean containsTableSharding() {
        for (RouteUnit each : routeUnits) {
            for (RouteMapper tableMapper : each.getTableMappers()) {
                if (!tableMapper.getActualName().equals(tableMapper.getLogicName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
