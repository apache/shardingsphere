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

/**
 * Federate table execution context generator.
 */
@RequiredArgsConstructor
public final class FederateExecutionContextGenerator {
    
    private final String tableName;
    
    private final ExecutionContext routeExecutionContext;
    
    private final FederateExecutionSQLGenerator generator;
    
    /**
     * Create execution context.
     *
     * @return execution context
     */
    public ExecutionContext generate() {
        RouteContext filteredRouteContext = new RouteContextFilter().filter(tableName, routeExecutionContext.getRouteContext());
        return new ExecutionContext(routeExecutionContext.getLogicSQL(), getExecutionUnits(filteredRouteContext.getRouteUnits()), filteredRouteContext);
    }
    
    private Collection<ExecutionUnit> getExecutionUnits(final Collection<RouteUnit> routeUnits) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each: routeUnits) {
            fillExecutionUnits(result, each);
        }
        return result;
    }
    
    private void fillExecutionUnits(final Collection<ExecutionUnit> executionUnits, final RouteUnit routeUnit) {
        for (RouteMapper each : routeUnit.getTableMappers()) {
            if (each.getLogicName().equalsIgnoreCase(tableName)) {
                executionUnits.add(new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(),
                        new SQLUnit(generator.generate(each.getActualName()), Collections.emptyList(), Collections.singletonList(each))));
            }
        }
    }
}
