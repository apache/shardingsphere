/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.type;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Route table unit collection.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@Getter
@ToString
public final class TableUnits {
    
    private final Collection<TableUnit> tableUnits = new LinkedHashSet<>();
    
    /**
     * Get all data source names.
     *
     * @return all data source names
     */
    public Collection<String> getDataSourceNames() {
        Collection<String> result = new HashSet<>(tableUnits.size(), 1);
        for (TableUnit each : tableUnits) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * Find routing table via data source name and actual table name.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return routing table
     */
    public Optional<RoutingTable> findRoutingTable(final String dataSourceName, final String actualTableName) {
        for (TableUnit each : tableUnits) {
            Optional<RoutingTable> result = each.findRoutingTable(dataSourceName, actualTableName);
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
        for (String logicTableName : logicTableNames) {
            Set<String> actualTableNames = getActualTableNames(dataSourceName, logicTableName);
            if (!actualTableNames.isEmpty()) {
                result.add(actualTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getActualTableNames(final String dataSourceName, final String logicTableName) {
        Set<String> result = new HashSet<>(tableUnits.size(), 1);
        for (TableUnit each : tableUnits) {
            result.addAll(each.getActualTableNames(dataSourceName, logicTableName));
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
        Set<String> result = new HashSet<>(tableUnits.size(), 1);
        for (TableUnit each : tableUnits) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName)) {
                result.addAll(each.getLogicTableNames(dataSourceName));
            }
        }
        return result;
    }
}
