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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Route unit.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class RouteUnit {
    
    private final RouteMapper dataSourceMapper;
    
    private final Collection<RouteMapper> tableMappers;
    
    public RouteUnit(final RouteMapper dataSourceMapper, final Collection<RouteMapper> tableMappers) {
        ShardingSpherePreconditions.checkNotNull(dataSourceMapper, () -> new IllegalArgumentException("`dataSourceMapper` is required"));
        ShardingSpherePreconditions.checkNotNull(tableMappers, () -> new IllegalArgumentException("`tableMappers` is required"));
        this.dataSourceMapper = dataSourceMapper;
        this.tableMappers = tableMappers;
    }
    
    /**
     * Get logic table names.
     *
     * @return logic table names
     */
    public Set<String> getLogicTableNames() {
        return tableMappers.stream().map(RouteMapper::getLogicName).collect(Collectors.toCollection(() -> new HashSet<>(tableMappers.size(), 1L)));
    }
    
    /**
     * Get actual table names.
     *
     * @param logicTableName logic table name
     * @return actual table names
     */
    public Set<String> getActualTableNames(final String logicTableName) {
        return tableMappers.stream().filter(each -> logicTableName.equalsIgnoreCase(each.getLogicName())).map(RouteMapper::getActualName).collect(Collectors.toSet());
    }
    
    /**
     * Find table mapper.
     *
     * @param logicDataSourceName logic data source name
     * @param actualTableName actual table name
     * @return table mapper
     */
    public Optional<RouteMapper> findTableMapper(final String logicDataSourceName, final String actualTableName) {
        for (RouteMapper each : tableMappers) {
            if (logicDataSourceName.equalsIgnoreCase(dataSourceMapper.getLogicName()) && actualTableName.equalsIgnoreCase(each.getActualName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
