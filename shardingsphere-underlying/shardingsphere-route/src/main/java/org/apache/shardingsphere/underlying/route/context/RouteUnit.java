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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Route unit.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class RouteUnit {
    
    private final RouteMapper dataSourceMapper;
    
    private final Collection<RouteMapper> tableMappers;
    
    /**
     * Find table mapper.
     *
     * @param dataSourceName data source name
     * @param actualTableName actual table name
     * @return table mapper
     */
    public Optional<RouteMapper> findTableMapper(final String dataSourceName, final String actualTableName) {
        for (RouteMapper each : tableMappers) {
            if (dataSourceName.equalsIgnoreCase(dataSourceMapper.getLogicName()) && actualTableName.equalsIgnoreCase(each.getActualName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get actual table names.
     *
     * @param logicTableName logic table name
     * @return actual table names
     */
    public Set<String> getActualTableNames(final String logicTableName) {
        Set<String> result = new HashSet<>();
        for (RouteMapper each : tableMappers) {
            if (logicTableName.equalsIgnoreCase(each.getLogicName())) {
                result.add(each.getActualName());
            }
        }
        return result;
    }
    
    /**
     * Get logic table names.
     *
     * @return  logic table names
     */
    public Set<String> getLogicTableNames() {
        Set<String> result = new HashSet<>(tableMappers.size(), 1);
        for (RouteMapper each : tableMappers) {
            result.add(each.getLogicName());
        }
        return result;
    }
}
