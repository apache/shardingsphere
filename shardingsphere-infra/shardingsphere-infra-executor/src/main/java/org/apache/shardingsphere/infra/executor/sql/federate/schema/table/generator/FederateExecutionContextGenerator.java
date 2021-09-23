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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Federate table execution context generator.
 */
@RequiredArgsConstructor
public final class FederateExecutionContextGenerator {
    
    private final String tableName;
    
    private final ExecutionContext routeExecutionContext;
    
    private final FederationSQLGenerator generator;
    
    /**
     * Create execution context.
     *
     * @return execution context
     */
    public ExecutionContext generate() {
        RouteContext filteredRouteContext = new RouteContextFilter().filter(tableName, routeExecutionContext.getRouteContext());
        return new ExecutionContext(routeExecutionContext.getLogicSQL(), generateExecutionUnits(filteredRouteContext.getRouteUnits()), filteredRouteContext);
    }
    
    private Collection<ExecutionUnit> generateExecutionUnits(final Collection<RouteUnit> routeUnits) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each: routeUnits) {
            result.addAll(generateExecutionUnits(each));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> generateExecutionUnits(final RouteUnit routeUnit) {
        return routeUnit.getTableMappers().stream().map(each -> generateExecutionUnit(routeUnit, each)).collect(Collectors.toList());
    }
    
    private ExecutionUnit generateExecutionUnit(final RouteUnit routeUnit, final RouteMapper tableMapper) {
        String sql = generator.generate(tableMapper.getActualName());
        return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), new SQLUnit(sql, Collections.emptyList(), Collections.singletonList(tableMapper)));
    }
}
