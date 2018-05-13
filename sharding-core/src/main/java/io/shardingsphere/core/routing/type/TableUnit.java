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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Route table unit.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class TableUnit {
    
    private final String dataSourceName;
    
    private final List<RoutingTable> routingTables = new LinkedList<>();
    
    /**
     * Find routing table via data source name and actual table name.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return routing table
     */
    public Optional<RoutingTable> findRoutingTable(final String dataSourceName, final String actualTableName) {
        for (RoutingTable each : routingTables) {
            if (dataSourceName.equalsIgnoreCase(this.dataSourceName) && each.getActualTableName().equalsIgnoreCase(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get actual tables' names via data source name.
     *
     * @param dataSourceName data source name
     * @param logicTableName logic table name
     * @return  actual tables' names
     */
    public Set<String> getActualTableNames(final String dataSourceName, final String logicTableName) {
        Set<String> result = new HashSet<>(routingTables.size(), 1);
        for (RoutingTable each : routingTables) {
            if (dataSourceName.equalsIgnoreCase(this.dataSourceName) && each.getLogicTableName().equalsIgnoreCase(logicTableName)) {
                result.add(each.getActualTableName());
            }
        }
        return result;
    }
    
    /**
     * Get logic tables' names via data source name.
     *
     * @param dataSourceName data source name
     * @return  logic tables' names
     */
    public Set<String> getLogicTableNames(final String dataSourceName) {
        Set<String> result = new HashSet<>(routingTables.size(), 1);
        for (RoutingTable each : routingTables) {
            if (dataSourceName.equalsIgnoreCase(this.dataSourceName)) {
                result.add(each.getLogicTableName());
            }
        }
        return result;
    }
}
