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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Route unit.
 * 
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class RouteUnit {
    
    private final String logicDataSourceName;
    
    private final String actualDataSourceName;
    
    private final List<TableUnit> tableUnits = new LinkedList<>();
    
    public RouteUnit(final String dataSourceName) {
        logicDataSourceName = dataSourceName;
        actualDataSourceName = dataSourceName;
    }
    
    /**
     * Get routing table unit via data source name and actual table name.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return routing table unit
     */
    public Optional<TableUnit> getTableUnit(final String dataSourceName, final String actualTableName) {
        for (TableUnit each : tableUnits) {
            if (dataSourceName.equalsIgnoreCase(logicDataSourceName) && actualTableName.equalsIgnoreCase(each.getActualTableName())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get actual tables' names via data source name.
     *
     * @param logicTableName logic table name
     * @return  actual tables' names
     */
    public Set<String> getActualTableNames(final String logicTableName) {
        Set<String> result = new HashSet<>();
        for (TableUnit each : tableUnits) {
            if (logicTableName.equalsIgnoreCase(each.getLogicTableName())) {
                result.add(each.getActualTableName());
            }
        }
        return result;
    }
    
    /**
     * Get logic tables' names via data source name.
     *
     * @return  logic tables' names
     */
    public Set<String> getLogicTableNames() {
        Set<String> result = new HashSet<>(tableUnits.size(), 1);
        for (TableUnit each : tableUnits) {
            result.add(each.getLogicTableName());
        }
        return result;
    }
}
