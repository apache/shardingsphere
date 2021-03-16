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

package org.apache.shardingsphere.infra.optimize.schema.table.execute;

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
 * Calcite table execution context generator.
 */
@RequiredArgsConstructor
public final class CalciteExecutionContextGenerator {
    
    private final String table;
    
    private final ExecutionContext initialExecutionContext;
    
    private final CalciteExecutionSQLGenerator filter;
    
    /**
     * Create execution context.
     *
     * @return execution context
     */
    public ExecutionContext generate() {
        RouteContext routeContext = getRouteContext(initialExecutionContext.getRouteContext());
        return new ExecutionContext(initialExecutionContext.getSqlStatementContext(),
                getExecutionUnits(routeContext.getRouteUnits(), filter), routeContext);
    }
    
    private Collection<ExecutionUnit> getExecutionUnits(final Collection<RouteUnit> routeUnits, final CalciteExecutionSQLGenerator filter) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each: routeUnits) {
            fillExecutionUnits(result, filter, each);
        }
        return result;
    }
    
    private void fillExecutionUnits(final Collection<ExecutionUnit> executionUnits, final CalciteExecutionSQLGenerator filter, final RouteUnit routeUnit) {
        for (RouteMapper mapper : routeUnit.getTableMappers()) {
            if (mapper.getLogicName().equals(table)) {
                executionUnits.add(new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(),
                        new SQLUnit(filter.generate(mapper.getActualName()), Collections.emptyList(), Collections.singletonList(mapper))));
            }
        }
    }
    
    private RouteContext getRouteContext(final RouteContext routeContext) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().addAll(getRouteUnits(routeContext));
        return result;
    }
    
    private Collection<RouteUnit> getRouteUnits(final RouteContext routeContext) {
        Collection<RouteUnit> result = new LinkedHashSet<>(routeContext.getRouteUnits().size(), 1);
        for (RouteUnit each : routeContext.getRouteUnits()) {
            RouteUnit routeUnit = getRouteUnit(each);
            if (!routeUnit.getTableMappers().isEmpty()) {
                result.add(routeUnit);
            }
        }
        return result;
    }
    
    private RouteUnit getRouteUnit(final RouteUnit routeUnit) {
        RouteUnit result = new RouteUnit(routeUnit.getDataSourceMapper(), new LinkedHashSet<>(routeUnit.getTableMappers().size(), 1));
        for (RouteMapper each : routeUnit.getTableMappers()) {
            if (each.getLogicName().equals(table)) {
                result.getTableMappers().add(each);
            }
        }
        return result;
    }
}
