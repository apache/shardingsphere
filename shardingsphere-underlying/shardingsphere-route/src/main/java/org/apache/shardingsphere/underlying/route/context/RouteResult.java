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

package org.apache.shardingsphere.underlying.route.context;

import com.google.common.base.Optional;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Route result.
 * 
 * @author zhangliang
 */
@Getter
public final class RouteResult {
    
    private final Collection<RouteUnit> routeUnits = new LinkedHashSet<>();
    
    /**
     * Judge is route for single database and table only or not.
     *
     * @return is route for single database and table only or not
     */
    public boolean isSingleRouting() {
        return 1 == routeUnits.size();
    }
    
    /**
     * Get all data source names.
     *
     * @return all data source names
     */
    public Collection<String> getDataSourceNames() {
        Collection<String> result = new HashSet<>(routeUnits.size(), 1);
        for (RouteUnit each : routeUnits) {
            result.add(each.getActualDataSourceName());
        }
        return result;
    }
    
    /**
     * Get routing table unit via data source name and actual table name.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return routing table unit
     */
    public Optional<TableUnit> getTableUnit(final String dataSourceName, final String actualTableName) {
        for (RouteUnit each : routeUnits) {
            Optional<TableUnit> result = each.getTableUnit(dataSourceName, actualTableName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get actual tables group via data source name and logic tables' names.
     * <p>
     * Actual tables in same group are belong one logic name.
     * </p>
     *
     * @param dataSourceName data source name
     * @param logicTableNames logic tables' names
     * @return actual tables group
     */
    public List<Set<String>> getActualTableNameGroups(final String dataSourceName, final Set<String> logicTableNames) {
        List<Set<String>> result = new ArrayList<>();
        for (String each : logicTableNames) {
            Set<String> actualTableNames = getActualTableNames(dataSourceName, each);
            if (!actualTableNames.isEmpty()) {
                result.add(actualTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getActualTableNames(final String dataSourceName, final String logicTableName) {
        Set<String> result = new HashSet<>();
        for (RouteUnit each : routeUnits) {
            if (dataSourceName.equalsIgnoreCase(each.getActualDataSourceName())) {
                result.addAll(each.getActualTableNames(logicTableName));
            }
        }
        return result;
    }
    
    /**
     * Get map relationship between data source and logic tables via data sources' names.
     *
     * @param dataSourceNames data sources' names
     * @return  map relationship between data source and logic tables
     */
    public Map<String, Set<String>> getDataSourceLogicTablesMap(final Collection<String> dataSourceNames) {
        Map<String, Set<String>> result = new HashMap<>();
        for (String each : dataSourceNames) {
            Set<String> logicTableNames = getLogicTableNames(each);
            if (!logicTableNames.isEmpty()) {
                result.put(each, logicTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getLogicTableNames(final String dataSourceName) {
        Set<String> result = new HashSet<>();
        for (RouteUnit each : routeUnits) {
            if (dataSourceName.equalsIgnoreCase(each.getActualDataSourceName())) {
                result.addAll(each.getLogicTableNames());
            }
        }
        return result;
    }
}
